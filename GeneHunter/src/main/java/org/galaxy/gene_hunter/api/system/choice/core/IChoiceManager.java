package org.galaxy.gene_hunter.api.system.choice.core;


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
    void doXeneRoll();
    void doWeaponRoll();


    /**
     * 对当前的holderData进行刷新
     */
    void refresh();


}
