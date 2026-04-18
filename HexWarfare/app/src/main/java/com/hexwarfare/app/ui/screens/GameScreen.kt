package com.hexwarfare.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.hexwarfare.app.domain.model.CarriedEquipment
import com.hexwarfare.app.domain.model.CommandRecord
import com.hexwarfare.app.domain.model.CommandSystem
import com.hexwarfare.app.domain.model.CommandType
import com.hexwarfare.app.domain.model.Commander
import com.hexwarfare.app.domain.model.DisciplineLevel
import com.hexwarfare.app.domain.model.Faction
import com.hexwarfare.app.domain.model.GameMap
import com.hexwarfare.app.domain.model.GameUnit
import com.hexwarfare.app.domain.model.HexCoord
import com.hexwarfare.app.domain.model.HexTile
import com.hexwarfare.app.domain.model.MoraleLevel
import com.hexwarfare.app.domain.model.PathFinder
import com.hexwarfare.app.domain.model.PersonnelLevel
import com.hexwarfare.app.domain.model.SettlementResult
import com.hexwarfare.app.domain.model.StaminaLevel
import com.hexwarfare.app.domain.model.TerrainType
import com.hexwarfare.app.domain.model.Turn
import com.hexwarfare.app.domain.model.TurnPhase
import com.hexwarfare.app.domain.model.UnitSafety
import com.hexwarfare.app.domain.model.UnitState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * 游戏UI状态
 */
data class GameUiState(
    val map: GameMap? = null,
    val selectedUnit: GameUnit? = null,
    val selectedTile: HexTile? = null,
    val units: List<GameUnit> = emptyList(),
    val currentTurn: Turn = Turn(),
    val reachableTiles: Set<HexCoord> = emptySet(),
    val moveCosts: Map<HexCoord, Int> = emptyMap(),
    val selectedPath: List<HexCoord> = emptyList(),
    val commandQuotaLeft: Int = CommandSystem.BASE_COMMAND_QUOTA,
    val commandHistory: List<CommandRecord> = emptyList(),
    val message: String = "",
    val showStateChangeDialog: Boolean = false,
    val showSupplyDialog: Boolean = false,
    val showSettlementDialog: Boolean = false,
    val settlementResult: SettlementResult? = null,
    val showUnitSelectionDialog: Boolean = false,
    val unitsOnSelectedTile: List<GameUnit> = emptyList()
)

@HiltViewModel
class GameViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        loadMap()
    }

    private fun loadMap() {
        val map = createSampleMap()
        val units = createSampleUnits()
        _uiState.value = GameUiState(
            map = map,
            units = units,
            message = "回合1 - 将军命令阶段"
        )
    }

    /**
     * 选择地块
     */
    fun selectTile(coord: HexCoord) {
        val tile = _uiState.value.map?.tiles?.get(coord) ?: return
        val unitsOnTile = _uiState.value.units.filter { it.coord == coord }

        // 获取该格子上的所有玩家单位
        val playerUnitsOnTile = unitsOnTile.filter { it.faction == Faction.PLAYER }

        if (playerUnitsOnTile.isNotEmpty()) {
            // 有玩家单位在格子上
            if (playerUnitsOnTile.size > 1) {
                // 多个玩家单位，弹出选择对话框
                _uiState.update {
                    it.copy(
                        selectedTile = tile,
                        showUnitSelectionDialog = true,
                        unitsOnSelectedTile = playerUnitsOnTile
                    )
                }
            } else {
                // 单个玩家单位，直接选中
                selectUnit(playerUnitsOnTile.first())
            }
        } else if (_uiState.value.selectedUnit != null) {
            // 有已选中的单位，尝试移动
            if (coord in _uiState.value.reachableTiles) {
                executeMove(coord)
            } else {
                // 清除选中状态，显示地块信息
                _uiState.update {
                    it.copy(
                        selectedTile = tile,
                        selectedUnit = null,
                        reachableTiles = emptySet(),
                        moveCosts = emptyMap()
                    )
                }
            }
        } else {
            // 没有选中单位，显示地块信息
            _uiState.update {
                it.copy(
                    selectedTile = tile,
                    selectedUnit = null,
                    reachableTiles = emptySet(),
                    moveCosts = emptyMap()
                )
            }
        }
    }

    /**
     * 选择指定单位（从对话框选择）
     */
    fun selectUnitFromTile(unit: GameUnit) {
        _uiState.update {
            it.copy(
                showUnitSelectionDialog = false,
                unitsOnSelectedTile = emptyList()
            )
        }
        selectUnit(unit)
    }

    /**
     * 关闭单位选择对话框
     */
    fun dismissUnitSelectionDialog() {
        _uiState.update {
            it.copy(
                showUnitSelectionDialog = false,
                unitsOnSelectedTile = emptyList()
            )
        }
    }

    /**
     * 选中单位
     */
    private fun selectUnit(unit: GameUnit) {
        if (_uiState.value.currentTurn.phase == TurnPhase.Supply ||
            _uiState.value.currentTurn.phase == TurnPhase.Settlement) {
            // 补给和结算阶段不能操作单位
            return
        }

        val map = _uiState.value.map ?: return
        val reachable = PathFinder.findReachableTiles(
            start = unit.coord,
            gameMap = map,
            movePower = unit.currentMovePower,
            unit = unit
        )

        _uiState.update {
            it.copy(
                selectedUnit = unit,
                selectedTile = map.tiles[unit.coord],
                reachableTiles = reachable.reachableTiles,
                moveCosts = reachable.moveCosts,
                selectedPath = emptyList()
            )
        }
    }

    /**
     * 清除选择
     */
    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedUnit = null,
                reachableTiles = emptySet(),
                moveCosts = emptyMap(),
                selectedPath = emptyList()
            )
        }
    }

    /**
     * 预览移动路径
     */
    fun previewMove(coord: HexCoord) {
        val unit = _uiState.value.selectedUnit ?: return
        val map = _uiState.value.map ?: return

        if (coord !in _uiState.value.reachableTiles) return

        val pathResult = PathFinder.findPath(
            start = unit.coord,
            goal = coord,
            gameMap = map,
            movePower = unit.currentMovePower,
            unit = unit
        )

        pathResult?.let { result ->
            _uiState.update { it.copy(selectedPath = result.path) }
        }
    }

    /**
     * 执行移动
     */
    private fun executeMove(targetCoord: HexCoord) {
        val unit = _uiState.value.selectedUnit ?: return
        val map = _uiState.value.map ?: return

        val pathResult = PathFinder.findPath(
            start = unit.coord,
            goal = targetCoord,
            gameMap = map,
            movePower = unit.currentMovePower,
            unit = unit
        ) ?: return

        // 检查配额
        val canUse = CommandSystem.canUseCommand(
            quotaLeft = _uiState.value.commandQuotaLeft,
            distance = unit.coord.distanceTo(targetCoord),
            isGeneralCommand = unit.isGeneralUnit
        )

        if (!canUse) {
            _uiState.update { it.copy(message = "命令配额不足或距离超出限制") }
            return
        }

        // 执行移动
        val movedUnit = unit.moveTo(targetCoord)
        val updatedUnits = _uiState.value.units.map {
            if (it.id == unit.id) movedUnit else it
        }

        // 记录命令
        val record = CommandRecord(
            unitId = unit.id,
            commandType = CommandType.MOVE,
            fromCoord = unit.coord,
            toCoord = targetCoord,
            turnNumber = _uiState.value.currentTurn.turnNumber,
            phase = _uiState.value.currentTurn.phase
        )

        _uiState.update {
            it.copy(
                units = updatedUnits,
                selectedUnit = movedUnit,
                commandQuotaLeft = if (unit.isGeneralUnit) it.commandQuotaLeft else it.commandQuotaLeft - 1,
                commandHistory = it.commandHistory + record,
                reachableTiles = emptySet(),
                moveCosts = emptyMap(),
                selectedPath = emptyList(),
                message = "${unit.name} 移动到 (${targetCoord.q}, ${targetCoord.r})，剩余移动力: ${movedUnit.currentMovePower}"
            )
        }
    }

    /**
     * 切换单位状态
     */
    fun showStateChangeDialog() {
        _uiState.update { it.copy(showStateChangeDialog = true) }
    }

    fun dismissStateChangeDialog() {
        _uiState.update { it.copy(showStateChangeDialog = false) }
    }

    fun changeUnitState(newState: UnitState) {
        val unit = _uiState.value.selectedUnit ?: return

        val canChange = CommandSystem.canChangeState(
            currentState = unit.state,
            targetState = newState,
            moraleLevel = unit.moraleLevel,
            consecutiveDefenseTurns = unit.consecutiveDefenseTurns,
            isUnderZOC = unit.isUnderZOC(_uiState.value.units),
            isVulnerableState = CommandSystem.isVulnerableState(newState),
            equipmentCount = unit.getEquipmentCount()
        )

        if (!canChange.allowed) {
            _uiState.update { it.copy(message = canChange.reason) }
            dismissStateChangeDialog()
            return
        }

        val updatedUnit = unit.changeState(newState)
        val updatedUnits = _uiState.value.units.map {
            if (it.id == unit.id) updatedUnit else it
        }

        _uiState.update {
            it.copy(
                units = updatedUnits,
                selectedUnit = updatedUnit,
                showStateChangeDialog = false,
                message = "${unit.name} 切换为 ${newState.displayName}"
            )
        }
    }

    /**
     * 进入下一阶段
     */
    fun nextPhase() {
        val current = _uiState.value.currentTurn
        val nextPhase = current.phase.next()

        // 如果是新的将军命令阶段，增加回合数
        val newTurnNumber = if (TurnPhase.isNewTurn(nextPhase)) {
            current.turnNumber + 1
        } else {
            current.turnNumber
        }

        // 重置单位移动力
        val resetUnits = if (TurnPhase.isNewTurn(nextPhase)) {
            _uiState.value.units.map { it.resetMovePower() }
        } else {
            _uiState.value.units
        }

        // 补给阶段处理
        if (current.phase == TurnPhase.Supply) {
            _uiState.update {
                it.copy(
                    showSupplyDialog = false,
                    message = "补给完成"
                )
            }
        }

        // 回合结算处理
        if (current.phase == TurnPhase.Settlement) {
            val settlementResult = processSettlement()
            _uiState.update {
                it.copy(
                    settlementResult = settlementResult,
                    showSettlementDialog = true,
                    message = "回合结算中..."
                )
            }
        }

        // 重置命令配额（新阶段开始时）
        val newQuota = if (nextPhase == TurnPhase.GeneralCommand) {
            CommandSystem.BASE_COMMAND_QUOTA
        } else {
            _uiState.value.commandQuotaLeft
        }

        _uiState.update {
            it.copy(
                currentTurn = Turn(
                    turnNumber = newTurnNumber,
                    phase = nextPhase,
                    commander = when (nextPhase) {
                        TurnPhase.GeneralCommand -> Commander.PLAYER_GENERAL
                        TurnPhase.ChiefOfStaffCommand -> Commander.PLAYER_CHIEF
                        TurnPhase.Supply -> Commander.PLAYER_GENERAL
                        TurnPhase.Settlement -> Commander.PLAYER_GENERAL
                    }
                ),
                commandQuotaLeft = newQuota,
                units = resetUnits,
                message = "回合${newTurnNumber} - ${nextPhase.displayName}"
            )
        }
    }

    /**
     * 处理回合结算
     */
    private fun processSettlement(): SettlementResult {
        val units = _uiState.value.units
        val moraleChanged = mutableMapOf<String, MoraleLevel>()
        val staminaChanged = mutableMapOf<String, StaminaLevel>()
        val casualties = mutableMapOf<String, Int>()
        val zocEffects = mutableMapOf<String, Boolean>()

        units.forEach { unit ->
            // 结算体力消耗
            val newStamina = when (unit.state) {
                UnitState.MARCH, UnitState.RAID -> unit.staminaLevel.consume()
                UnitState.REST -> unit.staminaLevel.recover()
                else -> unit.staminaLevel
            }
            staminaChanged[unit.id] = newStamina

            // 士气结算
            val newMorale = when {
                unit.staminaLevel == StaminaLevel.EXHAUSTED ->
                    MoraleLevel.entries[maxOf(0, unit.moraleLevel.ordinal - 1)]
                unit.state == UnitState.BATTLE_FORMATION_STRICT ->
                    MoraleLevel.entries[minOf(MoraleLevel.entries.size - 1, unit.moraleLevel.ordinal + 1)]
                else -> unit.moraleLevel
            }
            moraleChanged[unit.id] = newMorale

            // ZOC效果
            zocEffects[unit.id] = unit.isUnderZOC(units)
        }

        return SettlementResult(moraleChanged, staminaChanged, casualties, zocEffects)
    }

    /**
     * 关闭结算对话框
     */
    fun dismissSettlementDialog() {
        // 应用结算结果到单位
        val result = _uiState.value.settlementResult ?: return
        val updatedUnits = _uiState.value.units.map { unit ->
            val newStamina = result.staminaChanged[unit.id] ?: unit.staminaLevel
            val newMorale = result.moraleChanged[unit.id] ?: unit.moraleLevel
            unit.copy(staminaLevel = newStamina, moraleLevel = newMorale)
        }

        _uiState.update {
            it.copy(
                units = updatedUnits,
                showSettlementDialog = false,
                settlementResult = null
            )
        }
    }

    /**
     * 创建示例地图 (9x9)
     */
    private fun createSampleMap(): GameMap {
        val tiles = mutableMapOf<HexCoord, HexTile>()

        // 使用Cube坐标创建六角形地图
        for (q in -4..4) {
            for (r in -4..4) {
                if (kotlin.math.abs(q + r) <= 4) {
                    val coord = HexCoord(q, r)
                    val terrain = when {
                        kotlin.math.abs(q) <= 1 && kotlin.math.abs(r) <= 1 -> TerrainType.PLAINS
                        kotlin.math.abs(q) == 2 && kotlin.math.abs(r) <= 2 -> TerrainType.MOUNTAIN
                        kotlin.math.abs(r) == 2 && kotlin.math.abs(q) <= 2 -> TerrainType.RIVER
                        kotlin.math.abs(q + r) == 3 -> TerrainType.BUILDING
                        else -> TerrainType.PLAINS
                    }
                    tiles[coord] = HexTile(coord, terrain)
                }
            }
        }

        return GameMap(
            width = 9,
            height = 9,
            tiles = tiles,
            capitalPositions = mapOf(
                Faction.PLAYER to HexCoord(0, 0),
                Faction.ENEMY to HexCoord(3, -3)
            )
        )
    }

    /**
     * 创建示例单位
     */
    private fun createSampleUnits(): List<GameUnit> {
        return listOf(
            // 玩家单位
            GameUnit(
                id = "general",
                name = "将军",
                coord = HexCoord(0, 0),
                faction = Faction.PLAYER,
                personnelLevel = PersonnelLevel.HIGH,
                staminaLevel = StaminaLevel.FRESH,
                moraleLevel = MoraleLevel.HIGH,
                disciplineLevel = DisciplineLevel.STRICT,
                state = UnitState.BATTLE_FORMATION_STRICT,
                safety = UnitSafety.SAFE,
                baseMovePower = 4,
                currentMovePower = 4,
                isGeneralUnit = true
            ),
            GameUnit(
                id = "chief",
                name = "参谋长",
                coord = HexCoord(1, -1),
                faction = Faction.PLAYER,
                personnelLevel = PersonnelLevel.MEDIUM,
                staminaLevel = StaminaLevel.FRESH,
                moraleLevel = MoraleLevel.HIGH,
                disciplineLevel = DisciplineLevel.STRICT,
                state = UnitState.BATTLE_FORMATION_STRICT,
                safety = UnitSafety.SAFE,
                baseMovePower = 5,
                currentMovePower = 5,
                isGeneralUnit = true
            ),
            GameUnit(
                id = "unit1",
                name = "步兵一团",
                coord = HexCoord(-1, 0),
                faction = Faction.PLAYER,
                personnelLevel = PersonnelLevel.MEDIUM,
                staminaLevel = StaminaLevel.FRESH,
                moraleLevel = MoraleLevel.HIGH,
                disciplineLevel = DisciplineLevel.STRICT,
                state = UnitState.BATTLE_FORMATION_STRICT,
                safety = UnitSafety.SAFE,
                baseMovePower = 5,
                currentMovePower = 5
            ),
            GameUnit(
                id = "unit2",
                name = "骑兵团",
                coord = HexCoord(0, -1),
                faction = Faction.PLAYER,
                personnelLevel = PersonnelLevel.LOW,
                staminaLevel = StaminaLevel.FRESH,
                moraleLevel = MoraleLevel.NORMAL,
                disciplineLevel = DisciplineLevel.NORMAL,
                state = UnitState.MARCH,
                safety = UnitSafety.SAFE,
                baseMovePower = 7,
                currentMovePower = 7
            ),
            GameUnit(
                id = "unit3",
                name = "弓兵营",
                coord = HexCoord(-1, 1),
                faction = Faction.PLAYER,
                personnelLevel = PersonnelLevel.MEDIUM,
                staminaLevel = StaminaLevel.NORMAL,
                moraleLevel = MoraleLevel.LOW,
                disciplineLevel = DisciplineLevel.LOOSE,
                state = UnitState.BATTLE_FORMATION_LOOSE,
                safety = UnitSafety.DANGGER,
                baseMovePower = 4,
                currentMovePower = 4
            ),
            // 敌方单位
            GameUnit(
                id = "enemy1",
                name = "敌军步兵",
                coord = HexCoord(2, -1),
                faction = Faction.ENEMY,
                personnelLevel = PersonnelLevel.MEDIUM,
                staminaLevel = StaminaLevel.FRESH,
                moraleLevel = MoraleLevel.HIGH,
                disciplineLevel = DisciplineLevel.STRICT,
                state = UnitState.BATTLE_FORMATION_STRICT,
                safety = UnitSafety.SAFE,
                baseMovePower = 5,
                currentMovePower = 5
            ),
            GameUnit(
                id = "enemy2",
                name = "敌军骑兵",
                coord = HexCoord(3, -2),
                faction = Faction.ENEMY,
                personnelLevel = PersonnelLevel.LOW,
                staminaLevel = StaminaLevel.TIRED,
                moraleLevel = MoraleLevel.NORMAL,
                disciplineLevel = DisciplineLevel.NORMAL,
                state = UnitState.MARCH,
                safety = UnitSafety.SAFE,
                baseMovePower = 7,
                currentMovePower = 7
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部信息栏 - 回合和阶段信息
        TurnInfoBar(
            turnNumber = uiState.currentTurn.turnNumber,
            phase = uiState.currentTurn.phase,
            commandQuota = uiState.commandQuotaLeft,
            onNextPhase = { viewModel.nextPhase() }
        )

        // 地图区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            uiState.map?.let { map ->
                com.hexwarfare.app.ui.components.HexMapView(
                    map = map,
                    units = uiState.units,
                    selectedCoord = uiState.selectedUnit?.coord ?: uiState.selectedTile?.coord,
                    highlightedCoords = uiState.reachableTiles,
                    pathCoords = uiState.selectedPath,
                    moveCosts = uiState.moveCosts,
                    onTileClick = { coord ->
                        viewModel.selectTile(coord)
                    },
                    onTileHover = { coord ->
                        if (coord in uiState.reachableTiles) {
                            viewModel.previewMove(coord)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 消息提示
            if (uiState.message.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = uiState.message,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // 底部信息面板
        BottomInfoPanel(
            selectedUnit = uiState.selectedUnit,
            selectedTile = uiState.selectedTile,
            units = uiState.units,
            onStateChange = { viewModel.showStateChangeDialog() },
            onClearSelection = { viewModel.clearSelection() }
        )
    }

    // 状态切换对话框
    if (uiState.showStateChangeDialog) {
        StateChangeDialog(
            currentState = uiState.selectedUnit?.state,
            onStateSelected = { viewModel.changeUnitState(it) },
            onDismiss = { viewModel.dismissStateChangeDialog() }
        )
    }

    // 单位选择对话框（多个单位在同一格子时）
    if (uiState.showUnitSelectionDialog) {
        UnitSelectionDialog(
            units = uiState.unitsOnSelectedTile,
            onUnitSelected = { viewModel.selectUnitFromTile(it) },
            onDismiss = { viewModel.dismissUnitSelectionDialog() }
        )
    }

    // 回合结算对话框
    if (uiState.showSettlementDialog) {
        SettlementDialog(
            result = uiState.settlementResult,
            units = uiState.units,
            onDismiss = { viewModel.dismissSettlementDialog() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TurnInfoBar(
    turnNumber: Int,
    phase: TurnPhase,
    commandQuota: Int,
    onNextPhase: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 回合信息
            Column(modifier = Modifier.padding(end = 12.dp)) {
                Text(
                    text = "回合 $turnNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = phase.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // 配额信息
            Column(modifier = Modifier.padding(end = 12.dp)) {
                Text(
                    text = "命令配额",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$commandQuota",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (commandQuota > 2) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }

            // 下一阶段按钮
            Button(
                onClick = onNextPhase,
                modifier = Modifier.height(40.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("下一阶段", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun BottomInfoPanel(
    selectedUnit: GameUnit?,
    selectedTile: HexTile?,
    units: List<GameUnit>,
    onStateChange: () -> Unit,
    onClearSelection: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (selectedUnit != null) {
            // 单位信息面板 - 窄屏适配，可滚动
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 单位名称和状态
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedUnit.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "坐标: (${selectedUnit.coord.q}, ${selectedUnit.coord.r})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AssistChip(
                        onClick = { },
                        label = { Text(selectedUnit.state.displayName) },
                        leadingIcon = {
                            Icon(
                                imageVector = getStateIcon(selectedUnit.state),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 属性条 - 横向滚动
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 人力
                    StatChip(
                        label = "人力",
                        value = selectedUnit.personnelLevel.name,
                        icon = Icons.Default.Person
                    )
                    // 体力
                    StatChip(
                        label = "体力",
                        value = "${selectedUnit.staminaLevel.name} (${selectedUnit.currentMovePower}/${selectedUnit.getActualMovePower()})",
                        icon = Icons.Default.PlayArrow
                    )
                    // 士气
                    StatChip(
                        label = "士气",
                        value = selectedUnit.moraleLevel.name,
                        icon = Icons.Default.Star
                    )
                    // 纪律
                    StatChip(
                        label = "纪律",
                        value = selectedUnit.disciplineLevel.name,
                        icon = Icons.Default.Refresh
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onStateChange,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("切换状态")
                    }
                    OutlinedButton(
                        onClick = onClearSelection,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("取消选择")
                    }
                }
            }
        } else if (selectedTile != null) {
            // 地块信息面板 - 显示坐标、地形、单位信息
            val unitsOnTile = units.filter { it.coord == selectedTile.coord }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 地块坐标和地形
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "地块信息",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "坐标: (${selectedTile.coord.q}, ${selectedTile.coord.r})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AssistChip(
                        onClick = { },
                        label = { Text(selectedTile.terrain.displayName) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = getTerrainColor(selectedTile.terrain).copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 单位信息
                if (unitsOnTile.isNotEmpty()) {
                    Text(
                        text = "单位 (${unitsOnTile.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(unitsOnTile) { unit ->
                            UnitMiniCard(unit = unit)
                        }
                    }
                } else {
                    Text(
                        text = "无单位",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 关闭按钮
                OutlinedButton(
                    onClick = onClearSelection,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("关闭")
                }
            }
        } else {
            // 单位列表 - 窄屏横向滚动
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "己方单位",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val playerUnits = units.filter { it.faction == Faction.PLAYER }
                    items(playerUnits) { unit ->
                        UnitMiniCard(unit = unit)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    icon: ImageVector
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun UnitMiniCard(unit: GameUnit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .width(100.dp)
            .clickable { }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (unit.faction == Faction.PLAYER) Color.Blue
                        else Color.Red
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit.name.first().toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = unit.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = unit.state.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StateChangeDialog(
    currentState: UnitState?,
    onStateSelected: (UnitState) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("切换状态") },
        text = {
            LazyColumn {
                items(UnitState.entries.filter { it != UnitState.BATTLE_FORMATION_STRICT }) { state ->
                    val isSelected = state == currentState
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onStateSelected(state) },
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getStateIcon(state),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = state.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = getStateDescription(state),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun SettlementDialog(
    result: SettlementResult?,
    units: List<GameUnit>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("回合结算") },
        text = {
            LazyColumn {
                result?.let { res ->
                    items(units.filter { it.faction == Faction.PLAYER }) { unit ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = unit.name,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                res.staminaChanged[unit.id]?.let { stamina ->
                                    Text(
                                        text = "体力: ${unit.staminaLevel.name} → $stamina",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                res.moraleChanged[unit.id]?.let { morale ->
                                    Text(
                                        text = "士气: ${unit.moraleLevel.name} → $morale",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("确认")
            }
        }
    )
}

/**
 * 单位选择对话框（多个单位在同一格子时）
 */
@Composable
private fun UnitSelectionDialog(
    units: List<GameUnit>,
    onUnitSelected: (GameUnit) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择单位") },
        text = {
            LazyColumn {
                items(units) { unit ->
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onUnitSelected(unit) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 单位颜色标识
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = if (unit.faction == Faction.PLAYER)
                                    Color(0xFF2196F3) else Color(0xFFF44336)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = unit.name.first().toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = unit.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${unit.state.displayName} | 体力: ${unit.currentMovePower}/${unit.getActualMovePower()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (unit.isGeneralUnit) {
                                Spacer(modifier = Modifier.weight(1f))
                                AssistChip(
                                    onClick = { },
                                    label = { Text(if (unit.name.contains("将军")) "将军" else "参谋长") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Color(0xFFFFD700).copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun getStateIcon(state: UnitState): ImageVector = when (state) {
    UnitState.BATTLE_FORMATION_STRICT -> Icons.Default.Star
    UnitState.BATTLE_FORMATION_LOOSE -> Icons.Default.Star
    UnitState.MARCH -> Icons.Default.PlayArrow
    UnitState.REST -> Icons.Default.Check
    UnitState.DEFENSE -> Icons.Default.Check
    UnitState.TRANSPORT -> Icons.Default.Refresh
    UnitState.RAID -> Icons.Default.Star
}

private fun getStateDescription(state: UnitState): String = when (state) {
    UnitState.BATTLE_FORMATION_STRICT -> "野战严整，仅将军可下令"
    UnitState.BATTLE_FORMATION_LOOSE -> "野战散乱，秩序略显混乱"
    UnitState.MARCH -> "行军状态，移动力+2"
    UnitState.REST -> "休整状态，体力恢复，装备限制≤1"
    UnitState.DEFENSE -> "守备状态，需要士气高昂，最多3回合"
    UnitState.TRANSPORT -> "运输状态，装备携带量增加"
    UnitState.RAID -> "掠袭状态，移动力+1，可快速突袭"
}

/**
 * 获取地形颜色
 */
private fun getTerrainColor(terrain: TerrainType): Color = when (terrain) {
    TerrainType.PLAINS -> Color(0xFF90EE90)
    TerrainType.MOUNTAIN -> Color(0xFF8B8B83)
    TerrainType.RIVER -> Color(0xFF4169E1)
    TerrainType.BUILDING -> Color(0xFFD2691E)
    TerrainType.IMPASSABLE -> Color(0xFF2F2F2F)
}