package com.hexwarfare.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.hexwarfare.app.domain.model.*
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * 战斗选项对话框
 */
@Composable
fun CombatOptionsDialog(
    attacker: GameUnit,
    defender: GameUnit,
    availableCombats: List<CombatType>,
    onCombatSelected: (CombatType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("选择战斗类型", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "对 ${defender.name} 发起攻击",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                // 攻击方信息
                CombatUnitCard(unit = attacker, label = "攻击方")

                Spacer(modifier = Modifier.height(12.dp))

                // 防御方信息
                CombatUnitCard(unit = defender, label = "防守方")

                Spacer(modifier = Modifier.height(16.dp))

                // 战斗类型选项
                Text(
                    text = "可用战斗类型",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                availableCombats.forEach { combatType ->
                    val distance = attacker.coord.distanceTo(defender.coord)
                    val isInRange = combatType.isInRange(distance)

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isInRange)
                                MaterialTheme.colorScheme.surface
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getCombatTypeIcon(combatType),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = if (isInRange)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = combatType.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isInRange)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = combatType.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!isInRange) {
                                    Text(
                                        text = "距离${distance}格，无法使用",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            if (isInRange) {
                                FilledTonalButton(onClick = { onCombatSelected(combatType) }) {
                                    Text("发起")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun CombatUnitCard(unit: GameUnit, label: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (unit.faction == Faction.PLAYER)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (unit.faction == Faction.PLAYER)
                            Color(0xFF2196F3)
                        else
                            Color(0xFFF44336)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit.name.first().toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = unit.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$label | ${unit.personnelLevel.displayName()} | ${unit.state.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getCombatTypeIcon(combatType: CombatType) = when (combatType) {
    CombatType.ARTILLERY -> Icons.Default.Star
    CombatType.ARCHERY -> Icons.Default.ArrowForward
    CombatType.ASSAULT -> Icons.Default.Add
    CombatType.MELEE -> Icons.Default.Favorite
}

/**
 * 骰子动画对话框
 */
@Composable
fun DiceAnimationDialog(
    attackerName: String,
    defenderName: String,
    combatType: CombatType,
    onAnimationComplete: (List<Int>) -> Unit
) {
    var isRolling by remember { mutableStateOf(true) }
    var finalRolls by remember { mutableStateOf<List<Int>>(emptyList()) }
    val diceCount = 3

    LaunchedEffect(Unit) {
        delay(1500) // 动画持续1.5秒
        val rolls = (1..diceCount).map { Random.nextInt(1, 7) }
        finalRolls = rolls
        isRolling = false
        delay(500)
        onAnimationComplete(rolls)
    }

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$attackerName vs $defenderName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${combatType.displayName} - 骰子判定中...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 骰子动画
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    repeat(diceCount) { index ->
                        DiceFace(
                            value = if (isRolling) Random.nextInt(1, 7) else finalRolls.getOrElse(index) { 1 },
                            isRolling = isRolling,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isRolling && finalRolls.isNotEmpty()) {
                    Text(
                        text = "结果: ${finalRolls.minOrNull()}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DiceFace(
    value: Int,
    isRolling: Boolean,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isRolling) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "diceRotation"
    )

    Card(
        modifier = modifier.graphicsLayer {
            if (isRolling) {
                rotationZ = rotation
            }
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 战斗结果对话框
 */
@Composable
fun CombatResultDialog(
    result: CombatResult,
    onDismiss: () -> Unit
) {
    val hitResult = result.hitResult

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (result.defenderRouted || result.defenderEliminated)
                        Icons.Default.Star
                    else
                        Icons.Default.Check,
                    contentDescription = null,
                    tint = if (result.defenderRouted || result.defenderEliminated)
                        Color(0xFFFFD700)
                    else
                        MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        result.defenderEliminated -> "敌方被消灭！"
                        result.defenderRouted -> "敌方溃散！"
                        result.defenderFormationBroken || result.defenderCasualty || result.defenderMoraleDrop -> "战斗结算"
                        else -> "战斗结束"
                    },
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            LazyColumn {
                // 战斗基本信息
                item {
                    CombatInfoSection(title = "战斗信息") {
                        InfoRow("攻击方", result.attacker.name)
                        InfoRow("防守方", result.defender.name)
                        InfoRow("战斗类型", result.combatType.displayName)
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                // 判定结果
                item {
                    CombatInfoSection(title = "判定结果") {
                        HitResultRow("阵型", hitResult.formationHit, hitResult.diceRolls.getOrElse(0) { 0 }, hitResult.attackPower, hitResult.finalThreshold)
                        HitResultRow("减员", hitResult.casualtyHit, hitResult.diceRolls.getOrElse(1) { 0 }, hitResult.attackPower, hitResult.finalThreshold)
                        HitResultRow("士气", hitResult.moraleHit, hitResult.diceRolls.getOrElse(2) { 0 }, hitResult.attackPower, hitResult.finalThreshold)
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                // 战斗效果
                item {
                    CombatInfoSection(title = "战斗效果") {
                        if (result.defenderFormationBroken) {
                            EffectRow(Icons.Default.Warning, "防守方阵型被打乱", Color(0xFFFF9800))
                        }
                        if (result.defenderCasualty) {
                            EffectRow(Icons.Default.Person, "防守方遭受减员", Color(0xFFF44336))
                        }
                        if (result.defenderMoraleDrop) {
                            EffectRow(Icons.Default.Warning, "防守方士气下降", Color(0xFF9C27B0))
                        }
                        if (result.defenderRouted) {
                            EffectRow(Icons.Default.Star, "防守方溃散！", Color(0xFFFF0000))
                        }
                        if (!result.defenderFormationBroken && !result.defenderCasualty && !result.defenderMoraleDrop) {
                            EffectRow(Icons.Default.Check, "防守方未受明显影响", Color(0xFF4CAF50))
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                // 防守方状态变化
                item {
                    CombatInfoSection(title = "状态变化") {
                        StateChangeRow("兵力", result.defender.personnelLevel.displayName(), result.defenderCasualty)
                        StateChangeRow("士气", result.defender.moraleLevel.displayName(), result.defenderMoraleDrop)
                        StateChangeRow("纪律", result.defender.disciplineLevel.displayName(), result.defenderFormationBroken)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("确认")
            }
        },
        dismissButton = {}
    )
}

@Composable
private fun CombatInfoSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HitResultRow(
    name: String,
    hit: Boolean,
    diceValue: Int,
    attack: Int,
    threshold: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (hit) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = null,
            tint = if (hit) Color(0xFF4CAF50) else Color(0xFFF44336),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$diceValue + $attack >= $threshold",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (hit) "成功" else "失败",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (hit) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}

@Composable
private fun EffectRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StateChangeRow(label: String, value: String, changed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (changed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            if (changed) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 获取单位状态的中文显示名称
 */
fun UnitState.displayName(): String = when (this) {
    UnitState.BATTLE_FORMATION_STRICT -> "野战严整"
    UnitState.BATTLE_FORMATION_LOOSE -> "野战散乱"
    UnitState.MARCH -> "行军"
    UnitState.REST -> "休整"
    UnitState.DEFENSE -> "守备"
    UnitState.TRANSPORT -> "运输"
    UnitState.RAID -> "掠袭"
}
