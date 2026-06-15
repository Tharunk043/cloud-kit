package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import com.example.data.local.FamilyMemberEntity
// ─────────────────────────────────────────────────────────────────────────────
// Local UI Model — NOT a Room entity
// ─────────────────────────────────────────────────────────────────────────────

data class FamilyMember(
    val id: Int,
    val name: String,
    val email: String,
    val avatarColor: Long,      // ARGB hex, e.g. 0xFF7C3AED
    val spendingLimit: Double,  // monthly cap in USD
    val monthlySpent: Double,   // spend so far this month
    val isAdmin: Boolean
)

// ─────────────────────────────────────────────────────────────────────────────
// Internal colour palette / design tokens
// ─────────────────────────────────────────────────────────────────────────────

private val FamilyPrimary      = Color(0xFF7C3AED)   // vivid violet
private val FamilySecondary    = Color(0xFFA855F7)   // soft purple
private val FamilyAccent       = Color(0xFFF97316)   // vibrant orange
private val FamilySurface      = Color(0xFFF5F3FF)   // pale lavender bg
private val FamilyGlass        = Color(0xFFFFFFFF).copy(alpha = 0.12f)
private val FamilyBorder       = Color(0xFF7C3AED).copy(alpha = 0.25f)
private val FamilyGold         = Color(0xFFFFD700)

private val AvatarPresetColors = listOf(
    0xFF7C3AED,   // violet
    0xFFF97316,   // orange
    0xFF10B981,   // emerald
    0xFF3B82F6,   // blue
    0xFFEC4899,   // pink
    0xFFF59E0B    // amber
)

private val GradientPurpleOrange = Brush.linearGradient(
    colors = listOf(FamilyPrimary, FamilyAccent)
)

private val GradientHeaderBg = Brush.linearGradient(
    colors = listOf(Color(0xFF4C1D95), Color(0xFF7C3AED))
)

// ─────────────────────────────────────────────────────────────────────────────
// Helper: two-letter initials from a full name
// ─────────────────────────────────────────────────────────────────────────────

private fun initials(name: String): String {
    val parts = name.trim().split(" ")
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        parts.size == 1 && parts[0].isNotEmpty() -> parts[0].take(2).uppercase()
        else -> "??"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. FamilyManagementScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FamilyManagementScreen(
    onBack: () -> Unit,
    onAddMember: (String, String, Double, Long) -> Unit,
    onRemoveMember: (Int) -> Unit,
    onUpdateLimit: (Int, Double) -> Unit,
    familyMembersFlow: List<FamilyMemberEntity>,
    onUpgrade: () -> Unit = {}
) {
    val members = remember(familyMembersFlow) {
        familyMembersFlow.map { entity ->
            FamilyMember(
                id = entity.id,
                name = entity.name,
                email = entity.email,
                avatarColor = entity.avatarColor,
                spendingLimit = entity.spendingLimit,
                monthlySpent = entity.monthlySpent,
                isAdmin = entity.isAdmin
            )
        }
    }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(FamilySurface)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Header: Family Plan gradient card ──────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GradientHeaderBg)
                        .padding(top = 48.dp, bottom = 28.dp, start = 20.dp, end = 20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack, modifier = Modifier.padding(end = 8.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.Groups,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Family Plan",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Glassmorphism subscription card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(FamilyGlass)
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("★", color = FamilyGold, fontSize = 18.sp)
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "BiteCraft Family Gold",
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Active · Renews Jul 10, 2026",
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 12.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        PillChip("Up to 6 members", FamilyGold.copy(alpha = 0.25f), FamilyGold)
                                        PillChip("Shared Wallet", Color.White.copy(0.18f), Color.White)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(FamilyGold.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.WorkspacePremium,
                                        contentDescription = null,
                                        tint = FamilyGold,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Section title + Add button ─────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Family Members",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E1B4B)
                        )
                        Text(
                            text = "${members.size} / 6 slots used",
                            color = FamilyPrimary.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    FilledTonalButton(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = FamilyPrimary.copy(alpha = 0.1f),
                            contentColor = FamilyPrimary
                        )
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Member", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // ── Horizontal scrollable member cards ─────────────────────────
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(members, key = { it.id }) { member ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInHorizontally()
                        ) {
                            FamilyMemberCard(member = member)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            // ── Family Plan Benefits ───────────────────────────────────────
            item {
                Spacer(Modifier.height(24.dp))
                FamilyBenefitsCard()
            }

            // ── Analytics card ─────────────────────────────────────────────
            item {
                Spacer(Modifier.height(20.dp))
                FamilySpendingAnalyticsCard(members = members)
            }
        }

        // ── Bottom CTA – Upgrade button ─────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(FamilySurface)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(GradientPurpleOrange)
                    .clickable { onUpgrade() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Upgrade to Family Gold",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }

    // ── Add Member Dialog ──────────────────────────────────────────────────
    if (showAddDialog) {
        AddMemberDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, email, limit, color ->
                onAddMember(name, email, limit, color)
                showAddDialog = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FamilyMemberCard – individual card in horizontal scroll
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FamilyMemberCard(member: FamilyMember) {
    val avatarColor = Color(member.avatarColor)
    val progress = if (member.spendingLimit > 0.0) (member.monthlySpent / member.spendingLimit).coerceIn(0.0, 1.0) else 0.0
    val progressColor = when {
        progress >= 0.9 -> Color(0xFFEF4444)
        progress >= 0.65 -> Color(0xFFF97316)
        else -> avatarColor
    }

    Card(
        modifier = Modifier
            .width(180.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, FamilyBorder, RoundedCornerShape(20.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar circle with initials
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(avatarColor, avatarColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials(member.name),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = member.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF1E1B4B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(2.dp))

            // Role badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (member.isAdmin) FamilyPrimary.copy(alpha = 0.12f)
                        else Color(0xFF10B981).copy(alpha = 0.12f)
                    )
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (member.isAdmin) "Admin" else "Member",
                    color = if (member.isAdmin) FamilyPrimary else Color(0xFF10B981),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.height(12.dp))

            // Spending info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Spent", color = Color.Gray, fontSize = 11.sp)
                Text(
                    text = "$${String.format("%.0f", member.monthlySpent)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFF1E1B4B)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Limit", color = Color.Gray, fontSize = 11.sp)
                Text(
                    text = "$${String.format("%.0f", member.spendingLimit)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFF1E1B4B)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFE9D5FF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.toFloat())
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(progressColor, progressColor.copy(alpha = 0.7f))
                            )
                        )
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}% used",
                color = progressColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Family Plan Benefits card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FamilyBenefitsCard() {
    val benefits = listOf(
        Triple(Icons.Filled.AccountBalanceWallet, "Shared Wallet", "Pool funds across all members effortlessly"),
        Triple(Icons.Filled.Tune, "Individual Limits", "Set custom spending caps per member"),
        Triple(Icons.AutoMirrored.Filled.CallSplit, "Split Bill", "Divide any order among chosen members"),
        Triple(Icons.Filled.LocalOffer, "Exclusive Discounts", "Family-only coupons up to 25% off"),
        Triple(Icons.Filled.FlashOn, "Priority Delivery", "Skip the queue on shared orders"),
        Triple(Icons.Filled.Analytics, "Spending Analytics", "Real-time family budget insights")
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, FamilyBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Stars,
                    contentDescription = null,
                    tint = FamilyPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Family Plan Benefits",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E1B4B)
                )
            }

            Spacer(Modifier.height(16.dp))

            benefits.forEachIndexed { index, (icon, title, desc) ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color(0xFFF3F4F6)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FamilyPrimary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = FamilyPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E1B4B))
                        Text(desc, color = Color.Gray, fontSize = 11.sp, lineHeight = 15.sp)
                    }
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Small helper composable – pill chip label
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PillChip(label: String, bg: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. SplitBillSheet – ModalBottomSheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitBillSheet(
    totalAmount: Double,
    members: List<FamilyMember>,
    onDismiss: () -> Unit,
    onConfirm: (Map<FamilyMember, Double>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // State
    var equalSplit by remember { mutableStateOf(true) }
    val selectedIds = remember { mutableStateListOf<Int>().apply { addAll(members.map { it.id }) } }
    val customAmounts = remember { mutableStateMapOf<Int, String>().apply {
        members.forEach { put(it.id, "") }
    }}

    val selectedMembers = members.filter { it.id in selectedIds }
    val perPersonShare = if (equalSplit && selectedMembers.isNotEmpty())
        totalAmount / selectedMembers.size else 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 6.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE2E8F0))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Split This Bill",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color(0xFF1E1B4B)
                    )
                    Text(
                        text = "Total: $${String.format("%.2f", totalAmount)}",
                        color = FamilyPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(FamilyPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.CallSplit,
                        contentDescription = null,
                        tint = FamilyPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Equal split toggle
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (equalSplit) FamilyPrimary.copy(alpha = 0.06f) else Color(0xFFF9FAFB)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (equalSplit) FamilyPrimary.copy(alpha = 0.3f) else Color(0xFFE5E7EB),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Equal Split",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E1B4B)
                        )
                        Text(
                            text = if (equalSplit) "Each pays equally" else "Set custom amounts",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    AnimatedContent(
                        targetState = equalSplit,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "toggle_switch"
                    ) { isEqual ->
                        Switch(
                            checked = isEqual,
                            onCheckedChange = { equalSplit = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = FamilyPrimary
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = "Select Members",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )
            Spacer(Modifier.height(10.dp))

            // Member list with checkboxes
            members.forEach { member ->
                val isSelected = member.id in selectedIds
                val avatarColor = Color(member.avatarColor)

                AnimatedContent(
                    targetState = equalSplit,
                    transitionSpec = {
                        slideInVertically { it } + fadeIn() togetherWith
                        slideOutVertically { -it } + fadeOut()
                    },
                    label = "member_${member.id}"
                ) { isEqual ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                1.dp,
                                if (isSelected) FamilyPrimary.copy(alpha = 0.3f) else Color(0xFFE5E7EB),
                                RoundedCornerShape(14.dp)
                            ),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) FamilyPrimary.copy(alpha = 0.04f) else Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedIds.remove(member.id)
                                    else selectedIds.add(member.id)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (it) selectedIds.add(member.id)
                                    else selectedIds.remove(member.id)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = FamilyPrimary,
                                    uncheckedColor = Color(0xFFD1D5DB)
                                )
                            )
                            Spacer(Modifier.width(8.dp))

                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(avatarColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    initials(member.name),
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    member.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E1B4B)
                                )
                                Text(
                                    member.email,
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            // Share display
                            if (isEqual) {
                                Text(
                                    text = if (isSelected) "$${String.format("%.2f", perPersonShare)}" else "—",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) FamilyPrimary else Color.Gray
                                )
                            } else {
                                OutlinedTextField(
                                    value = customAmounts[member.id] ?: "",
                                    onValueChange = { v -> customAmounts[member.id] = v },
                                    modifier = Modifier.width(80.dp),
                                    singleLine = true,
                                    placeholder = { Text("0.00", fontSize = 12.sp) },
                                    prefix = { Text("$", fontSize = 12.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = FamilyPrimary,
                                        unfocusedBorderColor = Color(0xFFE5E7EB)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Running total summary when custom mode
            AnimatedVisibility(visible = !equalSplit) {
                val customTotal = members.sumOf { customAmounts[it.id]?.toDoubleOrNull() ?: 0.0 }
                val remaining = totalAmount - customTotal
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (remaining < 0.01) Color(0xFFECFDF5) else Color(0xFFFFF7ED)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (remaining < 0.01) "✓ Fully allocated" else "Remaining",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (remaining < 0.01) Color(0xFF059669) else Color(0xFFF97316)
                        )
                        if (remaining >= 0.01) {
                            Text(
                                text = "$${String.format("%.2f", remaining)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color(0xFFF97316)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Confirm button
            val confirmEnabled = selectedIds.isNotEmpty() &&
                    (equalSplit || members.sumOf { customAmounts[it.id]?.toDoubleOrNull() ?: 0.0 } >= totalAmount - 0.01)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (confirmEnabled) GradientPurpleOrange
                        else Brush.linearGradient(listOf(Color(0xFFD1D5DB), Color(0xFF9CA3AF)))
                    )
                    .clickable(enabled = confirmEnabled) {
                        val splits: Map<FamilyMember, Double> = if (equalSplit) {
                            selectedMembers.associateWith { perPersonShare }
                        } else {
                            members
                                .filter { it.id in selectedIds }
                                .associateWith { customAmounts[it.id]?.toDoubleOrNull() ?: 0.0 }
                        }
                        onConfirm(splits)
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Confirm Split",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. FamilySpendingAnalyticsCard – Canvas bar chart
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FamilySpendingAnalyticsCard(members: List<FamilyMember>) {
    val totalSpent = members.sumOf { it.monthlySpent }
    val maxSpent = members.maxOfOrNull { it.monthlySpent }?.coerceAtLeast(1.0) ?: 1.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, FamilyBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.BarChart,
                        contentDescription = null,
                        tint = FamilyPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Spending Analytics",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E1B4B)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "This Month",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$${String.format("%.2f", totalSpent)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = FamilyPrimary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Per-member horizontal bars
            members.forEach { member ->
                val barColor = Color(member.avatarColor)
                val fraction = (member.monthlySpent / maxSpent).coerceIn(0.0, 1.0).toFloat()
                val utilization = if (member.spendingLimit > 0)
                    ((member.monthlySpent / member.spendingLimit) * 100).toInt() else 0

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(barColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            initials(member.name),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    // Name
                    Text(
                        text = member.name.split(" ").first(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color(0xFF374151),
                        modifier = Modifier.width(56.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.width(8.dp))

                    // Canvas-drawn horizontal bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0xFFF3F4F6))
                    ) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val barWidth = size.width * fraction
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(barColor, barColor.copy(alpha = 0.65f)),
                                    start = Offset.Zero,
                                    end = Offset(size.width, 0f)
                                ),
                                topLeft = Offset.Zero,
                                size = Size(barWidth, size.height),
                                cornerRadius = CornerRadius(7.dp.toPx())
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Utilization %
                    Text(
                        text = "${utilization}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = when {
                            utilization >= 90 -> Color(0xFFEF4444)
                            utilization >= 65 -> Color(0xFFF97316)
                            else -> barColor
                        },
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(Modifier.height(12.dp))

            // Footer totals row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                _AnalyticStat(
                    label = "Total Spent",
                    value = "$${String.format("%.2f", totalSpent)}",
                    color = FamilyPrimary
                )
                _AnalyticStat(
                    label = "Total Budget",
                    value = "$${String.format("%.2f", members.sumOf { it.spendingLimit })}",
                    color = Color(0xFF10B981)
                )
                _AnalyticStat(
                    label = "Remaining",
                    value = "$${String.format("%.2f", (members.sumOf { it.spendingLimit } - totalSpent).coerceAtLeast(0.0))}",
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun _AnalyticStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = color)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. AddMemberDialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, email: String, limit: Double, avatarColor: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var limitSlider by remember { mutableFloatStateOf(200f) }
    var selectedColorIndex by remember { mutableIntStateOf(0) }

    val nameError = name.isNotBlank() && name.trim().length < 2
    val emailError = email.isNotBlank() && !email.contains("@")
    val canAdd = name.isNotBlank() && !nameError && email.isNotBlank() && !emailError

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, FamilyBorder, RoundedCornerShape(24.dp))
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Add Family Member",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E1B4B)
                        )
                        Text(
                            "Invite to your family plan",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = Color.Gray)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Avatar preview
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(AvatarPresetColors[selectedColorIndex]))
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (name.isNotBlank()) initials(name) else "?",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = FamilyPrimary)
                    },
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("Name must be at least 2 characters") }} else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FamilyPrimary,
                        focusedLabelColor = FamilyPrimary,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = {
                        Icon(Icons.Filled.Email, contentDescription = null, tint = FamilyPrimary)
                    },
                    isError = emailError,
                    supportingText = if (emailError) {{ Text("Enter a valid email address") }} else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FamilyPrimary,
                        focusedLabelColor = FamilyPrimary,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                Spacer(Modifier.height(20.dp))

                // Spending limit slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Monthly Spending Limit",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF374151)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FamilyPrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$${limitSlider.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = FamilyPrimary
                        )
                    }
                }

                Slider(
                    value = limitSlider,
                    onValueChange = { limitSlider = it },
                    valueRange = 0f..500f,
                    steps = 49,   // 50 steps → 10 each
                    colors = SliderDefaults.colors(
                        thumbColor = FamilyPrimary,
                        activeTrackColor = FamilyPrimary,
                        inactiveTrackColor = FamilyPrimary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$0", color = Color.Gray, fontSize = 11.sp)
                    Text("$500", color = Color.Gray, fontSize = 11.sp)
                }

                Spacer(Modifier.height(20.dp))

                // Avatar color picker
                Text(
                    "Choose Avatar Color",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF374151)
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AvatarPresetColors.forEachIndexed { index, colorLong ->
                        val isSelected = index == selectedColorIndex
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(colorLong))
                                .clickable { selectedColorIndex = index }
                                .then(
                                    if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Add button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (canAdd) GradientPurpleOrange
                            else Brush.linearGradient(listOf(Color(0xFFD1D5DB), Color(0xFF9CA3AF)))
                        )
                        .clickable(enabled = canAdd) {
                            onAdd(
                                name.trim(),
                                email.trim(),
                                limitSlider.toDouble(),
                                AvatarPresetColors[selectedColorIndex]
                            )
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.PersonAdd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Add to Family",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sample data factory (used for previews / default args)
// ─────────────────────────────────────────────────────────────────────────────

private fun sampleFamilyMembers(): List<FamilyMember> = listOf(
    FamilyMember(
        id = 1,
        name = "Alex Morgan",
        email = "alex.morgan@email.com",
        avatarColor = 0xFF7C3AED,
        spendingLimit = 300.0,
        monthlySpent = 178.50,
        isAdmin = true
    ),
    FamilyMember(
        id = 2,
        name = "Jamie Park",
        email = "jamie.park@email.com",
        avatarColor = 0xFF10B981,
        spendingLimit = 200.0,
        monthlySpent = 89.00,
        isAdmin = false
    ),
    FamilyMember(
        id = 3,
        name = "Sam Rivera",
        email = "sam.r@email.com",
        avatarColor = 0xFFF97316,
        spendingLimit = 150.0,
        monthlySpent = 143.20,
        isAdmin = false
    ),
    FamilyMember(
        id = 4,
        name = "Casey Lee",
        email = "casey.lee@email.com",
        avatarColor = 0xFF3B82F6,
        spendingLimit = 100.0,
        monthlySpent = 34.75,
        isAdmin = false
    )
)
