package dev.l3g7.griefer_utils.features.tweaks;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.render.RenderBarrierCheckEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

@Singleton
public class ShowBarriers extends Feature {

	private final BooleanSetting enabled = new BooleanSetting()
			.name("Barrieren anzeigen")
			.config("tweaks.showbarriers.active")
			.icon(Material.BARRIER)
			.defaultValue(false);


	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	public ShowBarriers() {
		super(Category.TWEAK);
	}

	@EventListener
	public void onRenderCheck(RenderBarrierCheckEvent event) {
		event.setCanceled(isActive() && isOnGrieferGames());
	}
}
