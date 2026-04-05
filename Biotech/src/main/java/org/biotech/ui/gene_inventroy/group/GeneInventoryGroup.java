package org.biotech.ui.gene_inventroy.group;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.biotech.api.BiotechAPI;
import org.biotech.api.system.gene.inventory.PlayerGeneInventoryData;
import org.biotech.api.system.trait.core.IStackTraitAccess;
import org.biotech.item.GeneItem;
import org.biotech.ui.IScalable;
import org.biotech.ui.gene_inventroy.element.inventory.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 基因槽位组（支持翻页）
 * 包含 n 行 GeneInventoryLine（每行 27 个槽位）
 * 采用纵向自动扩充布局
 *
 * 组件树：
 * GeneInventoryGroup  (COLUMN 堆叠)
 * ├── slotsContainer  (槽位容器，通过 left 偏移实现翻页)
 * │   ├── GeneInventoryLine (row 0)
 * │   ├── GeneInventoryLine (row 1)
 * │   └── GeneInventoryLine (row 2)
 * └── pageControlsContainer (翻页控件容器，X 位置对齐露出区域中心)
 *     ├── prevButton
 *     ├── pageText
 *     └── nextButton
 */
@Getter
@Setter
public class GeneInventoryGroup extends UIElement implements IScalable {
    /**
     * 管理槽位
     */
    private final UIElement inventorySlotContainer;
        private final List<PageSlotContainer> slotElements = new ArrayList<>();
    /** 
     * 功能区
     */
    private final UIElement zoneContainer;
        private FavoritesButton favoritesButton;
        private SwitchPage leftPage;
        private PageText pageText;
        private SwitchPage rightPage;



    public static final int baseWidth = 196;
    public static final int baseHeight = 96;

    public static final int baseInventoryHeight = 74;
    public static final int baseZonePaddingLeft = 30;

    // 布局常量 - 引用 PlayerGeneInventoryData 保持一致
    public static final int ROWS_PER_PAGE = PlayerGeneInventoryData.ROWS_PER_PAGE;
    public static final int SLOTS_PER_ROW = PlayerGeneInventoryData.SLOTS_PER_ROW;
    public static final int SLOTS_PER_PAGE = PlayerGeneInventoryData.SLOTS_PER_PAGE;
    public static final int TOTAL_PAGES = PlayerGeneInventoryData.TOTAL_PAGES;

    private static final float ROMAN_TEXT_SCALE = 0.62f;

    private float currentScale = 1.0f;

    private Player player;
    private ItemStackHandler geneSlots;
    private ItemStackHandler favoritesSlots;

    // 页面管理
    private PageManager pageManager;
    private boolean isFavoritesMode = false;

    // 阶梯偏移配置
    private final float stepOffsetBase;
    private final float stepOffsetMaxLine;

    public GeneInventoryGroup(Player player) {
        this(player, 13f, ROWS_PER_PAGE - 1);
    }

    public GeneInventoryGroup(Player player, float stepOffsetBase, float stepOffsetMaxLine) {
        super();
        this.stepOffsetBase = stepOffsetBase;
        this.stepOffsetMaxLine = stepOffsetMaxLine;
        initPlayerData(player);

        this.setId("gene_inventory_group");
        this.layout(layout -> {
            layout.flexDirection(FlexDirection.COLUMN);
            layout.width(baseWidth);
            layout.height(baseHeight);
        });

            this.inventorySlotContainer = new UIElement();
            inventorySlotContainer.setId("gene_inventory_slot_container");
            inventorySlotContainer.layout(layout -> {
                layout.widthPercent(100f);
                layout.height(baseInventoryHeight);
            });
            this.addChild(inventorySlotContainer);
                initInventorySlotContainer();

            this.zoneContainer = new UIElement();
            zoneContainer.setId("gene_inventory_zone_container");
            zoneContainer.layout(layout -> {
                layout.flexDirection(FlexDirection.ROW);
                layout.widthPercent(100f);
                layout.heightPercent(23f);
                layout.paddingLeft(baseZonePaddingLeft);
            });
            this.addChild(zoneContainer);

        // 初始化页面管理器（必须在 initZoneContainer 之前）
        this.pageManager = new PageManager();

        initZoneContainer();

        // 初始化页面显示（显示第一页，隐藏其他页）
        pageManager.initDisplay();
    }
    private void initPlayerData(Player player) {
        this.player = player;
        var inventoryData = BiotechAPI.getGeneData(player).getPlayerGeneInventoryData();
        this.geneSlots = inventoryData.getGeneSlots();
        this.favoritesSlots = inventoryData.getFavoriteSlots();
    }

    private void initInventorySlotContainer() {
        // 创建所有页的普通槽位行（只显示第一页）
        createAllPages("gene", geneSlots, true);
        // 创建所有页的收藏槽位行（默认隐藏）
        createAllPages("favorite", favoritesSlots, false);
    }

    /**
     * 创建所有页的槽位行
     * @param handler 物品处理器
     * @param visibleFirstPage 是否显示第一页
     */
    private void createAllPages(String typeId, IItemHandlerModifiable handler, boolean visibleFirstPage) {
        for (int page = 0; page < TOTAL_PAGES; page++) {
            boolean isVisible = (page == 0) && visibleFirstPage;
            PageSlotContainer pageContainer = createPageLines(typeId, page, handler, isVisible);
            inventorySlotContainer.addChild(pageContainer);
            slotElements.add(pageContainer);
        }
    }

    /**
     * 创建单页的槽位行，封装在一个绝对定位的PageSlotContainer中
     * @param typeId 类型标识（"gene" 或 "favorite"）
     * @param pageIndex 页码（从0开始）
     * @param handler 物品处理器
     * @param visible 是否显示
     * @return 包含该页所有槽位行的容器元素（绝对定位）
     */
    private PageSlotContainer createPageLines(String typeId, int pageIndex, IItemHandlerModifiable handler, boolean visible) {
        // 创建一页的容器，使用绝对定位
        PageSlotContainer pageContainer = new PageSlotContainer(typeId, pageIndex);

        for (int line = 0; line < ROWS_PER_PAGE; line++) {
            int globalLineIndex = pageIndex * ROWS_PER_PAGE + line;
            int startIndex = globalLineIndex * SLOTS_PER_ROW;
            float offsetWidth = stepOffsetBase * (stepOffsetMaxLine - line); // 阶梯偏移：第一行最大，最后一行0

            GeneInventoryLine inventoryLine = new GeneInventoryLine(
                    globalLineIndex,
                    offsetWidth,
                    startIndex,
                    SLOTS_PER_ROW,
                    handler
            );
            inventoryLine.setId("gene_inventory_line_" + globalLineIndex);
//            inventoryLine.setDisplay(visible);
            pageContainer.addLine(inventoryLine);
        }
        return pageContainer;
    }

    private void initZoneContainer() {
        this.favoritesButton = new FavoritesButton();
        favoritesButton.setId("favorites_button");
        favoritesButton.setOnClick(event -> toggleFavoritesMode());
        zoneContainer.addChild(favoritesButton);

        this.leftPage = new SwitchPage(false);
        leftPage.setId("left_page");
        leftPage.setOnClick(event -> pageManager.switchPage(-1));
        zoneContainer.addChild(leftPage);

        this.pageText = new PageText();
        pageText.setId("page_text");
        zoneContainer.addChild(pageText);

        this.rightPage = new SwitchPage(true);
        rightPage.setId("right_page");
        rightPage.setOnClick(event -> pageManager.switchPage(1));
        zoneContainer.addChild(rightPage);

        // 初始化页码显示
        updatePageText();
    }




    @Override
    public void scale(float scale) {
        this.currentScale = scale;
        this.layout(layout -> {
            layout.width(baseWidth * scale);
            layout.height(baseHeight * scale);
        });

        inventorySlotContainer.layout(layout -> {
            layout.height(baseInventoryHeight *scale);
        });
        for (PageSlotContainer pageContainer : slotElements) {
            pageContainer.scale(scale);
        }

        zoneContainer.layout(layout -> {
            layout.paddingLeft(baseZonePaddingLeft * scale);
        });
        if (favoritesButton != null) {
            favoritesButton.scale(scale);
        }
        if (leftPage != null) {
            leftPage.scale(scale);
        }
        if (pageText != null) {
            pageText.scale(scale);
        }
        if (rightPage != null) {
            rightPage.scale(scale);
        }
    }


    /**
     * 在hover槽位上绘制悬停纹理和物品图标
     * @param guiContext
     */
    @Override
    public void drawBackgroundAdditional(GUIContext guiContext) {
        // 通过 slotElements 找到当前显示的页面，然后绘制其中的槽位
        for (PageSlotContainer pageContainer : slotElements) {
            // 跳过隐藏的页面
            if (!pageContainer.isVisible()) continue;

            // 调用 PageSlotContainer 的绘制方法
            pageContainer.drawSlots(guiContext, currentScale);
        }
    }

    /**
     * 切换收藏模式
     */
    private void toggleFavoritesMode() {
        // 使用 PageManager 切换模式（会自动处理页面显示和页码重置）
        pageManager.switchMode(!isFavoritesMode);
    }

    /**
     * 更新页码显示
     */
    private void updatePageText() {
        if (pageText != null) {
            int currentPage = pageManager.getCurrentPage();
            int totalPages = pageManager.getTotalPages();
            pageText.setPage(currentPage - 1, totalPages);  // setPage 使用 0-based 页码
            pageText.setFavoritesMode(isFavoritesMode);
        }
    }

    /**
     * 页面槽位容器 - 封装单页的所有槽位行
     */
    public class PageSlotContainer extends UIElement implements IScalable {
        private final List<GeneInventoryLine> lines = new ArrayList<>();
        private final String typeId; // "gene" 或 "favorite"
        private final int pageIndex;
        private float currentScale = 1.0f;

        public PageSlotContainer(String typeId, int pageIndex) {
            super();
            this.typeId = typeId;
            this.pageIndex = pageIndex;
            this.setId(typeId + "_page_" + pageIndex);
            this.layout(layout -> {
                layout.flexDirection(FlexDirection.COLUMN);
                layout.positionType(TaffyPosition.ABSOLUTE);
                layout.widthPercent(100f);
                layout.left(0);
                layout.top(0);
            });
        }

        /**
         * 获取类型标识（"gene" 或 "favorite"）
         */
        public String getTypeId() {
            return typeId;
        }

        /**
         * 获取页码（从0开始）
         */
        public int getPageIndex() {
            return pageIndex;
        }

        /**
         * 添加槽位行到页面
         */
        public void addLine(GeneInventoryLine line) {
            lines.add(line);
            this.addChild(line);
        }

        /**
         * 获取页面中的所有槽位行
         */
        public List<GeneInventoryLine> getLines() {
            return lines;
        }

        @Override
        public void scale(float scale) {
            this.currentScale = scale;
            this.layout(layout -> {
                layout.height(baseInventoryHeight *scale);
            });
            for (GeneInventoryLine line : lines) {
                line.scale(scale);
            }
        }

        /**
         * 绘制页面内的槽位（背景和悬停效果）
         */
        public void drawSlots(GUIContext guiContext, float scale) {
            if (!isDisplayed()) return;
            for (GeneInventoryLine line : lines) {
                for (GeneSlot slot : line.getSlots()) {
                    // 只在非悬停的槽位上绘制背景
                    if (!slot.isSlotHovered()) {
                        drawSlotOverlay(guiContext, slot, scale);
                    }
                }

                for (GeneSlot slot : line.getSlots()) {
                    // 只在悬停的槽位上绘制悬停效果
                    if (slot.isSlotHovered()) {
                        drawSlotHover(guiContext, slot, scale);
                    }
                    drawItemStack(guiContext,slot,scale);

                }
            }
        }

        /**
         * 绘制槽位背景纹理
         */
        private void drawSlotOverlay(GUIContext context, GeneSlot slot, float scale) {
            var slotPos = slot.getSlotPos();
            var texture = slotPos.getTexture();
            int width = slotPos.getBaseWidth();
            int height = slotPos.getBaseHeight();

            // 使用 content 区域坐标（相对于屏幕的绝对坐标，已排除 padding）
            float x = slot.getContentX();
            float y = slot.getContentY();

            context.pose.pushPose();
            context.pose.translate(x, y, 0);
            context.pose.scale(scale, scale, 1);
            context.drawTexture(texture, 0, 0, width, height);
            context.pose.popPose();
        }

        /**
         * 绘制悬停纹理
         */
        private void drawSlotHover(GUIContext context, GeneSlot slot, float scale) {
            var hoverPos = GeneSlot.SlotPos.HoverOverlay;
            var slotPos = slot.getSlotPos();
            var texture = hoverPos.getTexture();
            int width = hoverPos.getBaseWidth();
            int height = hoverPos.getBaseHeight();

            // 使用 content 区域坐标（相对于屏幕的绝对坐标，已排除 padding）
            float x = slot.getContentX();
            float y = slot.getContentY();

            context.pose.pushPose();
            context.pose.translate(x, y, 1);
            context.pose.scale(scale, scale, 1);
            context.drawTexture(texture, slotPos.getHoverOffsetX(), slotPos.getHoverOffsetY(), width, height);
            context.pose.popPose();
        }

        private void drawItemStack(GUIContext context,GeneSlot slot,float scale) {

            var itemStack = slot.getSlot().getItem();

            if (itemStack.getItem() instanceof GeneItem){
                ResourceLocation textureResource = IStackTraitAccess.getSelectedTraitTexture(itemStack);
                if (textureResource == null) {
                    return;
                }
                IGuiTexture texture = SpriteTexture.of(textureResource);


                var slotPos = slot.getSlotPos();

                // 使用 content 区域坐标（相对于屏幕的绝对坐标，已排除 padding）
                float x = slot.getContentX();
                float y = slot.getContentY();

                context.pose.pushPose();
                context.pose.translate(x, y, 0);
                context.pose.scale(scale, scale, 1);
                context.drawTexture(texture, slotPos.getHoverOffsetX()+ 5,slotPos.getHoverOffsetY()+ 5, 12, 12);

                int traitCount = IStackTraitAccess.getTraitCount(itemStack);
                if (traitCount > 1) {
                    Font font = Minecraft.getInstance().font;
                    String roman = toRoman(traitCount);
                    int badgeX = 16 - Math.round(font.width(roman) * ROMAN_TEXT_SCALE);
                    int badgeY = 11;

                    context.pose.pushPose();
                    // Draw above slot/hover overlays to prevent being hidden.
                    context.pose.translate(badgeX, badgeY, 200);
                    context.pose.scale(ROMAN_TEXT_SCALE, ROMAN_TEXT_SCALE, 1.0F);
                    context.graphics.drawString(font, roman, slotPos.getHoverOffsetX(), slotPos.getHoverOffsetY(), 0xFFFFE6FF, true);
                    context.pose.popPose();
                }

                context.pose.popPose();



            }
        }


        private static String toRoman(int value) {
            int[] numbers = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
            String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
            StringBuilder result = new StringBuilder();
            int remaining = Math.max(value, 0);

            for (int i = 0; i < numbers.length && remaining > 0; i++) {
                while (remaining >= numbers[i]) {
                    result.append(symbols[i]);
                    remaining -= numbers[i];
                }
            }
            return result.toString();
        }
    }



    /**
     * 页面管理器
     * 管理普通模式和收藏模式下的页面切换
     * 两种模式拥有独立的页码状态
     * 
     * slotElements 存储结构（假设 TOTAL_PAGES = 3）：
     * [gene_page_0, favorite_page_0, gene_page_1, favorite_page_1, gene_page_2, favorite_page_2]
     */
    public class PageManager {
        // 普通模式页码（从1开始）
        private int genePage = 1;
        // 收藏模式页码（从1开始）
        private int favoritePage = 1;

        /**
         * 获取当前模式的页码
         */
        public int getCurrentPage() {
            return isFavoritesMode ? favoritePage : genePage;
        }

        /**
         * 设置当前模式的页码
         */
        private void setCurrentPage(int page) {
            if (isFavoritesMode) {
                favoritePage = page;
            } else {
                genePage = page;
            }
        }

        public int getTotalPages() {
            return TOTAL_PAGES;
        }

        /**
         * 获取当前��式对应的 typeId
         */
        private String getCurrentTypeId() {
            return isFavoritesMode ? "favorite" : "gene";
        }

        /**
         * 翻页
         * @param offset 1=下一页，-1=上一页
         */
        public void switchPage(int offset) {
            int currentPage = getCurrentPage();
            // 计算新页码（带循环）
            int newPage = currentPage + offset;
            if (newPage < 1) {
                newPage = TOTAL_PAGES;
            } else if (newPage > TOTAL_PAGES) {
                newPage = 1;
            }

            // 切换到新页
            goToPage(newPage);
        }

        /**
         * 跳转到指定页
         * @param pageIndex 目标页码（从1开始）
         */
        public void goToPage(int pageIndex) {
            if (pageIndex < 1 || pageIndex > TOTAL_PAGES) return;

            int currentPage = getCurrentPage();
            // 隐藏当前页
            setPageVisible(currentPage, false);
            // 更新当前页
            setCurrentPage(pageIndex);
            // 显示新页
            setPageVisible(pageIndex, true);

            updatePageText();
        }

        /**
         * 设置指定页的显示状态（仅当前模式）
         * @param pageIndex 页码（从1开始）
         * @param visible 是否显示
         */
        private void setPageVisible(int pageIndex, boolean visible) {
            String typeId = getCurrentTypeId();
            int pageZeroBased = pageIndex - 1; // 转换为0-based索引
            
            for (PageSlotContainer container : slotElements) {
                if (container.getTypeId().equals(typeId) && container.getPageIndex() == pageZeroBased) {
                    container.setDisplay(visible);
                    return; // 找到并设置后立即返回
                }
            }
        }

        /**
         * 切换到指定模式，显示该模式下保存的页码
         */
        public void switchMode(boolean favoritesMode) {
            // 隐藏当前模式下的当前页
            setPageVisible(getCurrentPage(), false);
            // 更新模式标记
            isFavoritesMode = favoritesMode;
            // 显示新模式下的保存页码
            setPageVisible(getCurrentPage(), true);
            updatePageText();
        }

        /**
         * 重置当前模式到第一页
         */
        public void resetCurrentPage() {
            goToPage(1);
        }

        /**
         * 初始化显示（显示各模式保存的页码，默认第一页）
         */
        public void initDisplay() {
            // 初始化普通模式显示
            for (PageSlotContainer container : slotElements) {
                boolean isGene = container.getTypeId().equals("gene");
                boolean isFirstPage = container.getPageIndex() == 0;
                // 普通模式显示第一页
                if (isGene) {
                    container.setDisplay(isFirstPage);
                }
            }
            // 初始化收藏模式显示（默认隐藏所有，除非当前是收藏模式）
            for (PageSlotContainer container : slotElements) {
                boolean isFavorite = container.getTypeId().equals("favorite");
                boolean isFirstPage = container.getPageIndex() == 0;
                // 收藏模式：如果是当前模式则显示第一页，否则隐藏
                if (isFavorite) {
                    container.setDisplay(isFavoritesMode && isFirstPage);
                }
            }
            updatePageText();
        }
    }
}
