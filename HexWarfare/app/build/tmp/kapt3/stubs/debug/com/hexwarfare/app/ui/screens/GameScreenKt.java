package com.hexwarfare.app.ui.screens;

import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.lifecycle.ViewModel;
import com.hexwarfare.app.domain.model.CarriedEquipment;
import com.hexwarfare.app.domain.model.CombatResult;
import com.hexwarfare.app.domain.model.CombatType;
import com.hexwarfare.app.domain.model.CombatEngine;
import com.hexwarfare.app.domain.model.CommandRecord;
import com.hexwarfare.app.domain.model.CommandSystem;
import com.hexwarfare.app.domain.model.CommandType;
import com.hexwarfare.app.domain.model.Commander;
import com.hexwarfare.app.domain.model.DisciplineLevel;
import com.hexwarfare.app.domain.model.Faction;
import com.hexwarfare.app.domain.model.GameMap;
import com.hexwarfare.app.domain.model.GameUnit;
import com.hexwarfare.app.domain.model.HexCoord;
import com.hexwarfare.app.domain.model.HexTile;
import com.hexwarfare.app.domain.model.MoraleLevel;
import com.hexwarfare.app.domain.model.PathFinder;
import com.hexwarfare.app.domain.model.PersonnelLevel;
import com.hexwarfare.app.domain.model.SettlementResult;
import com.hexwarfare.app.domain.model.StaminaLevel;
import com.hexwarfare.app.domain.model.TerrainType;
import com.hexwarfare.app.domain.model.Turn;
import com.hexwarfare.app.domain.model.TurnPhase;
import com.hexwarfare.app.domain.model.UnitSafety;
import com.hexwarfare.app.domain.model.UnitState;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000l\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u001aF\u0010\u0000\u001a\u00020\u00012\b\u0010\u0002\u001a\u0004\u0018\u00010\u00032\b\u0010\u0004\u001a\u0004\u0018\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u00072\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00010\t2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\tH\u0003\u001a\u0012\u0010\u000b\u001a\u00020\u00012\b\b\u0002\u0010\f\u001a\u00020\rH\u0007\u001a.\u0010\u000e\u001a\u00020\u00012\b\u0010\u000f\u001a\u0004\u0018\u00010\u00102\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u00072\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00010\tH\u0003\u001a \u0010\u0012\u001a\u00020\u00012\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u0016\u001a\u00020\u0017H\u0003\u001a4\u0010\u0018\u001a\u00020\u00012\b\u0010\u0019\u001a\u0004\u0018\u00010\u001a2\u0012\u0010\u001b\u001a\u000e\u0012\u0004\u0012\u00020\u001a\u0012\u0004\u0012\u00020\u00010\u001c2\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00010\tH\u0003\u001a.\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020\u001f2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00010\tH\u0003\u001a\u0010\u0010$\u001a\u00020\u00012\u0006\u0010%\u001a\u00020\u0003H\u0003\u001a8\u0010&\u001a\u00020\u00012\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u00072\u0012\u0010\'\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u001c2\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00010\tH\u0003\u001a\u0010\u0010(\u001a\u00020\u00142\u0006\u0010)\u001a\u00020\u001aH\u0002\u001a\u0010\u0010*\u001a\u00020\u00172\u0006\u0010)\u001a\u00020\u001aH\u0002\u001a\u0015\u0010+\u001a\u00020,2\u0006\u0010-\u001a\u00020.H\u0002\u00a2\u0006\u0002\u0010/\u00a8\u00060"}, d2 = {"BottomInfoPanel", "", "selectedUnit", "Lcom/hexwarfare/app/domain/model/GameUnit;", "selectedTile", "Lcom/hexwarfare/app/domain/model/HexTile;", "units", "", "onStateChange", "Lkotlin/Function0;", "onClearSelection", "GameScreen", "viewModel", "Lcom/hexwarfare/app/ui/screens/GameViewModel;", "SettlementDialog", "result", "Lcom/hexwarfare/app/domain/model/SettlementResult;", "onDismiss", "StatChip", "label", "", "value", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "StateChangeDialog", "currentState", "Lcom/hexwarfare/app/domain/model/UnitState;", "onStateSelected", "Lkotlin/Function1;", "TurnInfoBar", "turnNumber", "", "phase", "Lcom/hexwarfare/app/domain/model/TurnPhase;", "commandQuota", "onNextPhase", "UnitMiniCard", "unit", "UnitSelectionDialog", "onUnitSelected", "getStateDescription", "state", "getStateIcon", "getTerrainColor", "Landroidx/compose/ui/graphics/Color;", "terrain", "Lcom/hexwarfare/app/domain/model/TerrainType;", "(Lcom/hexwarfare/app/domain/model/TerrainType;)J", "app_debug"})
public final class GameScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void GameScreen(@org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.ui.screens.GameViewModel viewModel) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void TurnInfoBar(int turnNumber, com.hexwarfare.app.domain.model.TurnPhase phase, int commandQuota, kotlin.jvm.functions.Function0<kotlin.Unit> onNextPhase) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void BottomInfoPanel(com.hexwarfare.app.domain.model.GameUnit selectedUnit, com.hexwarfare.app.domain.model.HexTile selectedTile, java.util.List<com.hexwarfare.app.domain.model.GameUnit> units, kotlin.jvm.functions.Function0<kotlin.Unit> onStateChange, kotlin.jvm.functions.Function0<kotlin.Unit> onClearSelection) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void StatChip(java.lang.String label, java.lang.String value, androidx.compose.ui.graphics.vector.ImageVector icon) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void UnitMiniCard(com.hexwarfare.app.domain.model.GameUnit unit) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void StateChangeDialog(com.hexwarfare.app.domain.model.UnitState currentState, kotlin.jvm.functions.Function1<? super com.hexwarfare.app.domain.model.UnitState, kotlin.Unit> onStateSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SettlementDialog(com.hexwarfare.app.domain.model.SettlementResult result, java.util.List<com.hexwarfare.app.domain.model.GameUnit> units, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    /**
     * 单位选择对话框（多个单位在同一格子时）
     */
    @androidx.compose.runtime.Composable()
    private static final void UnitSelectionDialog(java.util.List<com.hexwarfare.app.domain.model.GameUnit> units, kotlin.jvm.functions.Function1<? super com.hexwarfare.app.domain.model.GameUnit, kotlin.Unit> onUnitSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    private static final androidx.compose.ui.graphics.vector.ImageVector getStateIcon(com.hexwarfare.app.domain.model.UnitState state) {
        return null;
    }
    
    private static final java.lang.String getStateDescription(com.hexwarfare.app.domain.model.UnitState state) {
        return null;
    }
    
    /**
     * 获取地形颜色
     */
    private static final long getTerrainColor(com.hexwarfare.app.domain.model.TerrainType terrain) {
        return 0L;
    }
}