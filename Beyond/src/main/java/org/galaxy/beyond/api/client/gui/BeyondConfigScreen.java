package org.galaxy.beyond.api.client.gui;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.ui.BooleanConfigurator;
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
            group.addConfigurator(createNodeColorGroup());
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
                        "beyond.config.field.dimension",
                        CommonConfig.ROGUE_DIMENSION::get,
                        CommonConfig.ROGUE_DIMENSION::set,
                        "minecraft:overworld",
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
        return grp;
    }

    private static ConfiguratorGroup createNodeColorGroup() {
        var grp = new ConfiguratorGroup("beyond.config.group.node_color", false);
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

    private static ConfiguratorGroup createLobbyGroup() {
        var grp = new ConfiguratorGroup("beyond.config.group.lobby", false);
        grp.addConfigurator(
                new NumberConfigurator("beyond.config.field.cooldown_seconds", CommonConfig.LOBBY_COOLDOWN_SECONDS::get,
                        v -> CommonConfig.LOBBY_COOLDOWN_SECONDS.set(v.intValue()), 10, false)
                        .setRange(0, 3600).setWheel(1)
        );
        return grp;
    }
}
