package org.galaxy.beyond.api.system.rogue.extension;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.core.IRogueExtension;

/**
 * 装饰器模式示例：在 startRogue 启动前后插入日志与数据初始化逻辑。
 *
 * <p>使用方式：
 * <pre>
 *   RogueManager manager = new RogueManager();
 *   manager.addExtension(new ExampleRogueExtension());
 * </pre>
 */
public class ExampleRogueExtension implements IRogueExtension {


    @Override
    public void onStartRogue(ServerLevel level) {
        beforeStart(level);


        afterStart(level);
    }

    private void beforeStart(ServerLevel level) {
        // TODO: 启动前逻辑：例如校验玩家状态、预生成地形等
    }

    private void afterStart(ServerLevel level) {
        // TODO: 启动后逻辑：例如广播标题、初始化计分板等
    }
}
