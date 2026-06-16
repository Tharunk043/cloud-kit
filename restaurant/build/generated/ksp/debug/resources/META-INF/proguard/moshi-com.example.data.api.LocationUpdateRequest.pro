-keepnames class com.example.data.api.LocationUpdateRequest
-if class com.example.data.api.LocationUpdateRequest
-keep class com.example.data.api.LocationUpdateRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
