package dev.l3g7.griefer_utils.features.world.better_schematica;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.SchematicaUtil;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Singleton
public class BetterSchematica extends Feature {

	private final BooleanSetting highlightBlocks = new BooleanSetting()
		.name("Ausgewählten Block hervorheben")
		.description("Markiert alle Blöcke vom selben Typ des in der Hand gehaltenen Items.")
		.icon("litematica/green_highlight");

	private final BooleanSetting savePosition = new BooleanSetting()
		.name("\"Speichern\" Knopf")
		.description("Fügt in der Schematic-Kontrolle einen Knopf hinzu, der die derzeit geladene Schematic mit Drehung, Spiegelung und Position speichert."
			+ "\nWenn die Schematic geladen wird, wird sie automatich an die gespeicherte Position geschoben.")
		.icon("litematica/axes");

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Besseres Schematica")
		.description("Erleichtert das Arbeiten mit Schematica.")
		.icon("litematica/litematica")
		.subSettings(highlightBlocks, savePosition);

	@Override
	public void init() {
		super.init();

		if (Constants.SCHEMATICA) {
			highlightBlocks.callback(b -> {
				if (isEnabled())
					SchematicaUtil.refresh();
			});

			enabled.callback(b -> {
				if (highlightBlocks.get())
					SchematicaUtil.refresh();
			});

			getCategory().getSetting().addCallback(b -> {
				if (enabled.get() && highlightBlocks.get())
					SchematicaUtil.refresh();
			});
			return;
		}

		enabled.name("§8Besseres Schematica")
			.description("Verbessert Schematica.\n\n(Es ist kein Schematica installiert.)")
			.set(false)
			.callback(b -> {
				if (b)
					enabled.set(false);
			});
	}

	@EventListener
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		if (Constants.SCHEMATICA && highlightBlocks.get())
			HighlightSchematicaBlocks.onRenderTick(event);
	}

	static boolean isHighlightBlocksEnabled() {
		if (!Constants.SCHEMATICA)
			return false;

		BetterSchematica betterSchematica = FileProvider.getSingleton(BetterSchematica.class);
		return betterSchematica.isEnabled() && betterSchematica.highlightBlocks.get();
	}

	static boolean isSavePositionEnabled() {
		if (!Constants.SCHEMATICA)
			return false;

		BetterSchematica betterSchematica = FileProvider.getSingleton(BetterSchematica.class);
		return betterSchematica.isEnabled() && betterSchematica.savePosition.get();
	}

}
