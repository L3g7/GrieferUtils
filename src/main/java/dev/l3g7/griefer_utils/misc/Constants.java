package dev.l3g7.griefer_utils.misc;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public class Constants {

    public static final String ADDON_NAME = "GrieferUtils";
    public static final String ADDON_PREFIX = "§8[§r§f§l" + ADDON_NAME + "§r§8] §r§f";

    // Split pattern into many parts, so it's much more readable
    public static final Pattern UNFORMATTED_JAVA_PLAYER_NAME_PATTERN = Pattern.compile("~?\\w{3,17}");
    public static final Pattern UNFORMATTED_BEDROCK_PLAYER_NAME_PATTERN = Pattern.compile("![\\w+]{3,17}");
    public static final Pattern UNFORMATTED_PLAYER_NAME_PATTERN = Pattern.compile(String.format("(?<player>%s|%s)", UNFORMATTED_JAVA_PLAYER_NAME_PATTERN, UNFORMATTED_BEDROCK_PLAYER_NAME_PATTERN));
    public static final Pattern PAYMENT_COMMAND_PATTERN = Pattern.compile(String.format("/pay %s (?<amount>[\\d,.]+)", UNFORMATTED_PLAYER_NAME_PATTERN));

    public static final Pattern FORMATTED_RANK_PATTERN = Pattern.compile("(?<rank>[§\\w+]{3,})");
    public static final Pattern FORMATTED_DELIMITER_PATTERN = Pattern.compile("§r§8§*l*\\u2503");
    public static final Pattern FORMATTED_JAVA_PLAYER_NAME_PATTERN = Pattern.compile("[~§\\w]{3,}");
    public static final Pattern FORMATTED_BEDROCK_PLAYER_NAME_PATTERN = Pattern.compile("[!§\\w+]{3,}");
    public static final Pattern FORMATTED_PLAYER_NAME_PATTERN = Pattern.compile(String.format("(?<name>%s|%s)", FORMATTED_JAVA_PLAYER_NAME_PATTERN, FORMATTED_BEDROCK_PLAYER_NAME_PATTERN));
    public static final Pattern FORMATTED_PLAYER_PATTERN = Pattern.compile(String.format("(?<player>%s %s %s)", FORMATTED_RANK_PATTERN, FORMATTED_DELIMITER_PATTERN, FORMATTED_PLAYER_NAME_PATTERN));

    public static final Pattern FORMATTED_CLAN_TAG_PATTERN = Pattern.compile("(?<clantag>§r§6\\[[#$§\\-\\w]{2,}§r§6] )?");

    public static final Pattern CHAT_MESSAGE_PATTERN = Pattern.compile("(?<message>.*)§*r*");

    public static final Pattern MESSAGE_RECEIVE_PATTERN = Pattern.compile(String.format("^§r§6\\[§r%s§r§6 \\-> §r§cmir§r§6\\] §r%s$", FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));
    public static final Pattern MESSAGE_SEND_PATTERN = Pattern.compile(String.format("^§r§6\\[§r§cmir§r§6 -> %s\\] §r%s$", FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));
    public static final Pattern PLOTCHAT_RECEIVE_PATTERN = Pattern.compile(String.format("^§r§8\\[§r§6Plot\\-Chat§r§8\\]\\[§r§6(?<id>-?\\d+;-?\\d+)§r§8\\] %s§r§8 : §r%s$", FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));
    public static final Pattern GLOBAL_RECEIVE_PATTERN = Pattern.compile(String.format("^§r%s%s§r§f §r§8» §r%s$", FORMATTED_CLAN_TAG_PATTERN, FORMATTED_PLAYER_PATTERN, CHAT_MESSAGE_PATTERN));

    public static final Pattern PAYMENT_RECEIVE_PATTERN = Pattern.compile(String.format("^%s(?<!§f) §r§ahat dir \\$(?<amount>[\\d.,]+) gegeben\\.§r$", FORMATTED_PLAYER_PATTERN));
    public static final Pattern PAYMENT_SEND_PATTERN = Pattern.compile(String.format("^§r§aDu hast %s§r§a \\$(?<amount>[\\d.,]+) gegeben\\.§r$", FORMATTED_PLAYER_PATTERN));


    public static final DecimalFormat DECIMAL_FORMAT_98 = new DecimalFormat("###,###.##################################################################################################", new DecimalFormatSymbols(Locale.GERMAN)); // max 98 decimal places
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

}
