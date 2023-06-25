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

package dev.l3g7.griefer_utils.core.misc;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Constant variables for the addon.
 */
public class Constants {

	public static boolean SCHEMATICA = false;

	public static final String ADDON_NAME = "GrieferUtils";
	public static final String ADDON_PREFIX = "§8[§r§f§l" + ADDON_NAME + "§r§8] §r§f";

	// Rank patterns
	public static final Pattern FORMATTED_RANK_PATTERN = Pattern.compile("(?<rank>[§\\w+]{3,})");
	public static final Pattern FORMATTED_DELIMITER_PATTERN = Pattern.compile("§r§8§*l* ?\\u2503");

	// Name patterns
	public static final Pattern UNFORMATTED_JAVA_PLAYER_NAME_PATTERN = Pattern.compile("~?\\w{3,17}");
	public static final Pattern UNFORMATTED_BEDROCK_PLAYER_NAME_PATTERN = Pattern.compile("![\\w+]{3,17}");
	public static final Pattern UNFORMATTED_PLAYER_NAME_PATTERN = Pattern.compile(String.format("(?<player>%s|%s)", UNFORMATTED_JAVA_PLAYER_NAME_PATTERN, UNFORMATTED_BEDROCK_PLAYER_NAME_PATTERN));

	public static final Pattern FORMATTED_JAVA_PLAYER_NAME_PATTERN = Pattern.compile("[~§\\w]{3,}");
	public static final Pattern FORMATTED_BEDROCK_PLAYER_NAME_PATTERN = Pattern.compile("[!§\\w+]{3,}");
	public static final Pattern FORMATTED_PLAYER_NAME_PATTERN = Pattern.compile(String.format("(?<name>%s|%s)", FORMATTED_JAVA_PLAYER_NAME_PATTERN, FORMATTED_BEDROCK_PLAYER_NAME_PATTERN));
	public static final Pattern FORMATTED_PLAYER_PATTERN = Pattern.compile(String.format("(?<player>%s ?%s %s)", FORMATTED_RANK_PATTERN, FORMATTED_DELIMITER_PATTERN, FORMATTED_PLAYER_NAME_PATTERN));

	// Miscellaneous patterns
	public static final Pattern FORMATTED_CLAN_TAG_PATTERN = Pattern.compile("(?<clantag>§r§6\\[[ÄÖÜäöü#$§\\-\\w]{2,}§r§6] )?");
	public static final Pattern CHAT_MESSAGE_PATTERN = Pattern.compile("(?<message>.*)§*r*");
	public static final Pattern PAYMENT_COMMAND_PATTERN = Pattern.compile(String.format("/pay %s (?<amount>[\\d,.]+)", UNFORMATTED_PLAYER_NAME_PATTERN));

	// Message patterns
	public static final Pattern MESSAGE_RECEIVE_PATTERN = Pattern.compile(String.format("^§r§6\\[§r%s§r§6 \\-> §r§cmir§r§6\\] §r%s$", FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));
	public static final Pattern MESSAGE_SEND_PATTERN = Pattern.compile(String.format("^§r§6\\[§r§cmir§r§6 -> %s\\] §r%s$", FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));
	public static final Pattern PLOTCHAT_RECEIVE_PATTERN = Pattern.compile(String.format("^§r§8\\[§r§6Plot\\-Chat§r§8\\]\\[§r§6(?<id>-?\\d+;-?\\d+)§r§8\\] %s§r§8 : §r%s$", FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));
	public static final Pattern GLOBAL_RECEIVE_PATTERN = Pattern.compile(String.format("^§r%s%s§r§f §r§8» §r%s$", FORMATTED_CLAN_TAG_PATTERN, FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));
	public static final Pattern GLOBAL_CHAT_PATTERN = Pattern.compile(String.format("^§r§a§l@§r§8\\[§r§6(?<cb>\\w+)§r§8] %s §r§8» §r%s$", FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));
	public static final Pattern STATUS_PATTERN = Pattern.compile(String.format("^%s§f (?<message>[^\u00bb]*)§*r*$", FORMATTED_PLAYER_PATTERN));

	public static final ImmutableList<Pattern> MESSAGE_PATTERNS = ImmutableList.of(GLOBAL_RECEIVE_PATTERN, PLOTCHAT_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN, GLOBAL_CHAT_PATTERN);

	public static final Pattern PAYMENT_RECEIVE_PATTERN = Pattern.compile(String.format("^%s(?<!§f) §r§ahat dir \\$(?<amount>[\\d.,]+) gegeben\\.§r$", FORMATTED_PLAYER_PATTERN));

	public static final DecimalFormat DECIMAL_FORMAT_98 = new DecimalFormat("###,###." + Strings.repeat("#", 98), new DecimalFormatSymbols(Locale.GERMAN)); // max 98 decimal places

	public static final String RSA_PUBLIC_KEY_4096 = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAiFR9WaKX2/452JkUHAEkhHnOWO1VItNO0ggHqQsgb54P78uCd2KX+mC63V0leI4th7l9iYlhBXJz76oLLbdqqJPGGmuZfPGQ2che9cWKRki3E/0bYpcBzhwnasfl7+GA8ZoLE363n8NNAC+GCr89Wd/4+GuSQD7rytRYr0n6DtYnsvwOnZjv2Pq8Sx3ARjhdM1U+S9Ys6favNws9b2x0KTgNwznuhrAgwPMKCRZh6FmJSqdIqWFvRWE0h6SW6if/eB9bcMFuCmpVL8+n36DElZHPMgYuznnPGvZJpxud16BoIGpScy8aih1DUHdmMPMaFZa2ZFnkv/Cc3vMM7cIxUQxgY4Den7WMcpJYLMdeWsaiazMe+IRHHBBPy/YTpdCDHfBc/klufc78wqS3DRrGsBiQlP4CofaXCQJ5Koia4/6PKTCJ24y8ahNrjlAEfx1ptH1ucQpCofkmU5Y0IR+B3C6sjZgVB4t6PDsigrqKjXd8qnFMhHnzqFeflC7l8mvxvv8klOfeYjsV2qA0TgZFv70SPj56vfucKAFGvpj9RonZaPSvHhFk/Z8hy/Du91zztcW1yfrENndHZ3PSDjEBDPlcjPzVr8iXdLMqn7ojMprPArz51a67ubz/lPVPzh0Iz3Nn09kbKnC9kMIi3CEZC54Q2jbHPpIZSgfaX71hk+8CAwEAAQ==";
	public static final String RSA_PUBLIC_KEY_2048 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArD/Idp8KR2HEqUe4rHAsSFbOq+21Tt7twnXcoKarDUTfiMN92gJw9tZjMVCokisT5t9BASpXb0Q0NfxM2AFPftq7SePXnVSIzvjolvV4lvbIlaeYHLfJ4scJerHdO5Eq8JAM2zOZnsNnwTnkuzyqaAoUhC9WjybXXMCj27JeGwGxemSfuJ25LYkrQMHuBTqSAiOqYI+mVmzXR/mz+56A8TWonJ2ako2BoQ6XSLpGpfCALmJZK+VyFkQ4zEN3bNjxOYQgFxCzPZgBdbmEuVLU0Z321CE11fzNMSd4bO81KTjgl4C25N2XC7PQaew4VRMOAh/FkYsTyvn4lripKZatiwIDAQAB";

}