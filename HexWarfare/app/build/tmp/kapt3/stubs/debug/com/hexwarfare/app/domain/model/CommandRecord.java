package com.hexwarfare.app.domain.model;

/**
 * 命令执行记录
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0014\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B5\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\u0007\u0012\u0006\u0010\t\u001a\u00020\n\u0012\u0006\u0010\u000b\u001a\u00020\f\u00a2\u0006\u0002\u0010\rJ\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\nH\u00c6\u0003J\t\u0010\u001e\u001a\u00020\fH\u00c6\u0003JE\u0010\u001f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00072\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\fH\u00c6\u0001J\u0013\u0010 \u001a\u00020!2\b\u0010\"\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010#\u001a\u00020\nH\u00d6\u0001J\t\u0010$\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\b\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0011R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018\u00a8\u0006%"}, d2 = {"Lcom/hexwarfare/app/domain/model/CommandRecord;", "", "unitId", "", "commandType", "Lcom/hexwarfare/app/domain/model/CommandType;", "fromCoord", "Lcom/hexwarfare/app/domain/model/HexCoord;", "toCoord", "turnNumber", "", "phase", "Lcom/hexwarfare/app/domain/model/TurnPhase;", "(Ljava/lang/String;Lcom/hexwarfare/app/domain/model/CommandType;Lcom/hexwarfare/app/domain/model/HexCoord;Lcom/hexwarfare/app/domain/model/HexCoord;ILcom/hexwarfare/app/domain/model/TurnPhase;)V", "getCommandType", "()Lcom/hexwarfare/app/domain/model/CommandType;", "getFromCoord", "()Lcom/hexwarfare/app/domain/model/HexCoord;", "getPhase", "()Lcom/hexwarfare/app/domain/model/TurnPhase;", "getToCoord", "getTurnNumber", "()I", "getUnitId", "()Ljava/lang/String;", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
public final class CommandRecord {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String unitId = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hexwarfare.app.domain.model.CommandType commandType = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hexwarfare.app.domain.model.HexCoord fromCoord = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hexwarfare.app.domain.model.HexCoord toCoord = null;
    private final int turnNumber = 0;
    @org.jetbrains.annotations.NotNull()
    private final com.hexwarfare.app.domain.model.TurnPhase phase = null;
    
    public CommandRecord(@org.jetbrains.annotations.NotNull()
    java.lang.String unitId, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.CommandType commandType, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord fromCoord, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord toCoord, int turnNumber, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.TurnPhase phase) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUnitId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.CommandType getCommandType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.HexCoord getFromCoord() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.HexCoord getToCoord() {
        return null;
    }
    
    public final int getTurnNumber() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.TurnPhase getPhase() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.CommandType component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.HexCoord component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.HexCoord component4() {
        return null;
    }
    
    public final int component5() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.TurnPhase component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.CommandRecord copy(@org.jetbrains.annotations.NotNull()
    java.lang.String unitId, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.CommandType commandType, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord fromCoord, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord toCoord, int turnNumber, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.TurnPhase phase) {
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