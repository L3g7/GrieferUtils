/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Constant variables for the addon.
 */
public class Constants {

	public static boolean SCHEMATICA = false;
	public static boolean EMOTECHAT = false;

	public static final String ADDON_NAME = "GrieferUtils";
	public static final String ADDON_PREFIX = "§8[§r§f§l" + ADDON_NAME + "§r§8] §r§f";

	// Rank patterns
	public static final Pattern FORMATTED_RANK_PATTERN = Pattern.compile("(?<rank>[§\\w+]{3,})");
	public static final Pattern FORMATTED_DELIMITER_PATTERN = Pattern.compile("§r§8§*l* ?\\u2503");

	// Name patterns
	public static final Pattern UNFORMATTED_JAVA_PLAYER_NAME_PATTERN = Pattern.compile("~?\\w{3,17}");
	public static final Pattern UNFORMATTED_BEDROCK_PLAYER_NAME_PATTERN = Pattern.compile("~?![\\w+]{3,17}");
	public static final Pattern UNFORMATTED_PLAYER_NAME_PATTERN = Pattern.compile(String.format("(?<player>%s|%s)", UNFORMATTED_JAVA_PLAYER_NAME_PATTERN, UNFORMATTED_BEDROCK_PLAYER_NAME_PATTERN));

	public static final Pattern FORMATTED_JAVA_PLAYER_NAME_PATTERN = Pattern.compile("[~§\\w]{3,}");
	public static final Pattern FORMATTED_BEDROCK_PLAYER_NAME_PATTERN = Pattern.compile("[~!§\\w+]{3,}");
	public static final Pattern FORMATTED_PLAYER_NAME_PATTERN = Pattern.compile(String.format("(?<name>%s|%s)", FORMATTED_JAVA_PLAYER_NAME_PATTERN, FORMATTED_BEDROCK_PLAYER_NAME_PATTERN));
	public static final Pattern FORMATTED_SUFFIX_PATTERN = Pattern.compile("(?<suffix> [^ ]+)?");
	public static final Pattern FORMATTED_PLAYER_PATTERN = Pattern.compile(String.format("(?<player>%s ?%s %s%s)", FORMATTED_RANK_PATTERN, FORMATTED_DELIMITER_PATTERN, FORMATTED_PLAYER_NAME_PATTERN, FORMATTED_SUFFIX_PATTERN));

	// Miscellaneous patterns
	public static final Pattern FORMATTED_CLAN_TAG_PATTERN = Pattern.compile("(?:§r§6\\[(?<clantag>[ÄÖÜäöü#$§+\\-\\w]{2,})§r§6] )?");
	public static final Pattern CHAT_MESSAGE_PATTERN = Pattern.compile("(?<message>.*)§*r*");
	public static final Pattern BLACKLIST_ERROR_PATTERN = Pattern.compile(String.format("§r%s \\| %s%s", FORMATTED_RANK_PATTERN, UNFORMATTED_PLAYER_NAME_PATTERN, CHAT_MESSAGE_PATTERN));

	// Message patterns
	public static final Pattern MESSAGE_RECEIVE_PATTERN = Pattern.compile(String.format("^§r§6\\[§r%s§r§6 \\-> §r§cmir§r§6\\] §r%s$", FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));
	public static final Pattern MESSAGE_SEND_PATTERN = Pattern.compile(String.format("^§r§6\\[§r§cmir§r§6 -> %s\\] §r%s$", FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));
	public static final Pattern PLOTCHAT_RECEIVE_PATTERN = Pattern.compile(String.format("^§r§8\\[§r§6Plot\\-Chat§r§8\\]\\[§r§6(?<id>-?\\d+;-?\\d+)§r§8\\] %s§r§8 : §r%s$", FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));
	public static final Pattern GLOBAL_RECEIVE_PATTERN = Pattern.compile(String.format("^§r%s%s§r§f §r§8» §r%s$", FORMATTED_CLAN_TAG_PATTERN, FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));
	public static final Pattern GLOBAL_CHAT_PATTERN = Pattern.compile(String.format("^§r§a§l@§r§8\\[§r§6(?<cb>\\w+)§r§8] %s%s §r§8» §r%s$", FORMATTED_CLAN_TAG_PATTERN, FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));
	public static final Pattern STATUS_PATTERN = Pattern.compile(String.format("^%s§f (?<message>[^\u00bb]*)§*r*$", FORMATTED_PLAYER_PATTERN));

	public static final List<Pattern> MESSAGE_PATTERNS = Arrays.asList(GLOBAL_RECEIVE_PATTERN, PLOTCHAT_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN, GLOBAL_CHAT_PATTERN);

	public static final Pattern PAYMENT_RECEIVE_PATTERN = Pattern.compile(String.format("^%s(?<!§f) §r§ahat dir \\$(?<amount>[\\d.,]+) (?:§r§a)?gegeben\\.§r$", FORMATTED_PLAYER_PATTERN));
	public static final Pattern PAYMENT_SEND_PATTERN = Pattern.compile(String.format("^§r§aDu hast %s ?§r§a ?\\$(?<amount>[\\d.,]+) gegeben\\.§r$", Constants.FORMATTED_PLAYER_PATTERN));

	public static final Pattern ORB_BUY_PATTERN = Pattern.compile("^\\[GrieferGames] Du hast erfolgreich das Produkt .+ für (?<orbs>[\\d.]+) Orbs gekauft\\.$");
	public static final Pattern ORB_SELL_PATTERN = Pattern.compile("^\\[Orbs] Du hast erfolgreich (?<amount>[\\d.]+) (?<item>[\\S ]+) für (?<orbs>[\\d.,]+) Orbs verkauft\\.$");
	public static final Pattern JOB_SELL_PATTERN = Pattern.compile("^§r§8\\[§r§6GrieferGames§r§8] §r§aDu hast §r§2(?<count>\\d+)§r§a Stack\\(s\\) §r§6[^§]+§r§a für §r§2(?<amount>\\d+)§r§2\\$§r§a geliefert\\.§r$");

	public static final DecimalFormat DECIMAL_FORMAT_98;

	static {
		char[] decimalPattern = new char[98];
		Arrays.fill(decimalPattern, '#');
		DECIMAL_FORMAT_98 = new DecimalFormat("###,###." + new String(decimalPattern), new DecimalFormatSymbols(Locale.GERMAN));
	}

	// Servers
	public static final String STATIC_API_URL = "https://api.grieferutils.l3g7.dev";
	public static final String DYNAMIC_API_URL = "https://s2.grieferutils.l3g7.dev";
	public static final String HIVEMIND_URL = "https://s1.grieferutils.l3g7.dev";
	public static final String BSF_URL = "https://s4.grieferutils.l3g7.dev";

}