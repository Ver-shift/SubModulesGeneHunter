package org.biotech.api.system.merge.core;

/**
 * 合并管理器接口 - 玩家身上的概率合并系统
 * 
 * 输出规则（根据输入词条数量）：
 * - Epic：必定双词条
 * - Rare：更高概率双词条
 * - Uncommon：极小概率双词条
 * - Common：不会有
 *
 */
public interface IMergeManager {

    /**
     * 每次讲基因放入输入槽位的时候进行触发
     */
    void updateSlotData();

    /**
     * 计算输入槽位的词条的总数
     */
    int traitCount();

    /**
     * 计算每个基因的平均词条数量 - 这个数值将会影响输出的稀有度
     * @return
     */
    float traitCountPerGene();

    /**
     * 计算输出的数量。
     * 固定3个合成一个gene。
     */
    int outputXeneCount(int traitCount);


    /**
     * 点击输出按钮触发
     * 如果平均词条数量为1，那么就触发 ServerConfig.getUncommonGeneTraitRollCount();，
     * 依次类推
     *
     * 消除指定数量的GeneItem，多出的部分不动。然后直接像玩家背包触发use 方法
     */
    void merge();





}
