package cz.sengycraft.region.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ComponentUtils {

    public static Component deserialize(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }

}
