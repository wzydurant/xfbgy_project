package com.hexwarfare.app.ui.components;

import androidx.compose.runtime.*;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.drawscope.DrawScope;
import androidx.compose.ui.graphics.drawscope.Stroke;
import com.hexwarfare.app.domain.model.Faction;
import com.hexwarfare.app.domain.model.GameMap;
import com.hexwarfare.app.domain.model.GameUnit;
import com.hexwarfare.app.domain.model.HexCoord;
import com.hexwarfare.app.domain.model.TerrainType;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000`\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\"\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0007\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\u001a\u0094\u0001\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\u000e\b\u0002\u0010\t\u001a\b\u0012\u0004\u0012\u00020\b0\n2\u000e\b\u0002\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\b0\u00052\u0014\b\u0002\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u000e0\r2\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\u00102\u0016\b\u0002\u0010\u0011\u001a\u0010\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u00102\b\b\u0002\u0010\u0012\u001a\u00020\u0013H\u0007\u001a$\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\u00160\u00152\u0006\u0010\u0017\u001a\u00020\b2\u0006\u0010\u0018\u001a\u00020\u0016H\u0002\u001a \u0010\u0019\u001a\u00020\b2\u0006\u0010\u001a\u001a\u00020\u00162\u0006\u0010\u001b\u001a\u00020\u00162\u0006\u0010\u0018\u001a\u00020\u0016H\u0002\u001aN\u0010\u001c\u001a\u00020\u0001*\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u00162\u0006\u0010\u001f\u001a\u00020\u00162\u0006\u0010\u0018\u001a\u00020\u00162\u0006\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020!2\u0006\u0010%\u001a\u00020!H\u0002\u00f8\u0001\u0000\u00a2\u0006\u0004\b&\u0010\'\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006("}, d2 = {"HexMapView", "", "map", "Lcom/hexwarfare/app/domain/model/GameMap;", "units", "", "Lcom/hexwarfare/app/domain/model/GameUnit;", "selectedCoord", "Lcom/hexwarfare/app/domain/model/HexCoord;", "highlightedCoords", "", "pathCoords", "moveCosts", "", "", "onTileClick", "Lkotlin/Function1;", "onTileHover", "modifier", "Landroidx/compose/ui/Modifier;", "hexToScreen", "Lkotlin/Pair;", "", "coord", "size", "screenToHex", "screenX", "screenY", "drawHexTile", "Landroidx/compose/ui/graphics/drawscope/DrawScope;", "centerX", "centerY", "fillColor", "Landroidx/compose/ui/graphics/Color;", "isSelected", "", "selectedColor", "strokeColor", "drawHexTile-exLbS_c", "(Landroidx/compose/ui/graphics/drawscope/DrawScope;FFFJZJJ)V", "app_debug"})
public final class HexMapViewKt {
    
    /**
     * Pointy-top 六角格布局的地图视图
     */
    @androidx.compose.runtime.Composable()
    public static final void HexMapView(@org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.GameMap map, @org.jetbrains.annotations.NotNull()
    java.util.List<com.hexwarfare.app.domain.model.GameUnit> units, @org.jetbrains.annotations.Nullable()
    com.hexwarfare.app.domain.model.HexCoord selectedCoord, @org.jetbrains.annotations.NotNull()
    java.util.Set<com.hexwarfare.app.domain.model.HexCoord> highlightedCoords, @org.jetbrains.annotations.NotNull()
    java.util.List<com.hexwarfare.app.domain.model.HexCoord> pathCoords, @org.jetbrains.annotations.NotNull()
    java.util.Map<com.hexwarfare.app.domain.model.HexCoord, java.lang.Integer> moveCosts, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.hexwarfare.app.domain.model.HexCoord, kotlin.Unit> onTileClick, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super com.hexwarfare.app.domain.model.HexCoord, kotlin.Unit> onTileHover, @org.jetbrains.annotations.NotNull()
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
     * 使用立方坐标舍入算法确保准确找到最近的六角格
     */
    private static final com.hexwarfare.app.domain.model.HexCoord screenToHex(float screenX, float screenY, float size) {
        return null;
    }
}