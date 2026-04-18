package com.hexwarfare.app.domain.model;

/**
 * 地图数据类
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B=\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0012\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b0\u0006\u0012\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\u0002\u0010\u000bJ\t\u0010\u0012\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\u0015\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b0\u0006H\u00c6\u0003J\u0015\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00070\u0006H\u00c6\u0003JI\u0010\u0016\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\u0014\b\u0002\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b0\u00062\u0014\b\u0002\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00070\u0006H\u00c6\u0001J\u0013\u0010\u0017\u001a\u00020\u00182\b\u0010\u0019\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001a\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u001b\u001a\u00020\u001cH\u00d6\u0001R\u001d\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u001d\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b0\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\rR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000f\u00a8\u0006\u001d"}, d2 = {"Lcom/hexwarfare/app/domain/model/GameMap;", "", "width", "", "height", "tiles", "", "Lcom/hexwarfare/app/domain/model/HexCoord;", "Lcom/hexwarfare/app/domain/model/HexTile;", "capitalPositions", "Lcom/hexwarfare/app/domain/model/Faction;", "(IILjava/util/Map;Ljava/util/Map;)V", "getCapitalPositions", "()Ljava/util/Map;", "getHeight", "()I", "getTiles", "getWidth", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "toString", "", "app_debug"})
public final class GameMap {
    private final int width = 0;
    private final int height = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<com.hexwarfare.app.domain.model.HexCoord, com.hexwarfare.app.domain.model.HexTile> tiles = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<com.hexwarfare.app.domain.model.Faction, com.hexwarfare.app.domain.model.HexCoord> capitalPositions = null;
    
    public GameMap(int width, int height, @org.jetbrains.annotations.NotNull()
    java.util.Map<com.hexwarfare.app.domain.model.HexCoord, com.hexwarfare.app.domain.model.HexTile> tiles, @org.jetbrains.annotations.NotNull()
    java.util.Map<com.hexwarfare.app.domain.model.Faction, com.hexwarfare.app.domain.model.HexCoord> capitalPositions) {
        super();
    }
    
    public final int getWidth() {
        return 0;
    }
    
    public final int getHeight() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<com.hexwarfare.app.domain.model.HexCoord, com.hexwarfare.app.domain.model.HexTile> getTiles() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<com.hexwarfare.app.domain.model.Faction, com.hexwarfare.app.domain.model.HexCoord> getCapitalPositions() {
        return null;
    }
    
    public final int component1() {
        return 0;
    }
    
    public final int component2() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<com.hexwarfare.app.domain.model.HexCoord, com.hexwarfare.app.domain.model.HexTile> component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<com.hexwarfare.app.domain.model.Faction, com.hexwarfare.app.domain.model.HexCoord> component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.GameMap copy(int width, int height, @org.jetbrains.annotations.NotNull()
    java.util.Map<com.hexwarfare.app.domain.model.HexCoord, com.hexwarfare.app.domain.model.HexTile> tiles, @org.jetbrains.annotations.NotNull()
    java.util.Map<com.hexwarfare.app.domain.model.Faction, com.hexwarfare.app.domain.model.HexCoord> capitalPositions) {
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