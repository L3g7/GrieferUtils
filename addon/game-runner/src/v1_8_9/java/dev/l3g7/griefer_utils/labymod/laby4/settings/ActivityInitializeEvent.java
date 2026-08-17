package dev.l3g7.griefer_utils.labymod.laby4.settings;


import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.WrappedWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.SettingElement;
import net.labymod.core.client.gui.screen.activity.activities.labymod.child.mods.ModsActivity;

import java.util.Deque;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

@ExclusiveTo(LABY_4)
public class ActivityInitializeEvent<A extends Activity> extends Event {

	private final A activity;

	private ActivityInitializeEvent(A activity) {
		this.activity = activity;
	}

	public A getActivity() {
		return activity;
	}

	public <T extends Widget> T get(String... idPath) {
		Widget widget = activity.document();
		for (String id : idPath) {
			widget = ((AbstractWidget<?>) widget).getChild(id);
			if (widget == null)
				return null;

			//noinspection deprecation
			if (widget instanceof WrappedWidget ww)
				widget = ww.childWidget();
		}
		return c(widget);
	}

	@OnEnable
	private static void register() {
		Laby4Util.register(net.labymod.api.event.client.gui.screen.ActivityInitializeEvent.class, event ->
			new ActivityInitializeEvent<>(event.activity()).fire());
	}

	public static class SettingActivityInitEvent extends ActivityInitializeEvent<ModsActivity> {
		private final Setting parent;

		private SettingActivityInitEvent(ModsActivity activity) {
			super(activity);
			Deque<SettingElement> openSettings = Reflection.get(activity, "openSettings");
			if (openSettings.peekFirst() == null)
				parent = MainPage.rootSetting;
			else
				parent = openSettings.peekFirst();
		}

		public Setting parent() {
			return parent;
		}

		public VerticalListWidget<Widget> settings() {
			return get("container", "mods-options-scroll", "mods-options-list");
		}

		@EventListener
		private static void register(ActivityInitializeEvent<ModsActivity> event) {
			new SettingActivityInitEvent(event.activity).fire();
		}
	}

}
