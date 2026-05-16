package org.galaxy.beyond.api.system.node.core;

/**
 * 节点状态，以策划案关卡系统3.0为准：LOCKED → PRE_NODE → PRE_EVENT → ON_EVENT → UNLOCKED
 */
public enum NodeState {

    LOCKED,
    PRE_NODE,
    PRE_EVENT,
    ON_EVENT,
    UNLOCKED

}
