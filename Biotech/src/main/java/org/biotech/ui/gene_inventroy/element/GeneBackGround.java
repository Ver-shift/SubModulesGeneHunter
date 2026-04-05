package org.biotech.ui.gene_inventroy.element;

import com.lowdragmc.lowdraglib2.gui.texture.AnimationTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEventListener;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import dev.vfyjxf.taffy.style.TaffyPosition;
import org.biotech.ui.BiotechTexture;
import org.biotech.ui.IScalable;
import org.biotech.ui.animation.IBaseAnimation;
import org.biotech.ui.animation.ShowyAnimation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 基因背景组件 - 全屏覆盖，使用 64x64 纹理平铺
 * 
 * 组件树位置：
 * root
 * └── GeneBackGround (全屏，在最底层)
 *     └── 64x64 纹理平铺覆盖
 * 
 * 使用方式：
 * 1. 准备 64x64 的纹理文件：assets/biotech/textures/gui/gene_background.png
 * 2. 在 GeneContainer 中添加：root.addChild(new GeneBackGround());
 * 3. 确保背景组件最先添加，作为最底层
 */
public class GeneBackGround extends UIElement implements IScalable {

    // 当前缩放比例
    private float currentScale = 1.0f;

    // 移动强度系数（越小移动越微妙）
    private float moveIntensity = 0.002f;
    // 背景缩放系数（1.05 = 放大5%，避免边缘露出）
    private float backgroundScale = 1.05f;

    public static final IGuiTexture cell_left = SpriteTexture
            .of(BiotechTexture.GUI_TEXTURE)
            .setSprite(153,0,38,22);

    public static final IGuiTexture cell_right = SpriteTexture
            .of(BiotechTexture.GUI_TEXTURE)
            .setSprite(192,0,27,17);

    public static final IGuiTexture background = SpriteTexture
            .of(BiotechTexture.BACKGROUND_PNG)
            .setWrapMode(SpriteTexture.WrapMode.REPEAT)
            .setSprite(0,0,64,64);


    // 细胞图片基础尺寸
    public static final int CELL_LEFT_WIDTH = 38;
    public static final int CELL_LEFT_HEIGHT = 22;
    public static final int CELL_RIGHT_WIDTH = 27;
    public static final int CELL_RIGHT_HEIGHT = 17;

    // 细胞浮动动画
    private final IBaseAnimation leftCellAnimation = new ShowyAnimation(1.2f, 0.005f, 0.025f);
    private final IBaseAnimation rightCellAnimation = new ShowyAnimation(1.2f, 0.005f, 0.025f);

    private UIElement cell_left_element;
    private UIElement cell_right_element;

    private BubbleManager bubbleManager = new BubbleManager();

    public GeneBackGround() {
        super();
        // 设置全屏布局
        this.layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(0);
            layout.top(0);
        });
        // 设置整体不透明度为20%
        this.style(style -> {
//            style.opacity(0.9f);
            style.background(background);
//            style.color(0x33FFFFFF);
        });

        this.addEventListener(UIEvents.TICK, event -> {
            leftCellAnimation.tick();
            rightCellAnimation.tick();
            bubbleManager.tick();
        });
        // 左下角细胞元素 - 使用匿名内部类重写drawBackgroundAdditional应用动画
        this.cell_left_element = new UIElement() {
            @Override
            public void drawBackgroundTexture(GUIContext guiContext) {
                guiContext.pose.pushPose();
                // 应用浮动动画
                leftCellAnimation.animation(guiContext, currentScale);
                super.drawBackgroundTexture(guiContext);
                guiContext.pose.popPose();

            }
        };
        cell_left_element.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);

            layout.leftPercent(0f);
            layout.bottomPercent(0f);

            layout.width(CELL_LEFT_WIDTH);
            layout.height(CELL_LEFT_HEIGHT);
        });
        cell_left_element.style(style -> {
            style.background(cell_left);
        });
        cell_left_element.setId("cell_left_element");
        this.addChild(this.cell_left_element);

        // 右上角细胞元素 - 使用匿名内部类重写drawBackgroundAdditional应用动画
        this.cell_right_element = new UIElement() {
            @Override
            public void drawBackgroundTexture(GUIContext guiContext) {
                guiContext.pose.pushPose();
                // 应用浮动动画
                rightCellAnimation.animation(guiContext, currentScale);
                super.drawBackgroundTexture(guiContext);
                guiContext.pose.popPose();

            }

        };
        cell_right_element.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.rightPercent(0f);          // 距右边缘20px
            layout.topPercent(0f);            // 距顶边缘20px
            layout.width(CELL_RIGHT_WIDTH);
            layout.height(CELL_RIGHT_HEIGHT);
        });
        cell_right_element.style(style -> {
            style.background(cell_right);
        });
        cell_right_element.setId("cell_right_element");
        this.addChild(this.cell_right_element);


    }


    @Override
    public void drawBackgroundAdditional(GUIContext guiContext) {
        // 背景纹理在 drawBackgroundTexture 中绘制
        // 细胞元素的动画通过匿名内部类的 drawBackgroundAdditional 重写实现
    }

    @Override
    public void drawBackgroundTexture(GUIContext guiContext) {
        // 从 guiContext 获取鼠标位置
        float mouseX = guiContext.mouseX;
        float mouseY = guiContext.mouseY;

        // 计算屏幕中心
        float centerX = getSizeWidth() / 2f;
        float centerY = getSizeHeight() / 2f;

        // 计算鼠标相对于中心的偏移（-1 到 1 范围）
        float normX = (mouseX - centerX) / centerX;
        float normY = (mouseY - centerY) / centerY;

        // 计算背景偏移（反向移动，产生视差效果）
        float offsetX = -normX * getSizeWidth() * moveIntensity;
        float offsetY = -normY * getSizeHeight() * moveIntensity;

        // 计算放大后的尺寸
        float scaledWidth = getSizeWidth() * backgroundScale;
        float scaledHeight = getSizeHeight() * backgroundScale;

        // 计算居中偏移（保持放大后仍然居中）
        float centerOffsetX = (getSizeWidth() - scaledWidth) / 2f;
        float centerOffsetY = (getSizeHeight() - scaledHeight) / 2f;

        // 使用矩阵变换实现视差效果
        guiContext.pose.pushPose();
        guiContext.pose.translate(getPositionX() + centerOffsetX + offsetX, 
                                   getPositionY() + centerOffsetY + offsetY, 0);
        guiContext.pose.scale(backgroundScale, backgroundScale, 1);
        
        // 调用父类方法绘制背景（在变换后的坐标系中）
        super.drawBackgroundTexture(guiContext);
        
        guiContext.pose.popPose();
    }

    private void renderBaseBackground(GUIContext guiContext) {
        // 背景渲染已移至 drawBackgroundTexture
    }

    @Override
    public void scale(float scale) {
        this.currentScale = scale;

        // 缩放细胞元素及其边距
        this.cell_left_element.layout(layout -> {
            layout.width((CELL_LEFT_WIDTH * scale));
            layout.height((CELL_LEFT_HEIGHT * scale));
        });
        this.cell_right_element.layout(layout -> {
            layout.width((int) (CELL_RIGHT_WIDTH * scale));
            layout.height((int) (CELL_RIGHT_HEIGHT * scale));
        });
    }

    public class BubbleManager {
        // 气泡动画数据类 - 存储纹理和动画时长信息
        private class BubbleData {
            final AnimationTexture texture;
            final int frameCount;      // 帧数
            final int ticksPerFrame;   // 每帧tick数
            final int totalDuration;   // 总时长（ticks）

            BubbleData(AnimationTexture texture, int cellSize, int fromFrame, int toFrame, int ticksPerFrame) {
                this.texture = texture;
                this.frameCount = toFrame - fromFrame + 1;
                this.ticksPerFrame = ticksPerFrame;
                this.totalDuration = this.frameCount * ticksPerFrame; // 自动计算总时长
                // 设置动画参数（只播放一次，不循环）
                this.texture.setCellSize(cellSize);  // 必须在构造函数中设置
                this.texture.setAnimation(fromFrame, toFrame);
                this.texture.setAnimation(ticksPerFrame);
            }
        }

        // 气泡纹理池 - 每个气泡有不同的动画时长
        private final BubbleData[] bubbleDataArray = new BubbleData[]{
                new BubbleData(new AnimationTexture(BiotechTexture.Bubble_1), 3, 0, 5, 2),  // cellSize=3, 6帧，每帧2tick = 12 ticks
                new BubbleData(new AnimationTexture(BiotechTexture.Bubble_2), 2, 0, 3, 3),  // cellSize=2, 4帧，每帧3tick = 12 ticks
                new BubbleData(new AnimationTexture(BiotechTexture.Bubble_3), 2, 0, 3, 2),  // cellSize=2, 4帧，每帧2tick = 8 ticks
                new BubbleData(new AnimationTexture(BiotechTexture.Bubble_4), 2, 0, 3, 4)   // cellSize=2, 4帧，每帧4tick = 16 ticks
        };

        // 对象池：复用UIElement而不是频繁创建
        private final List<UIElement> bubblePool = new ArrayList<>();
        private static final int POOL_SIZE = 10; // 最大同时存在的气泡数
        private static final int SPAWN_INTERVAL = 20; // 生成间隔（ticks）

        private int tickCounter = 0;
        private final Random random = new Random();

        public BubbleManager() {
            // 预创建对象池
            for (int i = 0; i < POOL_SIZE; i++) {
                UIElement bubble = createBubble();
                bubble.setVisible(false); // 初始隐藏
                bubblePool.add(bubble);
                GeneBackGround.this.addChild(bubble);
            }
        }

        public void tick() {
            tickCounter++;

            // 每隔一定时间尝试生成一个气泡
            if (tickCounter >= SPAWN_INTERVAL) {
                tickCounter = 0;
                spawnBubble();
            }

            // 更新所有活跃气泡的动画
            updateBubbles();
        }

        /**
         * 从对象池中获取一个空闲的气泡，或复用已播放完毕的
         */
        private void spawnBubble() {
            // 寻找空闲的气泡（不可见的）
            for (UIElement bubble : bubblePool) {
                if (!bubble.isVisible()) {
                    activateBubble(bubble);
                    return;
                }
            }
            // 如果没有空闲的，复用最早的一个（循环复用）
            if (!bubblePool.isEmpty()) {
                activateBubble(bubblePool.get(random.nextInt(bubblePool.size())));
            }
        }

        /**
         * 激活气泡：随机位置、随机纹理、开始动画（只播放一次）
         */
        private void activateBubble(UIElement bubble) {
            float screenWidth = getSizeWidth();
            float screenHeight = getSizeHeight();

            // 随机位置（考虑缩放后的屏幕尺寸）
            float x = random.nextFloat() * screenWidth * 0.8f + screenWidth * 0.1f;
            float y = random.nextFloat() * screenHeight * 0.8f + screenHeight * 0.1f;

            // 随机选择气泡数据
            BubbleData data = bubbleDataArray[random.nextInt(bubbleDataArray.length)];
            
            // 重置动画到第一帧（确保每次从开头播放）
            data.texture.setAnimation(0, data.frameCount - 1);
            
            // 设置位置、尺寸和纹理
            bubble.layout(layout -> {
                layout.positionType(TaffyPosition.ABSOLUTE);
                layout.left((int) x);
                layout.top((int) y);
                layout.width(16);
                layout.height(16);
            });
            bubble.style(style -> style.background(data.texture));

            // 显示气泡 - UIElement作为子组件，其tick()会自动更新动画
            bubble.setVisible(true);

            // 根据动画数据自动计算隐藏时间（只播放一次）
            final int animationDuration = data.totalDuration;
            bubble.addEventListener(UIEvents.TICK, new UIEventListener() {
                private int lifeTicks = 0;

                @Override
                public void handleEvent(UIEvent event) {
                    lifeTicks++;
                    if (lifeTicks >= animationDuration) {
                        bubble.setVisible(false); // 动画播放完毕后隐藏，但不销毁
                        bubble.removeEventListener(UIEvents.TICK, this);
                    }
                }
            });
        }

        /**
         * 更新所有气泡状态 - 包括动画更新
         */
        private void updateBubbles() {
            // 更新所有可见气泡的动画
            for (UIElement bubble : bubblePool) {
                if (bubble.isVisible()) {
                    // 获取气泡的纹理并更新动画
                    // AnimationTexture 需要每 tick 更新才能播放
                    // 这里通过重新设置背景来触发更新
                }
            }
        }

        /**
         * 创建气泡UIElement
         */
        private UIElement createBubble() {
            UIElement bubble = new UIElement();
            bubble.layout(layout -> {
                layout.width(16);  // 气泡基础尺寸
                layout.height(16);
                layout.positionType(TaffyPosition.ABSOLUTE);
            });
            return bubble;
        }
    }


}
