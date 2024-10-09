package cz.sengycraft.region.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.List;
import java.util.stream.Collectors;

public class ComponentUtils {

    public static Component deserialize(String message) {

        return MiniMessage.miniMessage().deserialize(message)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.FALSE)
                .decorationIfAbsent(TextDecoration.OBFUSCATED, TextDecoration.State.FALSE)
                .decorationIfAbsent(TextDecoration.STRIKETHROUGH, TextDecoration.State.FALSE)
                .decorationIfAbsent(TextDecoration.UNDERLINED, TextDecoration.State.FALSE);
    }

    public static List<Component> deserialize(List<String> lines) {
        return lines.stream().map(ComponentUtils::deserialize).collect(Collectors.toList());
    }

    public static String serialize(Component component) {
        return MiniMessage.miniMessage().serialize(component);
    }

    public static String serializePlain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

}
