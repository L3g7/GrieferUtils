package dev.l3g7.griefer_utils.labymod.laby4.settings;


import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.WrappedWidget;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

@ExclusiveTo(LABY_4)
public class ActivityInitializeEvent extends Event {

	private final Activity activity;

	private ActivityInitializeEvent(Activity activity) {
		this.activity = activity;
	}

	public Activity getActivity() {
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
	public static void register() {
		Laby4Util.register(net.labymod.api.event.client.gui.screen.ActivityInitializeEvent.class, event -> {
			new ActivityInitializeEvent(event.activity()).fire();
		});
	}

}
