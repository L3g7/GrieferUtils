package dev.l3g7.griefer_utils.core.misc.griefer_games;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.network.GrieferGamesPayloadEvent;
import dev.l3g7.griefer_utils.core.events.network.MysteryModPayloadEvent;

import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigDecimal;

import static dev.l3g7.griefer_utils.core.api.util.Util.elevate;

/**
 * Cash and bank balance.
 */
public class Balances {

	private static BigDecimal accountBalance = BigDecimal.ZERO;
	private static long bankBalance = 0;

	/**
	 * Get the current account balance.
	 */
	public static BigDecimal getBalance() {
		return accountBalance;
	}

	/**
	 * Get the current bank balance.
	 */
	public static long getBankBalance() {
		return bankBalance;
	}

	@EventListener
	private static void onBank(MysteryModPayloadEvent event) {
		if (!event.channel.equals("bank"))
			return;

		JsonObject payload = event.payload.getAsJsonObject();
		bankBalance = payload.get("amount").getAsLong();
	}

	@EventListener
	private static void onAccountBalance(GrieferGamesPayloadEvent event) {
		if (!event.channel.equals("accountbalance"))
			return;


		try (DataInputStream in = event.createStream()) {
			accountBalance = BigDecimal.valueOf(in.readDouble());
		} catch (IOException e) {
			throw elevate(e);
		}

	}

}
