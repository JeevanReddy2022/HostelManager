package com.example.hostelmanager.ui.dashboard

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

// Premium palette
private val PremiumBackground = Color(0xFFF6F8FB)
private val PremiumNavy = Color(0xFF0B1F33)
private val PremiumNavyLight = Color(0xFF123B5D)
private val PremiumTeal = Color(0xFF0F766E)
private val PremiumCard = Color(0xFFFFFFFF)
private val PremiumText = Color(0xFF0F172A)
private val PremiumMuted = Color(0xFF64748B)
private val PremiumBorder = Color(0xFFE2E8F0)
private val PremiumSky = Color(0xFF38BDF8)
private val PremiumGreen = Color(0xFF10B981)
private val PremiumOrange = Color(0xFFF97316)
private val PremiumRed = Color(0xFFE11D48)
private val PremiumSoftBlue = Color(0xFFE0F2FE)
private val PremiumSoftGreen = Color(0xFFD1FAE5)
private val PremiumSoftOrange = Color(0xFFFFEDD5)
private val PremiumSoftRed = Color(0xFFFFE4E6)

// Shared gradient so the floating pill and the footer button read as the same control
private val ManageGradient = Brush.horizontalGradient(
    listOf(PremiumNavy, PremiumNavyLight, PremiumTeal)
)

private val FabShape = RoundedCornerShape(22.dp)
private val FooterButtonShape = RoundedCornerShape(20.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    hostelId: String,
    onBackClick: () -> Unit,
    onManageHostelClick: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    LaunchedEffect(hostelId) {
        viewModel.setHostelId(hostelId)
    }

    val stats by viewModel.stats.collectAsState()
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // Height of the footer block (button + caption), measured once it lays out.
    val footerHeightPx = remember { mutableIntStateOf(0) }

    // 0f = footer still below the fold  ->  floating pill fully visible
    // 1f = footer fully on screen       ->  floating pill fully gone
    val rawReveal by remember(density) {
        derivedStateOf {
            val max = scrollState.maxValue
            when {
                // Not measured yet, or nothing to scroll: the footer is already reachable.
                max == Int.MAX_VALUE -> 1f
                max <= 0 -> 1f
                else -> {
                    val trailingBreathingRoom = with(density) { 40.dp.toPx() }
                    val zone = (footerHeightPx.value + trailingBreathingRoom).coerceAtLeast(1f)
                    val remaining = (max - scrollState.value).toFloat()
                    (1f - (remaining / zone)).coerceIn(0f, 1f)
                }
            }
        }
    }

    // Softens the hand-off so a fast fling still reads as a fade, not a pop.
    val reveal by animateFloatAsState(
        targetValue = rawReveal,
        animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing),
        label = "manageHostelReveal"
    )

    val fabAlpha = (1f - reveal * 1.35f).coerceIn(0f, 1f)
    val footerAlpha = (reveal * 1.15f).coerceIn(0f, 1f)

    Scaffold(
        containerColor = PremiumBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dashboard",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PremiumBackground,
                    titleContentColor = PremiumText,
                    navigationIconContentColor = PremiumText
                )
            )
        },
        floatingActionButton = {
            if (stats != null && fabAlpha > 0.01f) {
                ManageHostelFloatingPill(
                    alpha = fabAlpha,
                    onClick = onManageHostelClick
                )
            }
        }
    ) { padding ->
        val currentStats = stats

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (currentStats == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PremiumCard),
                    border = BorderStroke(1.dp, PremiumBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Loading dashboard...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PremiumMuted
                        )
                    }
                }
            } else {
                DashboardContent(
                    stats = currentStats,
                    onManageHostelClick = onManageHostelClick,
                    footerAlpha = footerAlpha,
                    onFooterHeightChanged = { footerHeightPx.value = it }
                )
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Manage Hostel — floating pill (bottom-right, fades out at the end) */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageHostelFloatingPill(
    alpha: Float,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = FabShape,
        color = PremiumNavy,
        contentColor = Color.White,
        shadowElevation = 10.dp,
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            val scale = 0.86f + 0.14f * alpha
            scaleX = scale
            scaleY = scale
            translationY = (1f - alpha) * 34f
        }
    ) {
        Row(
            modifier = Modifier
                .background(ManageGradient)
                .padding(horizontal = 22.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "Manage Hostel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Manage Hostel — footer button (fades in as you reach the bottom)   */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageHostelFooter(
    alpha: Float,
    onClick: () -> Unit,
    onHeightChanged: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { onHeightChanged(it.height) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = onClick,
            enabled = alpha > 0.15f,
            shape = FooterButtonShape,
            color = PremiumNavy,
            contentColor = Color.White,
            shadowElevation = if (alpha > 0.5f) 6.dp else 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .graphicsLayer {
                    this.alpha = alpha
                    translationY = (1f - alpha) * 22f
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ManageGradient)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Manage Hostel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "Rooms • Residents • Payments • Expenses • Reports",
            style = MaterialTheme.typography.bodySmall,
            color = PremiumMuted,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { this.alpha = alpha },
            textAlign = TextAlign.Center
        )
    }
}

/* ------------------------------------------------------------------ */
/*  Dashboard content                                                  */
/* ------------------------------------------------------------------ */

@Composable
private fun DashboardContent(
    stats: DashboardStats,
    onManageHostelClick: () -> Unit,
    footerAlpha: Float,
    onFooterHeightChanged: (Int) -> Unit
) {
    val currentMonth = YearMonth.now()
    val lastMonth = currentMonth.minusMonths(1)
    val currentMonthTitle = formatMonthTitle(currentMonth)
    val lastMonthTitle = formatMonthTitle(lastMonth)

    HeroProfitCard(
        hostelName = stats.hostelName,
        monthTitle = currentMonthTitle,
        profitPaise = stats.profitThisMonth,
        incomePaise = stats.incomeThisMonth,
        expensePaise = stats.expensesThisMonth
    )

    SectionTitle(currentMonthTitle)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SmallMetricCard(
            title = "Check-ins",
            value = stats.checkInsThisMonth.toString(),
            caption = "New stays",
            accent = PremiumSky,
            softAccent = PremiumSoftBlue,
            modifier = Modifier.weight(1f)
        )
        SmallMetricCard(
            title = "Payments",
            value = stats.paymentsThisMonth.toString(),
            caption = "Entries",
            accent = PremiumGreen,
            softAccent = PremiumSoftGreen,
            modifier = Modifier.weight(1f)
        )
    }

    WideMetricCard(
        title = "Rent Collected",
        value = formatCurrency(stats.rentCollectedThisMonth),
        caption = "Received in $currentMonthTitle",
        accent = PremiumSky,
        softAccent = PremiumSoftBlue
    )

    SectionTitle("Occupancy")

    OccupancyCard(
        activeResidents = stats.activeResidents,
        totalBeds = stats.totalBeds,
        availableBeds = stats.availableBeds
    )

    SectionTitle("Expense Pulse")

    StatementCard(
        rows = listOf(
            StatementRowData(
                label = "Today",
                amountPaise = stats.expensesToday,
                accent = PremiumOrange
            ),
            StatementRowData(
                label = currentMonthTitle,
                amountPaise = stats.expensesThisMonth,
                accent = PremiumOrange
            ),
            StatementRowData(
                label = lastMonthTitle,
                amountPaise = stats.expensesLastMonth,
                accent = PremiumOrange
            )
        ),
        isProfitCard = false
    )

    SectionTitle("Profitability")

    StatementCard(
        rows = listOf(
            StatementRowData(
                label = currentMonthTitle,
                amountPaise = stats.profitThisMonth,
                accent = PremiumGreen
            ),
            StatementRowData(
                label = lastMonthTitle,
                amountPaise = stats.profitLastMonth,
                accent = PremiumGreen
            ),
            StatementRowData(
                label = "Y.T.D",
                amountPaise = stats.profitYTD,
                accent = PremiumGreen
            )
        ),
        isProfitCard = true
    )

    ManageHostelFooter(
        alpha = footerAlpha,
        onClick = onManageHostelClick,
        onHeightChanged = onFooterHeightChanged
    )

    Spacer(modifier = Modifier.height(6.dp))
}

/* ------------------------------------------------------------------ */
/*  Cards                                                              */
/* ------------------------------------------------------------------ */

@Composable
private fun HeroProfitCard(
    hostelName: String,
    monthTitle: String,
    profitPaise: Long,
    incomePaise: Long,
    expensePaise: Long
) {
    val isLoss = profitPaise < 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PremiumNavy,
                            PremiumNavyLight,
                            PremiumTeal
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.14f))
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            monthTitle,
                            color = Color.White.copy(alpha = 0.92f),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isLoss) Color(0xFFFFE4E6) else Color(0xFFD1FAE5)
                            )
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            if (isLoss) "LOSS" else "PROFIT",
                            color = if (isLoss) PremiumRed else Color(0xFF047857),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column {
                    Text(
                        hostelName,
                        color = Color.White.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Net Profit",
                        color = Color.White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        formatCurrency(profitPaise),
                        color = if (isLoss) Color(0xFFFF8FA3) else Color.White,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeroMiniStat(
                        title = "Collected",
                        value = formatCurrency(incomePaise),
                        modifier = Modifier.weight(1f)
                    )
                    HeroMiniStat(
                        title = "Spent",
                        value = formatCurrency(expensePaise),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMiniStat(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(14.dp)
    ) {
        Column {
            Text(
                title,
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge,
        color = PremiumText,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 2.dp)
    )
}

@Composable
private fun SmallMetricCard(
    title: String,
    value: String,
    caption: String,
    accent: Color,
    softAccent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumCard),
        border = BorderStroke(1.dp, PremiumBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(softAccent),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
            }

            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = PremiumText,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PremiumText,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = PremiumMuted
                )
            }
        }
    }
}

@Composable
private fun WideMetricCard(
    title: String,
    value: String,
    caption: String,
    accent: Color,
    softAccent: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumCard),
        border = BorderStroke(1.dp, PremiumBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            softAccent,
                            Color.White
                        )
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = PremiumText,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = PremiumMuted
                )
            }
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun OccupancyCard(
    activeResidents: Int,
    totalBeds: Int,
    availableBeds: Int
) {
    val safeAvailable = availableBeds.coerceAtLeast(0)
    val progress = if (totalBeds <= 0) {
        0f
    } else {
        (activeResidents.toFloat() / totalBeds.toFloat()).coerceIn(0f, 1f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumCard),
        border = BorderStroke(1.dp, PremiumBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "Active Residents",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PremiumMuted
                    )
                    Text(
                        activeResidents.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = PremiumText,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Available Beds",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PremiumMuted
                    )
                    Text(
                        "$safeAvailable / $totalBeds",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (safeAvailable > 0) PremiumGreen else PremiumOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(PremiumBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(PremiumSky, PremiumGreen)
                            )
                        )
                )
            }

            Text(
                "${(progress * 100).toInt()}% occupied",
                style = MaterialTheme.typography.bodySmall,
                color = PremiumMuted
            )
        }
    }
}

private data class StatementRowData(
    val label: String,
    val amountPaise: Long,
    val accent: Color
)

@Composable
private fun StatementCard(
    rows: List<StatementRowData>,
    isProfitCard: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumCard),
        border = BorderStroke(1.dp, PremiumBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            rows.forEachIndexed { index, row ->
                val isLoss = row.amountPaise < 0
                val amountColor = if (isProfitCard) {
                    if (isLoss) PremiumRed else PremiumGreen
                } else {
                    row.accent
                }
                val softColor = if (isProfitCard) {
                    if (isLoss) PremiumSoftRed else PremiumSoftGreen
                } else {
                    PremiumSoftOrange
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(softColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(amountColor)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                row.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = PremiumText,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isProfitCard) {
                                Text(
                                    if (isLoss) "Loss" else "Profit",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = amountColor
                                )
                            }
                        }
                    }
                    Text(
                        formatCurrency(row.amountPaise),
                        style = MaterialTheme.typography.titleLarge,
                        color = amountColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                if (index != rows.lastIndex) {
                    HorizontalDivider(
                        color = PremiumBorder,
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Helpers                                                            */
/* ------------------------------------------------------------------ */

private fun formatMonthTitle(yearMonth: YearMonth): String {
    val monthName = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    return "$monthName - ${yearMonth.year}"
}

fun formatCurrency(amountPaise: Long): String {
    val rupees = abs(amountPaise) / 100
    val formatted = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(rupees)
    return if (amountPaise < 0) "-₹$formatted" else "₹$formatted"
}