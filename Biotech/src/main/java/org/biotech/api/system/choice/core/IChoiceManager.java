package org.biotech.api.system.choice.core;

import net.minecraft.world.entity.player.Player;

public interface IChoiceManager {


    /**
     * 打开基因选择界面
     */
    boolean openChoiceMenu();

    int getChoiceCount();

    void setChoiceCount(int choice);

    /**
     * 进行抽取
     */
    void doRoll();
}
