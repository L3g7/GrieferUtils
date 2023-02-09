package dev.l3g7.griefer_utils.features.item.generic_item_saver;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.event.events.render.RenderItemOverlayEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.components.EntryAddSetting;
import dev.l3g7.griefer_utils.util.misc.Config;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.core.LabyModCore;
import net.labymod.core.WorldRendererAdapter;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraftforge.client.event.MouseEvent;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM;

@Singleton
public class GenericItemSaver extends Feature {

	private String entryKey;

	private final EntryAddSetting newEntrySetting = new EntryAddSetting()
		.name("Item hinzufügen")
		.callback(this::openSelectGui);

	@MainElement(configureSubSettings = false)
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Generischer ItemSaver")
		.description("Deaktiviert Klicks und Dropping bei einstellbaren Items.")
		.icon("shield_with_sword")
		.subSettings(newEntrySetting);

	@Override
	public void init() {
		super.init();

		entryKey = getConfigKey() + ".entries";

		if (!Config.has(entryKey))
			return;

		JsonObject entries = Config.get(entryKey).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : entries.entrySet()) {
			try {
				ItemStack stack = ItemStack.loadItemStackFromNBT(JsonToNBT.getTagFromJson(entry.getKey()));
				JsonObject data = entry.getValue().getAsJsonObject();

				ItemDisplaySetting setting = new ItemDisplaySetting(stack);
				setting.drop.set(data.get("drop").getAsBoolean());
				setting.leftclick.set(data.get("leftclick").getAsBoolean());
				setting.rightclick.set(data.get("rightclick").getAsBoolean());

				List<SettingsElement> settings = enabled.getSubSettings().getElements();
				settings.add(settings.size() - 1, setting);
			} catch (NBTException ignored) {}
		}
	}

	@EventListener
	public void onGuiDraw(RenderItemOverlayEvent event) {
		if (getSetting(event.stack) == null)
			return;

		float zLevel = Reflection.get(drawUtils(), "zLevel");
		zLevel += 500;

		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.color(1, 1, 1, 1);
		drawUtils().bindTexture("griefer_utils/icons/shield_with_sword.png");

		float x = event.x - 0.5f;
		float y = event.y;
		float height = 1, width = 1f;
		float scale = 10 / width;
		GlStateManager.scale(scale, scale, 1);
		x *= 1 / scale;
		y *= 1 / scale;

		WorldRendererAdapter worldrenderer = LabyModCore.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(x, y + height, zLevel).tex(0, height).endVertex();
		worldrenderer.pos(x + width, y + height, zLevel).tex(width, height).endVertex();
		worldrenderer.pos(x + width, y, zLevel).tex(width, 0).endVertex();
		worldrenderer.pos(x, y, zLevel).tex(0, 0).endVertex();
		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();

		zLevel -= 500;
	}

	@EventListener
	private void onMouse(MouseEvent event) {
		if (!event.buttonstate)
			return;

		if (player() == null)
			return;

		InventoryPlayer inv = player().inventory;
		if (inv.getCurrentItem() == null)
			return;

		ItemDisplaySetting setting = getSetting(player().getHeldItem());
		if (setting == null)
			return;

		if ((event.button == 0 && setting.leftclick.get()) || (event.button == 1 && setting.rightclick.get()))
			event.setCanceled(true);
	}

	@EventListener
	private void onPacketSend(PacketEvent.PacketSendEvent event) {
		if (!(event.packet instanceof C07PacketPlayerDigging))
			return;

		if (((C07PacketPlayerDigging) event.packet).getStatus() == RELEASE_USE_ITEM)
			return;

		ItemDisplaySetting setting = getSetting(player().getHeldItem());
		if (setting != null && setting.drop.get())
			event.setCanceled(true);
	}

	private ItemDisplaySetting getSetting(ItemStack stack) {
		if (stack == null)
			return null;

		for (SettingsElement element : enabled.getSubSettings().getElements()) {
			if (!(element instanceof ItemDisplaySetting))
				continue;

			ItemDisplaySetting setting = (ItemDisplaySetting) element;

			ItemStack settingStack = setting.getStack();
			if (settingStack.getItem() != stack.getItem())
				continue;

			if (!stack.isItemStackDamageable() && settingStack.getMetadata() != stack.getMetadata())
				continue;

			if (Objects.equals(stack.getTagCompound(), settingStack.getTagCompound()))
				return setting;
		}

		return null;
	}

	private void openSelectGui() {
		ItemSelectGui.open(stack -> {
			ItemStack is = stack.copy();

			if (is.isItemStackDamageable())
				is.setItemDamage(0);

			is.stackSize = 1;

			ListIterator<SettingsElement> iterator = getMainElement().getSubSettings().getElements().listIterator();
			String nbt = is.serializeNBT().toString();

			while (iterator.hasNext()) {
				SettingsElement element = iterator.next();

				if (element instanceof ItemDisplaySetting)
					if (nbt.equals(((ItemDisplaySetting) element).getStack().serializeNBT().toString()))
						return;

				if (element instanceof EntryAddSetting)
					break;
			}

			iterator.previous();
			iterator.add(new ItemDisplaySetting(is));
			onChange();
		});
	}

	public void onChange() {
		mc().currentScreen.initGui();

		JsonObject object = new JsonObject();
		for (SettingsElement element : enabled.getSubSettings().getElements()) {
			if (!(element instanceof ItemDisplaySetting))
				continue;

			ItemDisplaySetting itemDisplaySetting = (ItemDisplaySetting) element;

			JsonObject entry = new JsonObject();
			entry.addProperty("drop", itemDisplaySetting.drop.get());
			entry.addProperty("leftclick", itemDisplaySetting.leftclick.get());
			entry.addProperty("rightclick", itemDisplaySetting.rightclick.get());

			object.add(itemDisplaySetting.getStack().serializeNBT().toString(), entry);
		}

		Config.set(entryKey, object);
		Config.save();
	}

}
