package cz.sengycraft.region.listeners;

import cz.sengycraft.region.gui.RegionsMenu;
import cz.sengycraft.region.regions.Region;
import cz.sengycraft.region.utils.ComponentUtils;
import cz.sengycraft.region.utils.MessageUtils;
import cz.sengycraft.region.utils.Pair;
import io.papermc.paper.event.player.ChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RenameListener implements Listener {

    private static RenameListener instance;

    public static RenameListener getInstance() {
        if (instance == null) instance = new RenameListener();
        return instance;
    }

    private Map<UUID, Region> renaming = new HashMap<>();

    public void add(UUID uuid, Region region) {
        renaming.put(uuid, region);
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Region region = renaming.get(uuid);

        if (region == null) return;

        event.setCancelled(true);
        renaming.remove(uuid);

        String message = ComponentUtils.serializePlain(event.message());

        if (message.equalsIgnoreCase("cancel")) {
            MessageUtils.sendMessage(player, "canceled");
            RegionsMenu.openRegionsMenu(player);
            return;
        }

        if (message.contains(" ")) {
            MessageUtils.sendMessage(player, "contains-spaces");
            RegionsMenu.openRegionsMenu(player);
            return;
        }

        try {
            region.setName(message);
            MessageUtils.sendMessage(player, "renamed", new Pair<>("{region}", message));
        } catch (Exception e) {
            MessageUtils.sendMessage(player, "region-error");
            System.out.println(e.getMessage());
        }

        RegionsMenu.openRegionsMenu(player);
    }




}
