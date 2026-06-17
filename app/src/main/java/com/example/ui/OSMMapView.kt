package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale
import java.util.concurrent.TimeUnit

// ─────────────────────────────────────────────────────────────────────────────
// Remove connected background pixels from image corners using flood-fill.
// Threshold 230 catches white, off-white, and light-grey aliased edges.
// ─────────────────────────────────────────────────────────────────────────────
private fun removeBackground(src: Bitmap, threshold: Int = 230): Bitmap {
    val w = src.width
    val h = src.height
    val pixels = IntArray(w * h)
    src.getPixels(pixels, 0, w, 0, 0, w, h)

    fun isBackground(pixel: Int): Boolean {
        val a = (pixel ushr 24) and 0xFF
        if (a < 128) return true                         // already transparent
        val r = (pixel ushr 16) and 0xFF
        val g = (pixel ushr 8) and 0xFF
        val b = pixel and 0xFF
        return r > threshold && g > threshold && b > threshold
    }

    val visited = BooleanArray(w * h)
    val queue = ArrayDeque<Int>()

    // Seed flood-fill from all 4 corners
    fun seed(x: Int, y: Int) {
        val idx = y * w + x
        if (!visited[idx] && isBackground(pixels[idx])) { queue.add(idx); visited[idx] = true }
    }
    seed(0, 0); seed(w - 1, 0); seed(0, h - 1); seed(w - 1, h - 1)

    val dx = intArrayOf(1, -1, 0, 0)
    val dy = intArrayOf(0, 0, 1, -1)
    while (queue.isNotEmpty()) {
        val idx = queue.removeFirst()
        pixels[idx] = 0x00000000          // make transparent
        val cx = idx % w
        val cy = idx / w
        for (d in 0..3) {
            val nx = cx + dx[d]; val ny = cy + dy[d]
            if (nx < 0 || nx >= w || ny < 0 || ny >= h) continue
            val nIdx = ny * w + nx
            if (!visited[nIdx] && isBackground(pixels[nIdx])) {
                visited[nIdx] = true
                queue.add(nIdx)
            }
        }
    }

    val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    result.setPixels(pixels, 0, w, 0, 0, w, h)
    return result
}

// ─────────────────────────────────────────────────────────────────────────────
// Scale + strip background. Used for all map markers.
// ─────────────────────────────────────────────────────────────────────────────
private fun scaledMarkerDrawable(ctx: Context, resId: Int, sizePx: Int,
                                  removeWhiteBg: Boolean = false): BitmapDrawable {
    val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
    var bmp = BitmapFactory.decodeResource(ctx.resources, resId, options)
    if (removeWhiteBg) bmp = removeBackground(bmp)

    val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(output)           // transparent by default
    val paint = android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG).apply { isAntiAlias = true }
    canvas.drawBitmap(bmp,
        android.graphics.Rect(0, 0, bmp.width, bmp.height),
        android.graphics.Rect(0, 0, sizePx, sizePx), paint)
    bmp.recycle()
    return BitmapDrawable(ctx.resources, output)
}

// ─────────────────────────────────────────────────────────────────────────────
// OSRM: fetch a real-road driving route between two coordinate pairs.
// Returns a list of GeoPoints along the road; empty list on failure.
// ─────────────────────────────────────────────────────────────────────────────
suspend fun fetchOsrmRouteShared(
    startLat: Double, startLng: Double,
    endLat: Double, endLng: Double
): List<GeoPoint> = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
        val url = "https://router.project-osrm.org/route/v1/driving/" +
                "$startLng,$startLat;$endLng,$endLat?overview=full&geometries=geojson"
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return@withContext emptyList()
        val json = JSONObject(body)
        val routes = json.optJSONArray("routes") ?: return@withContext emptyList()
        if (routes.length() == 0) return@withContext emptyList()
        val geometry = routes.getJSONObject(0).optJSONObject("geometry")
            ?: return@withContext emptyList()
        val coords = geometry.optJSONArray("coordinates") ?: return@withContext emptyList()
        val points = mutableListOf<GeoPoint>()
        for (i in 0 until coords.length()) {
            val pair = coords.getJSONArray(i)
            // GeoJSON coords are [lng, lat]; GeoPoint takes (lat, lng)
            points.add(GeoPoint(pair.getDouble(1), pair.getDouble(0)))
        }
        points
    } catch (e: Exception) {
        Log.e("OSRM", "Route fetch failed: ${e.message}")
        emptyList()
    }
}

private fun calculateBearing(start: GeoPoint, end: GeoPoint): Float {
    val lat1 = Math.toRadians(start.latitude)
    val lon1 = Math.toRadians(start.longitude)
    val lat2 = Math.toRadians(end.latitude)
    val lon2 = Math.toRadians(end.longitude)
    val dLon = lon2 - lon1
    val y = Math.sin(dLon) * Math.cos(lat2)
    val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)
    val brng = Math.atan2(y, x)
    return ((Math.toDegrees(brng) + 360) % 360).toFloat()
}

private class BoxedGeoPoint(var value: GeoPoint? = null)

private data class SnappedPoint(val point: GeoPoint, val segmentIdx: Int)

private fun normalizeDegrees(deg: Float): Float {
    return ((deg % 360f) + 360f) % 360f
}

private fun snapToRoute(p: GeoPoint, route: List<GeoPoint>): SnappedPoint {
    if (route.isEmpty()) return SnappedPoint(p, -1)
    if (route.size == 1) return SnappedPoint(route[0], 0)
    var minDistanceSq = Double.MAX_VALUE
    var bestPoint = route[0]
    var bestSegmentIdx = 0
    
    for (i in 0 until route.size - 1) {
        val a = route[i]
        val b = route[i + 1]
        
        val dLat = b.latitude - a.latitude
        val dLng = b.longitude - a.longitude
        val lenSq = dLat * dLat + dLng * dLng
        
        val proj = if (lenSq == 0.0) {
            a
        } else {
            val uLat = p.latitude - a.latitude
            val uLng = p.longitude - a.longitude
            val t = ((uLat * dLat + uLng * dLng) / lenSq).coerceIn(0.0, 1.0)
            GeoPoint(a.latitude + t * dLat, a.longitude + t * dLng)
        }
        
        val distLat = p.latitude - proj.latitude
        val distLng = p.longitude - proj.longitude
        val distSq = distLat * distLat + distLng * distLng
        if (distSq < minDistanceSq) {
            minDistanceSq = distSq
            bestPoint = proj
            bestSegmentIdx = i
        }
    }
    
    val distance = Math.sqrt(minDistanceSq)
    return if (distance < 0.0013) {
        SnappedPoint(bestPoint, bestSegmentIdx)
    } else {
        SnappedPoint(p, -1)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fallback: straight-line polyline Restaurant → Rider → Customer
// ─────────────────────────────────────────────────────────────────────────────
private fun straightLinePoints(
    restaurantLat: Double, restaurantLng: Double,
    driverLat: Double, driverLng: Double,
    customerLat: Double, customerLng: Double
): List<GeoPoint> = listOf(
    GeoPoint(restaurantLat, restaurantLng),
    GeoPoint(driverLat, driverLng),
    GeoPoint(customerLat, customerLng)
)

// ─────────────────────────────────────────────────────────────────────────────
// OSMDeliveryMap — Real OpenStreetMap with GPS, custom markers, OSRM route
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OSMDeliveryMap(
    orderId: Int,
    customerLat: Double,
    customerLng: Double,
    restaurantLat: Double,
    restaurantLng: Double,
    driverLat: Double,
    driverLng: Double,
    orderStatus: String,
    isDarkTheme: Boolean,
    getCachedRoute: (Int) -> List<Pair<Double, Double>>?,
    cacheRoute: (Int, List<Pair<Double, Double>>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Configure OSMDroid user agent (required to avoid tile server blocks)
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidTileCache = context.cacheDir.resolve("osmdroid")
            tileFileSystemCacheMaxBytes = 50L * 1024 * 1024 // 50 MB cache
        }
    }

    // Create the MapView once — centre on Vijayawada, AP
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 5.0
            maxZoomLevel = 20.0
            controller.setZoom(14.5)
            controller.setCenter(GeoPoint(16.5062, 80.6480))
            isTilesScaledToDpi = true
            setZoomRounding(true)
        }
    }

    var driverMarker by remember { mutableStateOf<Marker?>(null) }
    var restMarker by remember { mutableStateOf<Marker?>(null) }
    var homeMarker by remember { mutableStateOf<Marker?>(null) }
    var routePolyline by remember { mutableStateOf<Polyline?>(null) }
    var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var isRouteLoading by remember { mutableStateOf(false) }

    var lastStartLat by remember { mutableStateOf(0.0) }
    var lastStartLng by remember { mutableStateOf(0.0) }
    var lastEndLat by remember { mutableStateOf(0.0) }
    var lastEndLng by remember { mutableStateOf(0.0) }

    // Lifecycle management for MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE  -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    // Fetch OSRM route dynamically based on orderStatus and position; fall back to straight line
    LaunchedEffect(restaurantLat, restaurantLng, customerLat, customerLng, driverLat, driverLng, orderStatus) {
        val isEnRouteToRestaurant = orderStatus == "Confirmed" || orderStatus == "Accepted" || orderStatus == "Preparing" || orderStatus == "Ready"
        val startLat = if (isEnRouteToRestaurant && driverLat != 0.0) driverLat else restaurantLat
        val startLng = if (isEnRouteToRestaurant && driverLng != 0.0) driverLng else restaurantLng
        val endLat = if (isEnRouteToRestaurant) restaurantLat else customerLat
        val endLng = if (isEnRouteToRestaurant) restaurantLng else customerLng

        if (Math.abs(startLat - endLat) < 0.0001 && Math.abs(startLng - endLng) < 0.0001) {
            routePoints = listOf(GeoPoint(startLat, startLng), GeoPoint(endLat, endLng))
            return@LaunchedEffect
        }

        val distStart = Math.hypot(startLat - lastStartLat, startLng - lastStartLng)
        val distEnd = Math.hypot(endLat - lastEndLat, endLng - lastEndLng)

        if (routePoints.isEmpty() || distStart > 0.002 || distEnd > 0.002 || (lastStartLat == 0.0 && startLat != 0.0)) {
            lastStartLat = startLat
            lastStartLng = startLng
            lastEndLat = endLat
            lastEndLng = endLng

            isRouteLoading = true
            val osrmPoints = fetchOsrmRouteShared(startLat, startLng, endLat, endLng)
            val pts = if (osrmPoints.isNotEmpty()) {
                osrmPoints
            } else {
                straightLinePoints(startLat, startLng, driverLat, driverLng, endLat, endLng)
            }
            routePoints = pts
            isRouteLoading = false
        }

        withContext(Dispatchers.Main) {
            routePolyline?.let { poly ->
                poly.setPoints(routePoints)
                mapView.invalidate()
            }
        }
    }

    LaunchedEffect(orderStatus) {
        val isRiderAssigned = orderStatus == "Confirmed" || orderStatus == "OutForDelivery" || orderStatus == "Delivered" || orderStatus == "Accepted" || orderStatus == "Preparing" || orderStatus == "Ready"
        val midLat = if (isRiderAssigned && driverLat != 0.0) {
            (restaurantLat + customerLat + driverLat) / 3.0
        } else {
            (restaurantLat + customerLat) / 2.0
        }
        val midLng = if (isRiderAssigned && driverLng != 0.0) {
            (restaurantLng + customerLng + driverLng) / 3.0
        } else {
            (restaurantLng + customerLng) / 2.0
        }
        mapView.controller.animateTo(GeoPoint(midLat, midLng), 14.5, 800L)
    }

    // Animate rider marker along the actual road segments of the OSRM route.
    // Calculates segment bearings and smoothly interpolates rotation to steer naturally into turns.
    LaunchedEffect(driverLat, driverLng, driverMarker) {
        if (driverMarker == null) return@LaunchedEffect
        val target = GeoPoint(driverLat, driverLng)
        val start = driverMarker?.position ?: GeoPoint(driverLat, driverLng)

        if (start.latitude == target.latitude && start.longitude == target.longitude) {
            // No movement — just ensure marker is at correct position and rotated
            withContext(Dispatchers.Main) {
                driverMarker?.apply {
                    val snapped = if (routePoints.size >= 2) snapToRoute(target, routePoints).point else target
                    position = snapped
                    setAnchor(0.5f, 0.5f)
                    val routeBearing = if (routePoints.size >= 2) {
                        calculateBearing(routePoints[0], routePoints[1])
                    } else 0f
                    rotation = normalizeDegrees(routeBearing + 180f)
                }
                mapView.invalidate()
            }
            return@LaunchedEffect
        }

        // Construct the road path from start to target snapped onto the routePoints list
        val path = mutableListOf<GeoPoint>()
        if (routePoints.size >= 2) {
            val startSnap = snapToRoute(start, routePoints)
            val targetSnap = snapToRoute(target, routePoints)
            
            if (startSnap.segmentIdx != -1 && targetSnap.segmentIdx != -1) {
                path.add(startSnap.point)
                if (startSnap.segmentIdx < targetSnap.segmentIdx) {
                    for (idx in (startSnap.segmentIdx + 1)..targetSnap.segmentIdx) {
                        path.add(routePoints[idx])
                    }
                } else if (startSnap.segmentIdx > targetSnap.segmentIdx) {
                    for (idx in (startSnap.segmentIdx) downTo (targetSnap.segmentIdx + 1)) {
                        path.add(routePoints[idx])
                    }
                }
                path.add(targetSnap.point)
            } else {
                path.add(start)
                path.add(target)
            }
        } else {
            path.add(start)
            path.add(target)
        }

        // Calculate total distance along the gathered segments
        var totalDist = 0.0
        for (i in 0 until (path.size - 1)) {
            val p1 = path[i]
            val p2 = path[i + 1]
            val dLat = p2.latitude - p1.latitude
            val dLng = p2.longitude - p1.longitude
            totalDist += Math.sqrt(dLat * dLat + dLng * dLng)
        }

        if (totalDist == 0.0) {
            withContext(Dispatchers.Main) {
                driverMarker?.apply {
                    val snapped = if (routePoints.size >= 2) snapToRoute(target, routePoints).point else target
                    position = snapped
                    setAnchor(0.5f, 0.5f)
                    val segmentBearing = if (routePoints.size >= 2) {
                        val snap = snapToRoute(target, routePoints)
                        val idx = snap.segmentIdx.coerceIn(0, routePoints.size - 2)
                        calculateBearing(routePoints[idx], routePoints[idx + 1])
                    } else {
                        calculateBearing(start, target)
                    }
                    rotation = normalizeDegrees(segmentBearing + 180f)
                }
                mapView.invalidate()
            }
            return@LaunchedEffect
        }

        val steps = 50
        var currentSegmentIdx = 0
        var segmentStartPos = path[0]
        var segmentEndPos = path[1]
        var segmentDist = 0.0

        fun updateSegment(idx: Int) {
            currentSegmentIdx = idx
            segmentStartPos = path[idx]
            segmentEndPos = path[idx + 1]
            val dLat = segmentEndPos.latitude - segmentStartPos.latitude
            val dLng = segmentEndPos.longitude - segmentStartPos.longitude
            segmentDist = Math.sqrt(dLat * dLat + dLng * dLng)
        }
        updateSegment(0)

        var distanceTraveled = 0.0
        var currentRotation = driverMarker?.rotation ?: 0f

        for (step in 1..steps) {
            val fraction = step.toDouble() / steps
            val targetDistance = fraction * totalDist

            // Advance segments if we have traveled past the current segment boundary
            while (targetDistance > (distanceTraveled + segmentDist) && currentSegmentIdx < (path.size - 2)) {
                distanceTraveled += segmentDist
                updateSegment(currentSegmentIdx + 1)
            }

            // Calculate interpolation factor within the current segment
            val segmentFraction = if (segmentDist > 0.0) {
                ((targetDistance - distanceTraveled) / segmentDist).coerceIn(0.0, 1.0)
            } else 1.0

            val interpLat = segmentStartPos.latitude + (segmentEndPos.latitude - segmentStartPos.latitude) * segmentFraction
            val interpLng = segmentStartPos.longitude + (segmentEndPos.longitude - segmentStartPos.longitude) * segmentFraction

            // Calculate segment bearing and apply look-ahead turn blending
            val currentBearing = calculateBearing(segmentStartPos, segmentEndPos)
            val targetBearing = if (currentSegmentIdx < path.size - 2) {
                val nextStart = path[currentSegmentIdx + 1]
                val nextEnd = path[currentSegmentIdx + 2]
                val nextBearing = calculateBearing(nextStart, nextEnd)
                
                // Blend starting at 65% through current segment
                val blendStart = 0.65
                val blendFactor = if (segmentFraction > blendStart) {
                    ((segmentFraction - blendStart) / (1.0 - blendStart)).toFloat().coerceIn(0f, 1f)
                } else 0f
                
                var diffBrng = nextBearing - currentBearing
                while (diffBrng < -180f) diffBrng += 360f
                while (diffBrng > 180f) diffBrng -= 360f
                
                normalizeDegrees(currentBearing + diffBrng * blendFactor)
            } else {
                currentBearing
            }

            val targetRotation = normalizeDegrees(targetBearing + 180f)

            // Smoothly rotate the marker using a low-pass angular filter (steer into turns)
            var diff = targetRotation - currentRotation
            while (diff < -180f) diff += 360f
            while (diff > 180f) diff -= 360f

            currentRotation = normalizeDegrees(currentRotation + diff * 0.18f)

            // Leaning effect: shift the horizontal anchor based on turning rate (diff)
            val leanFactor = (diff * 0.003f).coerceIn(-0.1f, 0.1f)
            val anchorU = 0.5f - leanFactor

            withContext(Dispatchers.Main) {
                driverMarker?.apply {
                    position = GeoPoint(interpLat, interpLng)
                    rotation = currentRotation
                    setAnchor(anchorU, 0.5f)
                }
                mapView.invalidate()
            }
            kotlinx.coroutines.delay(75L) // 50 steps * 75ms = ~3.75s animation
        }

        // Reset anchor after animation loop finishes
        withContext(Dispatchers.Main) {
            driverMarker?.setAnchor(0.5f, 0.5f)
            mapView.invalidate()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                mapView.also { mv ->
                    val markerSizePx = 90

                    // ── Restaurant marker
                    val restMkr = Marker(mv).apply {
                        position = GeoPoint(restaurantLat, restaurantLng)
                        title = "Restaurant"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = scaledMarkerDrawable(ctx, R.drawable.ic_marker_restaurant, markerSizePx)
                    }
                    restMarker = restMkr

                    // ── Customer / home marker
                    val homeMkr = Marker(mv).apply {
                        position = GeoPoint(customerLat, customerLng)
                        title = "Your Delivery Location"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = scaledMarkerDrawable(ctx, R.drawable.ic_marker_home, markerSizePx)
                    }
                    homeMarker = homeMkr

                    // ── Delivery rider marker — use top-down 3D model look
                    val riderMkr = Marker(mv).apply {
                        position = GeoPoint(driverLat, driverLng)
                        title = "Delivery Rider"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = scaledMarkerDrawable(ctx, R.drawable.ic_rider_topdown_3d, 140, removeWhiteBg = true)
                    }
                    driverMarker = riderMkr

                    // ── Initial route polyline (straight line; OSRM replaces it via LaunchedEffect)
                    val poly = Polyline(mv).apply {
                        setPoints(
                            straightLinePoints(
                                restaurantLat, restaurantLng,
                                driverLat, driverLng,
                                customerLat, customerLng
                            )
                        )
                        outlinePaint.apply {
                            color = android.graphics.Color.parseColor("#FC8019") // BiteCraft orange
                            strokeWidth = 10f
                            isAntiAlias = true
                        }
                    }
                    routePolyline = poly

                    // ── GPS "My Location" overlay with blue dot
                    val myLocOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), mv).apply {
                        enableMyLocation()
                        val gpsBmp = BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_marker_gps)
                        val scaled = Bitmap.createScaledBitmap(gpsBmp, 60, 60, true)
                        setPersonIcon(scaled)
                        setPersonHotspot(30f, 30f)
                        setDirectionIcon(scaled)
                    }
                    locationOverlay = myLocOverlay

                    val isRiderAssigned = orderStatus == "Confirmed" || orderStatus == "OutForDelivery" || orderStatus == "Delivered" || orderStatus == "Accepted" || orderStatus == "Preparing" || orderStatus == "Ready"

                    mv.overlays.clear()
                    if (isRiderAssigned) {
                        mv.overlays.addAll(
                            listOf(poly, restMkr, homeMkr, riderMkr, myLocOverlay)
                        )
                    } else {
                        mv.overlays.addAll(
                            listOf(restMkr, homeMkr, myLocOverlay)
                        )
                    }

                    // Center map to fit all key points
                    val midLat = if (isRiderAssigned) {
                        (restaurantLat + customerLat + driverLat) / 3.0
                    } else {
                        (restaurantLat + customerLat) / 2.0
                    }
                    val midLng = if (isRiderAssigned) {
                        (restaurantLng + customerLng + driverLng) / 3.0
                    } else {
                        (restaurantLng + customerLng) / 2.0
                    }
                    mv.controller.setCenter(GeoPoint(midLat, midLng))
                    mv.controller.setZoom(14.5)
                }
            },
            update = { mv ->
                val isRiderAssigned = orderStatus == "Confirmed" || orderStatus == "OutForDelivery" || orderStatus == "Delivered" || orderStatus == "Accepted" || orderStatus == "Preparing" || orderStatus == "Ready"
                
                mv.overlays.clear()
                
                val poly = routePolyline
                val restMkr = restMarker
                val homeMkr = homeMarker
                val riderMkr = driverMarker
                val locOverlay = locationOverlay
                
                if (isRiderAssigned) {
                    if (poly != null) mv.overlays.add(poly)
                    if (restMkr != null) mv.overlays.add(restMkr)
                    if (homeMkr != null) mv.overlays.add(homeMkr)
                    if (riderMkr != null) mv.overlays.add(riderMkr)
                    if (locOverlay != null) mv.overlays.add(locOverlay)
                } else {
                    if (restMkr != null) mv.overlays.add(restMkr)
                    if (homeMkr != null) mv.overlays.add(homeMkr)
                    if (locOverlay != null) mv.overlays.add(locOverlay)
                }
                
                mv.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── Route loading spinner overlay card
        if (isRouteLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFFC8019), // BiteCraft Orange
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Calculating delivery route…",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // ── Current Location FAB
        FloatingActionButton(
            onClick = {
                locationOverlay?.myLocation?.let { loc ->
                    mapView.controller.animateTo(loc, 17.0, 800L)
                } ?: Toast.makeText(context, "Acquiring GPS location…", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 56.dp, end = 12.dp)
                .size(44.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color(0xFF1976D2),
            elevation = FloatingActionButtonDefaults.elevation(6.dp)
        ) {
            Icon(Icons.Filled.MyLocation, contentDescription = "My Location", modifier = Modifier.size(22.dp))
        }

        // ── Map attribution (OSM required)
        Text(
            text = "© OpenStreetMap contributors",
            fontSize = 9.sp,
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(Color.White.copy(alpha = 0.7f))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OSMLocationPickerSheet — Full interactive map for picking a delivery address
// Shows in BottomSheet; tapping the map drops a pin and reverse-geocodes it
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OSMLocationPickerSheet(
    initialLat: Double = 16.5062,   // Vijayawada, AP
    initialLng: Double = 80.6480,
    onAddressSelected: (address: String, lat: Double, lng: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var pickedAddress by remember { mutableStateOf("Tap the map to set your delivery location") }
    var pickedLat by remember { mutableStateOf(initialLat) }
    var pickedLng by remember { mutableStateOf(initialLng) }
    var isGeocoding by remember { mutableStateOf(false) }
    var pinMarker by remember { mutableStateOf<Marker?>(null) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidTileCache = context.cacheDir.resolve("osmdroid")
        }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 5.0
            maxZoomLevel = 20.0
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(initialLat, initialLng))
            isTilesScaledToDpi = true
            setZoomRounding(true)
        }
    }

    // GPS "My Location" overlay
    val myLocOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
        }
    }

    // Map tap receiver — drop pin at tapped location and reverse-geocode
    val tapReceiver = remember {
        object : org.osmdroid.events.MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                pickedLat = p.latitude
                pickedLng = p.longitude

                val mkr = pinMarker ?: Marker(mapView).also {
                    it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    it.icon = scaledMarkerDrawable(context, R.drawable.ic_marker_home, 80)
                    mapView.overlays.add(it)
                    pinMarker = it
                }
                mkr.position = p
                mapView.invalidate()

                // Reverse-geocode on IO thread
                isGeocoding = true
                scope.launch(Dispatchers.IO) {
                    val resolved = try {
                        if (Geocoder.isPresent()) {
                            val addrs = Geocoder(context, Locale.getDefault())
                                .getFromLocation(p.latitude, p.longitude, 1)
                            addrs?.firstOrNull()?.getAddressLine(0)
                                ?: "${String.format("%.5f", p.latitude)}, ${String.format("%.5f", p.longitude)}"
                        } else {
                            "${String.format("%.5f", p.latitude)}, ${String.format("%.5f", p.longitude)}"
                        }
                    } catch (e: Exception) {
                        "${String.format("%.5f", p.latitude)}, ${String.format("%.5f", p.longitude)}"
                    }
                    withContext(Dispatchers.Main) {
                        pickedAddress = resolved
                        isGeocoding = false
                    }
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean = false
        }
    }

    val tapOverlay = remember { MapEventsOverlay(tapReceiver) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE  -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(36.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFFC8019),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Set Delivery Location",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Interactive OSM map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            ) {
                AndroidView(
                    factory = { ctx ->
                        mapView.also { mv ->
                            mv.overlays.clear()
                            mv.overlays.add(tapOverlay)
                            mv.overlays.add(myLocOverlay)

                            // Drop initial home marker at default / provided location
                            val initMarker = Marker(mv).apply {
                                position = GeoPoint(initialLat, initialLng)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                icon = scaledMarkerDrawable(ctx, R.drawable.ic_marker_home, 80)
                                title = "Delivery Here"
                            }
                            pinMarker = initMarker
                            mv.overlays.add(initMarker)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // GPS center FAB
                FloatingActionButton(
                    onClick = {
                        myLocOverlay.myLocation?.let { loc ->
                            mapView.controller.animateTo(loc, 17.0, 600L)
                            pickedLat = loc.latitude
                            pickedLng = loc.longitude
                            pinMarker?.position = loc
                            mapView.invalidate()
                            scope.launch(Dispatchers.IO) {
                                val resolved = try {
                                    val addrs = Geocoder(context, Locale.getDefault())
                                        .getFromLocation(loc.latitude, loc.longitude, 1)
                                    addrs?.firstOrNull()?.getAddressLine(0)
                                        ?: "${String.format("%.5f", loc.latitude)}, ${String.format("%.5f", loc.longitude)}"
                                } catch (e: Exception) {
                                    "${String.format("%.5f", loc.latitude)}, ${String.format("%.5f", loc.longitude)}"
                                }
                                withContext(Dispatchers.Main) { pickedAddress = resolved }
                            }
                        } ?: Toast.makeText(
                            context,
                            "Enable GPS to use current location",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(44.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Color(0xFF1976D2),
                    elevation = FloatingActionButtonDefaults.elevation(6.dp)
                ) {
                    Icon(Icons.Filled.GpsFixed, contentDescription = "GPS", modifier = Modifier.size(22.dp))
                }

                // OSM attribution (required by tile license)
                Text(
                    "© OpenStreetMap contributors",
                    fontSize = 9.sp,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(Color.White.copy(alpha = 0.7f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Address display + confirm button
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Selected Address",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))

                    if (isGeocoding) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Resolving address…", fontSize = 13.sp, color = Color.Gray)
                        }
                    } else {
                        Text(
                            text = pickedAddress,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { onAddressSelected(pickedAddress, pickedLat, pickedLng) },
                        enabled = !isGeocoding && pickedAddress != "Tap the map to set your delivery location",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC8019))
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Confirm Delivery Location",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
