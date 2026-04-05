package org.biotech.api.init;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.biotech.Biotech;

/**
 * Client-only registrations.
 */
@EventBusSubscriber(modid = Biotech.MODID)
public class ClientInit {

	@SubscribeEvent
	public static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(MenuInit.GENE_MENU.get(), ModularUIContainerScreen::new);
	}
}
