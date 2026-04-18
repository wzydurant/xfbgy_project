package com.hexwarfare.app.domain.model;

/**
 * 六角格坐标类，使用 Cube 坐标系统
 * @param q 轴坐标
 * @param r 径向坐标
 * @param s 第三轴坐标（q + r + s = 0）
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\b\u0018\u0000 \u001a2\u00020\u0001:\u0001\u001aB\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0006J\t\u0010\u000b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\'\u0010\u000e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0003H\u00c6\u0001J\u000e\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\u0000J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0010\u001a\u0004\u0018\u00010\u0001H\u0096\u0002J\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00000\u0014J\b\u0010\u0015\u001a\u00020\u0003H\u0016J\u0012\u0010\u0016\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00030\u0017J\t\u0010\u0018\u001a\u00020\u0019H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\bR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\b\u00a8\u0006\u001b"}, d2 = {"Lcom/hexwarfare/app/domain/model/HexCoord;", "", "q", "", "r", "s", "(III)V", "getQ", "()I", "getR", "getS", "component1", "component2", "component3", "copy", "distanceTo", "other", "equals", "", "getNeighbors", "", "hashCode", "toOffset", "Lkotlin/Pair;", "toString", "", "Companion", "app_debug"})
public final class HexCoord {
    private final int q = 0;
    private final int r = 0;
    private final int s = 0;
    @org.jetbrains.annotations.NotNull()
    public static final com.hexwarfare.app.domain.model.HexCoord.Companion Companion = null;
    
    public HexCoord(int q, int r, int s) {
        super();
    }
    
    public final int getQ() {
        return 0;
    }
    
    public final int getR() {
        return 0;
    }
    
    public final int getS() {
        return 0;
    }
    
    /**
     * 获取邻居坐标
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.hexwarfare.app.domain.model.HexCoord> getNeighbors() {
        return null;
    }
    
    /**
     * 计算到另一个坐标的距离
     */
    public final int distanceTo(@org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord other) {
        return 0;
    }
    
    /**
     * 转换为 Offset 坐标（奇数行偏移）
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<java.lang.Integer, java.lang.Integer> toOffset() {
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
    
    public final int component1() {
        return 0;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final int component3() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.HexCoord copy(int q, int r, int s) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0006\u00a8\u0006\b"}, d2 = {"Lcom/hexwarfare/app/domain/model/HexCoord$Companion;", "", "()V", "fromOffset", "Lcom/hexwarfare/app/domain/model/HexCoord;", "col", "", "row", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * 从 Offset 坐标转换
         */
        @org.jetbrains.annotations.NotNull()
        public final com.hexwarfare.app.domain.model.HexCoord fromOffset(int col, int row) {
            return null;
        }
    }
}