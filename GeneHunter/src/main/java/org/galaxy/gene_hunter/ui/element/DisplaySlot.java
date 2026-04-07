package org.galaxy.gene_hunter.ui.element;

import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import lombok.Getter;
import lombok.Setter;
import org.biotech.ui.IScalable;

public class DisplaySlot extends ItemSlot implements IScalable {

    public static final int baseSize = 18;
    public static final int basePaddingAll = 1;

    @Getter @Setter
    private int slotIndex = -1;

    public DisplaySlot() {
        super();
        this.layout(layoutStyle -> {
            layoutStyle.width(baseSize);
            layoutStyle.height(baseSize);
            layoutStyle.paddingLeft(basePaddingAll);
        });
    }


    @Override
    public void scale(float scale) {
        float readl = scale * 0.618f;
        this.layout(layoutStyle -> {
            layoutStyle.width(baseSize * readl);
            layoutStyle.height(baseSize * readl);
            layoutStyle.paddingLeft(basePaddingAll * readl);
        });
    }

    @Override
    public void drawBackgroundTexture(GUIContext guiContext) {

    }

    @Override
    protected void onMouseDown(UIEvent event) {
        // ItemSlot 默认会把 mouseDown 标记为无处理并交给容器点击；
        // 输出槽改为自定义处理：直接触发领取逻辑。
        event.stopPropagation();
        event.hasHandler = true;

        // 客户端只发请求，服务端执行真正的领取逻辑。
        if (slotIndex >= 0) {
            RPCPacketDistributor.rpcToServer("gene_hunter:claim_slot", slotIndex);
        }
    }
}
