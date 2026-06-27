package com.collegereview.campuschronicles.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.collegereview.campuschronicles.GameViewModel
import com.collegereview.campuschronicles.R
import com.collegereview.campuschronicles.domain.models.*
import com.collegereview.campuschronicles.ui.components.TopHUD

@Composable
fun MapScreen(viewModel: GameViewModel, onAdminClick: () -> Unit) {
    val gameState by viewModel.gameState.collectAsState()
    val buildings by viewModel.buildings.collectAsState()
    val unanswered by viewModel.unansweredQuestions.collectAsState()

    if (gameState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF5B8DD9))
        }
        return
    }

    val scrollState = rememberScrollState()
    // Map width designed for 3-4 screen widths with denser clustering
    val mapWidth = 2200.dp

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        
        // --- LAYER 1: THE WORLD (Scrollable) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
        ) {
            Box(modifier = Modifier.requiredWidth(mapWidth).fillMaxHeight()) {
                
                // 1. Panoramic Background (Preserving Aspect Ratio)
                // We repeat the background image to fill the panoramic width without squishing
                Row(modifier = Modifier.fillMaxSize()) {
                    repeat(6) {
                        Image(
                            painter = painterResource(id = R.drawable.campus_bg),
                            contentDescription = null,
                            modifier = Modifier.fillMaxHeight().wrapContentWidth(),
                            contentScale = ContentScale.FillHeight
                        )
                    }
                }

                // 2. Campus Filler (Benches, Trees, Students)
                CampusDecorations()

                // 3. Interactive Buildings (Clustered into Zones)
                buildingCatalog.forEach { info ->
                    val buildingState = buildings.find { it.buildingType == info.type }
                    val hasQuiz = unanswered.any { it.mappedGameAction == info.gameAction }
                    val pos = getHotspotPosition(info.type)
                    
                    Box(
                        modifier = Modifier
                            .offset(x = pos.first.dp, y = pos.second.dp)
                    ) {
                        BuildingHotspot(
                            info = info,
                            state = buildingState,
                            hasQuiz = hasQuiz,
                            onClick = { viewModel.onBuildingClicked(info) }
                        )
                    }
                }
            }
        }

        // --- LAYER 2: HUD & UI (Fixed Overlay) ---
        Column(modifier = Modifier.fillMaxWidth()) {
            TopHUD(gameState!!, onAdminClick)
            CampusDevelopmentCard(buildings)
        }
    }
}

@Composable
fun CampusDecorations() {
    // Adding small decorative elements to break up empty space
    val decorations = listOf(
        Triple(650f, 200f, Icons.Default.Park),
        Triple(750f, 450f, Icons.Default.Chair),
        Triple(850f, 150f, Icons.Default.Groups),
        Triple(1300f, 300f, Icons.Default.Park),
        Triple(1450f, 550f, Icons.Default.Chair),
        Triple(1700f, 150f, Icons.Default.Groups),
        Triple(1000f, 400f, Icons.Default.Nature),
        Triple(400f, 600f, Icons.Default.Nature),
        Triple(1900f, 600f, Icons.Default.Nature)
    )

    decorations.forEach { (x, y, icon) ->
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .offset(x = x.dp, y = y.dp)
                .size(24.dp)
                .alpha(0.4f),
            tint = Color.White
        )
    }
}

@Composable
fun CampusDevelopmentCard(buildings: List<Building>) {
    val maxTotalLevels = buildingCatalog.size * 20
    val currentTotalLevels = buildings.sumOf { it.level }
    val developmentPercent = (currentTotalLevels.toFloat() / maxTotalLevels * 100).toInt()

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(0.55f)
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        color = Color(0xFF1E293B).copy(alpha = 0.95f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "CAMPUS COMPLETION",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { developmentPercent / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(CircleShape),
                        color = Color(0xFF10B981),
                        trackColor = Color(0xFF334155)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "$developmentPercent%",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
@Composable
fun BuildingHotspot(info: BuildingInfo, state: Building?, hasQuiz: Boolean, onClick: () -> Unit) {
    val isBuilt = state != null
    val level = state?.level ?: 0
    
    val infiniteTransition = rememberInfiniteTransition(label = "building_fx")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(140.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Main Game-Like Icon Container
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .offset(y = if (hasQuiz) bounce.dp else 0.dp)
                    .shadow(12.dp, CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = when {
                                hasQuiz -> listOf(Color(0xFFF87171), Color(0xFFEF4444))
                                !isBuilt -> listOf(Color(0xFF64748B), Color(0xFF475569))
                                else -> listOf(Color(0xFF60A5FA), Color(0xFF3B82F6))
                            }
                        ),
                        shape = CircleShape
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    imageVector = getBuildingIcon(info.type),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }

            // Status Badge (Top Left): Level or Locked
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .size(34.dp),
                shape = CircleShape,
                color = if (isBuilt) Color(0xFF0F172A) else Color(0xFF334155),
                border = BorderStroke(2.dp, if (isBuilt) Color(0xFF10B981) else Color(0xFF94A3B8)),
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isBuilt) {
                        Text(
                            "L$level",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    } else {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Action Badge (Top Right): Help/Quiz or Upgrade
            if (hasQuiz) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(34.dp),
                    shape = CircleShape,
                    color = Color(0xFFEF4444),
                    border = BorderStroke(2.dp, Color.White),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Help,
                            contentDescription = "New Quiz",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // High-Contrast Label
        Surface(
            color = Color.Black.copy(alpha = 0.8f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            shadowElevation = 4.dp
        ) {
            Text(
                info.displayName.uppercase(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

fun getBuildingIcon(type: BuildingType): ImageVector = when(type) {
    BuildingType.LIBRARY -> Icons.Default.Book
    BuildingType.LAB -> Icons.Default.Science
    BuildingType.CANTEEN -> Icons.Default.Restaurant
    BuildingType.SPORTS -> Icons.Default.SportsBasketball
    BuildingType.ADMIN_BLOCK -> Icons.Default.Business
    BuildingType.MEDICAL -> Icons.Default.MedicalServices
    BuildingType.HOSTEL -> Icons.Default.Hotel
    BuildingType.AUDITORIUM -> Icons.Default.EventSeat
    BuildingType.RESEARCH_CENTER -> Icons.Default.Biotech
    BuildingType.INNOVATION_HUB -> Icons.Default.Lightbulb
}

fun getHotspotPosition(type: BuildingType): Pair<Float, Float> {
    // Optimized clustering into 3 major zones: Academic, Student Life, and Events
    return when (type) {
        // --- ZONE 1: ACADEMIC (Left) ---
        BuildingType.LAB -> Pair(150f, 150f)
        BuildingType.LIBRARY -> Pair(450f, 250f)
        BuildingType.INNOVATION_HUB -> Pair(250f, 500f)
        BuildingType.RESEARCH_CENTER -> Pair(550f, 550f)
        
        // --- ZONE 2: STUDENT LIFE (Center) ---
        BuildingType.ADMIN_BLOCK -> Pair(900f, 150f)
        BuildingType.CANTEEN -> Pair(1150f, 350f)
        BuildingType.MEDICAL -> Pair(950f, 550f)
        BuildingType.HOSTEL -> Pair(1250f, 600f)
        
        // --- ZONE 3: EVENTS (Right) ---
        BuildingType.SPORTS -> Pair(1650f, 200f)
        BuildingType.AUDITORIUM -> Pair(1900f, 450f)
    }
}
