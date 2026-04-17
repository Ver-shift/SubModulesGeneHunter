package com.pz.beyond.api.system.zone;

import com.pz.beyond.api.system.rule.RuleData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.List;


public abstract class AbstractZone<M> {

    /**
     * 标识符
     */
    protected final ResourceLocation identifier;

    /**
     * 在地图上面的颜色
     */
    private int mapChunkColor = 0xFFFFE6FF;


    public AbstractZone(ResourceLocation identifier) {
        this.identifier = identifier;
    }

    public ResourceLocation getIdentifier() {
        return identifier;
    }

    protected M getAttachData(Level level){
        return null;
    }

    /**
     * 初始化区域数据
     * 在 ZoneData 首次创建时调用，用于添加默认的规则监听器
     * 
     * 子类必须实现此方法，通过 listeners 参数添加规则
     * 
     * @param listeners 规则监听器列表，向其中添加规则
     * @param level 服务端维度（可用于获取维度相关信息）
     */
    public abstract void initialize(List<RuleData> listeners, ServerLevel level);

    /**
     * 便捷方法：添加规则到列表
     */
    protected void addRule(List<RuleData> listeners, RuleData rule) {
        if (rule != null && listeners != null) {
            listeners.add(rule);
        }
    }

    /**
     * 便捷方法：批量添加规则
     */
    protected void addRules(List<RuleData> listeners, RuleData... rules) {
        if (listeners != null && rules != null) {
            for (RuleData rule : rules) {
                addRule(listeners, rule);
            }
        }
    }
}
