package org.biotech.ui.gene_inventroy.group;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.biotech.api.BiotechAPI;
import org.biotech.api.system.gene.GeneInstance;
import org.biotech.api.init.DataComponentInit;
import org.biotech.api.system.trait.core.ITrait;
import org.biotech.component.TraitComp;
import org.biotech.ui.BiotechTexture;
import org.biotech.ui.IScalable;
import org.biotech.ui.gene_inventroy.element.Info;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class InfoGroup extends UIElement implements IScalable {

    public UIElement empty;

    public UIElement container;
        public UIElement imageContainer;
            public UIElement imageEmpty;
            public UIElement topImage;
        public Info middleText;
        public UIElement bottomImage;

    public static final int baseEmptyWidth = 34;


    public static final int baseImageHeight = 7;
    public static final int baseImageWidth = 163;

    private float scale;

    private float imageEmptyWidth = 10;

    public static final float angle = 28.44f; // 计算bottom 和top 的paddingLeft 差距

    private Player player;
    private IDynamicStackHandler geneStackHandler;
    private List<String> cachedInfoSnapshot = List.of();

    public InfoGroup(Player player) {
        super();
        initData(player);

        this.layout(layout -> {

            layout.flexDirection(FlexDirection.ROW);
        });
            this.empty = new UIElement();
            empty.setId("info_empty");
            empty.layout(layout -> {
                layout.width(baseEmptyWidth);
                layout.heightPercent(100f);
            });
            this.addChild(empty);

            this.container = new UIElement();
            container.setId("container");
            container.layout(layout -> {
                layout.flexDirection(FlexDirection.COLUMN);
            });
            this.addChild(container);

                this.imageContainer = new UIElement();
                imageContainer.setId("image_container");
                imageContainer.layout(layout -> {
                    layout.flexDirection(FlexDirection.ROW);

                });
                container.addChild(imageContainer);

                    this.imageEmpty = new UIElement();
                    imageEmpty.setId("image_empty");
                    imageEmpty.layout(layout -> {
                        layout.width(imageEmptyWidth);
                        layout.height(baseImageHeight);
                    });
                    imageContainer.addChild(imageEmpty);

                    this.topImage = new UIElement();
                    topImage.setId("top_image");
                    topImage.layout(layout -> {
                        layout.height(baseImageHeight);
                        layout.width(baseImageWidth);
                    });
                    topImage.style(style -> {
                        style.backgroundTexture(BiotechTexture.Button_Slice);
                    });
                    imageContainer.addChild(topImage);

                this.middleText = new Info();
                middleText.setId("middle_text");
                container.addChild(middleText);

                this.bottomImage = new UIElement();
                bottomImage.setId("bottom_image");
                bottomImage.layout(layout -> {
                    layout.height(baseImageHeight);
                    layout.width(baseImageWidth);
                });
                bottomImage.style(style -> {
                    style.backgroundTexture(BiotechTexture.Button_Slice);
                });
                container.addChild(bottomImage);

            this.addEventListener(UIEvents.LAYOUT_CHANGED, event -> {
                calculatePaddingLeft(angle);
                // 缩放 imageEmpty
                imageEmpty.layout(layout -> {
                    layout.width(imageEmptyWidth);
                    layout.height(baseImageHeight * scale);
                });
                middleText.setSkewAngle(angle);
                middleText.setMaxSkewPadding(imageEmptyWidth);
            });

            // 实时读取 gene/xene 槽位组件并刷新到 Info 文本。
            this.addEventListener(UIEvents.TICK, event -> refreshInfoText());
    }

    private void initData(Player player) {
        this.player = player;
        this.geneStackHandler = BiotechAPI.getGeneEquipSlots(player);
        refreshInfoText();
    }

    private void refreshInfoText() {
        if (player == null || middleText == null) {
            return;
        }




        if (geneStackHandler == null) {
            geneStackHandler = BiotechAPI.getGeneEquipSlots(player);
        }

        List<MutableComponent> lines = buildSummedTraitInfo(geneStackHandler);

        // 内容不变时不重建，避免每 tick 重置滚动进度。
        List<String> snapshot = lines.stream().map(Component::getString).collect(Collectors.toList());
        if (Objects.equals(snapshot, cachedInfoSnapshot)) {
            return;
        }
        cachedInfoSnapshot = snapshot;

        middleText.setSkewAngle(angle);
        middleText.setMaxSkewPadding(imageEmptyWidth);
        middleText.updateInfo(lines);
    }

    private static List<MutableComponent> buildSummedTraitInfo(IDynamicStackHandler slots) {
        List<MutableComponent> lines = new ArrayList<>();
        if (slots == null) {
            return lines;
        }

        Map<String, Integer> countByTrait = new LinkedHashMap<>();
        Map<String, List<MutableComponent>> infoByTrait = new LinkedHashMap<>();

        for (int i = 0; i < slots.getSlots(); i++) {
            ItemStack stack = slots.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }

            GeneInstance geneInstance = stack.get(DataComponentInit.GENE_INSTANCE.get());
            if (geneInstance != null) {
                TraitComp geneComp = geneInstance.getComponents().get(DataComponentInit.TRAIT_COMP.get());
                collectTraitComp(geneComp, countByTrait, infoByTrait);
            }

            // 兼容可能直接挂在物品上的词条组件。
            TraitComp directComp = stack.get(DataComponentInit.TRAIT_COMP.get());
            collectTraitComp(directComp, countByTrait, infoByTrait);
        }

        for (Map.Entry<String, Integer> entry : countByTrait.entrySet()) {
            String traitKey = entry.getKey();
            int count = entry.getValue();
            List<MutableComponent> traitLines = infoByTrait.getOrDefault(traitKey, List.of());

            if (traitLines.isEmpty()) {
                continue;
            }

            for (MutableComponent line : traitLines) {
                MutableComponent out = line.copy();
                if (count > 1) {
                    out.append(Component.literal(" x" + count));
                }
                lines.add(out);
            }
        }

        return lines;
    }

    private static void collectTraitComp(
            TraitComp traitComp,
            Map<String, Integer> countByTrait,
            Map<String, List<MutableComponent>> infoByTrait
    ) {
        if (traitComp == null || traitComp.isEmpty()) {
            return;
        }

        for (ITrait trait : traitComp.getTraits()) {
            if (trait == null) {
                continue;
            }
            String key = trait.getId() != null ? trait.getId().toString() : trait.getDisplayName().getString();
            countByTrait.put(key, countByTrait.getOrDefault(key, 0) + 1);

            if (infoByTrait.containsKey(key)) {
                continue;
            }

            List<MutableComponent> uniqueInfo = trait.getUniqueInfo();
            List<MutableComponent> lines = new ArrayList<>();
            if (uniqueInfo != null) {
                for (MutableComponent info : uniqueInfo) {
                    if (info != null) {
                        lines.add(info.copy());
                    }
                }
            }

            if (lines.isEmpty()) {
                lines.add(trait.getDisplayName().copy());
            }
            infoByTrait.put(key, lines);
        }
    }


    /**
     * 根据 angle 计算 paddingLeft 并应用
     * 公式：|topY - bottomY| / tan(angle)
     * @param angle 角度（度）
     * @return paddingLeft 值
     */
    public float calculatePaddingLeft(float angle) {
        // 获取 top 和 bottom 的 Y 位置
        float topY = topImage.getPositionY();
        float bottomY = bottomImage.getPositionY();

        // 将角度转换为弧度
        double angleRadians = Math.toRadians(angle);

        // 计算 paddingLeft（取绝对值）
        float paddingLeft = Math.abs((float) ((topY - bottomY) * Math.tan(angleRadians)));

        // 应用到 imageEmptyWidth
        this.imageEmptyWidth = paddingLeft;

        return paddingLeft;
    }

    @Override
    public void scale(float scale) {
        // 缩放 empty
        empty.layout(layout -> {
            layout.width(baseEmptyWidth * scale);
        });

        this.scale = scale;


        // 缩放 topImage
        topImage.layout(layout -> {
            layout.height(baseImageHeight * scale);
            layout.width(baseImageWidth * scale);
        });

        // 缩放 middleText
        middleText.scale(scale);
        middleText.setSkewAngle(angle);
        middleText.setMaxSkewPadding(imageEmptyWidth);

        // 缩放 bottomImage
        bottomImage.layout(layout -> {
            layout.height(baseImageHeight * scale);
            layout.width(baseImageWidth * scale);
        });
    }
}
