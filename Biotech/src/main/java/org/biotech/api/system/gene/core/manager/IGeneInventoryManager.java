package org.biotech.api.system.gene.core.manager;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.biotech.item.GeneItem;

import java.util.function.Supplier;

public interface IGeneInventoryManager {

    /**
     * 打开基因库存界面。
     */
    boolean openGeneMenu();

    //=========================控制槽位的基本方法=============================
    /**
     * 将物品放置到玩家的基因背包
     */
    AddResult add(GeneItem item);

    AddResult add(int slotIndex, GeneItem item);

    /**
     * 将 ItemStack 放置到玩家的基因背包指定槽位
     * @param slotIndex 槽位索引，-1 表示自动寻找空槽
     * @param stack 物品栈
     * @return 添加结果
     */
    AddResult add(int slotIndex, ItemStack stack);

    /**
     * 将 ItemStack 放置到玩家的基因背包（自动寻找空槽）
     * @param stack 物品栈
     * @return 添加结果
     */
    AddResult add(ItemStack stack);

    /**
     * 根据稀有度在背包添加基因，
     * 消耗item的内容放在外面，
     * 概率根据配置文件调整
     * 史诗（Epic）：必定出双词条.
     * 稀有（Rare）：更高概率出双词条.
     * 少见（Uncommon）：极小概率出双词条.
     *
     */
    AddResult addUnidentified(Rarity rarity);

    int getFirstEmptySlot();

    boolean removeItem(int slotIndex);

    ItemStack getItemStack(int slotIndex);

    //=========================控制收藏槽位的基本方法=============================
    /**
     * 将物品放置到玩家的收藏槽位
     */
    AddResult addToFavorite(GeneItem item);

    AddResult addToFavorite(int slotIndex, GeneItem item);

    int getFirstEmptyFavoriteSlot();

    boolean removeFavoriteItem(int slotIndex);

    ItemStack getFavoriteItemStack(int slotIndex);



    /**
     * 往背包加入物品的返回值结果
     * 参考 Inventory.add() 的错误处理机制
     */
    enum AddResult {
        /** 成功添加 */
        SUCCESS(true, "Successfully added a new item"),
        /** 背包已满 */
        FULL(false, "Inventory is full"),
        /** 物品为空 */
        EMPTY_ITEM(false, "Item is empty"),
        /** 物品不可堆叠且无空槽 */
        NO_SPACE(false, "No space for non-stackable item"),
        /** 物品类型不允许 */
        INVALID_TYPE(false, "Item type not allowed");

        private final boolean success;
        private final String message;

        AddResult(final boolean success, final String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        /**
         * 创建崩溃报告（参考 Inventory.add() 的错误处理）
         * @param itemStack 物品堆
         * @param context 额外上下文信息
         * @return 崩溃报告
         */
        public CrashReport createCrashReport(ItemStack itemStack, Supplier<String> context) {
            CrashReport crashreport = CrashReport.forThrowable(new IllegalStateException(this.message), "Adding gene item to inventory");
            CrashReportCategory category = crashreport.addCategory("Gene item being added");
            category.setDetail("Registry Name", () -> String.valueOf(itemStack.getItemHolder().getRegisteredName()));
            category.setDetail("Item Class", () -> itemStack.getItem().getClass().getName());
            category.setDetail("Item count", () -> String.valueOf(itemStack.getCount()));
            category.setDetail("Result", () -> this.name());
            category.setDetail("Context", context);
            return crashreport;
        }

        /**
         * 抛出崩溃报告异常
         */
        public void throwCrash(ItemStack itemStack, Supplier<String> context) {
            throw new ReportedException(createCrashReport(itemStack, context));
        }
    }

}
