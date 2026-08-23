package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.gui;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.injection.InheritedInvoke;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.CommandSuggestions;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.CommandDispatcher;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.CommandDispatcher.Source;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.suggestions.minecraft.PlayerNameSuggestionProvider;
import net.labymod.ingamechat.GuiChatCustom;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.CommandDispatcher.SOURCE;

/**
 * Backport of 1.13.2 GuiChat to 1.8.9.
 */
@SuppressWarnings("SameParameterValue")
public class GuiChatShim {

	private final CommandDispatcher dispatcher;
	private final GuiChat gui;
	private final String defaultInput;
	public TextFieldShim inputField;

	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
	protected final List<String> commandUsage = Lists.newArrayList();
	protected int commandUsagePosition;
	protected int commandUsageWidth;
	private ParseResults<Source> currentParse;
	private CompletableFuture<Suggestions> pendingSuggestions;
	private SuggestionsList suggestions;
	private boolean hasEdits;
	private boolean isInsert;

	public GuiChatShim(CommandDispatcher dispatcher, GuiChat gui, String defaultInput) {
		this.dispatcher = dispatcher;
		this.gui = gui;
		this.defaultInput = defaultInput;
	}

	public interface DrawHoveringTextAccessor {
		void grieferUtils$renderHoveringText(List<String> lvt_1_1_, int lvt_2_1_, int lvt_3_1_);
	}

	@Mixin(GuiScreen.class)
	public static abstract class MixinGuiScreen implements DrawHoveringTextAccessor {
		@Shadow
		protected abstract void drawHoveringText(List<String> text, int x, int y);

		@Override
		public void grieferUtils$renderHoveringText(List<String> text, int x, int y) {
			this.drawHoveringText(text, x, y);
		}
	}

	public interface ShimAccessor {
		GuiChatShim grieferUtils$getShim();
	}

	@Mixin(GuiChat.class)
	public static class MixinGuiChat implements ShimAccessor {

		@Unique
		private GuiChatShim grieferUtils$shim;

		@Shadow
		protected GuiTextField inputField;

		@Shadow
		private String defaultInputFieldText;

		@Inject(method = "initGui", at = @At("TAIL"))
		public void onInitGui(CallbackInfo ci) {
			GuiChat instance = (GuiChat) (Object) this;
			if (grieferUtils$shim == null) {
				CommandDispatcher dispatcher = CommandSuggestions.getDispatcher();
				if (dispatcher != null)
					grieferUtils$shim = new GuiChatShim(dispatcher, instance, defaultInputFieldText);
				else
					return;
			}

			TextFieldShim inputField = new TextFieldShim(0, mc().fontRendererObj, 4, instance.height - 12, instance.width - 4, 12);
			this.inputField = inputField;
			this.inputField.setMaxStringLength(100);
			this.inputField.setEnableBackgroundDrawing(false);
			this.inputField.setFocused(true);
			this.inputField.setText(this.defaultInputFieldText);
			this.inputField.setCanLoseFocus(false);
			grieferUtils$shim.inputField = inputField;

			inputField.setTextFormatter(grieferUtils$shim::formatMessage);
			inputField.setTextAcceptHandler(grieferUtils$shim::acceptMessage);
			grieferUtils$shim.updateSuggestion();
		}

		@Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
		public void keyPressed(char lvt_1_1_, int lvt_2_1_, CallbackInfo ci) {
			if (grieferUtils$shim != null && grieferUtils$shim.keyPressed())
				ci.cancel();
		}

		@Inject(method = "handleMouseInput", at = @At("HEAD"), cancellable = true)
		public void handleMouseInput(CallbackInfo ci) {
			if (grieferUtils$shim != null && grieferUtils$shim.handleMouse())
				ci.cancel();
		}

		@Inject(method = "drawScreen", at = @At("HEAD"))
		public void draw(CallbackInfo ci) {
			if (grieferUtils$shim != null)
				grieferUtils$shim.render();
		}

		@Override
		public GuiChatShim grieferUtils$getShim() {
			return grieferUtils$shim;
		}
	}

	@ExclusiveTo(LABY_3)
	@Mixin(GuiChatCustom.class)
	public static class MixinGuiChatCustom {

		@InheritedInvoke(GuiChat.class)
		@Inject(method = "drawScreen", at = @At("HEAD"))
		public void draw(CallbackInfo ci) {
			GuiChatShim shim = ((ShimAccessor) this).grieferUtils$getShim();
			if (shim != null)
				shim.render();
		}
	}

	public boolean keyPressed() {
		if (suggestions != null && suggestions.keyPressed(Keyboard.getEventKey()))
			return true;

		if (Keyboard.getEventKey() == Keyboard.KEY_TAB) {
			hasEdits = true;
			showSuggestions();
			return true;
		}

		return false;
	}

	public boolean handleMouse() {
		// Scroll
		int delta = Mouse.getEventDWheel();
		if (delta != 0) {
			delta = MathHelper.clamp_int(delta, -1, 1);
			if (suggestions != null && suggestions.mouseScrolled(delta))
				return true;
		}

		// Click
		if (suggestions != null) {
			final int mouseX = Mouse.getX() * MinecraftUtil.screenWidth() / mc().displayWidth;
			final int mouseY = MinecraftUtil.screenHeight() - Mouse.getY() * MinecraftUtil.screenHeight() / mc().displayHeight - 1;
			return suggestions.mouseClicked(mouseX, mouseY);
		}

		return false;
	}

	public void render() {
		if (suggestions != null) {
			final int mouseX = Mouse.getX() * MinecraftUtil.screenWidth() / mc().displayWidth;
			final int mouseY = MinecraftUtil.screenHeight() - Mouse.getY() * MinecraftUtil.screenHeight() / mc().displayHeight - 1;
			suggestions.render(mouseX, mouseY);
		} else {
			int idx = 0;

			for (String lvt_6_1_ : commandUsage) {
				Gui.drawRect(commandUsagePosition - 1, gui.height - 14 - 13 - 12 * idx, commandUsagePosition + commandUsageWidth + 1, gui.height - 2 - 13 - 12 * idx, -16777216);
				mc().fontRendererObj.drawStringWithShadow(lvt_6_1_, (float) commandUsagePosition, (float) (gui.height - 14 - 13 + 2 - 12 * idx), -1);
				idx++;
			}
		}
	}

	public String formatMessage(String message, int cursorPos) {
		if (this.currentParse == null)
			return message;

		EnumChatFormatting[] colors = new EnumChatFormatting[]{EnumChatFormatting.AQUA, EnumChatFormatting.YELLOW, EnumChatFormatting.GREEN, EnumChatFormatting.LIGHT_PURPLE, EnumChatFormatting.GOLD};
		StringBuilder result = new StringBuilder(EnumChatFormatting.GRAY.toString());
		int start = 0;
		int idx = -1;
		CommandContextBuilder<Source> node = this.currentParse.getContext().getLastChild();

		for (ParsedArgument<Source, ?> arg : node.getArguments().values()) {
			idx++;
			if (idx >= colors.length)
				idx = 0;

			int cursorEnd = Math.max(arg.getRange().getStart() - cursorPos, 0);
			if (cursorEnd >= message.length())
				break;

			int cursorStart = Math.min(arg.getRange().getEnd() - cursorPos, message.length());
			if (cursorStart > 0) {
				result.append(message, start, cursorEnd);
				result.append(colors[idx]);
				result.append(message, cursorEnd, cursorStart);
				result.append(EnumChatFormatting.GRAY);
				start = cursorStart;
			}
		}

		if (this.currentParse.getReader().canRead()) {
			int cursorEnd = Math.max(this.currentParse.getReader().getCursor() - cursorPos, 0);
			if (cursorEnd < message.length()) {
				int cursorStart = Math.min(cursorEnd + this.currentParse.getReader().getRemainingLength(), message.length());
				result.append(message, start, cursorEnd);
				result.append(EnumChatFormatting.RED);
				result.append(message, cursorEnd, cursorStart);
				start = cursorStart;
			}
		}

		result.append(message, start, message.length());
		return result.toString();
	}

	public void acceptMessage() {
		hasEdits = !inputField.getText().equals(defaultInput);
		updateSuggestion();
	}

	private void showSuggestions() {
		if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
			int textWidth = 0;
			Suggestions newSuggestions = this.pendingSuggestions.join();
			if (!newSuggestions.getList().isEmpty()) {
				for (Suggestion suggestion : newSuggestions.getList())
					textWidth = Math.max(textWidth, mc().fontRendererObj.getStringWidth(suggestion.getText()));

				int start = MathHelper.clamp_int(getUsagePosition(newSuggestions.getRange().getStart()), 0, gui.width - textWidth);
				suggestions = new SuggestionsList(start, gui.height - 12, textWidth, newSuggestions);
			}
		}
	}

	private static int getLastWordIndex(String p_208603_0_) {
		if (Strings.isNullOrEmpty(p_208603_0_)) {
			return 0;
		} else {
			int lvt_1_1_ = 0;

			Matcher lvt_2_1_ = WHITESPACE_PATTERN.matcher(p_208603_0_);
			while (lvt_2_1_.find())
				lvt_1_1_ = lvt_2_1_.end();

			return lvt_1_1_;
		}
	}

	public void updateSuggestion() {
		this.currentParse = null;
		if (!this.isInsert) {
			inputField.setSuggestion(null);
			suggestions = null;
		}

		commandUsage.clear();
		String lvt_1_1_ = this.inputField.getText();
		StringReader lvt_2_1_ = new StringReader(lvt_1_1_);
		if (lvt_2_1_.canRead() && lvt_2_1_.peek() == '/') {
			lvt_2_1_.skip();
			this.currentParse = dispatcher.parse(lvt_2_1_, SOURCE);
			if (suggestions == null || !this.isInsert) {
				StringReader lvt_4_1_ = new StringReader(lvt_1_1_.substring(0, Math.min(lvt_1_1_.length(), this.inputField.getCursorPosition())));
				if (lvt_4_1_.canRead() && lvt_4_1_.peek() == '/') {
					lvt_4_1_.skip();
					ParseResults<Source> lvt_5_1_ = dispatcher.parse(lvt_4_1_, SOURCE);
					this.pendingSuggestions = dispatcher.getCompletionSuggestions(lvt_5_1_);
					this.pendingSuggestions.thenRun(() -> {
						if (this.pendingSuggestions.isDone()) {
							this.updateUsageInfo();
						}
					});
				}
			}
		} else {
			int lvt_4_2_ = getLastWordIndex(lvt_1_1_);
			this.pendingSuggestions = suggestNames(new SuggestionsBuilder(lvt_1_1_, lvt_4_2_));
		}

	}

	private static CompletableFuture<Suggestions> suggestNames(SuggestionsBuilder builder) {
		String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

		return PlayerNameSuggestionProvider.request(remaining).thenApply(matches -> {
			for (String name : matches)
				if (name.toLowerCase(Locale.ROOT).startsWith(remaining))
					builder.suggest(name);

			return builder.build();
		});
	}

	private void updateUsageInfo() {
		if (this.pendingSuggestions.join().isEmpty() && !this.currentParse.getExceptions().isEmpty() && this.inputField.getCursorPosition() == this.inputField.getText().length()) {
			int lvt_1_1_ = 0;

			for (Map.Entry<CommandNode<Source>, CommandSyntaxException> lvt_3_1_ : this.currentParse.getExceptions().entrySet()) {
				CommandSyntaxException lvt_4_1_ = lvt_3_1_.getValue();
				if (lvt_4_1_.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect()) {
					++lvt_1_1_;
				} else {
					commandUsage.add(lvt_4_1_.getMessage());
				}
			}

			if (lvt_1_1_ > 0) {
				commandUsage.add(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create().getMessage());
			}
		}

		commandUsagePosition = 0;
		commandUsageWidth = gui.width;
		if (commandUsage.isEmpty()) {
			this.fillNodeUsage(EnumChatFormatting.GRAY);
		}

		suggestions = null;
		if (this.hasEdits) {
			this.showSuggestions();
		}

	}

	private void fillNodeUsage(EnumChatFormatting p_195132_1_) {
		CommandContextBuilder<Source> ctx = this.currentParse.getContext();
		CommandContextBuilder<Source> lastChild = ctx.getLastChild();
		if (!lastChild.getNodes().isEmpty()) {
			CommandNode<Source> lastNode;
			int cursorPos;

			if (this.currentParse.getReader().canRead()) {
				ParsedCommandNode<Source> node = Iterables.getLast(lastChild.getNodes());
				lastNode = node.getNode();
				cursorPos = node.getRange().getEnd() + 1;
			} else if (lastChild.getNodes().size() > 1) {
				ParsedCommandNode<Source> node = Iterables.get(lastChild.getNodes(), lastChild.getNodes().size() - 2);
				lastNode = node.getNode();
				cursorPos = node.getRange().getEnd() + 1;
			} else {
				if (ctx == lastChild || lastChild.getNodes().isEmpty()) {
					return;
				}

				ParsedCommandNode<Source> node = Iterables.getLast(lastChild.getNodes());
				lastNode = node.getNode();
				cursorPos = node.getRange().getEnd() + 1;
			}

			Map<CommandNode<Source>, String> usage = dispatcher.getSmartUsage(lastNode, SOURCE);
			List<String> usageText = Lists.newArrayList();
			int width = 0;

			for (Map.Entry<CommandNode<Source>, String> lvt_10_1_ : usage.entrySet()) {
				if (!(lvt_10_1_.getKey() instanceof LiteralCommandNode)) {
					usageText.add(p_195132_1_ + lvt_10_1_.getValue());
					width = Math.max(width, mc().fontRendererObj.getStringWidth(lvt_10_1_.getValue()));
				}
			}

			if (!usageText.isEmpty()) {
				commandUsage.addAll(usageText);
				commandUsagePosition = MathHelper.clamp_int(getUsagePosition(cursorPos) + mc().fontRendererObj.getStringWidth(" "), 0, gui.width - width);
				commandUsageWidth = width;
			}
		}
	}

	private int getUsagePosition(int cursorPos) {
		return cursorPos > this.inputField.getText().length() ? this.inputField.xPosition : this.inputField.xPosition + mc().fontRendererObj.getStringWidth(this.inputField.getText().substring(0, cursorPos));
	}

	private static String calculateSuggestionSuffix(String p_208602_0_, String p_208602_1_) {
		return p_208602_1_.startsWith(p_208602_0_) ? p_208602_1_.substring(p_208602_0_.length()) : null;
	}

	private void setChatLine(String p_208604_1_) {
		this.inputField.setText(p_208604_1_);
	}

	record Rectangle2d(int x, int y, int width, int height) {
		public boolean contains(int p_199315_1_, int p_199315_2_) {
			return p_199315_1_ >= this.x && p_199315_1_ <= this.x + this.width && p_199315_2_ >= this.y && p_199315_2_ <= this.y + this.height;
		}
	}

	class SuggestionsList {
		private final Rectangle2d bounds;
		private final Suggestions suggestions;
		private final String text;
		private int field_198507_d;
		private int selected;
		private Vector2f field_198509_f;
		private boolean cycling;

		private SuggestionsList(int p_i47700_2_, int p_i47700_3_, int p_i47700_4_, Suggestions p_i47700_5_) {
			this.field_198509_f = new Vector2f();
			this.bounds = new Rectangle2d(p_i47700_2_ - 1, p_i47700_3_ - 3 - Math.min(p_i47700_5_.getList().size(), 10) * 12, p_i47700_4_ + 1, Math.min(p_i47700_5_.getList().size(), 10) * 12);
			suggestions = p_i47700_5_;
			this.text = inputField.getText();
			this.select(0);
		}

		public void render(int p_198500_1_, int p_198500_2_) {
			int lvt_3_1_ = Math.min(suggestions.getList().size(), 10);
			boolean lvt_5_1_ = this.field_198507_d > 0;
			boolean lvt_6_1_ = suggestions.getList().size() > this.field_198507_d + lvt_3_1_;
			boolean lvt_7_1_ = lvt_5_1_ || lvt_6_1_;
			boolean lvt_8_1_ = this.field_198509_f.x != (float) p_198500_1_ || this.field_198509_f.y != (float) p_198500_2_;
			if (lvt_8_1_) {
				this.field_198509_f = new Vector2f((float) p_198500_1_, (float) p_198500_2_);
			}

			if (lvt_7_1_) {
				Gui.drawRect(this.bounds.x(), this.bounds.y() - 1, this.bounds.x() + this.bounds.width(), this.bounds.y(), -805306368);
				Gui.drawRect(this.bounds.x(), this.bounds.y() + this.bounds.height(), this.bounds.x() + this.bounds.width(), this.bounds.y() + this.bounds.height() + 1, -805306368);
				if (lvt_5_1_) {
					for (int lvt_9_1_ = 0; lvt_9_1_ < this.bounds.width(); ++lvt_9_1_) {
						if (lvt_9_1_ % 2 == 0) {
							Gui.drawRect(this.bounds.x() + lvt_9_1_, this.bounds.y() - 1, this.bounds.x() + lvt_9_1_ + 1, this.bounds.y(), -1);
						}
					}
				}

				if (lvt_6_1_) {
					for (int lvt_9_2_ = 0; lvt_9_2_ < this.bounds.width(); ++lvt_9_2_) {
						if (lvt_9_2_ % 2 == 0) {
							Gui.drawRect(this.bounds.x() + lvt_9_2_, this.bounds.y() + this.bounds.height(), this.bounds.x() + lvt_9_2_ + 1, this.bounds.y() + this.bounds.height() + 1, -1);
						}
					}
				}
			}

			boolean lvt_9_3_ = false;

			for (int lvt_10_1_ = 0; lvt_10_1_ < lvt_3_1_; ++lvt_10_1_) {
				Suggestion lvt_11_1_ = suggestions.getList().get(lvt_10_1_ + this.field_198507_d);
				Gui.drawRect(this.bounds.x(), this.bounds.y() + 12 * lvt_10_1_, this.bounds.x() + this.bounds.width(), this.bounds.y() + 12 * lvt_10_1_ + 12, -805306368);
				if (p_198500_1_ > this.bounds.x() && p_198500_1_ < this.bounds.x() + this.bounds.width() && p_198500_2_ > this.bounds.y() + 12 * lvt_10_1_ && p_198500_2_ < this.bounds.y() + 12 * lvt_10_1_ + 12) {
					if (lvt_8_1_) {
						this.select(lvt_10_1_ + this.field_198507_d);
					}

					lvt_9_3_ = true;
				}

				mc().fontRendererObj.drawStringWithShadow(lvt_11_1_.getText(), (float) (this.bounds.x() + 1), (float) (this.bounds.y() + 2 + 12 * lvt_10_1_), lvt_10_1_ + this.field_198507_d == this.selected ? -256 : -5592406);
			}

			if (lvt_9_3_) {
				Message lvt_10_2_ = suggestions.getList().get(this.selected).getTooltip();
				if (lvt_10_2_ != null) {
					((DrawHoveringTextAccessor) gui).grieferUtils$renderHoveringText(Collections.singletonList(toTextComponent(lvt_10_2_).getFormattedText()), p_198500_1_, p_198500_2_);
				}
			}

		}

		public static IChatComponent toTextComponent(Message p_202465_0_) {
			return p_202465_0_ instanceof IChatComponent ? (IChatComponent) p_202465_0_ : new ChatComponentText(p_202465_0_.getString());
		}

		public boolean mouseClicked(int x, int y) {
			if (!this.bounds.contains(x, y)) {
				return false;
			} else {
				int lvt_4_1_ = (y - this.bounds.y()) / 12 + this.field_198507_d;
				if (lvt_4_1_ >= 0 && lvt_4_1_ < suggestions.getList().size()) {
					this.select(lvt_4_1_);
					this.useSuggestion();
				}

				return true;
			}
		}

		public boolean mouseScrolled(double p_198498_1_) {
			int lvt_3_1_ = (int) (Mouse.getEventX() * MinecraftUtil.currentResolution.getScaledWidth_double() / (double) mc().displayWidth);
			int lvt_4_1_ = (int) (Mouse.getEventY() * MinecraftUtil.currentResolution.getScaledHeight_double() / (double) mc().displayHeight);
			if (this.bounds.contains(lvt_3_1_, lvt_4_1_)) {
				this.field_198507_d = MathHelper.clamp_int((int) ((double) this.field_198507_d - p_198498_1_), 0, Math.max(suggestions.getList().size() - 10, 0));
				return true;
			} else {
				return false;
			}
		}

		public boolean keyPressed(int key) {
			if (key == Keyboard.KEY_UP) {
				this.cycle(-1);
				this.cycling = false;
				return true;
			} else if (key == Keyboard.KEY_DOWN) {
				this.cycle(1);
				this.cycling = false;
				return true;
			} else if (key == Keyboard.KEY_TAB) {
				if (this.cycling) {
					this.cycle(GuiScreen.isShiftKeyDown() ? -1 : 1);
				}

				this.useSuggestion();
				return true;
			} else if (key == Keyboard.KEY_ESCAPE) {
				this.hide();
				return true;
			} else {
				return false;
			}
		}

		public void cycle(int delta) {
			this.select(this.selected + delta);
			int lvt_2_1_ = this.field_198507_d;
			int lvt_3_1_ = this.field_198507_d + 10 - 1;
			if (this.selected < lvt_2_1_) {
				this.field_198507_d = MathHelper.clamp_int(this.selected, 0, Math.max(suggestions.getList().size() - 10, 0));
			} else if (this.selected > lvt_3_1_) {
				this.field_198507_d = MathHelper.clamp_int(this.selected + 1 - 10, 0, Math.max(suggestions.getList().size() - 10, 0));
			}

		}

		public void select(int p_199675_1_) {
			this.selected = p_199675_1_;
			if (this.selected < 0) {
				this.selected += suggestions.getList().size();
			}

			if (this.selected >= suggestions.getList().size()) {
				this.selected -= suggestions.getList().size();
			}

			Suggestion lvt_2_1_ = suggestions.getList().get(this.selected);
			inputField.setSuggestion(calculateSuggestionSuffix(inputField.getText(), lvt_2_1_.apply(this.text)));
		}

		public void useSuggestion() {
			Suggestion lvt_1_1_ = suggestions.getList().get(this.selected);
			isInsert = true;
			setChatLine(lvt_1_1_.apply(this.text));
			int lvt_2_1_ = lvt_1_1_.getRange().getStart() + lvt_1_1_.getText().length();
			inputField.setCursorPosition(lvt_2_1_);
			inputField.setSelectionPos(lvt_2_1_);
			this.select(this.selected);
			isInsert = false;
			this.cycling = true;

			updateSuggestion();
		}

		public void hide() {
			GuiChatShim.this.suggestions = null;
		}
	}

}
