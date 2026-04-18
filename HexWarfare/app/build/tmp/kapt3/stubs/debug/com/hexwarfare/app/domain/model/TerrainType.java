package com.hexwarfare.app.domain.model;

/**
 * 地形类型枚举
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u001f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u000b\u001a\u00020\fR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\bj\u0002\b\rj\u0002\b\u000ej\u0002\b\u000fj\u0002\b\u0010j\u0002\b\u0011\u00a8\u0006\u0012"}, d2 = {"Lcom/hexwarfare/app/domain/model/TerrainType;", "", "moveCost", "", "visionBonus", "defenseBonus", "(Ljava/lang/String;IIII)V", "getDefenseBonus", "()I", "getMoveCost", "getVisionBonus", "canPass", "", "PLAINS", "MOUNTAIN", "RIVER", "BUILDING", "IMPASSABLE", "app_debug"})
public enum TerrainType {
    /*public static final*/ PLAINS /* = new PLAINS(0, 0, 0) */,
    /*public static final*/ MOUNTAIN /* = new MOUNTAIN(0, 0, 0) */,
    /*public static final*/ RIVER /* = new RIVER(0, 0, 0) */,
    /*public static final*/ BUILDING /* = new BUILDING(0, 0, 0) */,
    /*public static final*/ IMPASSABLE /* = new IMPASSABLE(0, 0, 0) */;
    private final int moveCost = 0;
    private final int visionBonus = 0;
    private final int defenseBonus = 0;
    
    TerrainType(int moveCost, int visionBonus, int defenseBonus) {
    }
    
    public final int getMoveCost() {
        return 0;
    }
    
    public final int getVisionBonus() {
        return 0;
    }
    
    public final int getDefenseBonus() {
        return 0;
    }
    
    public final boolean canPass() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.hexwarfare.app.domain.model.TerrainType> getEntries() {
        return null;
    }
}