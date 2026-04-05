package org.biotech.api.system.gene.inventory;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.biotech.api.system.gene.core.IGeneItem;
import org.jetbrains.annotations.NotNull;

/**
 * 玩家基因库存数据 - 包含256个基因槽位和256个收藏槽位
 * 使用 LDLib2 的 PersistedParser 简化序列化
 */
@Data
public class PlayerGeneInventoryData implements IPersistedSerializable {

    // UI布局常量 - 通过页数计算槽位数量，防止不一致
    public static final int TOTAL_PAGES = 9;        // 总页数
    public static final int ROWS_PER_PAGE = 3;      // 每页行数
    public static final int SLOTS_PER_ROW = 9;      // 每行槽位数

    // 计算得出的槽位数量
    public static final int SLOTS_PER_PAGE = ROWS_PER_PAGE * SLOTS_PER_ROW;  // 每页槽位数 = 27
    public static final int GENE_SLOT_COUNT = TOTAL_PAGES * SLOTS_PER_PAGE;   // 总槽位数 = 243
    public static final int FAVORITE_SLOT_COUNT = TOTAL_PAGES * SLOTS_PER_PAGE; // 收藏槽位数 = 243

    // Codec 和 StreamCodec 由 PersistedParser 自动生成
    public static final Codec<PlayerGeneInventoryData> CODEC = PersistedParser.createCodec(PlayerGeneInventoryData::new);
    public static final StreamCodec<ByteBuf, PlayerGeneInventoryData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    //slot
    @Persisted
    private ItemStackHandler geneSlots;
    @Persisted
    private ItemStackHandler favoriteSlots;




//    //page
//    @Persisted
//    private int genePage;
//    @Persisted
//    private int favoritePage;

    public PlayerGeneInventoryData() {
        this.geneSlots = createGeneOnlyHandler(GENE_SLOT_COUNT);
        this.favoriteSlots = createGeneOnlyHandler(FAVORITE_SLOT_COUNT);
    }

    /**
     * 创建一个只接受 IGeneItem 的 ItemStackHandler
     */
    private static ItemStackHandler createGeneOnlyHandler(int slots) {
        return new ItemStackHandler(slots) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                // 只允许 IGeneItem 类型的物品
                return stack.getItem() instanceof IGeneItem;
            }
        };
    }


}
