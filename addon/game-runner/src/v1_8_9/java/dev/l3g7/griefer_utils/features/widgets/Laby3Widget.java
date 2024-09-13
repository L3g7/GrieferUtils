/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.Disableable;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.settings.SettingLoader;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.features.widgets.Widget.ComplexWidget;
import dev.l3g7.griefer_utils.features.widgets.Widget.LabyWidget;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.SwitchSettingImpl;
import net.labymod.core.LabyModCore;
import net.labymod.ingamegui.ModuleCategory;
import net.labymod.ingamegui.ModuleCategoryRegistry;
import net.labymod.ingamegui.ModuleConfig;
import net.labymod.ingamegui.enums.EnumDisplayType;
import net.labymod.ingamegui.enums.EnumModuleFormatting;
import net.labymod.ingamegui.moduletypes.SimpleModule;
import net.labymod.ingamegui.moduletypes.SimpleTextModule;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.settings.elements.CategoryModuleEditorElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;

import java.util.*;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static net.labymod.ingamegui.enums.EnumModuleFormatting.SQUARE_BRACKETS;

@ExclusiveTo(LABY_3)
public abstract class Laby3Widget extends SimpleTextModule implements Disableable, LabyWidget { // TODO simplify

	public static final ModuleCategory CATEGORY = new ModuleCategory(Constants.ADDON_NAME, true, null) {
		@Override
		public void createCategoryElement() {
			rawCategoryElement = new CategoryModuleEditorElement(Constants.ADDON_NAME, new IconData("griefer_utils/icons/icon.png")) {
				// Fix module count
				public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
					this.mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;

					int absoluteY = y + 7;
					DrawUtils draw = LabyMod.getInstance().getDrawUtils();
					draw.drawRectangle(x, y, maxX, maxY, ModColor.toRGB(200, 200, 200, mouseOver ? 50 : 30));
					int imageSize = maxY - y;
					Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/icon.png"));
					LabyMod.getInstance().getDrawUtils().drawTexture(x + 2, y + 2, 256, 256, 18, 18);

					draw.drawString(getDisplayName(), x + imageSize + 5, absoluteY);
					int totalSubCount = 0;
					int enabledCount = 0;

					for (SettingsElement element : getSubSettings().getElements()) {
						if (element instanceof BooleanElement) {
							++totalSubCount;
							if (((BooleanElement) element).getCurrentValue())
								++enabledCount;
						}
					}

					draw.drawRightString(enabledCount + "§7/§f" + totalSubCount, maxX - 5, absoluteY);
				}
			};

			// Populate subSettings
			for (net.labymod.ingamegui.Module module : net.labymod.ingamegui.Module.getModules())
				if (module.getCategory() != null && module.getCategory().equals(this))
					this.rawCategoryElement.getSubSettings().add(module.getBooleanElement().custom("An", "Aus"));

			// Inject headers
			SimpleModule stylingModule = new SimpleModule(){
				public String getDisplayName() { return ""; }
				public String getDisplayValue() { return ""; }
				public String getDefaultValue() { return ""; }
				public String getSettingName() { return ""; }
				public String getDescription() { return null; }
				public void loadSettings() {}
				public int getSortingId() { return 0; }
				public IconData getIconData() { return null; }
				public boolean isEnabled(EnumDisplayType displayType) { return true; }
			};

			List<SettingsElement> elems = CATEGORY.getCategoryElement().getSubSettings().getElements();

			int offset = 0;
			elems.add(offset++, (SettingsElement) HeaderSetting.create().entryHeight(8));
			elems.add(offset++, (SettingsElement) HeaderSetting.create("§r§l" + Constants.ADDON_NAME).scale(1.3));
			elems.add(offset++, (SettingsElement) HeaderSetting.create("Geld-Informationen"));
			elems.add(3 + offset++, (SettingsElement) HeaderSetting.create("Geld-Statistiken"));
			elems.add(6 + offset++, (SettingsElement) HeaderSetting.create("Countdowns"));
			elems.add(10 + offset++, (SettingsElement) HeaderSetting.create("Orb-Statistiken"));
			elems.add(12 + offset, (SettingsElement) HeaderSetting.create("Misc"));

			for (SettingsElement elem : elems)
				if (((ControlElement) elem).getModule() == null)
					Reflection.set(elem, "module", stylingModule);
		}
	};

	private Widget owner;

	@Override
	public void setOwner(Widget widget) {
		this.owner = widget;
	}

	@OnEnable
	public static void register() {
		ModuleCategoryRegistry.loadCategory(CATEGORY);

		FileProvider.getClassesWithSuperClass(Widget.class).stream()
			.filter(meta -> !meta.isAbstract())
			.map(meta -> (Widget) FileProvider.getSingleton(meta.load()))
			.map(Widget::<Laby3Widget>getVersionedWidget)
			.map(Laby3Widget::initModule)
			.sorted(Comparator.comparing(Laby3Widget::getComparisonName))
			.forEach(LabyMod.getInstance().getLabyModAPI()::registerModule);
	}

	@OnStartupComplete
	public static void fixModules() {
		// Fix bug where category doesn't appear in module gui
		if (!ModuleCategoryRegistry.getCategories().contains(CATEGORY))
			ModuleCategoryRegistry.getCategories().add(CATEGORY);

		if (!ModuleCategoryRegistry.ADDON_CATEGORY_LIST.contains(CATEGORY))
			ModuleCategoryRegistry.ADDON_CATEGORY_LIST.add(CATEGORY);

		// Fix the modules' settings not having descriptions
		for (net.labymod.ingamegui.Module module : Laby3Widget.getModules())
			if (module.getCategory() == CATEGORY)
				module.getBooleanElement().setDescriptionText(module.getDescription());
	}

	public SwitchSettingImpl mainElement;
	private String configKey;

	private Laby3Widget initModule() {
		SettingLoader.MainElementData data = SettingLoader.initMainElement(owner, "modules");
		mainElement = (SwitchSettingImpl) data.mainElement;
		configKey = data.configKey;
		return this;
	}

	protected Text toText(String text) {
		return new Text(text, valueColor, bold, italic, underline);
	}

	public String getComparisonName() {
		return owner.getClass().getPackage().getName() + "." + getControlName();
	}

	public String getControlName() { return mainElement.getDisplayName(); }

	public String[] getKeys() { return getDefaultKeys(); }
	public String[] getDefaultKeys() { return new String[]{ mainElement.getDisplayName().replace("\n", "")}; }

	public IconData getIconData() { return mainElement.getIconData(); }
	public String getSettingName() { return configKey; }
	public String getDescription() { return mainElement.getDescriptionText(); }
	public boolean isShown() { return !LabyMod.getInstance().isInGame() || ServerCheck.isOnGrieferGames(); }
	public boolean isEnabled() { return mainElement.get(); }

	public void loadSettings() {}
	public int getSortingId() { return 0; }
	public ModuleCategory getCategory() { return CATEGORY; }
	public EnumModuleFormatting getDisplayFormatting() { return super.getDisplayFormatting(); }

	public void fillSubSettings(List<SettingsElement> list) {
		list.add((SettingsElement) HeaderSetting.create().entryHeight(8));
		list.add((SettingsElement) HeaderSetting.create("§r§l" + Constants.ADDON_NAME).scale(1.3));
		list.add((SettingsElement) HeaderSetting.create(getControlName().replace("\n", "")));
		super.fillSubSettings(list);
		list.add((SettingsElement) HeaderSetting.create());

		getBooleanElement().addCallback(mainElement::set);
		mainElement.callback(b -> Reflection.set(rawBooleanElement, "currentValue", b));
		Reflection.set(rawBooleanElement, "currentValue", mainElement.get());

		List<SettingsElement> settings = mainElement.getSubSettings().getElements();
		if (!settings.isEmpty())
			list.addAll(settings.subList(4, settings.size()));
	}

	public static class ComplexLaby3Widget extends Laby3Widget {

		private final ComplexWidget widget;

		public ComplexLaby3Widget(ComplexWidget widget) {
			this.widget = widget;
		}

		@Override
		public String[] getValues() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String[] getDefaultValues() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isShown() {
			return super.isShown() && widget.isVisibleInGame();
		}

		public void draw(double x, double y, double rightX) {
			List<List<TextPart>> allLines = getTextsParts();
			if (!this.getModuleConfigElement().isUsingExtendedSettings()) {
				this.padding = ModuleConfig.getConfig().getPadding();
				this.backgroundVisible = ModuleConfig.getConfig().isBackgroundVisible();
				if (this.backgroundVisible)
					this.backgroundTransparency = ModuleConfig.getConfig().getBackgroundTransparency();
			}

			if (this.backgroundVisible) {
				int color = this.backgroundColor;
				if (!this.getModuleConfigElement().isUsingExtendedSettings())
					color = ModuleConfig.getConfig().getBackgroundColor();

				int red = 255 & color >> 16;
				int blue = 255 & color;
				int green = 255 & color >> 8;
				color = this.backgroundTransparency << 24 | red << 16 | green << 8 | blue;
				double width = this.getRawWidth() + this.scaleModuleSize((float)this.padding * 2.0F, true);
				double height = this.getRawHeight() + this.scaleModuleSize((float)this.padding * 2.0F, true);
				LabyMod.getInstance().getDrawUtils().drawRect(x - 1.0, y - 1.0, x + width + 1.0, y + height - 1.0, color);
			}

			double paddingSize = this.scaleModuleSize(this.padding, true);
			drawTextParts(allLines, x + paddingSize, y + paddingSize, rightX);
		}

		private List<List<TextPart>> getTextsParts() {
			this.setColors();
			if (!this.getModuleConfigElement().isUsingExtendedSettings())
				this.keyVisible = ModuleConfig.getConfig().isKeyVisible();

			this.setFormattings();
			EnumModuleFormatting displayFormatting = this.getDisplayFormatting();
			List<List<TextPart>> texts = new ArrayList<>();

			for (ComplexWidget.KVPair line : widget.getLines()) {
				IChatComponent key = line.key;
				if (key == null)
					key = new ChatComponentText(mainElement.getDisplayName().replace("\n", ""));

				TextPart value;
				if (line.value instanceof IChatComponent component)
					value = new TextPart(component, -1);
				else
					value = new TextPart(new ChatComponentText(String.valueOf(line.value)), line.color);

				String beforeKey = displayFormatting == SQUARE_BRACKETS ? "[" : "";
				String afterKey = switch (displayFormatting) {
					case DEFAULT -> "";
					case COLON -> ": ";
					case BRACKETS -> "> ";
					case SQUARE_BRACKETS -> "] ";
					case HYPHEN -> " - ";
				};
				texts.add(stylizeTextPart(beforeKey, afterKey, key, value));
			}

			return texts;
		}

		private List<TextPart> stylizeTextPart(String beforeKey, String afterKey, IChatComponent key, TextPart value) {
			List<TextPart> texts = new ArrayList<>();
			int bracketsColor = this.bracketsColor != -1 ? this.bracketsColor : ModuleConfig.getConfig().getBracketsColor();
			int prefixColor = this.prefixColor != -1 ? this.prefixColor : ModuleConfig.getConfig().getPrefixColor();
			int valueColor = this.valueColor != -1 ? this.valueColor : ModuleConfig.getConfig().getValuesColor();

			if (!beforeKey.isEmpty() && keyVisible)
				texts.add(new TextPart(new ChatComponentText(beforeKey), bracketsColor, true));

			if (keyVisible)
				texts.add(new TextPart(key, prefixColor, true));

			if (!afterKey.isEmpty() && keyVisible)
				texts.add(new TextPart(new ChatComponentText(afterKey), bracketsColor, true));

			if (value.color == -1)
				value.color = valueColor;
			texts.add(value);
			return texts;
		}

		private void drawTextParts(List<List<TextPart>> lines, double x, double y, double rightX) {
			boolean rightBound = rightX != -1.0;
			double finalX = x;
			FontRenderer fontRenderer = LabyModCore.getMinecraft().getFontRenderer();

			for (List<TextPart> texts : lines) {
				x = rightBound ? rightX : finalX;

				if (rightBound)
					Collections.reverse(texts);

				for (TextPart text : texts) {
					int stringWidth = fontRenderer.getStringWidth(text.text.getUnformattedText());
					if (rightBound)
						x -= stringWidth;

					LabyMod.getInstance().getDrawUtils().drawStringWithShadow(text.text.getUnformattedText(), x, y, text.color);
					if (!rightBound)
						x += stringWidth;
				}

				y += 10.0;
			}
		}

		@Override
		public int getLines() {
			return widget.getLines().length;
		}

		private class TextPart {
			private final IChatComponent text;
			private int color;

			public TextPart(IChatComponent text, int color) {
				this(text, color, false);
			}

			public TextPart(IChatComponent text, int color, boolean inferStyle) {
				if (inferStyle) {
					if (bold)
						text.getChatStyle().setBold(true);

					if (italic)
						text.getChatStyle().setItalic(true);

					if (underline)
						text.getChatStyle().setUnderlined(true);
				}

				this.text = text;
				this.color = color;
			}

		}

	}

}
