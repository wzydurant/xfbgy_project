package com.hexwarfare.app.domain.model;

/**
 * 回合数据类
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B#\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0007H\u00c6\u0003J\'\u0010\u0012\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\u0013\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0016\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u0017\u001a\u00020\u0018H\u00d6\u0001R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u0019"}, d2 = {"Lcom/hexwarfare/app/domain/model/Turn;", "", "turnNumber", "", "phase", "Lcom/hexwarfare/app/domain/model/TurnPhase;", "commander", "Lcom/hexwarfare/app/domain/model/Commander;", "(ILcom/hexwarfare/app/domain/model/TurnPhase;Lcom/hexwarfare/app/domain/model/Commander;)V", "getCommander", "()Lcom/hexwarfare/app/domain/model/Commander;", "getPhase", "()Lcom/hexwarfare/app/domain/model/TurnPhase;", "getTurnNumber", "()I", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "toString", "", "app_debug"})
public final class Turn {
    private final int turnNumber = 0;
    @org.jetbrains.annotations.NotNull()
    private final com.hexwarfare.app.domain.model.TurnPhase phase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hexwarfare.app.domain.model.Commander commander = null;
    
    public Turn(int turnNumber, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.TurnPhase phase, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.Commander commander) {
        super();
    }
    
    public final int getTurnNumber() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.TurnPhase getPhase() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.Commander getCommander() {
        return null;
    }
    
    public Turn() {
        super();
    }
    
    public final int component1() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.TurnPhase component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.Commander component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.Turn copy(int turnNumber, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.TurnPhase phase, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.Commander commander) {
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