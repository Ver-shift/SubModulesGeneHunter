package org.galaxy.beyond.api.system.zone.async;

/**
 * 可活动区域拓展任务类型。
 * <p>
 * 三种任务共用同一套搜索优先级：
 * <ol>
 *     <li>先读取本任务对应的最小拓展半径，保证每次拓展至少有基础范围。</li>
 *     <li>然后搜索范围内全部可定位的节点结构，先创建稳定的节点数据和颜色。</li>
 *     <li>节点结构按拓展前的 active 区域分类：接触 active 的是已发现节点，完全在 empty 外侧的是新节点。</li>
 *     <li>最小节点数量要求只由新节点满足，已发现节点只参与播报和半径覆盖，不占用新节点需求。</li>
 *     <li>如果在最大搜索半径内找到了足够节点，只拓展到刚好覆盖这些节点的半径。</li>
 *     <li>如果最大搜索半径内没有节点，或者节点数量不够，就直接拓展到最大半径。</li>
 * </ol>
 * 注意：最小拓展半径只是下限，不是搜索失败时的回退值。搜索失败仍然要扩大可活动区域，
 * 否则玩家会一直卡在边缘，看起来像没有真正搜索过。
 */
public enum ZoneExpansionJobType {
    /**
     * 初始区块拓展。
     * <p>
     * 用于游戏准备完成后的安全区初始化。它从初始安全区附近生成第一批可活动区块，
     * 主要目标是建立玩家开局可探索空间，并尽量连接到配置要求数量的新节点。
     * 已经接触 active 的节点会作为已发现节点播报，但不会满足初始化新节点数量。
     * <p>
     * 配置优先级：
     * <ol>
     *     <li>{@code ACTIVE_ZONE_MIN_EXPAND}：初始化最小拓展半径，只作为下限。</li>
     *     <li>{@code ACTIVE_ZONE_MIN_NODES}：初始化希望覆盖的新节点数量。</li>
     *     <li>{@code ACTIVE_ZONE_MAX_EXPAND}：节点数量不够时最终打开的最大半径。</li>
     * </ol>
     */
    INITIAL,

    /**
     * 节点解锁后的拓展。
     * <p>
     * 用于玩家完成当前节点后继续向外扩张。优先级是先满足新节点连接数量，
     * 再决定实际拓展半径；如果找不到足够节点，则按最大半径打开新的探索空间。
     * 已发现节点和新节点会分开播报，避免把 active 内部节点误当成新发现目标。
     * <p>
     * 配置优先级：
     * <ol>
     *     <li>{@code ACTIVE_ZONE_NODE_EXPAND_RADIUS}：节点解锁后的最小拓展半径，只作为下限。</li>
     *     <li>{@code ACTIVE_ZONE_MIN_CONNECTIONS}：本次希望连接到的新节点数量。</li>
     *     <li>{@code ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS}：节点数量不够时最终打开的最大半径。</li>
     * </ol>
     */
    NODE_UNLOCK,

    /**
     * 世界种子物品触发的拓展。
     * <p>
     * 用于玩家在区域边缘主动开拓。它和节点解锁使用同类搜索逻辑，
     * 但触发点来自玩家当前位置；没找到节点也不是失败，只是扩出新的可活动区域。
     * 搜索会先定位全部候选结构，再筛选 active 外侧的新节点来满足配置需求。
     * <p>
     * 配置优先级与节点解锁一致：
     * <ol>
     *     <li>{@code ACTIVE_ZONE_NODE_EXPAND_RADIUS}：世界种子的最小拓展半径，只作为下限。</li>
     *     <li>{@code ACTIVE_ZONE_MIN_CONNECTIONS}：优先尝试满足的新节点数量。</li>
     *     <li>{@code ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS}：节点数量不够时最终打开的最大半径。</li>
     * </ol>
     */
    WORLD_SEED
}
