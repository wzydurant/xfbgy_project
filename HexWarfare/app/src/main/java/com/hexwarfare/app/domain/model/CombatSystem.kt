package com.hexwarfare.app.domain.model

import kotlin.random.Random

/**
 * 战斗类型枚举
 */
enum class CombatType(
    val displayName: String,
    val minRange: Int,
    val maxRange: Int,
    val description: String
) {
    ARTILLERY("炮击", 1, 5, "1-5格，打散阵型"),
    ARCHERY("远射", 1, 1, "相隔1格，移动力-1"),
    ASSAULT("对攻", 1, 1, "相邻格"),
    MELEE("肉搏", 0, 0, "同格");

    /**
     * 检查目标距离是否在攻击范围内
     */
    fun isInRange(distance: Int): Boolean {
        return distance in minRange..maxRange
    }
}

/**
 * 战斗属性数据类 (阵型打击/减员/士气打击值)
 */
data class CombatStats(
    val formationAttack: Int,  // 阵型打击
    val casualtyAttack: Int,    // 减员打击
    val moraleAttack: Int       // 士气打击
) {
    companion object {
        // 长矛对攻：2/1/1，肉搏：1/1/0
        val SPEAR_ASSAULT = CombatStats(2, 1, 1)
        val SPEAR_MELEE = CombatStats(1, 1, 0)

        // 剑对攻：1/1/1，肉搏：0/3/1
        val SWORD_ASSAULT = CombatStats(1, 1, 1)
        val SWORD_MELEE = CombatStats(0, 3, 1)

        // 弓对攻：1/1/1，肉搏：1/1/1
        val BOW_ASSAULT = CombatStats(1, 1, 1)
        val BOW_MELEE = CombatStats(1, 1, 1)

        // 无武器默认
        val UNARMED_ASSAULT = CombatStats(1, 1, 1)
        val UNARMED_MELEE = CombatStats(1, 1, 1)

        /**
         * 根据武器类型和战斗类型获取战斗属性
         */
        fun fromWeaponAndType(weapon: WeaponType?, combatType: CombatType): CombatStats {
            return when (combatType) {
                CombatType.ASSAULT -> when (weapon) {
                    WeaponType.SPEAR -> SPEAR_ASSAULT
                    WeaponType.SWORD -> SWORD_ASSAULT
                    WeaponType.BOW -> BOW_ASSAULT
                    null -> UNARMED_ASSAULT
                }
                CombatType.MELEE -> when (weapon) {
                    WeaponType.SPEAR -> SPEAR_MELEE
                    WeaponType.SWORD -> SWORD_MELEE
                    WeaponType.BOW -> BOW_MELEE
                    null -> UNARMED_MELEE
                }
                CombatType.ARTILLERY -> CombatStats(2, 2, 1)
                CombatType.ARCHERY -> CombatStats(1, 1, 1)
            }
        }
    }
}

/**
 * 战斗打击结果
 */
data class CombatHitResult(
    val formationHit: Boolean,   // 阵型打击成功
    val casualtyHit: Boolean,    // 减员打击成功
    val moraleHit: Boolean,       // 士气打击成功
    val diceRolls: List<Int>,     // 骰子点数
    val attackPower: Int,         // 打击能力
    val defensePower: Int,        // 防御力
    val moraleModifier: Int,       // 士气修正
    val finalThreshold: Int      // 最终阈值
)

/**
 * 单项打击判定结果
 */
data class SingleHitResult(
    val hit: Boolean,
    val diceValue: Int,
    val threshold: Int,
    val modifier: Int
)

/**
 * 战斗结果
 */
data class CombatResult(
    val attacker: GameUnit,
    val defender: GameUnit,
    val combatType: CombatType,
    val hitResult: CombatHitResult,
    val defenderFormationBroken: Boolean = false,      // 阵型被打乱
    val defenderCasualty: Boolean = false,              // 兵力减员
    val defenderMoraleDrop: Boolean = false,            // 士气下降
    val defenderRouted: Boolean = false,                 // 溃散
    val attackerEliminated: Boolean = false,            // 攻击方被消灭
    val defenderEliminated: Boolean = false,            // 防守方被消灭
    val combatLog: List<String> = emptyList()
)

/**
 * 骰子掷骰结果
 */
data class DiceRollResult(
    val diceCount: Int,
    val rolls: List<Int>,
    val finalValue: Int  // 取较小值规则下的最终值
)

/**
 * 骰子系统
 */
object DiceSystem {
    private const val DICE_MIN = 1
    private const val DICE_MAX = 6

    /**
     * 根据兵力等级确定骰子数量和规则
     * 满编(2人力)：1颗骰
     * 缺员(1人力)：2颗骰，取较小值
     * 残部(0人力)：3颗骰，取较小值
     */
    fun getDiceRules(personnelLevel: PersonnelLevel): Pair<Int, DiceSelectionRule> {
        return when (personnelLevel) {
            PersonnelLevel.HIGH -> 1 to DiceSelectionRule.HIGHEST
            PersonnelLevel.MEDIUM -> 2 to DiceSelectionRule.LOWEST
            PersonnelLevel.LOW -> 3 to DiceSelectionRule.LOWEST
        }
    }

    /**
     * 掷骰子
     */
    fun rollDice(personnelLevel: PersonnelLevel): DiceRollResult {
        val (diceCount, selectionRule) = getDiceRules(personnelLevel)
        val rolls = (1..diceCount).map { Random.nextInt(DICE_MIN, DICE_MAX + 1) }
        val finalValue = when (selectionRule) {
            DiceSelectionRule.HIGHEST -> rolls.maxOrNull() ?: 0
            DiceSelectionRule.LOWEST -> rolls.minOrNull() ?: 0
        }
        return DiceRollResult(diceCount, rolls, finalValue)
    }

    /**
     * 掷多组骰子（用于三项独立判定）
     */
    fun rollMultipleDice(personnelLevel: PersonnelLevel, count: Int): List<DiceRollResult> {
        return (1..count).map { rollDice(personnelLevel) }
    }
}

enum class DiceSelectionRule {
    HIGHEST,  // 取最高值
    LOWEST    // 取最低值
}

/**
 * 战斗计算引擎
 */
object CombatEngine {

    /**
     * 计算防御力
     * 基础防御力：野战严整/守备 = 5，其余 = 4
     * 装备加成：盾牌+1（不叠加）、盔甲+1/件
     * 兵力修正：满编0、缺员-1、残部-2
     * 士气修正：惊慌失措-1
     */
    fun calculateDefensePower(unit: GameUnit): Int {
        var defense = when (unit.state) {
            UnitState.BATTLE_FORMATION_STRICT, UnitState.DEFENSE -> 5
            else -> 4
        }

        // 装备加成
        unit.carriedEquipment.defensiveGear?.let { gear ->
            when (gear) {
                DefensiveGearType.SHIELD -> defense += 1
                DefensiveGearType.ARMOR -> defense += 1
            }
        }

        // 额外盔甲（携带装备中的数量）
        // 注意：这里简化处理，实际可能有多个盔甲

        // 兵力修正
        defense += when (unit.personnelLevel) {
            PersonnelLevel.HIGH -> 0
            PersonnelLevel.MEDIUM -> -1
            PersonnelLevel.LOW -> -2
        }

        // 士气修正（惊慌失措）
        if (unit.moraleLevel == MoraleLevel.BROKEN) {
            defense -= 1
        }

        // 散乱惩罚（肉搏时）
        if (unit.state == UnitState.BATTLE_FORMATION_LOOSE) {
            defense -= 1
        }

        return defense.coerceAtLeast(0)
    }

    /**
     * 计算打击能力
     */
    fun calculateAttackPower(unit: GameUnit, combatType: CombatType): Int {
        val stats = CombatStats.fromWeaponAndType(unit.carriedEquipment.weapon, combatType)

        // 散乱惩罚（肉搏时）
        val formationBonus = if (unit.state == UnitState.BATTLE_FORMATION_LOOSE && combatType == CombatType.MELEE) {
            -1
        } else {
            0
        }

        return when (combatType) {
            CombatType.ASSAULT -> stats.formationAttack + formationBonus
            CombatType.MELEE -> stats.casualtyAttack + formationBonus
            CombatType.ARCHERY -> stats.formationAttack
            CombatType.ARTILLERY -> stats.formationAttack
        }
    }

    /**
     * 执行三项独立打击判定
     */
    fun resolveCombat(attacker: GameUnit, defender: GameUnit, combatType: CombatType): CombatResult {
        val attackStats = CombatStats.fromWeaponAndType(attacker.carriedEquipment.weapon, combatType)
        val defensePower = calculateDefensePower(defender)

        // 士气修正
        val moraleModifier = getMoraleModifier(attacker, defender)

        // 掷骰（三项打击独立判定）
        val formationRoll = DiceSystem.rollDice(attacker.personnelLevel)
        val casualtyRoll = DiceSystem.rollDice(attacker.personnelLevel)
        val moraleRoll = DiceSystem.rollDice(attacker.personnelLevel)

        // 计算阈值 = 防御力 - 士气修正
        val formationThreshold = defensePower - moraleModifier
        val casualtyThreshold = defensePower - moraleModifier
        val moraleThreshold = defensePower - moraleModifier

        // 判定是否成功
        val formationHit = formationRoll.finalValue + attackStats.formationAttack >= formationThreshold
        val casualtyHit = casualtyRoll.finalValue + attackStats.casualtyAttack >= casualtyThreshold
        val moraleHit = moraleRoll.finalValue + attackStats.moraleAttack >= moraleThreshold

        val diceRolls = listOf(formationRoll.finalValue, casualtyRoll.finalValue, moraleRoll.finalValue)

        val hitResult = CombatHitResult(
            formationHit = formationHit,
            casualtyHit = casualtyHit,
            moraleHit = moraleHit,
            diceRolls = diceRolls,
            attackPower = attackStats.formationAttack,
            defensePower = defensePower,
            moraleModifier = moraleModifier,
            finalThreshold = formationThreshold
        )

        // 构建战斗日志
        val combatLog = mutableListOf<String>()
        combatLog.add("=== 战斗报告 ===")
        combatLog.add("攻击方: ${attacker.name} (${attacker.personnelLevel.displayName()})")
        combatLog.add("防守方: ${defender.name} (${defender.personnelLevel.displayName()})")
        combatLog.add("战斗类型: ${combatType.displayName}")
        combatLog.add("防御力: $defensePower | 士气修正: $moraleModifier")
        combatLog.add("---")
        combatLog.add("阵型判定: 骰子${formationRoll.finalValue} + ${attackStats.formationAttack} >= $formationThreshold → ${if (formationHit) "成功" else "失败"}")
        combatLog.add("减员判定: 骰子${casualtyRoll.finalValue} + ${attackStats.casualtyAttack} >= $casualtyThreshold → ${if (casualtyHit) "成功" else "失败"}")
        combatLog.add("士气判定: 骰子${moraleRoll.finalValue} + ${attackStats.moraleAttack} >= $moraleThreshold → ${if (moraleHit) "成功" else "失败"}")

        // 应用战斗结果
        var result = CombatResult(
            attacker = attacker,
            defender = defender,
            combatType = combatType,
            hitResult = hitResult,
            combatLog = combatLog
        )

        // 阵型打击成功
        if (formationHit) {
            result = result.copy(defenderFormationBroken = true)
            combatLog.add("→ ${defender.name} 阵型被打乱！")
        }

        // 减员打击成功
        if (casualtyHit) {
            result = result.copy(defenderCasualty = true)
            combatLog.add("→ ${defender.name} 遭受减员！")
        }

        // 士气打击成功
        if (moraleHit) {
            result = result.copy(defenderMoraleDrop = true)
            combatLog.add("→ ${defender.name} 士气下降！")
        }

        return result
    }

    /**
     * 获取士气修正
     */
    private fun getMoraleModifier(attacker: GameUnit, defender: GameUnit): Int {
        val attackerMorale = attacker.moraleLevel.getMoraleValue()
        val defenderMorale = defender.moraleLevel.getMoraleValue()
        return attackerMorale - defenderMorale
    }

    /**
     * 应用战斗结果到单位
     */
    fun applyCombatResult(result: CombatResult): Pair<GameUnit, GameUnit> {
        var attacker = result.attacker
        var defender = result.defender

        // 处理防守方
        var newDiscipline = defender.disciplineLevel
        var newMorale = defender.moraleLevel
        var newPersonnel = defender.personnelLevel

        // 阵型打击成功 -> 纪律降级
        if (result.defenderFormationBroken) {
            newDiscipline = when (defender.disciplineLevel) {
                DisciplineLevel.STRICT -> DisciplineLevel.NORMAL
                DisciplineLevel.NORMAL -> DisciplineLevel.LOOSE
                DisciplineLevel.LOOSE -> DisciplineLevel.BROKEN
                DisciplineLevel.BROKEN -> DisciplineLevel.BROKEN
            }
        }

        // 士气打击成功 -> 士气降级
        if (result.defenderMoraleDrop) {
            newMorale = when (defender.moraleLevel) {
                MoraleLevel.HIGH -> MoraleLevel.NORMAL
                MoraleLevel.NORMAL -> MoraleLevel.LOW
                MoraleLevel.LOW -> MoraleLevel.BROKEN
                MoraleLevel.BROKEN -> MoraleLevel.BROKEN
            }
        }

        // 减员打击成功 -> 兵力降级
        if (result.defenderCasualty) {
            newPersonnel = when (defender.personnelLevel) {
                PersonnelLevel.HIGH -> PersonnelLevel.MEDIUM
                PersonnelLevel.MEDIUM -> PersonnelLevel.LOW
                PersonnelLevel.LOW -> PersonnelLevel.LOW // 已经最低了
            }
        }

        // 检查溃散条件：士气惊慌失措 + 非野战严整
        val shouldRout = newMorale == MoraleLevel.BROKEN && defender.state != UnitState.BATTLE_FORMATION_STRICT
        if (shouldRout) {
            newPersonnel = PersonnelLevel.LOW
            newDiscipline = DisciplineLevel.BROKEN
            defender = defender.copy(
                personnelLevel = newPersonnel,
                moraleLevel = newMorale,
                disciplineLevel = newDiscipline,
                state = UnitState.BATTLE_FORMATION_LOOSE
            )
            result.combatLog.forEach { /* 已在上面处理 */ }
        } else {
            defender = defender.copy(
                personnelLevel = newPersonnel,
                moraleLevel = newMorale,
                disciplineLevel = newDiscipline
            )
        }

        // 肉搏特殊处理：每回合额外降低1级士气
        if (result.combatType == CombatType.MELEE) {
            newMorale = when (defender.moraleLevel) {
                MoraleLevel.HIGH -> MoraleLevel.NORMAL
                MoraleLevel.NORMAL -> MoraleLevel.LOW
                MoraleLevel.LOW -> MoraleLevel.BROKEN
                MoraleLevel.BROKEN -> MoraleLevel.BROKEN
            }
            defender = defender.copy(moraleLevel = newMorale)
        }

        return attacker to defender
    }
}

/**
 * ZOC系统
 */
object ZOCSystem {

    /**
     * 获取单位ZOC范围（格数）
     */
    fun getZOCRange(unit: GameUnit): Int {
        return when (unit.state) {
            UnitState.BATTLE_FORMATION_STRICT -> 7    // 本格 + 相邻6格
            UnitState.DEFENSE -> 7                    // 本格 + 相邻6格
            UnitState.RAID -> 3                        // 本格 + 相邻2格（仅补给阻断）
            UnitState.BATTLE_FORMATION_LOOSE -> 1    // 仅本格
            else -> 1                                  // 仅本格
        }
    }

    /**
     * 获取ZOC范围内的所有坐标
     */
    fun getZOC范围(unit: GameUnit): Set<HexCoord> {
        val range = getZOCRange(unit)
        val coords = mutableSetOf<HexCoord>()

        if (range == 1) {
            coords.add(unit.coord)
        } else {
            // 本格
            coords.add(unit.coord)
            // 邻居
            unit.coord.getNeighbors().forEach { neighbor ->
                coords.add(neighbor)
                // 如果范围>=7，还要加邻居的邻居
                if (range >= 7) {
                    neighbor.getNeighbors().forEach { neighbor2 ->
                        coords.add(neighbor2)
                    }
                }
            }
        }

        return coords
    }

    /**
     * 检查某坐标是否在敌方ZOC内
     */
    fun isInEnemyZOC(coord: HexCoord, unit: GameUnit, allUnits: List<GameUnit>): Boolean {
        return allUnits.any { other ->
            other.faction != unit.faction && // 敌方单位
            other.coord.distanceTo(coord) <= getZOCRange(other) - 1 && // 在ZOC范围内
            coord != other.coord // 不是敌方单位本身
        }
    }

    /**
     * 获取某单位的所有ZOC坐标
     */
    fun getZOCForUnit(unit: GameUnit, allUnits: List<GameUnit>): Set<HexCoord> {
        return allUnits
            .filter { it.faction != unit.faction }
            .flatMap { getZOC范围(it) }
            .toSet()
    }

    /**
     * 检查是否可以在某坐标切换状态
     */
    fun canChangeStateAt(unit: GameUnit, allUnits: List<GameUnit>): Boolean {
        return !isInEnemyZOC(unit.coord, unit, allUnits)
    }

    /**
     * 检查移动是否在ZOC中停止
     */
    fun shouldStopInZOC(unit: GameUnit, targetCoord: HexCoord, allUnits: List<GameUnit>): Boolean {
        // 如果目标是敌方单位所在格，不停止（进入战斗）
        if (allUnits.any { it.faction != unit.faction && it.coord == targetCoord }) {
            return false
        }
        return isInEnemyZOC(targetCoord, unit, allUnits)
    }
}

// 扩展函数用于显示名称
fun PersonnelLevel.displayName(): String = when (this) {
    PersonnelLevel.HIGH -> "满编"
    PersonnelLevel.MEDIUM -> "缺员"
    PersonnelLevel.LOW -> "残部"
}

fun MoraleLevel.displayName(): String = when (this) {
    MoraleLevel.HIGH -> "高昂"
    MoraleLevel.NORMAL -> "正常"
    MoraleLevel.LOW -> "低落"
    MoraleLevel.BROKEN -> "崩溃"
}

fun DisciplineLevel.displayName(): String = when (this) {
    DisciplineLevel.STRICT -> "严整"
    DisciplineLevel.NORMAL -> "正常"
    DisciplineLevel.LOOSE -> "涣散"
    DisciplineLevel.BROKEN -> "崩溃"
}
