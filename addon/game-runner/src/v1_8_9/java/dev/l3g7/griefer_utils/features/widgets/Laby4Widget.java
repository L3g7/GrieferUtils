/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.api.event_bus.Disableable;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.api.util.StringUtil;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.CategorySettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.SwitchSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.SettingLoader;
import dev.l3g7.griefer_utils.core.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.widgets.Laby4Widget.ModuleConfig;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.hud.position.HudSize;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.navigation.elements.ScreenNavigationElement;
import net.labymod.api.client.gui.screen.ScreenInstance;
import net.labymod.api.client.gui.screen.widget.widgets.hud.HudWidgetWidget;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.core.client.gui.navigation.elements.LabyModNavigationElement;
import net.labymod.core.client.gui.screen.activity.activities.NavigationActivity;
import net.labymod.core.client.gui.screen.activity.activities.labymod.LabyModActivity;
import net.labymod.core.client.gui.screen.activity.activities.labymod.child.WidgetsEditorActivity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@ExclusiveTo(LABY_4)
public abstract class Laby4Widget extends TextHudWidget<ModuleConfig> implements Disableable {

	private static final HudWidgetCategory CATEGORY = new HudWidgetCategory(Laby4Widget.class, Laby4Util.getNamespace()) {
		@Override
		public @NotNull Component title() {
			return Component.text("§l" + Constants.ADDON_NAME);
		}

		@Override
		public @NotNull Component description() {
			return Component.text("\"Erweitert dein Spielerlebnis um wichtige Informationen =D\"\n~ FeuersteinHD");
		}
	};

	private TextLine line;
	private SwitchSettingImpl setting;
	private Object owner; // TODO beautify

	public Laby4Widget() {
		super(UUID.randomUUID().toString(), ModuleConfig.class);
		Reflection.set(this, "id", "griefer_utils_" + StringUtil.convertCasing(getClass().getSimpleName()));

		bindCategory(CATEGORY);
	}

	@Override
	public @NotNull Component displayName() {
		// Use stripped name for settings
		if (!"initialize".equals(new Throwable().getStackTrace()[1].getMethodName()))
			return Component.text(getSetting().name().replaceAll("\n", ""));

		return getSetting().displayName();
	}

	@Override
	public Icon getIcon() {
		return getSetting().getIcon();
	}

	@Override
	public void load(ModuleConfig config) {
		super.load(config);

		// Init settings
		config.setting = getSetting();
		injectSettings();

		// Init text line
		createText();
	}

	// Text line

	protected void createText() {
		Object value = getValue();
		if (value == null)
			value = Component.empty();
		line = createLine(getSetting().name().replaceAll("\n", ""), value);
	}

	@Override
	public void onTick(boolean isEditorContext) {
		Object value = getValue();
		if (value != null)
			line.updateAndFlush(value);
	}

	public Object getValue() {
		return null;
	}

	public void reinitialize() {

		// Check if in widget activity
		if (!(Laby4Util.getActivity() instanceof NavigationActivity navActivity))
			return;

		ScreenNavigationElement element = Reflection.get(navActivity, "element");
		if (!(element instanceof LabyModNavigationElement))
			return;

		LabyModActivity activity = (LabyModActivity) element.getScreen();
		if (activity == null)
			return;

		if (activity.getById("widgets") != activity.getActiveTab())
			return;

		// Reinitialize (if not dragging)
		ScreenInstance instance = Reflection.get(activity.getActiveTab(), "instance");
		WidgetsEditorActivity editor = (WidgetsEditorActivity) instance;

		for (HudWidgetWidget widget : editor.renderer().getChildren()) {
			if (widget.hudWidget() == this) {
				if (!widget.isDragging())
					editor.renderer().reinitializeHudWidget(this, "moved");
				break;
			}
		}
	}

	// Settings

	private SwitchSettingImpl getSetting() {
		if (setting != null)
			return setting;

		BaseSetting<?> setting = SettingLoader.initMainElement(owner, "modules").mainElement;
		if (!(setting instanceof SwitchSettingImpl mainSetting))
			throw new UnsupportedOperationException(setting.getClass().toString());

		this.setting = mainSetting;
		return mainSetting;
	}

	private void injectSettings() {
		if (setting.getSettings().isEmpty())
			return;

		List<Setting> labySettings = getSettings();
		if (!labySettings.isEmpty() && labySettings.get(0) instanceof CategorySetting)
			return;

		CategorySettingImpl wrapper = (CategorySettingImpl) CategorySetting.create()
			.name("Generelle Einstellungen")
			.icon("cog");

		wrapper.addSettings(labySettings);
		labySettings.clear();
		labySettings.add(wrapper);

		labySettings.addAll(setting.getSettings());
		setting.getChildSettings().forEach(s -> s.create(null));
	}

	// Registration

	protected String getComparisonName() {
		return getClass().getPackage().getName() + getSetting().name();
	}

	@OnEnable
	public static void register() {
		Laby.labyAPI().hudWidgetRegistry().categoryRegistry().register(CATEGORY);

		List<Laby4Widget> widgets = new ArrayList<>();

		FileProvider.getClassesWithSuperClass(SimpleWidget.class).stream()
			.map(meta -> (SimpleWidget) FileProvider.getSingleton(meta.load()))
			.map(SimpleLaby4Widget::new)
			.forEach(widgets::add);

		FileProvider.getClassesWithSuperClass(LabyWidget.class).stream()
			.map(meta -> (LabyWidget) FileProvider.getSingleton(meta.load()))
			.map(w -> {
				Laby4Widget widget = w.getVersionedWidget();
				widget.owner = w;
				return widget;
			})
			.forEach(widgets::add);

		widgets.stream()
			.sorted((a, b) -> a.getComparisonName().compareToIgnoreCase(b.getComparisonName())) // TODO grouping?
			.forEach(Laby.labyAPI().hudWidgetRegistry()::register);
	}

	public static class ModuleConfig extends TextHudWidgetConfig {

		private transient SwitchSetting setting;

		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			setting.set(enabled);
		}

		@Override
		public boolean isEnabled() {
			return setting.get();
		}

	}

	public static abstract class CustomRenderTextLine extends TextLine {

		public CustomRenderTextLine(TextHudWidget<?> widget) {
			super(widget, (Component) null, "");
			// Fix LabyMod using line#renderableComponent#getWidth instead of line#getWidth to calculate x offset
			this.renderableComponent = new RenderableComponent(null, null, Style.EMPTY, 0, 0, List.of(), 0) {
				@Override
				public float getWidth() {
					return CustomRenderTextLine.this.getWidth();
				}
			};
		}

		@Override
		protected void flushInternal() {}

		protected RenderableComponent createRenderableComponent(Component c) {
			return RenderableComponent.builder().disableCache().format(c);
		}

		@Override
		public State state() {
			return isAvailable() ? state : State.DISABLED;
		}

		public abstract boolean isAvailable();

		@Override
		public abstract float getWidth();

		@Override
		public abstract void renderLine(Stack stack, float x, float y, float space, HudSize hudWidgetSize);

	}

	private static class SimpleLaby4Widget extends Laby4Widget {

		private final SimpleWidget widget;

		public SimpleLaby4Widget(SimpleWidget widget) {
			this.widget = widget;
			super.owner = widget;
		}

		@Override
		public Object getValue() {
			return Component.text(widget.getValue(), TextColor.color(widget.getColor()));
		}

		@Override
		public boolean isVisibleInGame() {
			return widget.isVisibleInGame();
		}

	}
}
