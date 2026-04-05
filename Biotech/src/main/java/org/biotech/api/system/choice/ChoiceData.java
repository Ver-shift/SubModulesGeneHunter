package org.biotech.api.system.choice;

import lombok.Data;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChoiceData {

    /**
     * 用来进行暂时性的存储，用来显示存储信息
     */
    private List<ItemStack> choiceHolder = new ArrayList<>();



}
