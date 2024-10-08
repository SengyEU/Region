package cz.sengycraft.region.utils;

import cz.sengycraft.region.configuration.ConfigurationManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class MessageUtils {

    static FileConfiguration messages = ConfigurationManager.getInstance().getConfiguration("messages");

    public static void sendMessage(CommandSender sender, String messageKey) {
        sender.sendMessage(ComponentUtils.deserialize(messages.getString(messageKey)));
    }

    @SafeVarargs
    public static void sendMessage(CommandSender sender, String messageKey, Pair<String, String>... placeholders) {

        String message = messages.getString(messageKey);

        message = replacePlaceholders(message, placeholders);


        sender.sendMessage(ComponentUtils.deserialize(message));
    }

    @SafeVarargs
    public static String replacePlaceholders(String text, Pair<String, String>... placeholders) {

        for (Pair<String, String> placeholder : placeholders) {
            text = text.replace(placeholder.getLeft(), placeholder.getRight());
        }

        return text;
    }

    @SafeVarargs
    public static List<String> replacePlaceholders(List<String> lines, Pair<String, String>... placeholders) {
        List<String> newLines = new ArrayList<>();

        for (String line : lines) {
            for (Pair<String, String> placeholder : placeholders) {
                line = line.replace(placeholder.getLeft(), placeholder.getRight());
            }

            newLines.add(line);
        }

        return newLines;
    }

}
