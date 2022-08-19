package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageReceiveEvent;
import dev.l3g7.griefer_utils.event.events.chat.MessageSendEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.tweaks.BankScoreboard;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import dev.l3g7.griefer_utils.settings.elements.RadioSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import dev.l3g7.griefer_utils.misc.mxparser.Expression;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class Calculator extends Feature {

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(?<equation>[^}]*)}");
	private static final Pattern SIMPLE_EQUATION_PATTERN = Pattern.compile("(?<= )(?<equation>\\d+k?(?: *[*k+\\-:/^,e]+ *\\d+k?)+)(?:(?= )|$)");
	private static final BigDecimal THOUSAND = new BigDecimal(1000);

	private final RadioSetting<WithdrawAction> autoWithdraw = new RadioSetting<>(WithdrawAction.class)
			.name("Auto. abheben")
			.icon("bank")
			.config("features.calculator.auto_withdraw")
			.defaultValue(WithdrawAction.SUGGEST)
			.stringProvider(WithdrawAction::getName);

	private final BooleanSetting depositAll = new BooleanSetting()
			.name("* einzahlen")
			.description("Aktiviert den * Placeholder, mit dem sich das gesamte Guthaben einzahlen lässt.")
			.icon("bank")
			.config("features.calculator.deposit_all")
			.defaultValue(true);

	private final BooleanSetting placeholder = new BooleanSetting()
			.name("Placeholder in Nachrichten")
			.description("Ermöglicht in einer Nachricht eingebettete Gleichungen, indem sie mit {} eingerahmt werden.")
			.icon("regex")
			.config("features.calculator.placeholder")
			.defaultValue(true);

	private final BooleanSetting autoEquationDetect = new BooleanSetting()
			.name("Automatische Gleichungserkennung")
			.description("Erkennt automatisch in einer Nachricht eingebettete Gleichungen, auch wenn sie nicht mit {} eingerahmt sind.")
			.icon("regex")
			.config("features.calculator.auto_equation_detect")
			.defaultValue(false);

	private final NumberSetting decimalPlaces = new NumberSetting()
			.name("Nachkommastellen")
			.description("Auf wie viele Nachkommastellen das Ergebnis gerundet werden soll")
			.icon(Material.STONE_BUTTON)
			.min(0).max(98)
			.config("features.calculator.decimal_places")
			.defaultValue(2);

	private final BooleanSetting enabled = new BooleanSetting()
			.name("Rechner")
			.icon("calculator")
			.defaultValue(true)
			.config("features.calculator.active")
			.subSettingsWithHeader("Rechner", decimalPlaces, new HeaderSetting(),
					autoWithdraw, depositAll, placeholder, autoEquationDetect);

	public Calculator() {
		super(Category.FEATURE);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	private BigDecimal lastPayment = BigDecimal.ZERO;
	private String lastPaymentReceiver;

	/**
	 * Get the current balance based on the scoreboard value
	 */
	private BigDecimal getCurrentBalance() {
		return new BigDecimal(world().getScoreboard().getTeam("money_value").getColorPrefix().replace("$", "").replace(".", "").replace(",", "."));
	}

	@EventListener
	public void onMessageReceive(MessageReceiveEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		/* ************* *
		 * Auto-Withdraw *
		 * ************* */
		if (event.getFormatted().equals("§r§cFehler:§r§4 §r§4Du hast nicht genug Guthaben.§r")) {
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
		if (!isActive() || !isOnGrieferGames())
			return;

		// Save payment (for auto-withdraw)
		Matcher paymentMatcher = Constants.PAYMENT_COMMAND_PATTERN.matcher(event.getMsg());
		if (paymentMatcher.matches()) {
			lastPaymentReceiver = paymentMatcher.group("player");
			lastPayment = new BigDecimal(paymentMatcher.group("amount").replace(".", "").replace(",", "."));
		}

		// If /bank abheben with the exact difference was sent and withdraw is SUGGEST
		if (lastPaymentReceiver != null) {
			BigDecimal moneyRequired = lastPayment.subtract(getCurrentBalance()).setScale(0, RoundingMode.CEILING).max(THOUSAND);
			if (event.getMsg().equals(String.format("/bank abheben %d", moneyRequired.toBigInteger())) && autoWithdraw.get() == WithdrawAction.SUGGEST) {
				// Wait 1 tick (chat screen still open)
				TickScheduler.runNextTick(() -> suggest("/pay %s %s", lastPaymentReceiver, lastPayment.toPlainString()));
				return;
			}
		}

		/* ************* *
		 *  Deposit all  *
		 * ************* */
		if (event.getMsg().equalsIgnoreCase("/bank einzahlen *") && depositAll.get()) {
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
		if ((autoEquationDetect.get() && evalEquations(SIMPLE_EQUATION_PATTERN, event))
				|| (placeholder.get() && evalEquations(PLACEHOLDER_PATTERN, event)))
			event.setCanceled(true);
	}

	private boolean evalEquations(Pattern pattern, MessageSendEvent event) {
		String msg = event.getMsg();
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
			} while (matcher.find());

			// Fix symbols
			if (msg.toLowerCase().startsWith("/pay") || msg.toLowerCase().startsWith("/bank"))
				msg = msg.replace(".", "").replace(',', '.');

			send(msg);
			return true;
		}
		return false;
	}

	private enum WithdrawAction {

		SEND("Senden"), SUGGEST("Vorschlagen"), NONE("Nein");

		final String name;

		WithdrawAction(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

}
