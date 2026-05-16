package org.galaxy.beyond.api.system.rogue.core;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class Pipeline {

    private final StepNode first;
    private final Map<RogueState, StepNode> lookup = new HashMap<>();

    Pipeline(StepNode first) {
        this.first = first;
        for (StepNode n = first; n != null; n = n.next) {
            lookup.put(n.state, n);
        }
    }

    public StepNode first() {
        return first;
    }

    @Nullable
    public StepNode lookup(RogueState state) {
        return lookup.get(state);
    }

    /**
     * 计算当前节点的下一步。
     * 如果是 subEntry，先进入子流程；如果子流程结束，返回 subBackTo。
     */
    StepNode advance(StepNode current) {
        // 如果当前是 subEntry，进入子流程第一步
        if (current.subEntry && current.subPipeline != null) {
            return current.subPipeline.first();
        }

        // 如果在子流程内
        Pipeline sub = findEnclosingSub(current);
        if (sub != null) {
            StepNode subNext = sub.advance(current);
            if (subNext != null) {
                return subNext;
            }
            // 子流程结束，找到回到哪个节点
            StepNode subEntry = findSubEntry(sub);
            if (subEntry != null && subEntry.subBackTo != null) {
                return lookup(subEntry.subBackTo);
            }
            // 没有 backTo，回到 subEntry 的 next
            if (subEntry != null) {
                return subEntry.next;
            }
        }

        return current.next;
    }

    @Nullable
    private Pipeline findEnclosingSub(StepNode node) {
        for (StepNode n = first; n != null; n = n.next) {
            if (n.subPipeline != null && containsStep(n.subPipeline, node)) {
                return n.subPipeline;
            }
        }
        return null;
    }

    private boolean containsStep(Pipeline p, StepNode node) {
        for (StepNode n = p.first; n != null; n = n.next) {
            if (n == node) return true;
            if (n.subPipeline != null && containsStep(n.subPipeline, node)) return true;
        }
        return false;
    }

    @Nullable
    private StepNode findSubEntry(Pipeline sub) {
        for (StepNode n = first; n != null; n = n.next) {
            if (n.subPipeline == sub) return n;
        }
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private StepNode head;
        private StepNode tail;
        private StepNode lastSubEntry;

        public Builder start(RogueState state) {
            return step(state, new IRoguePhase() {}, new ITransition() {
                public boolean isSatisfied(net.minecraft.server.level.ServerLevel level, org.galaxy.beyond.api.system.rogue.RogueContext ctx) {
                    return true;
                }
            });
        }

        public Builder step(RogueState state, IRoguePhase phase, ITransition transition) {
            StepNode node = new StepNode(state, phase, transition);
            if (head == null) {
                head = node;
            } else {
                tail.next = node;
            }
            tail = node;
            return this;
        }

        public Builder step(RogueState state) {
            return step(state, new IRoguePhase() {}, new ITransition() {
                public boolean isSatisfied(net.minecraft.server.level.ServerLevel level, org.galaxy.beyond.api.system.rogue.RogueContext ctx) {
                    return true;
                }
            });
        }

        public Builder sub(Pipeline subPipeline, RogueState backTo) {
            if (tail == null) {
                throw new IllegalStateException("Must have at least one step before sub()");
            }
            tail.subEntry = true;
            tail.subPipeline = subPipeline;
            tail.subBackTo = backTo;
            lastSubEntry = tail;
            return this;
        }

        public Pipeline build() {
            if (head == null) {
                throw new IllegalStateException("Pipeline must have at least one step");
            }
            return new Pipeline(head);
        }
    }
}
