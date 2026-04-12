package com.pz.beyond.api.system.progress;

import lombok.Data;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedList;

/**
 * 进度，
 */
@Data
public class Progress {

    /**
     * 一局优秀的类型。
     */
    private ProgressType identifier;
    /**
     * 多个进度节点。
     */
    private LinkedList<Scene> scenes = new LinkedList<>();
    private Scene currentScene;


    public int getNodesCount() {
        return scenes.size();
    }

}
