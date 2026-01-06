package sim2.app.talleb_5edma.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sim2.app.talleb_5edma.models.*

// ==================== COULEURS ====================
private val SuccessGreen = Color(0xFF10B981)
private val WarningOrange = Color(0xFFF59E0B)
private val ErrorRed = Color(0xFFDC2626)
private val InfoBlue = Color(0xFF3B82F6)
private val CardBackground = Color.White
private val BackgroundLight = Color(0xFFF8F8FB)
private val TextPrimary = Color(0xFF1F2937)
private val TextSecondary = Color(0xFF6B7280)
private val BorderColor = Color(0xFFE5E7EB)

// Couleurs pour les catégories
private val TravailColor = Color(0xFFDC2626)
private val EtudesColor = Color(0xFF3B82F6)
private val ReposColor = Color(0xFF10B981)
private val ActivitesColor = Color(0xFFF97316)

// ==================== SCORE CARD ====================
@Composable
fun ScoreCard(
    score: Int,
    scoreBreakdown: ScoreBreakdown? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Score d'équilibre",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            // Jauge circulaire
            CircularScoreGauge(
                score = score,
                modifier = Modifier.size(160.dp)
            )
            
            // Label du score
            Text(
                text = getScoreLabel(score),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = getScoreColor(score)
            )
            
            // Breakdown du score (si disponible)
            scoreBreakdown?.let { breakdown ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(color = BorderColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ScoreBreakdownItem("Score de base", breakdown.baseScore, Color.Gray)
                    if (breakdown.workStudyBalance != 0) {
                        ScoreBreakdownItem(
                            "Équilibre travail/études",
                            breakdown.workStudyBalance,
                            if (breakdown.workStudyBalance > 0) SuccessGreen else ErrorRed
                        )
                    }
                    if (breakdown.restPenalty != 0) {
                        ScoreBreakdownItem("Repos", breakdown.restPenalty, ErrorRed)
                    }
                    if (breakdown.conflictPenalty != 0) {
                        ScoreBreakdownItem("Conflits", breakdown.conflictPenalty, ErrorRed)
                    }
                    if (breakdown.overloadPenalty != 0) {
                        ScoreBreakdownItem("Surcharge", breakdown.overloadPenalty, ErrorRed)
                    }
                    if (breakdown.bonuses != 0) {
                        ScoreBreakdownItem("Bonus", breakdown.bonuses, SuccessGreen)
                    }
                }
            }
        }
    }
}

@Composable
fun CircularScoreGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val progress = score / 100f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1500),
        label = "score_animation"
    )
    
    val scoreColor = getScoreColor(score)
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Cercle de fond
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFFE5E7EB),
                radius = size.minDimension / 2 - 16.dp.toPx(),
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Cercle de progression
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20.dp.toPx()
            val sweepAngle = 360f * animatedProgress
            
            drawArc(
                color = scoreColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        // Texte au centre
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )
            Text(
                text = "/100",
                fontSize = 20.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ScoreBreakdownItem(
    label: String,
    value: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextSecondary
        )
        Text(
            text = if (value >= 0) "+$value" else "$value",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

// ==================== STATISTICS CARDS ====================
@Composable
fun StatisticsCards(
    analyseHebdomadaire: AnalyseHebdomadaireEnhanced,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Statistiques hebdomadaires",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatisticCard(
                label = "Travail",
                value = analyseHebdomadaire.heuresTravail,
                color = TravailColor,
                icon = Icons.Default.Work,
                modifier = Modifier.weight(1f)
            )
            StatisticCard(
                label = "Études",
                value = analyseHebdomadaire.heuresEtudes,
                color = EtudesColor,
                icon = Icons.Default.School,
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatisticCard(
                label = "Repos",
                value = analyseHebdomadaire.heuresRepos,
                color = ReposColor,
                icon = Icons.Default.Bedtime,
                modifier = Modifier.weight(1f)
            )
            StatisticCard(
                label = "Activités",
                value = analyseHebdomadaire.heuresActivites,
                color = ActivitesColor,
                icon = Icons.Default.FitnessCenter,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatisticCard(
    label: String,
    value: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            
            Text(
                text = "${String.format("%.1f", value)}h",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// ==================== CONFLICTS LIST ====================
@Composable
fun ConflictsList(
    conflicts: List<Conflict>,
    modifier: Modifier = Modifier
) {
    if (conflicts.isEmpty()) return
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Conflits détectés",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ErrorRed.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "${conflicts.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        
        conflicts.forEach { conflict ->
            ConflictCard(conflict = conflict)
        }
    }
}

@Composable
fun ConflictCard(
    conflict: Conflict
) {
    val severityColor = when (conflict.severity.lowercase()) {
        "high" -> ErrorRed
        "medium" -> WarningOrange
        else -> WarningOrange.copy(alpha = 0.7f)
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground,
        border = BorderStroke(2.dp, severityColor.copy(alpha = 0.3f)),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = severityColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = conflict.date,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = severityColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = conflict.severity.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = severityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ConflictEventRow("1", conflict.event1)
                Text("⏱ Chevauchment: ${conflict.overlapDuration} min", fontSize = 12.sp, color = TextSecondary)
                ConflictEventRow("2", conflict.event2)
            }
            
            Divider(color = BorderColor)
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = InfoBlue,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = conflict.suggestion,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ConflictEventRow(
    number: String,
    event: ConflictEvent
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = TextSecondary.copy(alpha = 0.1f),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.titre,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = event.heureDebut,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

// ==================== OVERLOADED DAYS LIST ====================
@Composable
fun OverloadedDaysList(
    overloadedDays: List<OverloadedDay>,
    modifier: Modifier = Modifier
) {
    if (overloadedDays.isEmpty()) return
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Jours surchargés",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        overloadedDays.forEach { day ->
            OverloadedDayCard(day = day)
        }
    }
}

@Composable
fun OverloadedDayCard(
    day: OverloadedDay
) {
    val levelColor = when (day.level.lowercase()) {
        "critique" -> ErrorRed
        "très élevé" -> ErrorRed.copy(alpha = 0.8f)
        "élevé" -> WarningOrange
        else -> WarningOrange.copy(alpha = 0.7f)
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, levelColor.copy(alpha = 0.3f)),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${day.jour} ${day.date}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${String.format("%.1f", day.totalHours)}h total",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = levelColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = day.level,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = levelColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            
            if (day.recommendations.isNotEmpty()) {
                Divider(color = BorderColor)
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    day.recommendations.forEach { rec ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = InfoBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = rec,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== RECOMMENDATIONS LIST ====================
@Composable
fun RecommendationsList(
    recommendations: List<Recommandation>,
    modifier: Modifier = Modifier
) {
    if (recommendations.isEmpty()) return
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Recommandations IA",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        recommendations.forEach { recommendation ->
            RecommendationCard(recommendation = recommendation)
        }
    }
}

@Composable
fun RecommendationCard(
    recommendation: Recommandation
) {
    val priorityColor = when (recommendation.priorite.lowercase()) {
        "haute" -> ErrorRed
        "moyenne" -> WarningOrange
        else -> SuccessGreen
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, BorderColor),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = priorityColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = recommendation.titre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = priorityColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = recommendation.priorite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = priorityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = InfoBlue.copy(alpha = 0.1f)
            ) {
                Text(
                    text = recommendation.type,
                    fontSize = 11.sp,
                    color = InfoBlue,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Text(
                text = recommendation.description,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )
            
            recommendation.actionSuggeree?.let { action ->
                Divider(color = BorderColor)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = InfoBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = action,
                        fontSize = 13.sp,
                        color = InfoBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ==================== HEALTH SUMMARY CARD ====================
@Composable
fun HealthSummaryCard(
    healthSummary: HealthSummary,
    modifier: Modifier = Modifier
) {
    val statusColor = when (healthSummary.status.lowercase()) {
        "excellent" -> SuccessGreen
        "bon" -> InfoBlue
        "moyen" -> WarningOrange
        "critique" -> ErrorRed
        else -> TextSecondary
    }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        border = BorderStroke(2.dp, statusColor.copy(alpha = 0.3f)),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Résumé de santé",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = healthSummary.status.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            if (healthSummary.mainIssues.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Points à améliorer",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    healthSummary.mainIssues.forEach { issue ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = issue,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
            
            if (healthSummary.mainStrengths.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Points forts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    healthSummary.mainStrengths.forEach { strength ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = strength,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== UTILITAIRES ====================
fun getScoreLabel(score: Int): String {
    return when {
        score >= 80 -> "Excellent"
        score >= 60 -> "Bon"
        score >= 40 -> "Moyen"
        else -> "À améliorer"
    }
}

fun getScoreColor(score: Int): Color {
    return when {
        score >= 80 -> SuccessGreen
        score >= 60 -> InfoBlue
        score >= 40 -> WarningOrange
        else -> ErrorRed
    }
}
