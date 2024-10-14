package cz.sengycraft.region.gui;

import cz.sengycraft.region.configuration.ConfigurationManager;
import cz.sengycraft.region.listeners.RenameListener;
import cz.sengycraft.region.regions.Region;
import cz.sengycraft.region.regions.wand.WandManager;
import cz.sengycraft.region.utils.ComponentUtils;
import cz.sengycraft.region.utils.ItemStackUtils;
import cz.sengycraft.region.utils.MessageUtils;
import cz.sengycraft.region.utils.Pair;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;

public class RegionMenu {

    static FileConfiguration config = ConfigurationManager.getInstance().getConfiguration("config");

    public static void openRegionMenu(Player player, Region region) {
        String title = config.getString("regions.gui.region.title").replace("{region}", region.getName());
        int rows = config.getInt("regions.gui.region.rows");

        Gui gui = Gui.gui()
                .title(ComponentUtils.deserialize(title))
                .rows(rows)
                .disableAllInteractions()
                .create();

        addControlButtons(gui, player, region);

        gui.open(player);
    }

    private static void addControlButtons(Gui gui, Player player, Region region) {
        ItemStack renameItem = getControlItem("rename", region);
        HashSet<Integer> renameSlots = ItemStackUtils.getSlots(config.getString("regions.gui.region.rename.slot"));
        gui.setItem(renameSlots.stream().toList(), new GuiItem(renameItem, event -> {
            gui.close(player);
            RenameListener.getInstance().add(player.getUniqueId(), region);
            MessageUtils.sendMessage(player, "rename");
        }));

        ItemStack whitelistAddItem = getControlItem("whitelist-add", region);
        HashSet<Integer> whitelistAddSlots = ItemStackUtils.getSlots(config.getString("regions.gui.region.whitelist-add.slot"));
        gui.setItem(whitelistAddSlots.stream().toList(), new GuiItem(whitelistAddItem, event -> WhitelistAddMenu.openWhitelistAddMenu(player, region)));

        ItemStack whitelistRemoveItem = getControlItem("whitelist-remove", region);
        HashSet<Integer> whitelistRemoveSlots = ItemStackUtils.getSlots(config.getString("regions.gui.region.whitelist-remove.slot"));
        gui.setItem(whitelistRemoveSlots.stream().toList(), new GuiItem(whitelistRemoveItem, event -> WhitelistRemoveMenu.openWhitelistRemoveMenu(player, region)));

        ItemStack redefineLocationItem = getControlItem("redefine-location", region);
        HashSet<Integer> redefineLocationSlots = ItemStackUtils.getSlots(config.getString("regions.gui.region.redefine-location.slot"));
        gui.setItem(redefineLocationSlots.stream().toList(), new GuiItem(redefineLocationItem, event -> {
            Location[] loc = WandManager.getInstance().getLocations(player.getUniqueId());

            if (loc == null || loc[0] == null || loc[1] == null) {
                MessageUtils.sendMessage(player, "locations-not-set");
            } else {
                if (!loc[0].getWorld().getName().equalsIgnoreCase(loc[1].getWorld().getName())) {
                    MessageUtils.sendMessage(player, "not-in-same-world");
                } else {
                    try {
                        region.setPos1(loc[0]);
                        region.setPos2(loc[1]);
                        MessageUtils.sendMessage(player, "changed-location", new Pair<>("{region}", region.getName()));
                        WandManager.getInstance().clearLocations(player.getUniqueId());
                        RegionsMenu.openRegionsMenu(player);
                    } catch (Exception e) {
                        MessageUtils.sendMessage(player, "region-error");
                    }
                }
            }
        }));

        ItemStack editFlagsItem = getControlItem("edit-flags", region);
        HashSet<Integer> editFlagsSlots = ItemStackUtils.getSlots(config.getString("regions.gui.region.edit-flags.slot"));
        gui.setItem(editFlagsSlots.stream().toList(), new GuiItem(editFlagsItem, event -> FlagsMenu.openFlagsMenu(player, region)));

        ItemStack backItem = getControlItem("back", region);
        HashSet<Integer> backSlots = ItemStackUtils.getSlots(config.getString("regions.gui.region.back.slot"));
        gui.setItem(backSlots.stream().toList(), new GuiItem(backItem, event -> RegionsMenu.openRegionsMenu(player)));
    }

    private static ItemStack getControlItem(String key, Region region) {
        String materialName = config.getString("regions.gui.region." + key + ".material");
        Material material = Material.getMaterial(materialName.toUpperCase());

        String title = config.getString("regions.gui.region." + key + ".title");
        List<String> lore = config.getStringList("regions.gui.region." + key + ".lore").stream().map(string -> string.replace("{region}", region.getName())).toList();

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ComponentUtils.deserialize(title));
        meta.lore(ComponentUtils.deserialize(lore));

        item.setItemMeta(meta);
        return item;
    }
}