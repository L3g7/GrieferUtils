/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.util.misc;

import com.google.common.collect.ImmutableList;

import java.util.regex.Pattern;

/**
 * Constant variables for the addon.
 */
public class Constants {

	public static final String ADDON_NAME = "GrieferUtils";
	public static final String ADDON_PREFIX = "§8[§r§f§l" + ADDON_NAME + "§r§8] §r§f";

	// Rank patterns
	public static final Pattern FORMATTED_RANK_PATTERN = Pattern.compile("(?<rank>[§\\w+]{3,})");
	public static final Pattern FORMATTED_DELIMITER_PATTERN = Pattern.compile("§r§8§*l*\\u2503");

	// Name patterns
	public static final Pattern FORMATTED_JAVA_PLAYER_NAME_PATTERN = Pattern.compile("[~§\\w]{3,}");
	public static final Pattern FORMATTED_BEDROCK_PLAYER_NAME_PATTERN = Pattern.compile("[!§\\w+]{3,}");
	public static final Pattern FORMATTED_PLAYER_NAME_PATTERN = Pattern.compile(String.format("(?<name>%s|%s)", FORMATTED_JAVA_PLAYER_NAME_PATTERN, FORMATTED_BEDROCK_PLAYER_NAME_PATTERN));
	public static final Pattern FORMATTED_PLAYER_PATTERN = Pattern.compile(String.format("(?<player>%s %s %s)", FORMATTED_RANK_PATTERN, FORMATTED_DELIMITER_PATTERN, FORMATTED_PLAYER_NAME_PATTERN));

	// Miscellaneous patterns
	public static final Pattern FORMATTED_CLAN_TAG_PATTERN = Pattern.compile("(?<clantag>§r§6\\[§r[§\\-\\w]{3,}§r§6] )?");
	public static final Pattern CHAT_MESSAGE_PATTERN = Pattern.compile("(?<message>.*)§*r*");

	// Message patterns
	public static final Pattern MESSAGE_RECEIVE_PATTERN = Pattern.compile(String.format("^§r§6\\[§r%s§r§6 \\-> §r§cmir§r§6\\] §r%s$", FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));
	public static final Pattern MESSAGE_SEND_PATTERN = Pattern.compile(String.format("^§r§6\\[§r§cmir§r§6 -> %s\\] §r%s$", FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));
	public static final Pattern PLOTCHAT_RECEIVE_PATTERN = Pattern.compile(String.format("^§r§8\\[§r§6Plot\\-Chat§r§8\\]\\[§r§6(?<id>-?\\d+;-?\\d+)§r§8\\] %s§r§8 : §r%s$", FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));
	public static final Pattern GLOBAL_RECEIVE_PATTERN = Pattern.compile(String.format("^§r%s%s§r§f §r§8» §r%s$", FORMATTED_CLAN_TAG_PATTERN, FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));

	public static final ImmutableList<Pattern> MESSAGE_PATTERNS = ImmutableList.of(GLOBAL_RECEIVE_PATTERN, PLOTCHAT_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN);

	public static final Pattern PAYMENT_RECEIVE_PATTERN = Pattern.compile(String.format("^%s(?<!§f) §r§ahat dir \\$(?<amount>[\\d.,]+) gegeben\\.§r$", FORMATTED_PLAYER_PATTERN));

}
