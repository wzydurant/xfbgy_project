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

/**
 * 游戏UI状态
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b6\b\u0086\b\u0018\u00002\u00020\u0001B\u00e1\u0001\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\u000e\b\u0002\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00050\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\r\u0012\u0014\b\u0002\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00110\u0010\u0012\u000e\b\u0002\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u000e0\t\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0011\u0012\u000e\b\u0002\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\t\u0012\b\b\u0002\u0010\u0016\u001a\u00020\u0017\u0012\b\b\u0002\u0010\u0018\u001a\u00020\u0019\u0012\b\b\u0002\u0010\u001a\u001a\u00020\u0019\u0012\b\b\u0002\u0010\u001b\u001a\u00020\u0019\u0012\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u001d\u0012\b\b\u0002\u0010\u001e\u001a\u00020\u0019\u0012\u000e\b\u0002\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00050\t\u00a2\u0006\u0002\u0010 J\u000b\u0010=\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000f\u0010>\u001a\b\u0012\u0004\u0012\u00020\u00150\tH\u00c6\u0003J\t\u0010?\u001a\u00020\u0017H\u00c6\u0003J\t\u0010@\u001a\u00020\u0019H\u00c6\u0003J\t\u0010A\u001a\u00020\u0019H\u00c6\u0003J\t\u0010B\u001a\u00020\u0019H\u00c6\u0003J\u000b\u0010C\u001a\u0004\u0018\u00010\u001dH\u00c6\u0003J\t\u0010D\u001a\u00020\u0019H\u00c6\u0003J\u000f\u0010E\u001a\b\u0012\u0004\u0012\u00020\u00050\tH\u00c6\u0003J\u000b\u0010F\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010G\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u000f\u0010H\u001a\b\u0012\u0004\u0012\u00020\u00050\tH\u00c6\u0003J\t\u0010I\u001a\u00020\u000bH\u00c6\u0003J\u000f\u0010J\u001a\b\u0012\u0004\u0012\u00020\u000e0\rH\u00c6\u0003J\u0015\u0010K\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00110\u0010H\u00c6\u0003J\u000f\u0010L\u001a\b\u0012\u0004\u0012\u00020\u000e0\tH\u00c6\u0003J\t\u0010M\u001a\u00020\u0011H\u00c6\u0003J\u00e5\u0001\u0010N\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00072\u000e\b\u0002\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00050\t2\b\b\u0002\u0010\n\u001a\u00020\u000b2\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\r2\u0014\b\u0002\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00110\u00102\u000e\b\u0002\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u000e0\t2\b\b\u0002\u0010\u0013\u001a\u00020\u00112\u000e\b\u0002\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\t2\b\b\u0002\u0010\u0016\u001a\u00020\u00172\b\b\u0002\u0010\u0018\u001a\u00020\u00192\b\b\u0002\u0010\u001a\u001a\u00020\u00192\b\b\u0002\u0010\u001b\u001a\u00020\u00192\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u001d2\b\b\u0002\u0010\u001e\u001a\u00020\u00192\u000e\b\u0002\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00050\tH\u00c6\u0001J\u0013\u0010O\u001a\u00020\u00192\b\u0010P\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010Q\u001a\u00020\u0011H\u00d6\u0001J\t\u0010R\u001a\u00020\u0017H\u00d6\u0001R\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\t\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u0011\u0010\u0013\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010&R\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010(R\u0011\u0010\u0016\u001a\u00020\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010*R\u001d\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00110\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010,R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010.R\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u000e0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010\"R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u00101R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u00103R\u0013\u0010\u001c\u001a\u0004\u0018\u00010\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u00105R\u0011\u0010\u001b\u001a\u00020\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u00107R\u0011\u0010\u0018\u001a\u00020\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u00107R\u0011\u0010\u001a\u001a\u00020\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u00107R\u0011\u0010\u001e\u001a\u00020\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u00107R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00050\t\u00a2\u0006\b\n\u0000\u001a\u0004\b;\u0010\"R\u0017\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00050\t\u00a2\u0006\b\n\u0000\u001a\u0004\b<\u0010\"\u00a8\u0006S"}, d2 = {"Lcom/hexwarfare/app/ui/screens/GameUiState;", "", "map", "Lcom/hexwarfare/app/domain/model/GameMap;", "selectedUnit", "Lcom/hexwarfare/app/domain/model/GameUnit;", "selectedTile", "Lcom/hexwarfare/app/domain/model/HexTile;", "units", "", "currentTurn", "Lcom/hexwarfare/app/domain/model/Turn;", "reachableTiles", "", "Lcom/hexwarfare/app/domain/model/HexCoord;", "moveCosts", "", "", "selectedPath", "commandQuotaLeft", "commandHistory", "Lcom/hexwarfare/app/domain/model/CommandRecord;", "message", "", "showStateChangeDialog", "", "showSupplyDialog", "showSettlementDialog", "settlementResult", "Lcom/hexwarfare/app/domain/model/SettlementResult;", "showUnitSelectionDialog", "unitsOnSelectedTile", "(Lcom/hexwarfare/app/domain/model/GameMap;Lcom/hexwarfare/app/domain/model/GameUnit;Lcom/hexwarfare/app/domain/model/HexTile;Ljava/util/List;Lcom/hexwarfare/app/domain/model/Turn;Ljava/util/Set;Ljava/util/Map;Ljava/util/List;ILjava/util/List;Ljava/lang/String;ZZZLcom/hexwarfare/app/domain/model/SettlementResult;ZLjava/util/List;)V", "getCommandHistory", "()Ljava/util/List;", "getCommandQuotaLeft", "()I", "getCurrentTurn", "()Lcom/hexwarfare/app/domain/model/Turn;", "getMap", "()Lcom/hexwarfare/app/domain/model/GameMap;", "getMessage", "()Ljava/lang/String;", "getMoveCosts", "()Ljava/util/Map;", "getReachableTiles", "()Ljava/util/Set;", "getSelectedPath", "getSelectedTile", "()Lcom/hexwarfare/app/domain/model/HexTile;", "getSelectedUnit", "()Lcom/hexwarfare/app/domain/model/GameUnit;", "getSettlementResult", "()Lcom/hexwarfare/app/domain/model/SettlementResult;", "getShowSettlementDialog", "()Z", "getShowStateChangeDialog", "getShowSupplyDialog", "getShowUnitSelectionDialog", "getUnits", "getUnitsOnSelectedTile", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class GameUiState {
    @org.jetbrains.annotations.Nullable()
    private final com.hexwarfare.app.domain.model.GameMap map = null;
    @org.jetbrains.annotations.Nullable()
    private final com.hexwarfare.app.domain.model.GameUnit selectedUnit = null;
    @org.jetbrains.annotations.Nullable()
    private final com.hexwarfare.app.domain.model.HexTile selectedTile = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.hexwarfare.app.domain.model.GameUnit> units = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hexwarfare.app.domain.model.Turn currentTurn = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<com.hexwarfare.app.domain.model.HexCoord> reachableTiles = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<com.hexwarfare.app.domain.model.HexCoord, java.lang.Integer> moveCosts = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.hexwarfare.app.domain.model.HexCoord> selectedPath = null;
    private final int commandQuotaLeft = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.hexwarfare.app.domain.model.CommandRecord> commandHistory = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String message = null;
    private final boolean showStateChangeDialog = false;
    private final boolean showSupplyDialog = false;
    private final boolean showSettlementDialog = false;
    @org.jetbrains.annotations.Nullable()
    private final com.hexwarfare.app.domain.model.SettlementResult settlementResult = null;
    private final boolean showUnitSelectionDialog = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.hexwarfare.app.domain.model.GameUnit> unitsOnSelectedTile = null;
    
    public GameUiState(@org.jetbrains.annotations.Nullable()
    com.hexwarfare.app.domain.model.GameMap map, @org.jetbrains.annotations.Nullable()
    com.hexwarfare.app.domain.model.GameUnit selectedUnit, @org.jetbrains.annotations.Nullable()
    com.hexwarfare.app.domain.model.HexTile selectedTile, @org.jetbrains.annotations.NotNull()
    java.util.List<com.hexwarfare.app.domain.model.GameUnit> units, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.Turn currentTurn, @org.jetbrains.annotations.NotNull()
    java.util.Set<com.hexwarfare.app.domain.model.HexCoord> reachableTiles, @org.jetbrains.annotations.NotNull()
    java.util.Map<com.hexwarfare.app.domain.model.HexCoord, java.lang.Integer> moveCosts, @org.jetbrains.annotations.NotNull()
    java.util.List<com.hexwarfare.app.domain.model.HexCoord> selectedPath, int commandQuotaLeft, @org.jetbrains.annotations.NotNull()
    java.util.List<com.hexwarfare.app.domain.model.CommandRecord> commandHistory, @org.jetbrains.annotations.NotNull()
    java.lang.String message, boolean showStateChangeDialog, boolean showSupplyDialog, boolean showSettlementDialog, @org.jetbrains.annotations.Nullable()
    com.hexwarfare.app.domain.model.SettlementResult settlementResult, boolean showUnitSelectionDialog, @org.jetbrains.annotations.NotNull()
    java.util.List<com.hexwarfare.app.domain.model.GameUnit> unitsOnSelectedTile) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.hexwarfare.app.domain.model.GameMap getMap() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.hexwarfare.app.domain.model.GameUnit getSelectedUnit() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.hexwarfare.app.domain.model.HexTile getSelectedTile() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.hexwarfare.app.domain.model.GameUnit> getUnits() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.Turn getCurrentTurn() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Set<com.hexwarfare.app.domain.model.HexCoord> getReachableTiles() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<com.hexwarfare.app.domain.model.HexCoord, java.lang.Integer> getMoveCosts() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.hexwarfare.app.domain.model.HexCoord> getSelectedPath() {
        return null;
    }
    
    public final int getCommandQuotaLeft() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.hexwarfare.app.domain.model.CommandRecord> getCommandHistory() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getMessage() {
        return null;
    }
    
    public final boolean getShowStateChangeDialog() {
        return false;
    }
    
    public final boolean getShowSupplyDialog() {
        return false;
    }
    
    public final boolean getShowSettlementDialog() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.hexwarfare.app.domain.model.SettlementResult getSettlementResult() {
        return null;
    }
    
    public final boolean getShowUnitSelectionDialog() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.hexwarfare.app.domain.model.GameUnit> getUnitsOnSelectedTile() {
        return null;
    }
    
    public GameUiState() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.hexwarfare.app.domain.model.GameMap component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.hexwarfare.app.domain.model.CommandRecord> component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component11() {
        return null;
    }
    
    public final boolean component12() {
        return false;
    }
    
    public final boolean component13() {
        return false;
    }
    
    public final boolean component14() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.hexwarfare.app.domain.model.SettlementResult component15() {
        return null;
    }
    
    public final boolean component16() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.hexwarfare.app.domain.model.GameUnit> component17() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.hexwarfare.app.domain.model.GameUnit component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.hexwarfare.app.domain.model.HexTile component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.hexwarfare.app.domain.model.GameUnit> component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.Turn component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Set<com.hexwarfare.app.domain.model.HexCoord> component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<com.hexwarfare.app.domain.model.HexCoord, java.lang.Integer> component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.hexwarfare.app.domain.model.HexCoord> component8() {
        return null;
    }
    
    public final int component9() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.ui.screens.GameUiState copy(@org.jetbrains.annotations.Nullable()
    com.hexwarfare.app.domain.model.GameMap map, @org.jetbrains.annotations.Nullable()
    com.hexwarfare.app.domain.model.GameUnit selectedUnit, @org.jetbrains.annotations.Nullable()
    com.hexwarfare.app.domain.model.HexTile selectedTile, @org.jetbrains.annotations.NotNull()
    java.util.List<com.hexwarfare.app.domain.model.GameUnit> units, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.Turn currentTurn, @org.jetbrains.annotations.NotNull()
    java.util.Set<com.hexwarfare.app.domain.model.HexCoord> reachableTiles, @org.jetbrains.annotations.NotNull()
    java.util.Map<com.hexwarfare.app.domain.model.HexCoord, java.lang.Integer> moveCosts, @org.jetbrains.annotations.NotNull()
    java.util.List<com.hexwarfare.app.domain.model.HexCoord> selectedPath, int commandQuotaLeft, @org.jetbrains.annotations.NotNull()
    java.util.List<com.hexwarfare.app.domain.model.CommandRecord> commandHistory, @org.jetbrains.annotations.NotNull()
    java.lang.String message, boolean showStateChangeDialog, boolean showSupplyDialog, boolean showSettlementDialog, @org.jetbrains.annotations.Nullable()
    com.hexwarfare.app.domain.model.SettlementResult settlementResult, boolean showUnitSelectionDialog, @org.jetbrains.annotations.NotNull()
    java.util.List<com.hexwarfare.app.domain.model.GameUnit> unitsOnSelectedTile) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}