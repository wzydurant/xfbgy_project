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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\b\u0010\n\u001a\u00020\u000bH\u0002J\u000e\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\rH\u0002J\b\u0010\u000f\u001a\u00020\u0010H\u0002J\u000e\u0010\u0011\u001a\u00020\u00102\u0006\u0010\u0012\u001a\u00020\u0013R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\t\u00a8\u0006\u0014"}, d2 = {"Lcom/hexwarfare/app/ui/screens/GameViewModel;", "Landroidx/lifecycle/ViewModel;", "()V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/hexwarfare/app/ui/screens/GameUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "createSampleMap", "Lcom/hexwarfare/app/domain/model/GameMap;", "createSampleUnits", "", "Lcom/hexwarfare/app/domain/model/Unit;", "loadMap", "", "selectTile", "coord", "Lcom/hexwarfare/app/domain/model/HexCoord;", "app_debug"})
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
    
    public final void selectTile(@org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord coord) {
    }
    
    private final com.hexwarfare.app.domain.model.GameMap createSampleMap() {
        return null;
    }
    
    private final java.util.List<com.hexwarfare.app.domain.model.Unit> createSampleUnits() {
        return null;
    }
}