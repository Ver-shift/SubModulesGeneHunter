package org.galaxy.beyond.api.client.gui.scene;

import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Transform2D;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.IGUIContext;
import com.lowdragmc.lowdraglib2.gui.ui.style.PropertyRegistry;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelperClient;
import com.lowdragmc.lowdraglib2.math.interpolate.Eases;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.ProgressType;
import org.galaxy.beyond.api.system.rogue.SceneType;

import java.util.ArrayList;
import java.util.List;

public class SceneLine extends UIElement{


    private final ProgressType progressType;
    private final List<SceneEntry> sceneEntries;

    public SceneLine(ProgressType progressType) {
        this.progressType = progressType;

        this.layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.flexDirection(FlexDirection.COLUMN);
        });

        sceneEntries = new ArrayList<>();
        for (SceneType sceneType : progressType.getScenes()) {
            var scene = new SceneEntry(sceneType,sceneEntries.size(),progressType);
            scene.setId(sceneType.name());
            sceneEntries.add(scene);
            this.addChild(scene);
        }
        Beyond.debugInfo("已经加载了" + sceneEntries.size()+ "节点");
    }

    @Override
    protected void drawBackgroundAdditional(IGUIContext context) {
        //在中间渲染一个半透明的薄膜
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
                layout.paddingAll(2);
            });
            this.style(style -> {
                style.background(Sprites.BORDER);
            });

        }


        @Override
        protected void drawBackgroundAdditional(IGUIContext context) {
            if (!(context instanceof GUIContext gui)) {
                super.drawBackgroundAdditional(context);
                return;
            }

            if (scene == null) {
                super.drawBackgroundAdditional(context);
                return;
            }

            ItemStack item = itemFor(scene);
            if (!item.isEmpty()) {
                DrawerHelperClient.drawItemStack(gui, item,(int) getContentX(),(int) getContentY(), 0);
            }
            if (progressType.getScenesIndex() == index) {
                context.drawTexture(new ColorRectTexture(0x80FFFFFF), (int) getContentX(), (int) getContentY(), getContentWidth(), getContentHeight());
            }
            super.drawBackgroundAdditional(context);
        }

        private static ItemStack itemFor(SceneType scene) {
            return switch (scene) {
                case HARVEST -> new ItemStack(Items.IRON_INGOT);
                case REPOSE -> new ItemStack(Items.CAMPFIRE);
                case CLIMAX -> new ItemStack(Items.DRAGON_HEAD);
                case EMPTY -> ItemStack.EMPTY;
            };
        }
    }
}
