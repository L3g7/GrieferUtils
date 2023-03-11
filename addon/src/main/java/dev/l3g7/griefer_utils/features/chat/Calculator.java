/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.chat;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.player.scoreboard.BankScoreboard;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import net.labymod.utils.Material;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.mariuszgromada.math.mxparser.Expression;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

@Singleton
public class Calculator extends Feature {

	private final DropDownSetting<WithdrawAction> autoWithdraw = new DropDownSetting<>(WithdrawAction.class)
		.name("Auto. abheben")
		.icon("bank")
		.defaultValue(WithdrawAction.SUGGEST);

	private final BooleanSetting depositAll = new BooleanSetting()
		.name("* einzahlen")
		.description("Aktiviert den * Placeholder, mit dem sich das gesamte Guthaben einzahlen lässt.")
		.icon("bank")
		.defaultValue(true);

	private final BooleanSetting placeholder = new BooleanSetting()
		.name("Placeholder in Nachrichten")
		.description("Ermöglicht in einer Nachricht eingebettete Gleichungen, indem sie mit {} eingerahmt werden.")
		.icon("regex")
		.defaultValue(true);

	private final BooleanSetting autoEquationDetect = new BooleanSetting()
		.name("Automatische Gleichungserkennung")
		.description("Erkennt automatisch in einer Nachricht eingebettete Gleichungen, auch wenn sie nicht mit {} eingerahmt sind.")
		.icon("regex")
		.defaultValue(true);

	private final NumberSetting decimalPlaces = new NumberSetting()
		.name("Nachkommastellen")
		.description("Auf wie viele Nachkommastellen das Ergebnis gerundet werden soll")
		.icon(Material.STONE_BUTTON)
		.defaultValue(2)
		.min(0).max(98);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Rechner")
		.description("Ein Rechner in Nachrichten.")
		.icon("calculator")
		.subSettings(decimalPlaces, new HeaderSetting(),
			autoWithdraw, depositAll, placeholder, autoEquationDetect);

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(?<equation>[^}]*)}");
	private static final Pattern SIMPLE_EQUATION_PATTERN = Pattern.compile("(?<= )(?<equation>\\d+(?:(?: *[*k+\\-:/^,e]+ *\\d+k?)+|k+))(?:(?= )|$)");
	private static final BigDecimal THOUSAND = new BigDecimal(1000);
	private BigDecimal lastPayment = BigDecimal.ZERO;
	private String lastPaymentReceiver;

	/**
	 * Get the current balance based on the scoreboard value
	 */
	private BigDecimal getCurrentBalance() {
		return new BigDecimal(world().getScoreboard().getTeam("money_value").getColorPrefix().replaceAll("[$.]", "").replace(",", "."));
	}


	@EventListener
	public void onMessageReceive(ClientChatReceivedEvent event) {
		if (!ServerCheck.isOnGrieferGames())
			return;

		/* ************* *
		 * Auto-Withdraw *
		 * ************* */
		if (event.message.getFormattedText().equals("§r§cFehler:§r§4 §r§4Du hast nicht genug Guthaben.§r")) {
			event.setCanceled(true);
			BigDecimal moneyRequired = lastPayment.subtract(getCurrentBalance()).setScale(0, RoundingMode.CEILING).max(THOUSAND);
			BigDecimal difference = moneyRequired.subtract(new BigDecimal(BankScoreboard.getBankBalance()));

			// Bank balance is smaller than money required, unable to withdraw
			if (difference.compareTo(BigDecimal.ZERO) > 0) {
				display(Constants.ADDON_PREFIX + "§r§4⚠ §cDir fehlen %s$. §4⚠§r", difference.toPlainString());
			} else {
				// Withdraw difference
				switch (autoWithdraw.get()) {
					case SUGGEST:
						suggest("/bank abheben %d", moneyRequired.toBigInteger());
						break;
					case SEND:
						send("/bank abheben %d", moneyRequired.toBigInteger());
						suggest("/pay %s %s", lastPaymentReceiver, lastPayment.toPlainString());
						break;
				}
			}
		}
	}

	@EventListener
	public void onMessageSend(MessageSendEvent event) {
		if (!ServerCheck.isOnGrieferGames() || world() == null || world().getScoreboard().getTeam("money_value") == null)
			return;

		// Save payment (for auto-withdraw)
		Matcher paymentMatcher = Constants.PAYMENT_COMMAND_PATTERN.matcher(event.message);
		if (paymentMatcher.matches()) {
			lastPaymentReceiver = paymentMatcher.group("player");
			lastPayment = new BigDecimal(paymentMatcher.group("amount").replace(".", "").replace(",", "."));
		}

		// If /bank abheben with the exact difference was sent and withdraw is SUGGEST
		if (lastPaymentReceiver != null) {
			BigDecimal moneyRequired = lastPayment.subtract(getCurrentBalance()).setScale(0, RoundingMode.CEILING).max(THOUSAND);
			if (event.message.equals(String.format("/bank abheben %d", moneyRequired.toBigInteger())) && autoWithdraw.get() == WithdrawAction.SUGGEST) {
				// Wait 1 tick (chat screen still open)
				TickScheduler.runAfterClientTicks(() -> suggest("/pay %s %s", lastPaymentReceiver, lastPayment.toPlainString()), 1);
				return;
			}
		}

		/* ************* *
		 *  Deposit all  *
		 * ************* */
		if (event.message.equalsIgnoreCase("/bank einzahlen *") && depositAll.get()) {
			if (getCurrentBalance().compareTo(THOUSAND) < 0) {
				display(Constants.ADDON_PREFIX + "§r§4⚠ §cDir fehlen %s$. §4⚠§r", THOUSAND.subtract(getCurrentBalance()).toPlainString());
			} else
				send("/bank einzahlen %d", getCurrentBalance().setScale(0, RoundingMode.FLOOR).toBigInteger());
			event.setCanceled(true);
			return;
		}

		/* ************* *
		 *    Equation   *
		 * ************* */
		if (((autoEquationDetect.get() || event.message.startsWith("/pay ") || event.message.startsWith("/bank ")) && evalEquations(SIMPLE_EQUATION_PATTERN, event))
			|| (placeholder.get() && evalEquations(PLACEHOLDER_PATTERN, event)))
			event.setCanceled(true);
	}

	private boolean evalEquations(Pattern pattern, MessageSendEvent event) {
		String msg = event.message;
		Matcher matcher = pattern.matcher(msg);
		if (matcher.find()) {
			do {
				String equation = matcher.group("equation");

				// Check if equation is empty
				if (equation.isEmpty()) {
					display(Constants.ADDON_PREFIX + "§r§4⚠ §cLeere Gleichung! §4⚠§r");
					return true;
				}

				// Calculate
				equation = equation.replace("k", " * 1000").replace(",", ".");
				Expression exp = new Expression(equation);
				if (!exp.checkSyntax()) {
					display(Constants.ADDON_PREFIX + "§r§4⚠ §cFehler beim Berechnen von \"%s\"! §4⚠§r", equation);
					display("§c" + exp.getErrorMessage().trim());
					return true;
				}
				double expResult = exp.calculate();

				// Check if result is valid
				if (Double.isInfinite(expResult)) {
					display(Constants.ADDON_PREFIX + "§r§4⚠ §c\"%s\" ist unendlich! §4⚠§r", equation);
					return true;
				}
				if (Double.isNaN(expResult)) {
					display(Constants.ADDON_PREFIX + "§r§4⚠ §c\"%s\" ist ungültig! §4⚠§r", equation);
					return true;
				}

				// Replace value
				msg = msg.substring(0, matcher.start()) + Constants.DECIMAL_FORMAT_98.format(new BigDecimal(expResult).setScale(decimalPlaces.get(), RoundingMode.HALF_UP)) + msg.substring(matcher.end());
				matcher = pattern.matcher(msg);
			} while (matcher.find());

			// Fix symbols
			if (msg.toLowerCase().startsWith("/pay") || msg.toLowerCase().startsWith("/bank"))
				msg = msg.replace(".", "").replace(',', '.');

			send(msg);
			return true;
		}
		return false;
	}

	@OnEnable
	private void loadLibrary() throws IOException, ReflectiveOperationException {
		File mixinLibrary = new File("libraries/org/mariuszgromada/math/MathParser.org-mXparser/5.1.0/MathParser.org-mXparser-5.1.0.jar");
		if (!mixinLibrary.exists()) {
			// Download library
			mixinLibrary.getParentFile().mkdirs();
			HttpsURLConnection c = (HttpsURLConnection) new URL("https://repo1.maven.org/maven2/org/mariuszgromada/math/MathParser.org-mXparser/5.1.0/MathParser.org-mXparser-5.1.0.jar").openConnection();
			c.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
			Files.copy(c.getInputStream(), mixinLibrary.toPath());
		}

		// Add jar file to parent of LaunchClassLoader
		Field parent = Launch.classLoader.getClass().getDeclaredField("parent");
		parent.setAccessible(true);
		Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		addURL.setAccessible(true);
		addURL.invoke(parent.get(Launch.classLoader), mixinLibrary.toURI().toURL());
		addURL.invoke(Launch.classLoader, mixinLibrary.toURI().toURL());
	}

	private enum WithdrawAction {

		NONE("Nein"), SUGGEST("Vorschlagen"), SEND("Senden");

		private final String name;
		WithdrawAction(String name) {
			this.name = name;
		}
	}
}
