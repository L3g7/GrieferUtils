package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.ShiftAirCheckEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;

@Singleton
public class SaferSneak extends Feature {

	private final NumberSetting minFallDistance = new NumberSetting()
		.name("Minimale Falldistanz")
		.description("Die minimale Differenz zwischen dir und dem Block unter dir, in Prozent einer Block-Höhe."
			+ "\n\n§7Referenz-Höhen:"
			+ "\n§7Kiste: 87%"
			+ "\n§7Stufen: 50%")
		.icon("ruler")
		.defaultValue(87)
		.max(100)
		.min(1);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Erweitertes Sneaken")
		.description("Verringert die minimale Falldistanz, bei der Sneaken das Fallen verhindert.")
		.icon("sneaking")
		.subSettings(minFallDistance);

	@EventListener
	private void onShiftAirCheck(ShiftAirCheckEvent event) {
		event.boundingBoxOffset = minFallDistance.get() / -100d;
	}

}
