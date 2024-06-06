/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.laby4;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.misc.functions.Function;
import dev.l3g7.griefer_utils.labymod.laby4.events.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.labymod.laby4.settings.BaseSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.HeaderSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.SwitchSettingImpl;
import dev.l3g7.griefer_utils.core.settings.types.*;
import dev.l3g7.griefer_utils.features.item.recraft.laby4.crafter.CraftAction;
import dev.l3g7.griefer_utils.features.item.recraft.laby4.crafter.CraftPlayer;
import dev.l3g7.griefer_utils.features.item.recraft.laby4.crafter.CraftRecorder;
import dev.l3g7.griefer_utils.features.item.recraft.laby4.decompressor.DecompressAction;
import dev.l3g7.griefer_utils.features.item.recraft.laby4.decompressor.DecompressPlayer;
import dev.l3g7.griefer_utils.features.item.recraft.laby4.decompressor.DecompressRecorder;
import dev.l3g7.griefer_utils.features.item.recraft.laby4.recipe.RecipeAction;
import dev.l3g7.griefer_utils.features.item.recraft.laby4.recipe.RecipePlayer;
import dev.l3g7.griefer_utils.features.item.recraft.laby4.recipe.RecipeRecorder;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.configuration.loader.annotation.SpriteTexture;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.accessor.impl.ConfigPropertySettingAccessor;
import net.labymod.api.configuration.settings.type.list.ListSetting;
import net.labymod.api.configuration.settings.type.list.ListSettingConfig;
import net.labymod.api.configuration.settings.type.list.ListSettingEntry;
import net.labymod.api.util.KeyValue;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.features.item.recraft.laby4.RecraftRecording.RecordingMode.CRAFT;
import static dev.l3g7.griefer_utils.features.item.recraft.laby4.RecraftRecording.RecordingMode.RECIPE;
import static net.labymod.api.Textures.SpriteCommon.X;

public class RecraftRecording extends net.labymod.api.configuration.loader.Config implements ListSettingConfig {

	public LinkedList<RecraftAction> actions = new LinkedList<>();
	public ItemStack icon = new ItemStack(Blocks.barrier);

	final StringSetting name = StringSetting.create()
		.name("Name")
		.description("Wie diese Aufzeichnung heißt.")
		.icon(Items.name_tag);

	final KeySetting key = KeySetting.create()
		.name("Taste")
		.description("Mit welcher Taste diese Aufzeichung abgespielt werden soll.")
		.icon("key")
		.pressCallback(pressed -> {
			if (pressed && ServerCheck.isOnCitybuild() && FileProvider.getSingleton(Recraft.class).isEnabled()) {
				play(false);
			}
		});

	final SwitchSetting ignoreSubIds = SwitchSetting.create()
		.name("Sub-IDs ignorieren")
		.description("Ob beim Auswählen der Zutaten die Sub-IDs (z.B. unterschiedliche Holz-Typen) ignoriert werden sollen.")
		.icon(new ItemStack(Blocks.log, 1, 2));

	public final DropDownSetting<RecordingMode> mode = DropDownSetting.create(RecordingMode.class)
		.name("Modus")
		.description("Ob die Aufzeichnung /craft, /rezepte ausführt oder dekomprimiert.")
		.icon("knowledge_book")
		.defaultValue(RECIPE);

	public final SwitchSetting craftAll = SwitchSetting.create()
		.name("Alles vercraften")
		.description("Ob die Aufzeichnung so lange wiederholt werden soll, bis alle Items im Inventar verbraucht wurden.")
		.icon("arrow_circle");

	public final ButtonSetting startRecording = ButtonSetting.create()
		.name("Aufzeichnung starten")
		.icon("camera")
		.callback(this::startRecording);

	public final RecraftSuccessorSetting successor = new RecraftSuccessorSetting();

	public RecraftRecording(String name) {
		this.name.set(name);
		if (!"Leere Aufzeichnung".equals(name)) { // cursed
			this.name.callback(Recraft.pages::notifyChange);
			key.callback(Recraft.pages::notifyChange);
			ignoreSubIds.callback(Recraft.pages::notifyChange);
			mode.callback(Recraft.pages::notifyChange);
			craftAll.callback(Recraft.pages::notifyChange);
			successor.callback(Recraft.pages::notifyChange);
		}
	}

	public void create(Object parent) {
		name.create(parent);
		key.create(parent);
		ignoreSubIds.create(parent);
		mode.create(parent);
		mode.callback(v -> {
			mode.icon(v.icon);
			actions.clear();
			icon = new ItemStack(Blocks.barrier);
		});
		((SwitchSettingImpl) craftAll).setVisibleSupplier(() -> mode.get() == CRAFT);
		craftAll.create(parent);
		startRecording.create(parent);
		successor.create(parent);
	}

	@Override
	public boolean isInvalid() {
		return name.get().isBlank();
	}

	@Override
	public @NotNull Component entryDisplayName() {
		return Component.text(name.get());
	}

	@Override
	public @NotNull List<Setting> toSettings(@Nullable Setting parent, SpriteTexture texture) {
		startRecording.buttonIcon(actions.isEmpty() ? "recording_red" : "recording_white");
		return Arrays.asList((Setting) name, (Setting) key, (Setting) ignoreSubIds, (Setting) mode, (Setting) craftAll, (Setting) startRecording, new HeaderSettingImpl("Nachfolgende Aufzeichnung"), successor);
	}

	public void play(boolean isSuccessor) {
		Recraft.playingSuccessor = isSuccessor;
		Recraft.ignoreSubIds = ignoreSubIds.get();
		mode.get().player.accept(this);
	}

	public boolean playSuccessor() {
		RecraftRecording recording = successor.get();
		if (recording == null)
			return true;

		if (mode.get() == RECIPE || recording.mode.get() == RECIPE) {
			TickScheduler.runAfterClientTicks(() -> recording.play(true), 1);
			return true;
		}

		return !CraftPlayer.play(recording, recording::playSuccessor, false);
	}

	public void startRecording() {
		mode.get().recorder.accept(this);
	}

	public enum RecordingMode implements Named {

		RECIPE("Rezept", "knowledge_book", RecipeRecorder::startRecording, RecipePlayer::play, RecipeAction::fromJson),
		CRAFT("/craft", ItemUtil.createItem(Blocks.crafting_table, 0, true), CraftRecorder::startRecording, CraftPlayer::play, CraftAction::fromJson),
		DECOMPRESS("Dekomprimieren", "chest", DecompressRecorder::startRecording, DecompressPlayer::play, DecompressAction::fromJson);

		private final String displayName;
		private final Object icon;
		private final Consumer<RecraftRecording> recorder, player;
		private final Function<JsonElement, RecraftAction> actionParser;

		RecordingMode(String displayName, Object icon, Consumer<RecraftRecording> recorder, Consumer<RecraftRecording> player, Function<JsonElement, RecraftAction> actionParser) {
			this.displayName = displayName;
			this.icon = icon;
			this.recorder = recorder;
			this.player = player;
			this.actionParser = actionParser;
		}

		@Override
		public String getName() {
			return displayName;
		}

	}

	// NOTE: cleanup? merge?
	@ExclusiveTo(LABY_4)
	public static class RecraftRecordingListSetting extends ListSetting implements BaseSettingImpl<RecraftRecordingListSetting, List<RecraftRecording>> {

		private final ExtendedStorage<List<RecraftRecording>> storage;

		public RecraftRecordingListSetting() {
			this(new ExtendedStorage<>(v -> {
				JsonArray pages = new JsonArray();
				for (RecraftRecording entry : v) {
					JsonObject obj = new JsonObject();
					obj.addProperty("name", entry.name.get());
					obj.add("keys", entry.key.getStorage().encodeFunc.apply(entry.key.get()));

					obj.addProperty("icon", new RecraftAction.Ingredient(entry.icon, entry.icon.stackSize).toLong());

					obj.addProperty("ignore_sub_ids", entry.ignoreSubIds.get());
					obj.addProperty("mode", entry.mode.get().getName());
					obj.addProperty("craftAll", entry.craftAll.get());

					JsonArray jsonActions = new JsonArray();
					for (RecraftAction action : entry.actions)
						jsonActions.add(action.toJson());
					obj.add("actions", jsonActions);

					RecraftRecording recording = entry.successor.get();
					if (recording == null) {
						obj.addProperty("successor", -1);
					} else {
						int pageIdx = 0;

						pageLoop:
						for (RecraftPage page : Recraft.pages.get()) {
							int recIdx = 0;
							for (RecraftRecording rec : page.recordings.get()) {
								if (rec == recording) {
									obj.addProperty("successor", pageIdx << 16 | recIdx);
									break pageLoop;
								}
								recIdx++;
							}

							pageIdx++;
						}
					}


					pages.add(obj);
				}
				return pages;
			}, entries -> {
				List<RecraftRecording> v = new ArrayList<>();
				for (JsonElement elem : entries.getAsJsonArray()) {
					JsonObject object = elem.getAsJsonObject();
					RecraftRecording recording = new RecraftRecording(object.get("name").getAsString());
					recording.key.set(recording.key.getStorage().decodeFunc.apply(object.get("keys")));

					JsonElement icon = object.get("icon");
					if (icon != null) {
						if (icon.isJsonPrimitive()) {
							RecraftAction.Ingredient ingredient = RecraftAction.Ingredient.fromLong(icon.getAsLong());
							recording.icon = new ItemStack(Item.getItemById(ingredient.itemId), ingredient.compression, ingredient.meta);
						} else {
							JsonObject iconObj = icon.getAsJsonObject();
							recording.icon = new ItemStack(Item.getItemById(iconObj.get("id").getAsInt()), iconObj.get("compression").getAsInt(), iconObj.get("meta").getAsInt()); // NOTE: move to ConfigPatcher?
						}
					}

					if (object.has("ignore_sub_ids")) {
						recording.ignoreSubIds.set(object.get("ignore_sub_ids").getAsBoolean());
					}

					if (object.has("mode")) {
						String mode = object.get("mode").getAsString();
						for (RecordingMode recordingMode : RecordingMode.values())
							if (recordingMode.getName().equals(mode))
								recording.mode.set(recordingMode);

						if (recording.mode.get() == RecordingMode.CRAFT)
							recording.craftAll.set(object.get("craftAll").getAsBoolean());
					} else {
						v.add(recording);
						continue;
					}

					JsonArray jsonActions = object.getAsJsonArray("actions");
					for (JsonElement jsonAction : jsonActions)
						recording.actions.add(recording.mode.get().actionParser.apply(jsonAction));


					if (object.has("successor")) {
						int index = object.get("successor").getAsInt();

						// All recordings must have been loaded before the selected one can be loaded
						TickScheduler.runAfterRenderTicks(() -> {
							int pageIdx = 0;

							pageLoop:
							for (RecraftPage page : Recraft.pages.get()) {
								int recIdx = 0;
								for (RecraftRecording rec : page.recordings.get()) {
									if ((pageIdx << 16 | recIdx) == index) {
										recording.successor.set(rec);
										break pageLoop;
									}
									recIdx++;
								}

								pageIdx++;
							}
						}, 1);

					}

					v.add(recording);
				}
				return v;
			}, new ArrayList<>()));
			EventRegisterer.register(this);
		}

		public RecraftRecordingListSetting(ExtendedStorage<List<RecraftRecording>> storage) {
			super(UUID.randomUUID().toString(), null, null, new String[0], null, false, null, (byte) -127,
				new ConfigPropertySettingAccessor(null, null, null, null) {
					@Override
					public <T> T get() {
						return c(storage.value == null ? storage.fallbackValue : storage.value);
					}

					@Override
					public <T> void set(T value) {
						List<RecraftRecording> list = get();
						ArrayList<RecraftRecording> val = c(value);
						list.clear();
						list.addAll(val);
					}

					@Override
					public Type getGenericType() {
						return new ParameterizedType() {
							public Type[] getActualTypeArguments() {return new Type[]{RecraftRecording.class};}

							public Type getRawType() {return null;}

							public Type getOwnerType() {return null;}
						};
					}
				}
			);

			this.storage = storage;
		}

		@Override
		public ListSettingEntry createNew() {
			RecraftRecording config = new RecraftRecording("Unbenannte Aufzeichnung");
			get().add(config);

			ListSettingEntry entry = new ListSettingEntry(this, config.entryDisplayName(), get().size()) {
				public Icon getIcon() {
					return SettingsImpl.buildIcon(config.icon);
				}
			};

			entry.addSettings(config);
			config.create(entry);
			return entry;
		}

		@Override
		public List<KeyValue<Setting>> getElements() {
			List<KeyValue<Setting>> list = new ArrayList<>();

			List<RecraftRecording> entries = get();

			for (int i = 0; i < entries.size(); ++i) {
				RecraftRecording config = entries.get(i);
				if (config.isInvalid()) {
					entries.remove(i--);
				} else {
					config.create(this);
					ListSettingEntry entry = new ListSettingEntry(this, config.entryDisplayName(), i) {
						@Override
						public Icon getIcon() {
							return SettingsImpl.buildIcon(config.icon);
						}
					};
					entry.addSettings(config);
					list.add(new KeyValue<>(entry.getId(), entry));
				}
			}

			return list;
		}

		@Override
		public Component displayName() {
			return Component.text(name());
		}

		@Override
		public Icon getIcon() {
			return storage.icon;
		}

		@Override
		public ExtendedStorage<List<RecraftRecording>> getStorage() {
			return storage;
		}

		@EventListener
		private void onInit(SettingActivityInitEvent event) {
			if (event.holder() != this)
				return;

			// Update entry widgets
			for (Widget w : event.settings().getChildren()) {
				if (w instanceof SettingWidget s && s.setting() instanceof ListSettingEntry entry) {
					SettingsImpl.hookChildAdd(s, e -> {
						if (e.childWidget() instanceof FlexibleContentWidget content) {
							// Update button icons
							ButtonWidget btn = (ButtonWidget) content.getChild("advanced-button").childWidget();
							btn.updateIcon(SettingsImpl.buildIcon("pencil_vec")); // NOTE: use original icons?
							content.removeChild("delete-button");

							content.addContent(ButtonWidget.icon(X, () -> {
								get().remove(entry.listIndex());
								notifyChange();
								event.activity.reload();
							}).addId("delete-button"));
						}
					});
				}
			}
		}

	}
}
