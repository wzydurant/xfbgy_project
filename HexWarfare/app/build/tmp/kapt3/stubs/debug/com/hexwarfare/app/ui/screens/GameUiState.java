package com.hexwarfare.app.ui.screens;

import androidx.compose.foundation.layout.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.lifecycle.ViewModel;
import com.hexwarfare.app.domain.model.Faction;
import com.hexwarfare.app.domain.model.GameMap;
import com.hexwarfare.app.domain.model.HexCoord;
import com.hexwarfare.app.domain.model.HexTile;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;
import com.hexwarfare.app.domain.model.Unit;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B-\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\u0002\u0010\tJ\u000b\u0010\u0010\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\u0011\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u00c6\u0003J1\u0010\u0013\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u00c6\u0001J\u0013\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0017\u001a\u00020\u0018H\u00d6\u0001J\t\u0010\u0019\u001a\u00020\u001aH\u00d6\u0001R\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006\u001b"}, d2 = {"Lcom/hexwarfare/app/ui/screens/GameUiState;", "", "map", "Lcom/hexwarfare/app/domain/model/GameMap;", "selectedTile", "Lcom/hexwarfare/app/domain/model/HexTile;", "units", "", "Lcom/hexwarfare/app/domain/model/Unit;", "(Lcom/hexwarfare/app/domain/model/GameMap;Lcom/hexwarfare/app/domain/model/HexTile;Ljava/util/List;)V", "getMap", "()Lcom/hexwarfare/app/domain/model/GameMap;", "getSelectedTile", "()Lcom/hexwarfare/app/domain/model/HexTile;", "getUnits", "()Ljava/util/List;", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "", "app_debug"})
public final class GameUiState {
    @org.jetbrains.annotations.Nullable()
    private final com.hexwarfare.app.domain.model.GameMap map = null;
    @org.jetbrains.annotations.Nullable()
    private final com.hexwarfare.app.domain.model.HexTile selectedTile = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.hexwarfare.app.domain.model.Unit> units = null;
    
    public GameUiState(@org.jetbrains.annotations.Nullable()
    com.hexwarfare.app.domain.model.GameMap map, @org.jetbrains.annotations.Nullable()
    com.hexwarfare.app.domain.model.HexTile selectedTile, @org.jetbrains.annotations.NotNull()
    java.util.List<com.hexwarfare.app.domain.model.Unit> units) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.hexwarfare.app.domain.model.GameMap getMap() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.hexwarfare.app.domain.model.HexTile getSelectedTile() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.hexwarfare.app.domain.model.Unit> getUnits() {
        return null;
    }
    
    public GameUiState() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.hexwarfare.app.domain.model.GameMap component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.hexwarfare.app.domain.model.HexTile component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.hexwarfare.app.domain.model.Unit> component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.ui.screens.GameUiState copy(@org.jetbrains.annotations.Nullable()
    com.hexwarfare.app.domain.model.GameMap map, @org.jetbrains.annotations.Nullable()
    com.hexwarfare.app.domain.model.HexTile selectedTile, @org.jetbrains.annotations.NotNull()
    java.util.List<com.hexwarfare.app.domain.model.Unit> units) {
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