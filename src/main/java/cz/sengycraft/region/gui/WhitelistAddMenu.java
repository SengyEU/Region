package cz.sengycraft.region.gui;

import cz.sengycraft.region.configuration.ConfigurationManager;
import cz.sengycraft.region.regions.Region;
import cz.sengycraft.region.utils.ComponentUtils;
import cz.sengycraft.region.utils.ItemStackUtils;
import cz.sengycraft.region.utils.MessageUtils;
import cz.sengycraft.region.utils.Pair;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class WhitelistAddMenu {

    static FileConfiguration config = ConfigurationManager.getInstance().getConfiguration("config");

    public static void openWhitelistAddMenu(Player player, Region region) {
        String title = config.getString("regions.gui.whitelist-add.title").replace("{region}", region.getName());
        int rows = config.getInt("regions.gui.whitelist-add.rows");

        PaginatedGui gui = Gui.paginated()
                .title(ComponentUtils.deserialize(title))
                .rows(rows)
                .pageSize((rows - 1) * 9)
                .disableAllInteractions()
                .create();

        addControlButtons(gui, player, region);
        addPlayerItems(gui, player, region);

        gui.open(player);
    }

    private static void addControlButtons(PaginatedGui gui, Player player, Region region) {

        ItemStack backItem = getControlItem("back");
        HashSet<Integer> backSlots = ItemStackUtils.getSlots(config.getString("regions.gui.whitelist-add.back.slot"));
        gui.setItem(backSlots.stream().toList(), new GuiItem(backItem, event -> RegionMenu.openRegionMenu(player, region)));

        addPaginationControls(gui);
    }

    private static void addPaginationControls(PaginatedGui gui) {
        ItemStack previousPageItem = getControlItem("previous-page");
        HashSet<Integer> previousPageSlots = ItemStackUtils.getSlots(config.getString("regions.gui.whitelist-add.previous-page.slot"));
        gui.setItem(previousPageSlots.stream().toList(), new GuiItem(previousPageItem, event -> gui.previous()));

        ItemStack nextPageItem = getControlItem("next-page");
        HashSet<Integer> nextPageSlots = ItemStackUtils.getSlots(config.getString("regions.gui.whitelist-add.next-page.slot"));
        gui.setItem(nextPageSlots.stream().toList(), new GuiItem(nextPageItem, event -> gui.next()));
    }

    private static void addPlayerItems(PaginatedGui gui, Player player, Region region) {
        List<String> players = getAllPlayers(region);
        for (String target : players) {
            ItemStack playerItem = getPlayerItem(target);
            gui.addItem(new GuiItem(playerItem, event -> {
                try {
                    region.addWhitelistedPlayer(target);
                    MessageUtils.sendMessage(player, "whitelist.added", new Pair<>("{region}", region.getName()), new Pair<>("{player}", target));
                    RegionMenu.openRegionMenu(player, region);
                } catch (Exception e) {
                    MessageUtils.sendMessage(player, "region-error");
                }
            }));
        }
    }

    private static ItemStack getPlayerItem(String player) {
        String materialName = config.getString("regions.gui.whitelist-add.player.item");
        Material material = Material.getMaterial(materialName.toUpperCase());

        String title = config.getString("regions.gui.whitelist-add.player.title").replace("{player}", player);
        List<String> lore = config.getStringList("regions.gui.whitelist-add.player.lore");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ComponentUtils.deserialize(title));
        meta.lore(ComponentUtils.deserialize(lore));

        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getControlItem(String key) {
        String materialName = config.getString("regions.gui.whitelist-add." + key + ".material");
        Material material = Material.getMaterial(materialName.toUpperCase());

        String title = config.getString("regions.gui.whitelist-add." + key + ".title");
        List<String> lore = config.getStringList("regions.gui.whitelist-add." + key + ".lore");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ComponentUtils.deserialize(title));
        meta.lore(ComponentUtils.deserialize(lore));

        item.setItemMeta(meta);
        return item;
    }

    private static List<String> getAllPlayers(Region region) {
        List<String> players = Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        players.removeAll(region.getWhitelistedPlayers());

        return players;
    }
}