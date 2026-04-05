package org.biotech.ui;

import com.lowdragmc.lowdraglib2.gui.texture.AnimationTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;

/**
 * 生物科技 UI 纹理定义
 * 
 * 纹理坐标参考 biotech_gene_ui.png 图片布局
 */
public class BiotechTexture {

    // ==================== 纹理路径 ====================
    
    /** 基因背景图片路径 */
    public static final String BACKGROUND_PNG = "biotech:textures/gui/gene_background.png";
    
    /** 主 UI 纹理图集路径 */
    public static final String GUI_TEXTURE = "biotech:textures/gui/biotech_gene_ui.png";

    public static final String DNA =  "biotech:textures/gui/dna_state.png";
    public static final String DNA_TWO = "biotech:textures/gui/dna_two.png";


    public static final IGuiTexture Button_Slice = SpriteTexture
            .of(GUI_TEXTURE)                                    // 纹理图集路径
            .setSprite(40, 220, 12, 12)                           // 纹理坐标(x,y) 和大小(width,height)
            .setBorder(3, 3, 3, 3)                             // 9-slice 边框:左、上、右、下各 3 像素
            .setWrapMode(SpriteTexture.WrapMode.CLAMP);        // 中心区域拉伸填充


    public static final String Bubble_1 = "biotech:textures/gui/animation/bubble_1.png";
    public static final String Bubble_2 = "biotech:textures/gui/animation/bubble_2.png";
    public static final String Bubble_3 = "biotech:textures/gui/animation/bubble_3.png";
    public static final String Bubble_4 = "biotech:textures/gui/animation/bubble_4.png";

}
