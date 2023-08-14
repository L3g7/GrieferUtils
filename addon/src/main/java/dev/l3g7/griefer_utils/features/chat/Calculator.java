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
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.uncategorized.settings.BugReporter;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.*;
import net.labymod.utils.Material;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.mXparser;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.features.player.scoreboard.BankScoreboard.getBankBalance;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

@Singleton
public class Calculator extends Feature {

	private final DropDownSetting<WithdrawAction> autoWithdraw = new DropDownSetting<>(WithdrawAction.class)
		.name("Auto. abheben")
		.icon("bank")
		.defaultValue(WithdrawAction.SUGGEST);

	private final BooleanSetting starPlaceholder = new BooleanSetting()
		.name("\"*\"-Placeholder")
		.description("Aktiviert den * Placeholder, mit dem sich das gesamte Guthaben einzahlen, abheben oder überweisen lässt.")
		.icon("bank")
		.defaultValue(true);

	private final StringSetting prefix = new StringSetting()
		.name("Präfix")
		.description("Der Präfix für Berechnungen ohne abgeschickter Chatnachricht."
			+ "\nDas Ergebnis wird automatisch in die Zwischenablage kopiert.")
		.icon(Material.NAME_TAG)
		.defaultValue("/c ");

	private final StringSetting placeholderStart = (StringSetting) new StringSetting()
		.name("Placeholder Anfang")
		.description("Welches Zeichen den Anfang des Placeholders markieren soll.")
		.icon("regex")
		.defaultValue("{")
		.callback(s -> this.updatePlaceholderPattern())
		.maxLength(1);

	private final StringSetting placeholderEnd = (StringSetting) new StringSetting()
		.name("Placeholder Ende")
		.description("Welches Zeichen das Ende des Placeholders markieren soll.")
		.icon("regex")
		.defaultValue("}")
		.callback(s -> this.updatePlaceholderPattern())
		.maxLength(1);

	private final BooleanSetting placeholder = new BooleanSetting()
		.name("Placeholder in Nachrichten")
		.description("Ermöglicht in einer Nachricht eingebettete Gleichungen, indem sie mit {} eingerahmt werden.")
		.icon("regex")
		.defaultValue(true)
		.subSettings(placeholderStart, placeholderEnd);

	private final BooleanSetting autoEquationDetect = new BooleanSetting()
		.name("Automatische Gleichungserkennung")
		.description("Erkennt automatisch in einer Nachricht eingebettete Gleichungen, auch wenn sie nicht mit {} eingerahmt sind.")
		.icon("regex")
		.defaultValue(true);

	private final NumberSetting decimalPlaces = new NumberSetting()
		.name("Nachkommastellen")
		.description("Auf wie viele Nachkommastellen das Ergebnis gerundet werden soll.")
		.icon(Material.STONE_BUTTON)
		.defaultValue(2)
		.min(0).max(98);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Rechner")
		.description("Ein Rechner in Nachrichten.")
		.icon("calculator")
		.subSettings(decimalPlaces, new HeaderSetting(),
			autoWithdraw, starPlaceholder, placeholder, autoEquationDetect, prefix);

	private static Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(?<equation>[^}]*)}");
	private static final Pattern SIMPLE_EQUATION_PATTERN = Pattern.compile("(?:(?<= )|^)(?<equation>[+-]?\\d+(?:[.,]\\d+)?k* *[+\\-/*^ek] *[+-]?\\d+(?:[.,]\\d+)?k*|[+-]?\\d+(?:[.,]\\d+)?k+)(?:(?= )|$)");
	private static final BigDecimal THOUSAND = new BigDecimal(1000);
	private BigDecimal lastPayment = BigDecimal.ZERO;
	private String lastPaymentReceiver;

	private void updatePlaceholderPattern() {
		if (placeholderStart.get().isEmpty() || placeholderEnd.get().isEmpty())
			return;

		String start = Pattern.quote(placeholderStart.get());
		String end = Pattern.quote(placeholderEnd.get());
		PLACEHOLDER_PATTERN = Pattern.compile(String.format("%s(?<equation>[^%s]*)%s", start, end, end));
	}

	/**
	 * Get the current balance based on the scoreboard value
	 */
	private BigDecimal getCurrentBalance() {
		try {
			return new BigDecimal(world().getScoreboard().getTeam("money_value").getColorPrefix().replaceAll("[$.]", "").replace(",", "."));
		} catch (NumberFormatException e) {
			return BigDecimal.ZERO;
		}
	}

	@EventListener
	public void onServerSwitch(ServerEvent.ServerSwitchEvent event) {
		lastPaymentReceiver = null;
	}

	@EventListener
	public void onMessageReceive(MessageReceiveEvent event) {
		if (!ServerCheck.isOnGrieferGames())
			return;

		/* ************* *
		 * Auto-Withdraw *
		 * ************* */
		if (event.message.getFormattedText().equals("§r§cFehler:§r§4 §r§4Du hast nicht genug Guthaben.§r")) {
			event.setCanceled(true);
			BigDecimal moneyRequired = lastPayment.subtract(getCurrentBalance()).setScale(0, RoundingMode.CEILING).max(THOUSAND);
			BigDecimal difference = moneyRequired.subtract(new BigDecimal(getBankBalance()));

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
		if (event.message.equalsIgnoreCase(prefix.get().trim())) {
			event.setCanceled(true);
			display(Constants.ADDON_PREFIX + "§cSyntax: " + prefix.get().trim() + " [Rechnung]");
			return;
		}

		if (event.message.toLowerCase().startsWith(prefix.get().trim().toLowerCase() + " ")) {
			event.setCanceled(true);

			String message = event.message.substring(prefix.get().trim().length()).trim();
			double exp = calculate(message);
			if (Double.isNaN(exp))
				return;

			String text = Constants.DECIMAL_FORMAT_98.format(exp);
			display(Constants.ADDON_PREFIX + "Ergebnis: " + text);
			StringSelection sel = new StringSelection(text.replace(".", ""));
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
			return;
		}

		if (!ServerCheck.isOnGrieferGames() || world() == null || world().getScoreboard().getTeam("money_value") == null)
			return;

		// Save payment (for auto-withdraw)
		Matcher paymentMatcher = Constants.PAYMENT_COMMAND_PATTERN.matcher(event.message);
		if (paymentMatcher.matches()) {
			lastPaymentReceiver = paymentMatcher.group("player");
			try {
				lastPayment = new BigDecimal(paymentMatcher.group("amount").replace(",", ""));
			} catch (NumberFormatException e) {
				// Ignore command - GrieferGames will display an error, so we don't have to
				return;
			}
		}

		// If /bank abheben with the exact difference was sent and withdraw is SUGGEST
		if (lastPaymentReceiver != null && event.message.startsWith("/bank abheben")) {
			BigDecimal moneyRequired = lastPayment.subtract(getCurrentBalance()).setScale(0, RoundingMode.CEILING).max(THOUSAND);
			if (event.message.equals(String.format("/bank abheben %d", moneyRequired.toBigInteger())) && autoWithdraw.get() == WithdrawAction.SUGGEST) {
				// Wait 1 tick (chat screen still open)
				TickScheduler.runAfterRenderTicks(() -> suggest("/pay %s %s", lastPaymentReceiver, lastPayment.toPlainString()), 1);
				return;
			}
		}

		/* ****************** *
		 *  Star placeholder  *
		 * ****************** */
		if (event.message.equalsIgnoreCase("/bank einzahlen *") && starPlaceholder.get()) {
			if (getCurrentBalance().compareTo(THOUSAND) < 0)
				display(Constants.ADDON_PREFIX + "§r§4⚠ §cDir fehlen %s$. §4⚠§r", THOUSAND.subtract(getCurrentBalance()).toPlainString());
			else
				send("/bank einzahlen %d", getCurrentBalance().setScale(0, RoundingMode.FLOOR).toBigInteger());
			event.setCanceled(true);
			return;
		} else if (event.message.equalsIgnoreCase("/bank abheben *") && starPlaceholder.get()) {
			if (getBankBalance() < 1000)
				display(Constants.ADDON_PREFIX + "§r§4⚠ §cDir fehlen %s$. §4⚠§r", (1000 - getBankBalance()));
			else
				send("/bank abheben %d", getBankBalance());
			event.setCanceled(true);
			return;
		} else if (starPlaceholder.get()) {
			Matcher matcher = Pattern.compile(String.format("/pay %s \\*", Constants.UNFORMATTED_PLAYER_NAME_PATTERN)).matcher(event.message);
			if (matcher.matches()) {
				send("/pay %s %d", matcher.group("player"), getCurrentBalance().longValue());
				event.setCanceled(true);
				return;
			}
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

				// Don't calculate if equation is "24/7"
				if (equation.equals("24/7")
					&& !(event.message.startsWith("/pay ") || event.message.startsWith("/bank ")) // Calculate 24/7 in /pay and /bank
					&& pattern != PLACEHOLDER_PATTERN) { // Calculate 24/7 if specified in placeholder
					return false;
				}

				double expResult = calculate(equation);
				if (Double.isNaN(expResult))
					return true;

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

	private double calculate(String equation) {
		equation = equation.replace("k", " * 1000").replace(",", ".");
		Expression exp;
		try {
			exp = new Expression(equation);
		} catch (NoSuchMethodError e) {
			// Sometimes, the constructor cannot be found.
			// I have no idea why so here's a bit of logging to find out more.
			display(Constants.ADDON_PREFIX + "§r§4⚠ §cFehler beim Berechnen von \"%s\"! §4⚠§r", equation);
			display("§cDie Bibliothek konnte nicht geladen werden.");

			StringBuilder builder = new StringBuilder();
			try {
				builder.append("mXparser metadata:\nVersion: ").append(mXparser.VERSION).append("\nMethods:");
				for (Method method : Expression.class.getDeclaredMethods())
					builder.append("\n").append(method.toString());

				MessageDigest m = MessageDigest.getInstance("SHA-256");
				byte[] buffer = new byte[4096];
				try (InputStream content = Expression.class.getClassLoader().getResource(Expression.class.getName().replace('.', '/') + ".class").openStream()) {
					int size;
					while ((size = content.read(buffer)) != -1)
						m.update(buffer, 0, size);
					builder.append("\nClass-Hash: ").append(m);
				}
			} catch (Throwable ex) {
				builder.append("\nError while analysing: ").append(ex);
			}

			BugReporter.reportError(new Throwable(builder.toString(), e));
			return Double.NaN;
		}

		if (!exp.checkSyntax()) {
			display(Constants.ADDON_PREFIX + "§r§4⚠ §cFehler beim Berechnen von \"%s\"! §4⚠§r", equation);
			display("§c" + exp.getErrorMessage().trim());
			return Double.NaN;
		}

		double expResult = exp.calculate();

		// Check if result is valid
		if (Double.isInfinite(expResult)) {
			display(Constants.ADDON_PREFIX + "§r§4⚠ §c\"%s\" ist unendlich! §4⚠§r", equation);
			return Double.NaN;
		}

		if (Double.isNaN(expResult)) {
			display(Constants.ADDON_PREFIX + "§r§4⚠ §c\"%s\" ist ungültig! §4⚠§r", equation);
			return Double.NaN;
		}

		return expResult;
	}

	private enum WithdrawAction {

		NONE("Nein"), SUGGEST("Vorschlagen"), SEND("Senden");

		private final String name;
		WithdrawAction(String name) {
			this.name = name;
		}
	}
}
