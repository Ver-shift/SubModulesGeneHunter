package org.galaxy.beyond.api.client.gui;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.ui.BooleanConfigurator;
import com.lowdragmc.lowdraglib2.configurator.ui.ColorConfigurator;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.NumberConfigurator;
import com.lowdragmc.lowdraglib2.configurator.ui.StringConfigurator;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Inspector;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.galaxy.beyond.api.config.CommonConfig;

public class BeyondConfigScreen {

    public static void register(ModContainer modContainer) {
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (IConfigScreenFactory) BeyondConfigScreen::create
        );
    }

    public static Screen create(ModContainer modContainer, Screen parentScreen) {
        var inspector = new Inspector();
        inspector.inspect(buildConfigurable(), null, CommonConfig.SPEC::save);
        inspector.layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
        });

        var root = new UIElement();
        root.layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.paddingAll(8);
            layout.flexDirection(FlexDirection.COLUMN);
        });
        root.addChild(inspector);

        var modularUI = ModularUI.of(UI.of(root));
        return new ModularUIScreen(modularUI, Component.translatable("beyond.config.title"));
    }

    private static IConfigurable buildConfigurable() {
        return IConfigurable.create(group -> {
            group.addConfigurator(createGenericGroup());
            group.addConfigurator(createRogueGroup());
            group.addConfigurator(createActiveZoneGroup());
            group.addConfigurator(createRenderGroup());
            group.addConfigurator(createNodeWeightGroup());
            group.addConfigurator(createLobbyGroup());
        });
    }

    private static ConfiguratorGroup createGenericGroup() {
        var grp = new ConfiguratorGroup("beyond.config.group.generic", false);
        grp.addConfigurator(
                new BooleanConfigurator(
                        "beyond.config.field.debug_messages",
                        CommonConfig.DEBUG_MODE::get,
                        CommonConfig.DEBUG_MODE::set,
                        false,
                        false
                )
        );
        return grp;
    }

    private static ConfiguratorGroup createRogueGroup() {
        var grp = new ConfiguratorGroup("beyond.config.group.rogue", false);
        grp.addConfigurator(
                new StringConfigurator(
                        "beyond.config.field.dimensions",
                        CommonConfig.ROGUE_DIMENSION::get,
                        CommonConfig.ROGUE_DIMENSION::set,
                        "minecraft:overworld|beyond:safe_zone_structure|beyond:node_structure",
                        false
                )
        );
        return grp;
    }

    private static ConfiguratorGroup createActiveZoneGroup() {
        var grp = new ConfiguratorGroup("beyond.config.group.active_zone", false);
        grp.addConfigurator(
                new NumberConfigurator("beyond.config.field.min_expand", CommonConfig.ACTIVE_ZONE_MIN_EXPAND::get,
                        v -> CommonConfig.ACTIVE_ZONE_MIN_EXPAND.set(v.intValue()), 50, false)
                        .setRange(1, 500).setWheel(1)
        );
        grp.addConfigurator(
                new NumberConfigurator("beyond.config.field.max_expand", CommonConfig.ACTIVE_ZONE_MAX_EXPAND::get,
                        v -> CommonConfig.ACTIVE_ZONE_MAX_EXPAND.set(v.intValue()), 400, false)
                        .setRange(1, 1000).setWheel(1)
        );
        grp.addConfigurator(
                new NumberConfigurator("beyond.config.field.min_nodes", CommonConfig.ACTIVE_ZONE_MIN_NODES::get,
                        v -> CommonConfig.ACTIVE_ZONE_MIN_NODES.set(v.intValue()), 12, false)
                        .setRange(1, 1000).setWheel(1)
        );
        grp.addConfigurator(
                new NumberConfigurator("beyond.config.field.node_expand_radius", CommonConfig.ACTIVE_ZONE_NODE_EXPAND_RADIUS::get,
                        v -> CommonConfig.ACTIVE_ZONE_NODE_EXPAND_RADIUS.set(v.intValue()), 6, false)
                        .setRange(1, 100).setWheel(1)
        );
        grp.addConfigurator(
                new NumberConfigurator("beyond.config.field.node_max_expand_radius", CommonConfig.ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS::get,
                        v -> CommonConfig.ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS.set(v.intValue()), 400, false)
                        .setRange(1, 1000).setWheel(1)
        );
        grp.addConfigurator(
                new NumberConfigurator("beyond.config.field.min_connections", CommonConfig.ACTIVE_ZONE_MIN_CONNECTIONS::get,
                        v -> CommonConfig.ACTIVE_ZONE_MIN_CONNECTIONS.set(v.intValue()), 3, false)
                        .setRange(1, 100).setWheel(1)
        );
        grp.addConfigurator(
                new NumberConfigurator("beyond.config.field.border_visible_chunks", CommonConfig.ACTIVE_ZONE_BORDER_VISIBLE_CHUNKS::get,
                        v -> CommonConfig.ACTIVE_ZONE_BORDER_VISIBLE_CHUNKS.set(v.intValue()), 3, false)
                        .setRange(0, 64).setWheel(1)
        );
        grp.addConfigurator(
                new ColorConfigurator("beyond.config.field.active_zone_render_color", CommonConfig.ACTIVE_ZONE_RENDER_COLOR::get,
                        CommonConfig.ACTIVE_ZONE_RENDER_COLOR::set, 0x64DCDCDC, false)
        );
        return grp;
    }

    private static ConfiguratorGroup createRenderGroup() {
        var grp = new ConfiguratorGroup("beyond.config.group.render", false);
        grp.addConfigurator(createBooleanConfigurator(
                "beyond.config.field.zone_render_enabled",
                CommonConfig.ZONE_RENDER_ENABLED,
                true
        ));
        grp.addConfigurator(createSafeZoneBorderGroup());
        grp.addConfigurator(createActiveZoneBorderGroup());
        grp.addConfigurator(createNodeRenderGroup());
        return grp;
    }

    private static ConfiguratorGroup createSafeZoneBorderGroup() {
        var grp = new ConfiguratorGroup("beyond.config.group.safe_zone_border", false);
        grp.addConfigurator(createBooleanConfigurator(
                "beyond.config.field.safe_zone_border",
                CommonConfig.RENDER_SAFE_ZONE_BORDER,
                true
        ));
        grp.addConfigurator(createBooleanConfigurator(
                "beyond.config.field.debug_show_safe_zone_border",
                CommonConfig.DEBUG_SHOW_SAFE_ZONE_BORDER,
                true
        ));
        return grp;
    }

    private static ConfiguratorGroup createActiveZoneBorderGroup() {
        var grp = new ConfiguratorGroup("beyond.config.group.active_zone_border", false);
        grp.addConfigurator(createBooleanConfigurator(
                "beyond.config.field.active_zone_border",
                CommonConfig.RENDER_ACTIVE_ZONE_BORDER,
                true
        ));
        grp.addConfigurator(createBooleanConfigurator(
                "beyond.config.field.debug_show_active_zone_border",
                CommonConfig.DEBUG_SHOW_ACTIVE_ZONE_BORDER,
                true
        ));
        grp.addConfigurator(
                new NumberConfigurator("beyond.config.field.border_visible_chunks", CommonConfig.ACTIVE_ZONE_BORDER_VISIBLE_CHUNKS::get,
                        v -> CommonConfig.ACTIVE_ZONE_BORDER_VISIBLE_CHUNKS.set(v.intValue()), 3, false)
                        .setRange(0, 64).setWheel(1)
        );
        grp.addConfigurator(
                new ColorConfigurator("beyond.config.field.active_zone_render_color", CommonConfig.ACTIVE_ZONE_RENDER_COLOR::get,
                        CommonConfig.ACTIVE_ZONE_RENDER_COLOR::set, 0x64DCDCDC, false)
        );
        return grp;
    }

    private static ConfiguratorGroup createNodeRenderGroup() {
        var grp = new ConfiguratorGroup("beyond.config.group.node_render", false);
        grp.addConfigurator(createNodeRenderColorGroup(
                "beyond.config.group.green_node",
                CommonConfig.RENDER_GREEN_NODES,
                CommonConfig.DEBUG_SHOW_GREEN_NODES,
                CommonConfig.VISIBLE_GREEN_NODE_COUNT,
                CommonConfig.GREEN_NODE_RENDER_COLOR,
                0x8C00FF00
        ));
        grp.addConfigurator(createNodeRenderColorGroup(
                "beyond.config.group.orange_node",
                CommonConfig.RENDER_ORANGE_NODES,
                CommonConfig.DEBUG_SHOW_ORANGE_NODES,
                CommonConfig.VISIBLE_ORANGE_NODE_COUNT,
                CommonConfig.ORANGE_NODE_RENDER_COLOR,
                0x8CFFA500
        ));
        grp.addConfigurator(createNodeRenderColorGroup(
                "beyond.config.group.red_node",
                CommonConfig.RENDER_RED_NODES,
                CommonConfig.DEBUG_SHOW_RED_NODES,
                CommonConfig.VISIBLE_RED_NODE_COUNT,
                CommonConfig.RED_NODE_RENDER_COLOR,
                0x8CFF0000
        ));
        grp.addConfigurator(createNodeRenderColorGroup(
                "beyond.config.group.blue_node",
                CommonConfig.RENDER_BLUE_NODES,
                CommonConfig.DEBUG_SHOW_BLUE_NODES,
                CommonConfig.VISIBLE_BLUE_NODE_COUNT,
                CommonConfig.BLUE_NODE_RENDER_COLOR,
                0x8C0000FF
        ));
        return grp;
    }

    private static ConfiguratorGroup createNodeWeightGroup() {
        var grp = new ConfiguratorGroup("beyond.config.group.node_weight", false);
        grp.addConfigurator(
                new NumberConfigurator("beyond.config.field.green_weight", CommonConfig.NODE_COLOR_GREEN_WEIGHT::get,
                        v -> CommonConfig.NODE_COLOR_GREEN_WEIGHT.set(v.intValue()), 20, false)
                        .setRange(0, 1000).setWheel(1)
        );
        grp.addConfigurator(
                new NumberConfigurator("beyond.config.field.orange_weight", CommonConfig.NODE_COLOR_ORANGE_WEIGHT::get,
                        v -> CommonConfig.NODE_COLOR_ORANGE_WEIGHT.set(v.intValue()), 60, false)
                        .setRange(0, 1000).setWheel(1)
        );
        grp.addConfigurator(
                new NumberConfigurator("beyond.config.field.red_weight", CommonConfig.NODE_COLOR_RED_WEIGHT::get,
                        v -> CommonConfig.NODE_COLOR_RED_WEIGHT.set(v.intValue()), 20, false)
                        .setRange(0, 1000).setWheel(1)
        );
        return grp;
    }

    private static ConfiguratorGroup createNodeRenderColorGroup(
            String label,
            ModConfigSpec.BooleanValue normalVisible,
            ModConfigSpec.BooleanValue debugVisible,
            ModConfigSpec.IntValue visibleCount,
            ModConfigSpec.IntValue color,
            int defaultColor
    ) {
        var grp = new ConfiguratorGroup(label, false);
        grp.addConfigurator(createBooleanConfigurator("beyond.config.field.node_visible", normalVisible, true));
        grp.addConfigurator(createBooleanConfigurator("beyond.config.field.node_debug_visible", debugVisible, true));
        grp.addConfigurator(
                new NumberConfigurator("beyond.config.field.visible_node_count", visibleCount::get,
                        v -> visibleCount.set(v.intValue()), 1, false)
                        .setRange(0, 64).setWheel(1)
        );
        grp.addConfigurator(
                new ColorConfigurator("beyond.config.field.node_render_color", color::get,
                        color::set, defaultColor, false)
        );
        return grp;
    }

    private static ConfiguratorGroup createLobbyGroup() {
        var grp = new ConfiguratorGroup("beyond.config.group.lobby", false);
        grp.addConfigurator(
                new NumberConfigurator("beyond.config.field.cooldown_seconds", CommonConfig.LOBBY_COOLDOWN_SECONDS::get,
                        v -> CommonConfig.LOBBY_COOLDOWN_SECONDS.set(v.intValue()), 10, false)
                        .setRange(0, 3600).setWheel(1)
        );
        return grp;
    }

    private static BooleanConfigurator createBooleanConfigurator(
            String label,
            ModConfigSpec.BooleanValue value,
            boolean defaultValue
    ) {
        return new BooleanConfigurator(label, value::get, value::set, defaultValue, false);
    }
}
