package com.pz.beyond.api.system.progress.core;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * 关卡会话生命周期阶段（简化版）。
 * <p>
 * 完整流程：INACTIVE → WARMUP → IN_PROGRESS → COMPLETED
 */
public enum SessionState implements StringRepresentable {
    /** 无活动关卡，或关卡未初始化 */
    INACTIVE("inactive"),
    /** 热身倒计时，战利品袋右键后进入 */
    WARMUP("warmup"),
    /** 关卡进行中，节点可交互 */
    IN_PROGRESS("in_progress"),
    /** 全部场景完成，关卡结束 */
    COMPLETED("completed");

    private final String name;

    SessionState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /** 仅 IN_PROGRESS 状态下节点可交互 */
    public boolean canInteractWithNode() {
        return this == IN_PROGRESS;
    }

    public static final Codec<SessionState> CODEC = StringRepresentable.fromEnum(SessionState::values);
}
