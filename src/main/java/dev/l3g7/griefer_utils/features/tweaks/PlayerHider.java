package dev.l3g7.griefer_utils.features.tweaks;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.List;

@Singleton
public class PlayerHider extends Feature {

	private static final List<String> BLOCKED_SOUNDS = Arrays.asList("random.eat", "random.burp", "random.drink");

	private boolean playingOwnSounds = false;

	private final BooleanSetting enabled = new BooleanSetting()
		.name("Spieler verstecken")
		.description("Versteckt andere Spieler.")
		.icon("blindness")
		.config("tweaks.player_hider.active")
		.defaultValue(false)
		.callback(isActive -> {
			if (isOnGrieferGames())
				for (EntityPlayer player : world().playerEntities)
					updatePlayer(player);
		});

	public PlayerHider() {
		super(Category.TWEAK);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		for (EntityPlayer player : world().playerEntities)
			updatePlayer(player);
	}

	/**
	 * Handles the player model
	 */
	@SubscribeEvent
	public void onEntityRender(RenderPlayerEvent.Pre event) {
		if (!isActive() || !isOnGrieferGames() || event.entity == player())
			return;

		event.setCanceled(true);
	}

	/**
	 * Makes sure your own sounds are still played
	 */
	@SubscribeEvent
	public void onSoundPlayAtEntity(PlaySoundAtEntityEvent event) {
		if (event.entity.equals(player()))
			playingOwnSounds = true;
	}

	/**
	 * Cancels other players' sounds
	 */
	@SubscribeEvent
	public void onSoundPlay(PlaySoundEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		if (playingOwnSounds) {
			playingOwnSounds = false;
			return;
		}

		if (event.name.startsWith("step.") || BLOCKED_SOUNDS.contains(event.name))
			event.result = null;
	}

	private void updatePlayer(EntityPlayer player) {
		if (player == player())
			return;

		// Shadows
		if (player.isInvisible() != isActive())
			player.setInvisible(isActive() || player.isPotionActive(Potion.invisibility));

		// Fire
		if (player.isImmuneToFire() != isActive())
			Reflection.set(player, isActive(), "isImmuneToFire", "field_70178_ae", "ab");

		// Sprinting particles
		player.setSprinting(!isActive());

		// Effect particles
		if (isActive())
			Reflection.invoke(player, new String[] {"resetPotionEffectMetadata", "func_175133_bi", "bj"});
		else
			Reflection.invoke(player, new String[] {"updatePotionMetadata", "func_175135_B", "B"});

		player.setSilent(isActive());
		player.setEating(false);
		player.clearItemInUse();
	}

}
