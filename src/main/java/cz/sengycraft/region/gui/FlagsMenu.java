package cz.sengycraft.region.gui;

import cz.sengycraft.region.configuration.ConfigurationManager;
import cz.sengycraft.region.regions.Region;
import cz.sengycraft.region.regions.flags.Flag;
import cz.sengycraft.region.regions.flags.FlagState;
import cz.sengycraft.region.utils.ComponentUtils;
import cz.sengycraft.region.utils.ItemStackUtils;
import cz.sengycraft.region.utils.MessageUtils;
import cz.sengycraft.region.utils.Pair;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class FlagsMenu {

    static FileConfiguration config = ConfigurationManager.getInstance().getConfiguration("config");

    public static void openFlagsMenu(Player player, Region region) {
        String title = config.getString("regions.gui.flags.title").replace("{region}", region.getName());
        int rows = config.getInt("regions.gui.flags.rows");

        PaginatedGui gui = Gui.paginated()
                .title(ComponentUtils.deserialize(title))
                .rows(rows)
                .pageSize((rows - 1) * 9)
                .disableAllInteractions()
                .create();

        addControlButtons(gui, player, region);
        addFlags(gui, player, region);

        gui.open(player);
    }

    private static void addControlButtons(PaginatedGui gui, Player player, Region region) {
        ItemStack backItem = getControlItem("back");
        HashSet<Integer> backSlots = ItemStackUtils.getSlots(config.getString("regions.gui.flags.back.slot"));
        gui.setItem(backSlots.stream().toList(), new GuiItem(backItem, event -> RegionMenu.openRegionMenu(player, region)));

        addPaginationControls(gui);
    }

    private static void addPaginationControls(PaginatedGui gui) {
        ItemStack previousPageItem = getControlItem("previous-page");
        HashSet<Integer> previousPageSlots = ItemStackUtils.getSlots(config.getString("regions.gui.flags.previous-page.slot"));
        gui.setItem(previousPageSlots.stream().toList(), new GuiItem(previousPageItem, event -> gui.previous()));

        ItemStack nextPageItem = getControlItem("next-page");
        HashSet<Integer> nextPageSlots = ItemStackUtils.getSlots(config.getString("regions.gui.flags.next-page.slot"));
        gui.setItem(nextPageSlots.stream().toList(), new GuiItem(nextPageItem, event -> gui.next()));
    }

    private static void addFlags(PaginatedGui gui, Player player, Region region) {
        for (Map.Entry<Flag, FlagState> flagEntry : getFlags(region).entrySet()) {
            ItemStack flagItem = getFlagItem(flagEntry);
            gui.addItem(new GuiItem(flagItem, event -> {
                try {
                    // Toggle the flag state
                    FlagState newState = getNextFlagState(flagEntry.getValue());
                    region.changeState(flagEntry.getKey().getName(), newState);

                    // Send confirmation message
                    MessageUtils.sendMessage(player, "flag.changed",
                            new Pair<>("{region}", region.getName()),
                            new Pair<>("{flag}", flagEntry.getKey().getName()),
                            new Pair<>("{state}", newState.toString()));

                    FlagsMenu.openFlagsMenu(player, region);
                } catch (Exception e) {
                    MessageUtils.sendMessage(player, "region-error");
                }
            }));
        }
    }

    private static ItemStack getFlagItem(Map.Entry<Flag, FlagState> flagEntry) {
        String materialName = config.getString("regions.gui.flags.flag.item");
        Material material = Material.getMaterial(materialName.toUpperCase());

        String title = config.getString("regions.gui.flags.flag.title").replace("{flag}", flagEntry.getKey().getName());
        List<String> lore = new ArrayList<>();
        for (FlagState flagState : FlagState.values()) {
            if (flagState == flagEntry.getValue()) {
                lore.add(config.getString("regions.gui.flags.flag.active").replace("{state}", flagState.toString()));
            } else {
                lore.add(config.getString("regions.gui.flags.flag.inactive").replace("{state}", flagState.toString()));
            }
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ComponentUtils.deserialize(title));
        meta.lore(ComponentUtils.deserialize(lore));

        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getControlItem(String key) {
        String materialName = config.getString("regions.gui.flags." + key + ".material");
        Material material = Material.getMaterial(materialName.toUpperCase());

        String title = config.getString("regions.gui.flags." + key + ".title");
        List<String> lore = config.getStringList("regions.gui.flags." + key + ".lore");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ComponentUtils.deserialize(title));
        meta.lore(ComponentUtils.deserialize(lore));

        item.setItemMeta(meta);
        return item;
    }

    private static Map<Flag, FlagState> getFlags(Region region) {
        return region.getFlags();
    }

    private static FlagState getNextFlagState(FlagState currentState) {

        FlagState[] states = FlagState.values();
        int nextIndex = (currentState.ordinal() + 1) % states.length;
        return states[nextIndex];
    }
}
