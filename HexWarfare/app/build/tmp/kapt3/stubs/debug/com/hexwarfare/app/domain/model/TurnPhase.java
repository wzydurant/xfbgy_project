package com.hexwarfare.app.domain.model;

/**
 * 回合阶段封闭类
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u0000 \t2\u00020\u0001:\u0005\b\t\n\u000b\fB\u000f\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0007\u001a\u00020\u0000R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u0082\u0001\u0004\r\u000e\u000f\u0010\u00a8\u0006\u0011"}, d2 = {"Lcom/hexwarfare/app/domain/model/TurnPhase;", "", "displayName", "", "(Ljava/lang/String;)V", "getDisplayName", "()Ljava/lang/String;", "next", "ChiefOfStaffCommand", "Companion", "GeneralCommand", "Settlement", "Supply", "Lcom/hexwarfare/app/domain/model/TurnPhase$ChiefOfStaffCommand;", "Lcom/hexwarfare/app/domain/model/TurnPhase$GeneralCommand;", "Lcom/hexwarfare/app/domain/model/TurnPhase$Settlement;", "Lcom/hexwarfare/app/domain/model/TurnPhase$Supply;", "app_debug"})
public abstract class TurnPhase {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String displayName = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.hexwarfare.app.domain.model.TurnPhase.Companion Companion = null;
    
    private TurnPhase(java.lang.String displayName) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDisplayName() {
        return null;
    }
    
    /**
     * 获取下一个阶段
     */
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.TurnPhase next() {
        return null;
    }
    
    /**
     * 参谋长命令阶段 - 参谋长可以下达命令
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u00c6\n\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0013\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u00d6\u0003J\t\u0010\u0007\u001a\u00020\bH\u00d6\u0001J\t\u0010\t\u001a\u00020\nH\u00d6\u0001\u00a8\u0006\u000b"}, d2 = {"Lcom/hexwarfare/app/domain/model/TurnPhase$ChiefOfStaffCommand;", "Lcom/hexwarfare/app/domain/model/TurnPhase;", "()V", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class ChiefOfStaffCommand extends com.hexwarfare.app.domain.model.TurnPhase {
        @org.jetbrains.annotations.NotNull()
        public static final com.hexwarfare.app.domain.model.TurnPhase.ChiefOfStaffCommand INSTANCE = null;
        
        private ChiefOfStaffCommand() {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/hexwarfare/app/domain/model/TurnPhase$Companion;", "", "()V", "isNewTurn", "", "phase", "Lcom/hexwarfare/app/domain/model/TurnPhase;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * 判断是否是新回合开始
         */
        public final boolean isNewTurn(@org.jetbrains.annotations.NotNull()
        com.hexwarfare.app.domain.model.TurnPhase phase) {
            return false;
        }
    }
    
    /**
     * 将军命令阶段 - 将军可以下达命令给从属单位
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u00c6\n\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0013\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u00d6\u0003J\t\u0010\u0007\u001a\u00020\bH\u00d6\u0001J\t\u0010\t\u001a\u00020\nH\u00d6\u0001\u00a8\u0006\u000b"}, d2 = {"Lcom/hexwarfare/app/domain/model/TurnPhase$GeneralCommand;", "Lcom/hexwarfare/app/domain/model/TurnPhase;", "()V", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class GeneralCommand extends com.hexwarfare.app.domain.model.TurnPhase {
        @org.jetbrains.annotations.NotNull()
        public static final com.hexwarfare.app.domain.model.TurnPhase.GeneralCommand INSTANCE = null;
        
        private GeneralCommand() {
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
    
    /**
     * 回合结算阶段 - 处理回合结束的各种结算
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u00c6\n\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0013\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u00d6\u0003J\t\u0010\u0007\u001a\u00020\bH\u00d6\u0001J\t\u0010\t\u001a\u00020\nH\u00d6\u0001\u00a8\u0006\u000b"}, d2 = {"Lcom/hexwarfare/app/domain/model/TurnPhase$Settlement;", "Lcom/hexwarfare/app/domain/model/TurnPhase;", "()V", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class Settlement extends com.hexwarfare.app.domain.model.TurnPhase {
        @org.jetbrains.annotations.NotNull()
        public static final com.hexwarfare.app.domain.model.TurnPhase.Settlement INSTANCE = null;
        
        private Settlement() {
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
    
    /**
     * 补给阶段 - 资源产出结算、粮食补给判定等
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u00c6\n\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0013\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u00d6\u0003J\t\u0010\u0007\u001a\u00020\bH\u00d6\u0001J\t\u0010\t\u001a\u00020\nH\u00d6\u0001\u00a8\u0006\u000b"}, d2 = {"Lcom/hexwarfare/app/domain/model/TurnPhase$Supply;", "Lcom/hexwarfare/app/domain/model/TurnPhase;", "()V", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class Supply extends com.hexwarfare.app.domain.model.TurnPhase {
        @org.jetbrains.annotations.NotNull()
        public static final com.hexwarfare.app.domain.model.TurnPhase.Supply INSTANCE = null;
        
        private Supply() {
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
}