package dev.l3g7.griefer_utils.features.features.selfDisguise;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageReceiveEvent;
import dev.l3g7.griefer_utils.event.events.chat.MessageSendEvent;
import dev.l3g7.griefer_utils.event.events.server.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.server.ServerQuitEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * @author L3g73 (L3g7 said i should put that there ._.)
 */
@Singleton
public class SelfDisguise extends Feature {

	private String command = null;
	private Entity currentDisguise = null;
	private boolean blockCoordinates = false;

	private final BooleanSetting enabled = new BooleanSetting()
			.name("SelfDisguise")
			.description("Erlaubt das Sehen der derzeitigen Verkleidung im Third-Person-Modus.")
			.icon("fading_steve")
			.config("features.self_disguise.active")
			.defaultValue(true);

	public SelfDisguise() {
		super(Category.FEATURE);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@EventListener
	public void onCbJoin(CityBuildJoinEvent event) {
		undisguise();
	}

	@EventListener
	public void onQuit(ServerQuitEvent event) {
		undisguise();
	}

	@EventListener
	public void onSend(MessageSendEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		String text = event.getMsg();

		if (text.equals("/ud")) {
			undisguise();
			return;
		}

		if (text.startsWith("/d "))
			command = event.getMsg().toLowerCase().replace('-', '_');
	}

	@EventListener
	public void onReceive(MessageReceiveEvent event) {
		if (!isActive() || !isOnGrieferGames() || command == null)
			return;

		String text = event.getUnformatted();

		// Return if the entity doesn't exist
		if (text.startsWith("Falsche Benutzung: ")) {
			command = null;
			return;
		}

		if (!text.startsWith("Du bist nun als "))
			return;

		if (currentDisguise != null)
			world().removeEntity(currentDisguise);

		Disguise disguise = DisguiseParser.parse(command);
		currentDisguise = disguise.getEntity();
		blockCoordinates = disguise.isBlockCoordinates();

		command = null;
	}

	@SubscribeEvent
	public void onTick(TickEvent.RenderTickEvent event) {
		if (!isActive() || !isOnGrieferGames() || !isDisguised())
			return;

		if (mc().gameSettings.thirdPersonView == 0) {
			currentDisguise.setInvisible(true);
			player().setInvisible(false);
			if (currentDisguise instanceof EntityFallingBlock) // FallingBlocks don't actually go invisible, so they're just moved out of the world
				currentDisguise.setPosition(player().posX, -5, player().posZ);
			return;
		}

		EntityPlayerSP p = player();
		player().setInvisible(true);
		currentDisguise.setInvisible(false);
		if (blockCoordinates)
			currentDisguise.setLocationAndAngles(((int) p.posX) + 0.5 * Math.signum(p.posX), (int) (p.posY + 0.5), ((int) p.posZ) + 0.5 * Math.signum(p.posZ), p.rotationYaw, p.rotationPitch);
		else
			currentDisguise.setLocationAndAngles(p.posX, p.posY, p.posZ, p.rotationYaw, p.rotationPitch);
		currentDisguise.setRotationYawHead(p.getRotationYawHead());
	}
	private void undisguise() {

		if (isDisguised())
			world().removeEntity(currentDisguise);
		command = null;
		player().setInvisible(false);
		currentDisguise = null;
	}

	public boolean isDisguised() {
		return currentDisguise != null;
	}
}