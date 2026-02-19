package dev.l3g7.griefer_utils.labymod.laby4.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.settings.AbstractSetting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.ButtonSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.activity.activities.labymod.child.SettingContentActivity;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.accessor.impl.ConfigPropertySettingAccessor;
import net.labymod.api.configuration.settings.type.SettingPermissionHolder;
import net.labymod.api.configuration.settings.type.list.ListSetting;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static net.labymod.api.Textures.SpriteCommon.X;

public abstract class AbstractListSettingImpl<S extends AbstractSetting<S, List<V>>, V> extends ListSetting implements Laby4Setting<S, List<V>> {

	private final ExtendedStorage<List<V>> storage;

	public AbstractListSettingImpl() {
		super(UUID.randomUUID().toString(), null, null, new String[0], (SettingPermissionHolder) null, null, (byte) -127,
			new ConfigPropertySettingAccessor(null, null, null, null) {
				@Override
				public <T> T get() {
					return c(new ArrayList<>());
				}

				@Override
				public Type getGenericType() {
					return new ParameterizedType() {
						public Type[] getActualTypeArguments() {return new Type[]{Void.class};}

						public Type getRawType() {return null;}

						public Type getOwnerType() {return null;}
					};
				}
			}
		);

		storage = new ExtendedStorage<>(entries -> {
			JsonArray array = new JsonArray();
			for (V entry : entries)
				array.add(encode(entry));

			return array;
		}, elem -> {
			List<V> entries = new ArrayList<>();
			for (JsonElement entry : elem.getAsJsonArray())
				entries.add(decode(entry));

			return entries;
		}, new ArrayList<>());

		EventRegisterer.register(this);
		init();
	}

	protected abstract JsonElement encode(V value);

	protected abstract V decode(JsonElement value);

	protected abstract void edit(int editIndex, SettingContentActivity parent);

	protected abstract void add(SettingContentActivity parent);

	protected abstract String getName(V entry);

	protected abstract Icon getIcon(V entry);

	@Override
	public Component displayName() {
		return Component.text(name());
	}

	@Override
	public Component getDescription() {
		String description = storage.description;
		return description == null ? null : Component.text(description);
	}

	@Override
	public Icon getIcon() {
		return getStorage().icon;
	}

	@Override
	public ExtendedStorage<List<V>> getStorage() {
		return storage;
	}

	@EventListener
	private void onInit(SettingActivityInitEvent event) {
		if (event.holder() != this)
			return;

		List<V> values = get();

		// Add entries
		for (int i = 0; i < values.size(); i++) {
			V value = values.get(i);

			ButtonSettingImpl entry = new ButtonSettingImpl();
			entry.name(getName(value));
			entry.icon(getIcon(value));

			entry.setParent((Setting) this);

			int idx = i;
			event.settings().addChild(entry.createUnwrappedWidget(
				ButtonWidget.icon(
					Icons.of(Laby4Util.isVanillaTheme() ? "pencil_padded" : "high_res/pencil_vec"),
					() -> edit(idx, event.activity)
				).addId("delete-button"), // Actually an edit button, but id is required for styling

				ButtonWidget.icon(X, () -> {
					values.remove(idx);
					notifyChange();
					event.activity.reload();
				}).addId("delete-button")
			));
		}

		// Hook add button
		event.get("setting-header", "add-button").setPressable(() -> add(event.activity));
	}

}
