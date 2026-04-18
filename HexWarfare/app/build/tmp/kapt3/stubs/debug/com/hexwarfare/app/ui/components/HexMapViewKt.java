package com.hexwarfare.app.ui.components;

import androidx.compose.runtime.*;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.drawscope.DrawScope;
import androidx.compose.ui.graphics.drawscope.Stroke;
import com.hexwarfare.app.domain.model.Faction;
import com.hexwarfare.app.domain.model.GameMap;
import com.hexwarfare.app.domain.model.HexCoord;
import com.hexwarfare.app.domain.model.TerrainType;
import com.hexwarfare.app.domain.model.Unit;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000L\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0007\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\u001aF\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\n2\b\b\u0002\u0010\u000b\u001a\u00020\fH\u0007\u001a$\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u000f0\u000e2\u0006\u0010\u0010\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\u000fH\u0002\u001a \u0010\u0012\u001a\u00020\b2\u0006\u0010\u0013\u001a\u00020\u000f2\u0006\u0010\u0014\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u000fH\u0002\u001aN\u0010\u0015\u001a\u00020\u0001*\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u000f2\u0006\u0010\u0018\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u000f2\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u001a2\u0006\u0010\u001e\u001a\u00020\u001aH\u0002\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001f\u0010 \u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006!"}, d2 = {"HexMapView", "", "map", "Lcom/hexwarfare/app/domain/model/GameMap;", "units", "", "Lcom/hexwarfare/app/domain/model/Unit;", "selectedCoord", "Lcom/hexwarfare/app/domain/model/HexCoord;", "onTileClick", "Lkotlin/Function1;", "modifier", "Landroidx/compose/ui/Modifier;", "hexToScreen", "Lkotlin/Pair;", "", "coord", "size", "screenToHex", "screenX", "screenY", "drawHexTile", "Landroidx/compose/ui/graphics/drawscope/DrawScope;", "centerX", "centerY", "fillColor", "Landroidx/compose/ui/graphics/Color;", "isSelected", "", "selectedColor", "strokeColor", "drawHexTile-exLbS_c", "(Landroidx/compose/ui/graphics/drawscope/DrawScope;FFFJZJJ)V", "app_debug"})
public final class HexMapViewKt {
    
    /**
     * Pointy-top 六角格布局的地图视图
     */
    @androidx.compose.runtime.Composable()
    public static final void HexMapView(@org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.GameMap map, @org.jetbrains.annotations.NotNull()
    java.util.List<com.hexwarfare.app.domain.model.Unit> units, @org.jetbrains.annotations.Nullable()
    com.hexwarfare.app.domain.model.HexCoord selectedCoord, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.hexwarfare.app.domain.model.HexCoord, kotlin.Unit> onTileClick, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * 将六角格坐标转换为屏幕坐标 (Pointy-top)
     */
    private static final kotlin.Pair<java.lang.Float, java.lang.Float> hexToScreen(com.hexwarfare.app.domain.model.HexCoord coord, float size) {
        return null;
    }
    
    /**
     * 将屏幕坐标转换为六角格坐标 (Pointy-top)
     */
    private static final com.hexwarfare.app.domain.model.HexCoord screenToHex(float screenX, float screenY, float size) {
        return null;
    }
}