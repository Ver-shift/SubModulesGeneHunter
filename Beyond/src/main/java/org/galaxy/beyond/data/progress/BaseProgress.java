package org.galaxy.beyond.data.progress;

import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 关卡模板基类 —— 子类覆盖 scenes/green/red/orange 即可组合出完整关卡。
 * <p>
 * 和 Minecraft RecipeProvider 的继承模式一致：基类定义结构，子类覆盖数据。
 */
public abstract class BaseProgress {

    /** 构建完整 ProgressDefinition */
    public ProgressDefinition build() {
        return ProgressDefinition.builder()
                .sceneRolls(scenes())
                .encounters(encounters())
                .build();
    }

    /** 场景序列 —— 每个 roll 按 order 排序后从加权条目中随机抽一个 SceneType */
    protected abstract List<SceneRoll> scenes();

    /** 按颜色分组的遭遇池 —— 子类分别覆盖 */
    protected List<Encounter> greenEncounters()  { return List.of(); }
    protected List<Encounter> orangeEncounters() { return List.of(); }
    protected List<Encounter> redEncounters()    { return List.of(); }

    /** 合并三色遭遇 */
    protected List<Encounter> encounters() {
        List<Encounter> all = new ArrayList<>();
        all.addAll(greenEncounters());
        all.addAll(orangeEncounters());
        all.addAll(redEncounters());
        return all;
    }
}
