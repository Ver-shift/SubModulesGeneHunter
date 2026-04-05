package org.biotech.ui.gene_inventroy.element;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Transform2D;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.biotech.ui.BiotechTexture;
import org.biotech.ui.IScalable;

/**
 * DNA 链条组件 - 使用 AnimationTexture 实现无缝滚动
 * 
 * 纹理结构：
 * - 两张 mid 图片头尾相接
 * - 总长度：62 像素
 * - 宽度：19 像素
 * 
 * 使用 AnimationTexture 的帧动画实现平滑滚动，无需手动计算位置
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DNA extends UIElement implements IScalable {

    public static final int baseWidth = 19;
    public static final int baseHeight = 279;


    public static final IGuiTexture dna = SpriteTexture
            .of(BiotechTexture.DNA)
            .setSprite(0,0,baseWidth,baseHeight);

    public DNA(){
        super();
        this.layout(layout -> {
            layout.width(baseWidth);
            layout.height(baseHeight);

        });
        this.style(style->{
            style.backgroundTexture(dna);
        });
    }

    @Override
    public void scale(float scale) {
        this.layout(layout -> {
            layout.width((baseWidth) * scale);
            layout.height((baseHeight * scale));
        });
        this.style(style->{
            style.transform2D(new Transform2D().translate(12 *scale,50 *scale));
        });
    }
}
