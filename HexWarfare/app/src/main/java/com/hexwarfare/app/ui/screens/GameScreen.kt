package com.hexwarfare.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.hexwarfare.app.domain.model.Faction
import com.hexwarfare.app.domain.model.GameMap
import com.hexwarfare.app.domain.model.HexCoord
import com.hexwarfare.app.domain.model.HexTile
import com.hexwarfare.app.domain.model.Unit as GameUnit
import com.hexwarfare.app.ui.components.HexMapView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class GameUiState(
    val map: GameMap? = null,
    val selectedTile: HexTile? = null,
    val units: List<GameUnit> = emptyList()
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
        _uiState.value = GameUiState(map = map, units = units)
    }

    fun selectTile(coord: HexCoord) {
        val tile = _uiState.value.map?.tiles?.get(coord)
        _uiState.value = _uiState.value.copy(selectedTile = tile)
    }

    private fun createSampleMap(): GameMap {
        val tiles = mutableMapOf<HexCoord, HexTile>()
        for (q in -2..2) {
            for (r in -2..2) {
                val coord = HexCoord(q, r)
                val terrain = when {
                    (q == 0 && r == 0) -> com.hexwarfare.app.domain.model.TerrainType.PLAINS
                    kotlin.math.abs(q) == 1 && r == 0 -> com.hexwarfare.app.domain.model.TerrainType.MOUNTAIN
                    q == 0 && kotlin.math.abs(r) == 1 -> com.hexwarfare.app.domain.model.TerrainType.RIVER
                    else -> com.hexwarfare.app.domain.model.TerrainType.PLAINS
                }
                tiles[coord] = HexTile(coord, terrain)
            }
        }
        return GameMap(
            width = 5,
            height = 5,
            tiles = tiles,
            capitalPositions = mapOf(
                Faction.PLAYER to HexCoord(0, 0)
            )
        )
    }

    private fun createSampleUnits(): List<GameUnit> {
        return listOf(
            GameUnit(
                id = "unit1",
                coord = HexCoord(-1, 0),
                faction = Faction.PLAYER,
                personnelLevel = com.hexwarfare.app.domain.model.PersonnelLevel.MEDIUM,
                staminaLevel = com.hexwarfare.app.domain.model.StaminaLevel.FRESH,
                moraleLevel = com.hexwarfare.app.domain.model.MoraleLevel.HIGH,
                disciplineLevel = com.hexwarfare.app.domain.model.DisciplineLevel.STRICT,
                state = com.hexwarfare.app.domain.model.UnitState.BATTLE_FORMATION_STRICT,
                safety = com.hexwarfare.app.domain.model.UnitSafety.SAFE
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
        // Top bar
        TopAppBar(
            title = { Text("游戏") }
        )

        // Map
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            uiState.map?.let { map ->
                HexMapView(
                    map = map,
                    units = uiState.units,
                    selectedCoord = uiState.selectedTile?.coord,
                    onTileClick = { coord ->
                        viewModel.selectTile(coord)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Info panel
        uiState.selectedTile?.let { tile ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "坐标: (${tile.coord.q}, ${tile.coord.r})",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "地形: ${tile.terrain.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
