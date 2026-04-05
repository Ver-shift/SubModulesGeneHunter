package org.biotech.ui.gene_inventroy.element.merge;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import com.mojang.blaze3d.systems.RenderSystem;
import org.biotech.api.BiotechAPI;
import org.biotech.network.MergePacket;
import org.biotech.ui.BiotechTexture;
import org.biotech.ui.IScalable;
import org.biotech.ui.animation.IBaseAnimation;
import org.biotech.ui.animation.ShowyAnimation;
import org.biotech.util.TodoDebugLog;
import org.lwjgl.opengl.GL11;


/**
 * 外部有包装，
 */
public class MergeOutputSlot extends UIElement implements IScalable {

    public static final int baseWidth = 54;
    public static final int baseHeight = 41;

    public static final int slotWidth = 24;
    public static final int slotHeight = 25;

    // 背景纹理偏移量
    public static final int backgroundOffsetX = -8;
    public static final int backgroundOffsetY = -3;

    private float currentScale = 1.0f;

    public static final int baseFontSize = 5;
    // 浅绿色文本颜色 #94af60
    private static final int TEXT_COLOR = 0x94af60;

    public static final IGuiTexture backGround = SpriteTexture
            .of(BiotechTexture.GUI_TEXTURE)
            .setSprite(109,0,43,35);

    public static final IGuiTexture slotOverlay = SpriteTexture
            .of(BiotechTexture.GUI_TEXTURE)
            .setSprite(70,154,24,25);
    public static final IGuiTexture hoverOverlay = SpriteTexture
            .of(BiotechTexture.GUI_TEXTURE)
            .setSprite(0,180,24,25);


    private int lableYOff = 3;

    // 背景浮动动画
    private IBaseAnimation backgroundAnimation;
    // 槽位浮动动画（参考 EquipGeneSlot）
    private IBaseAnimation slotAnimation;

    private ItemSlot outputSlot;
    private Label outputLabel; //用于输出能够进行的输出
    private final Player player;

    public MergeOutputSlot(Player player){
        this.player = player;
        // 初始化背景动画
        this.backgroundAnimation = new ShowyAnimation(0.5f, 0.005f, 0.025f);
        this.slotAnimation = new ShowyAnimation(0.4f, 0.004f, 0.02f);

        this.layout(layoutStyle -> {
            layoutStyle.width(baseWidth);
            layoutStyle.height(baseHeight);
            layoutStyle.flexDirection(FlexDirection.COLUMN);
            // 使用 Flexbox 自动布局让子元素居中
            layoutStyle.alignItems(AlignItems.CENTER);      // 交叉轴居中
            layoutStyle.justifyContent(AlignContent.CENTER); // 主轴居中
        });

        // TICK事件驱动动画更新，并刷新可输出数量文本。
        this.addEventListener(UIEvents.TICK, event -> {
            backgroundAnimation.tick();
            slotAnimation.tick();
            refreshOutputNumber();
        });

        this.outputSlot = new ItemSlot(){
            @Override
            public void drawBackgroundTexture(GUIContext guiContext) {

            }

            @Override
            public void drawBackgroundAdditional(GUIContext guiContext) {
                if (getSlot().hasItem()) {
                    // 有产物预览时仅渲染物品本身，不渲染任何背景/悬停底图。
                    super.drawBackgroundAdditional(guiContext);
                    return;
                }

                // 使用 content 区域坐标（相对于屏幕的绝对坐标，已排除 padding）
                float x = outputSlot.getContentX();
                float y = outputSlot.getContentY();

                guiContext.pose.pushPose();
                guiContext.pose.translate(x, y, 0);
                guiContext.pose.scale(currentScale, currentScale, 1);
                // 应用浮动动画
                backgroundAnimation.animation(guiContext, currentScale);
                guiContext.drawTexture(backGround, backgroundOffsetX, backgroundOffsetY, 43, 35);
                guiContext.pose.popPose();
                super.drawBackgroundAdditional(guiContext);
            }

            @Override
            protected void drawSlotOverlay(GUIContext guiContext) {
                if (getSlot().hasItem()) {
                    return;
                }
                guiContext.pose.pushPose();
                slotAnimation.animation(guiContext, currentScale);
                backgroundAnimation.animation(guiContext, currentScale);
                super.drawSlotOverlay(guiContext);
                guiContext.pose.popPose();
            }

            @Override
            protected void drawHover(GUIContext guiContext) {
                if (getSlot().hasItem()) {
                    return;
                }
                guiContext.pose.pushPose();
                slotAnimation.animation(guiContext, currentScale);
                backgroundAnimation.animation(guiContext, currentScale);
                super.drawHover(guiContext);
                guiContext.pose.popPose();
            }

            @Override
            protected void drawItemStack(GUIContext guiContext, net.minecraft.world.item.ItemStack itemStack) {
                if (itemStack.isEmpty()) {
                    return;
                }
                guiContext.pose.pushPose();
                slotAnimation.animation(guiContext, currentScale);

                // Ensure item icon is rendered above UI layers.
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
                guiContext.pose.translate(0, 0, 232);
                guiContext.graphics.renderItem(itemStack, 0, 0);
                // Keep depth state consistent with GUI rendering pipeline.
                RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
                RenderSystem.depthMask(false);
                RenderSystem.disableDepthTest();
                guiContext.pose.popPose();
            }
            
            @Override
            protected void onMouseDown(UIEvent event) {
                // ItemSlot 默认会把 mouseDown 标记为无处理并交给容器点击；
                // 输出槽改为自定义处理：直接触发 merge。
                event.stopPropagation();
                event.hasHandler = true;

                // 客户端只发请求，服务端执行真正的 merge 逻辑。
                var mergeData = BiotechAPI.getGeneData(player).getMergeData();
                TodoDebugLog.info("merge_output_click", () -> "send MergePacket with cachedOutput="
                        + mergeData.getCachedOutputXeneCount() + ", traits=" + mergeData.getCachedTraitCount()); // TODO: remove after debug
                PacketDistributor.sendToServer(new MergePacket());
            }
        };
        outputSlot.layout(layoutStyle -> {
            layoutStyle.width(slotWidth);
            layoutStyle.height(slotHeight);
            layoutStyle.paddingAll(0);
        });
        outputSlot.slotStyle(style->{
            style.slotOverlay(slotOverlay);
            style.hoverOverlay(hoverOverlay);
            style.acceptQuickMove(false);
            style.isPlayerSlot(false);
            style.quickMovePriority(-100);
        });
        outputSlot.bind(BiotechAPI.getGeneData(player).getMergeData().getOutputSlots(), 0);
        this.addChild(outputSlot);

        this.outputLabel = new Label();
        outputLabel.textStyle(style->{
            style.fontSize(baseFontSize);
            style.textColor(TEXT_COLOR);
            style.textAlignHorizontal(Horizontal.CENTER); // 水平居中
            style.textAlignVertical(Vertical.CENTER);     // 垂直居中
        });
        outputLabel.layout(layoutStyle -> {
            layoutStyle.widthPercent(10f);
            layoutStyle.heightPercent(5f);
            layoutStyle.paddingTop(lableYOff);
        });
        setNumber(0); // 初始化显示为0
        this.addChild(outputLabel);
        refreshOutputNumber();
    }

    /**
     * 设置显示的数字
     * @param number 要显示的数字
     */
    public void setNumber(int number) {
        outputLabel.setValue(Component.literal(String.valueOf(number)));
    }

    private void refreshOutputNumber() {
        var mergeData = BiotechAPI.getGeneData(player).getMergeData();
        setNumber(mergeData.getCachedOutputXeneCount());
    }

    @Override
    public void drawBackgroundAdditional(GUIContext guiContext) {

    }

    @Override
    public void scale(float scale) {
        this.currentScale = scale;
        this.layout(layoutStyle -> {
            layoutStyle.width(baseWidth * scale);
            layoutStyle.height(baseHeight * scale);
        });
        outputSlot.layout(layoutStyle -> {
            layoutStyle.width(slotWidth * scale);
            layoutStyle.height(slotHeight * scale);
        });
        outputLabel.textStyle(style -> {
            style.fontSize((int)(baseFontSize * scale));
        });
        outputLabel.layout(layoutStyle -> {
            layoutStyle.paddingTop(lableYOff * scale);
        });
    }
}
