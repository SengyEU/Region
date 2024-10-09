package cz.sengycraft.region.gui;

import cz.sengycraft.region.configuration.ConfigurationManager;
import cz.sengycraft.region.regions.Region;
import cz.sengycraft.region.regions.RegionManager;
import cz.sengycraft.region.utils.ComponentUtils;
import cz.sengycraft.region.utils.ItemStackUtils;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegionsMenu {

    static RegionManager regionManager = RegionManager.getInstance();
    static FileConfiguration config = ConfigurationManager.getInstance().getConfiguration("config");

    public static void openRegionsMenu(Player player) {
        Set<Region> regions = regionManager.getRegions();

        String title = config.getString("regions.gui.regions.title");
        int rows = config.getInt("regions.gui.regions.rows");
        int pageSize = (rows - 1) * 9;

        PaginatedGui gui = Gui.paginated()
                .title(ComponentUtils.deserialize(title))
                .rows(rows)
                .pageSize(pageSize)
                .disableAllInteractions()
                .create();

        for (Region region : regions) {
            ItemStack regionItem = getRegionItem(region);
            gui.addItem(new GuiItem(regionItem, event -> {
                RegionMenu.openRegionMenu(player, region);
            }));
        }

        addControlButtons(gui, player);

        gui.open(player);
    }

    private static ItemStack getRegionItem(Region region) {
        String materialName = config.getString("regions.gui.regions.region.item");
        Material material = Material.getMaterial(materialName.toUpperCase());

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String title = config.getString("regions.gui.regions.region.title");
        List<String> lore = config.getStringList("regions.gui.regions.region.lore");

        meta.displayName(ComponentUtils.deserialize(title.replace("{region}", region.getName())));
        meta.lore(ComponentUtils.deserialize(lore.stream()
                .map(line -> line.replace("{region}", region.getName()))
                .toList()));

        item.setItemMeta(meta);
        return item;
    }

    private static void addControlButtons(PaginatedGui gui, Player player) {
        ItemStack previousPageItem = getControlItem("previous-page");
        HashSet<Integer> prevPageSlots = ItemStackUtils.getSlots(config.getString("regions.gui.regions.previous-page.slot"));
        gui.setItem(prevPageSlots.stream().toList(), new GuiItem(previousPageItem, event -> gui.previous()));

        ItemStack nextPageItem = getControlItem("next-page");
        HashSet<Integer> nextPageSlots = ItemStackUtils.getSlots(config.getString("regions.gui.regions.next-page.slot"));
        gui.setItem(nextPageSlots.stream().toList(), new GuiItem(nextPageItem, event -> gui.next()));

        ItemStack refreshItem = getControlItem("refresh");
        HashSet<Integer> refreshSlots = ItemStackUtils.getSlots(config.getString("regions.gui.regions.refresh.slot"));
        gui.setItem(refreshSlots.stream().toList(), new GuiItem(refreshItem, event -> RegionsMenu.openRegionsMenu(player)));

        fillEmptySlots(gui);
    }

    private static ItemStack getControlItem(String key) {
        String materialName = config.getString("regions.gui.regions." + key + ".material");
        Material material = Material.getMaterial(materialName.toUpperCase());

        String title = config.getString("regions.gui.regions." + key + ".title");
        List<String> lore = config.getStringList("regions.gui.regions." + key + ".lore");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ComponentUtils.deserialize(title));
        meta.lore(ComponentUtils.deserialize(lore));

        item.setItemMeta(meta);
        return item;
    }

    private static void fillEmptySlots(PaginatedGui gui) {
        String fillerMaterial = config.getString("regions.gui.regions.fill-item.material");
        Material material = Material.getMaterial(fillerMaterial.toUpperCase());

        String slotRange = config.getString("regions.gui.regions.fill-item.slot");
        HashSet<Integer> slots = ItemStackUtils.getSlots(slotRange);
        gui.setItem(slots.stream().toList(), new GuiItem(new ItemStack(material)));
    }
}