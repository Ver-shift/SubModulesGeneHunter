package org.galaxy.beyond.api.client.gui.scene;

import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Transform2D;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.ui.style.PropertyRegistry;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.lowdragmc.lowdraglib2.math.interpolate.Eases;
import com.lowdragmc.lowdraglib2.syncdata.ISubscription;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.client.Minecraft;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.ProgressType;
import org.galaxy.beyond.api.system.rogue.SceneType;

import java.util.ArrayList;
import java.util.List;

public class SceneLine extends UIElement{


    private ProgressType progressType;
    private List<SceneEntry> sceneEntries;
    private List<SceneType> lastScenes = List.of();
    private int lastScenesIndex = -1;
    private float currentOffsetY;
    private ISubscription scrollAnimation = () -> {};

    public SceneLine(ProgressType progressType) {
        this.progressType = progressType;

        this.layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.flexDirection(FlexDirection.COLUMN);

        });
        sceneEntries = new ArrayList<>();
        rebuildEntries(progressType);
        Beyond.debugInfo("已经加载了" + sceneEntries.size()+ "节点");
        addEventListener(UIEvents.TICK, event -> updateScrollPosition());
    }

    private void rebuildEntries(ProgressType progressType) {
        for (SceneEntry entry : sceneEntries) {
            removeChild(entry);
        }
        sceneEntries.clear();
        for (SceneType sceneType : progressType.getScenes()) {
            var scene = new SceneEntry(sceneType,sceneEntries.size(),progressType);
            scene.setId(sceneType.name());
            sceneEntries.add(scene);
            this.addChild(scene);
        }
        lastScenes = List.copyOf(progressType.getScenes());
    }

    public void drawBackgroundAdditional(GUIContext context) {
        //在中间渲染一个半透明的薄膜
    }

    private void updateScrollPosition() {
        refreshProgressType();
        if (sceneEntries.isEmpty()) {
            return;
        }

        float entryHeight = sceneEntries.getFirst().getSizeHeight();
        if (entryHeight <= 0) {
            return;
        }

        int scenesIndex = clampedScenesIndex();
        float targetOffsetY = -scenesIndex * entryHeight;
        if (lastScenesIndex < 0) {
            applyScrollOffset(targetOffsetY);
        } else if (scenesIndex != lastScenesIndex) {
            animateScrollOffset(targetOffsetY);
        } else if (Float.compare(targetOffsetY, currentOffsetY) != 0) {
            applyScrollOffset(targetOffsetY);
        }
        lastScenesIndex = scenesIndex;
    }

    private void refreshProgressType() {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        ProgressType current = BeyondAPI.getProgressType(level);
        if (current == progressType && current.getScenes().equals(lastScenes)) return;

        progressType = current;
        lastScenesIndex = -1;
        rebuildEntries(progressType);
    }

    private int clampedScenesIndex() {
        int lastIndex = sceneEntries.size() - 1;
        return Math.clamp(progressType.getScenesIndex(), 0, lastIndex);
    }

    private void applyScrollOffset(float offsetY) {
        scrollAnimation.unsubscribe();
        currentOffsetY = offsetY;
        style(style -> style.transform2D(new Transform2D().translate(0, offsetY)));
    }

    private void animateScrollOffset(float offsetY) {
        scrollAnimation.unsubscribe();
        currentOffsetY = offsetY;
        animation(anim -> scrollAnimation = anim.duration(0.35f)
                .ease(Eases.QUAD_IN_OUT)
                .style(PropertyRegistry.TRANSFORM_2D, new Transform2D().translate(0, offsetY))
                .start());
    }

    private static class SceneEntry extends UIElement {
        private final SceneType scene;
        private final int index;
        private final ProgressType progressType;

        private SceneEntry(SceneType scene,int index,ProgressType progressType) {
            this.scene = scene;
            this.index = index;
            this.progressType = progressType;

            this.layout(layout -> {
                layout.widthPercent(100);
                layout.heightPercent(20);
                layout.paddingAll(3);
            });
            this.style(style -> {
                style.background(Sprites.BORDER);
            });

        }


        public void drawBackgroundAdditional(GUIContext context) {
            if (scene == null) {
                return;
            }

            if (progressType.getScenesIndex() == index) {
                context.drawTexture(new ColorRectTexture(0x80FFFFFF), (int) getContentX(), (int) getContentY(), getContentWidth(), getContentHeight());
            }
        }
    }
}
