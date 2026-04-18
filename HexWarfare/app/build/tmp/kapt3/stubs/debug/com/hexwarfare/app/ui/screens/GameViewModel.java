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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rJ\u0006\u0010\u000e\u001a\u00020\u000bJ\b\u0010\u000f\u001a\u00020\u0010H\u0002J\u000e\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00130\u0012H\u0002J\u0006\u0010\u0014\u001a\u00020\u000bJ\u0006\u0010\u0015\u001a\u00020\u000bJ\u0006\u0010\u0016\u001a\u00020\u000bJ\u0010\u0010\u0017\u001a\u00020\u000b2\u0006\u0010\u0018\u001a\u00020\u0019H\u0002J\b\u0010\u001a\u001a\u00020\u000bH\u0002J\u0006\u0010\u001b\u001a\u00020\u000bJ\u000e\u0010\u001c\u001a\u00020\u000b2\u0006\u0010\u001d\u001a\u00020\u0019J\b\u0010\u001e\u001a\u00020\u001fH\u0002J\u000e\u0010 \u001a\u00020\u000b2\u0006\u0010\u001d\u001a\u00020\u0019J\u0010\u0010!\u001a\u00020\u000b2\u0006\u0010\"\u001a\u00020\u0013H\u0002J\u000e\u0010#\u001a\u00020\u000b2\u0006\u0010\"\u001a\u00020\u0013J\u0006\u0010$\u001a\u00020\u000bR\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\t\u00a8\u0006%"}, d2 = {"Lcom/hexwarfare/app/ui/screens/GameViewModel;", "Landroidx/lifecycle/ViewModel;", "()V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/hexwarfare/app/ui/screens/GameUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "changeUnitState", "", "newState", "Lcom/hexwarfare/app/domain/model/UnitState;", "clearSelection", "createSampleMap", "Lcom/hexwarfare/app/domain/model/GameMap;", "createSampleUnits", "", "Lcom/hexwarfare/app/domain/model/GameUnit;", "dismissSettlementDialog", "dismissStateChangeDialog", "dismissUnitSelectionDialog", "executeMove", "targetCoord", "Lcom/hexwarfare/app/domain/model/HexCoord;", "loadMap", "nextPhase", "previewMove", "coord", "processSettlement", "Lcom/hexwarfare/app/domain/model/SettlementResult;", "selectTile", "selectUnit", "unit", "selectUnitFromTile", "showStateChangeDialog", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class GameViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.hexwarfare.app.ui.screens.GameUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.hexwarfare.app.ui.screens.GameUiState> uiState = null;
    
    @javax.inject.Inject()
    public GameViewModel() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.hexwarfare.app.ui.screens.GameUiState> getUiState() {
        return null;
    }
    
    private final void loadMap() {
    }
    
    /**
     * 选择地块
     */
    public final void selectTile(@org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord coord) {
    }
    
    /**
     * 选择指定单位（从对话框选择）
     */
    public final void selectUnitFromTile(@org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.GameUnit unit) {
    }
    
    /**
     * 关闭单位选择对话框
     */
    public final void dismissUnitSelectionDialog() {
    }
    
    /**
     * 选中单位
     */
    private final void selectUnit(com.hexwarfare.app.domain.model.GameUnit unit) {
    }
    
    /**
     * 清除选择
     */
    public final void clearSelection() {
    }
    
    /**
     * 预览移动路径
     */
    public final void previewMove(@org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord coord) {
    }
    
    /**
     * 执行移动
     */
    private final void executeMove(com.hexwarfare.app.domain.model.HexCoord targetCoord) {
    }
    
    /**
     * 切换单位状态
     */
    public final void showStateChangeDialog() {
    }
    
    public final void dismissStateChangeDialog() {
    }
    
    public final void changeUnitState(@org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.UnitState newState) {
    }
    
    /**
     * 进入下一阶段
     */
    public final void nextPhase() {
    }
    
    /**
     * 处理回合结算
     */
    private final com.hexwarfare.app.domain.model.SettlementResult processSettlement() {
        return null;
    }
    
    /**
     * 关闭结算对话框
     */
    public final void dismissSettlementDialog() {
    }
    
    /**
     * 创建示例地图 (9x9)
     */
    private final com.hexwarfare.app.domain.model.GameMap createSampleMap() {
        return null;
    }
    
    /**
     * 创建示例单位
     */
    private final java.util.List<com.hexwarfare.app.domain.model.GameUnit> createSampleUnits() {
        return null;
    }
}