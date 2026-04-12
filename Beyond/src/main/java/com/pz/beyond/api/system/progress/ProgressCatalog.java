package com.pz.beyond.api.system.progress;

import lombok.Data;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

/**
 * 进度目录，玩家通过gui 选择当前的进度，获得相应的体验。
 */
@Data
public class ProgressCatalog {

    private HashMap<ResourceLocation,Progress> progress= new HashMap<>();
    private Progress currentProgress;
}
