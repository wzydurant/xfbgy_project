package com.hexwarfare.app.domain.model;

/**
 * A* 路径查找器，用于六角格地图
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0005\n\u0002\u0010$\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J4\u0010\u0003\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\rJ*\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\rJ\"\u0010\u0010\u001a\u00020\u000b2\u0006\u0010\u0011\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\t2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u0002J\u001c\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00060\u00132\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0006J\u0018\u0010\u0014\u001a\u00020\u000b2\u0006\u0010\u0015\u001a\u00020\u00062\u0006\u0010\u0016\u001a\u00020\u0006H\u0002J*\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00060\u00132\u0012\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00060\u00192\u0006\u0010\u001a\u001a\u00020\u0006H\u0002\u00a8\u0006\u001b"}, d2 = {"Lcom/hexwarfare/app/domain/model/PathFinder;", "", "()V", "findPath", "Lcom/hexwarfare/app/domain/model/PathResult;", "start", "Lcom/hexwarfare/app/domain/model/HexCoord;", "goal", "gameMap", "Lcom/hexwarfare/app/domain/model/GameMap;", "movePower", "", "unit", "Lcom/hexwarfare/app/domain/model/GameUnit;", "findReachableTiles", "Lcom/hexwarfare/app/domain/model/MoveRangeResult;", "getMoveCost", "coord", "getSimplePath", "", "heuristic", "a", "b", "reconstructPath", "cameFrom", "", "current", "app_debug"})
public final class PathFinder {
    @org.jetbrains.annotations.NotNull()
    public static final com.hexwarfare.app.domain.model.PathFinder INSTANCE = null;
    
    private PathFinder() {
        super();
    }
    
    /**
     * 计算两点之间的最短路径
     * @param start 起点坐标
     * @param goal 终点坐标
     * @param gameMap 游戏地图
     * @param movePower 单位移动力
     * @param unit 当前单位（用于检查ZOC等）
     * @return 路径结果，包含路径、消耗和剩余移动力
     */
    @org.jetbrains.annotations.Nullable()
    public final com.hexwarfare.app.domain.model.PathResult findPath(@org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord start, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord goal, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.GameMap gameMap, int movePower, @org.jetbrains.annotations.Nullable()
    com.hexwarfare.app.domain.model.GameUnit unit) {
        return null;
    }
    
    /**
     * 计算给定移动力范围内的所有可达格
     * @param start 起点坐标
     * @param gameMap 游戏地图
     * @param movePower 单位移动力
     * @param unit 当前单位
     * @return 可移动区域结果
     */
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.MoveRangeResult findReachableTiles(@org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord start, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.GameMap gameMap, int movePower, @org.jetbrains.annotations.Nullable()
    com.hexwarfare.app.domain.model.GameUnit unit) {
        return null;
    }
    
    /**
     * A* 启发式函数，使用六角格距离
     */
    private final int heuristic(com.hexwarfare.app.domain.model.HexCoord a, com.hexwarfare.app.domain.model.HexCoord b) {
        return 0;
    }
    
    /**
     * 重建路径
     */
    private final java.util.List<com.hexwarfare.app.domain.model.HexCoord> reconstructPath(java.util.Map<com.hexwarfare.app.domain.model.HexCoord, com.hexwarfare.app.domain.model.HexCoord> cameFrom, com.hexwarfare.app.domain.model.HexCoord current) {
        return null;
    }
    
    /**
     * 获取移动到某格的消耗
     */
    private final int getMoveCost(com.hexwarfare.app.domain.model.HexCoord coord, com.hexwarfare.app.domain.model.GameMap gameMap, com.hexwarfare.app.domain.model.GameUnit unit) {
        return 0;
    }
    
    /**
     * 获取从起点到终点的路径，简化版本（不考虑障碍）
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.hexwarfare.app.domain.model.HexCoord> getSimplePath(@org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord start, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord goal) {
        return null;
    }
}