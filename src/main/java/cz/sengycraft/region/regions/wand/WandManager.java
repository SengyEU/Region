package cz.sengycraft.region.regions.wand;

import cz.sengycraft.region.RegionPlugin;
import cz.sengycraft.region.configuration.ConfigurationManager;
import cz.sengycraft.region.utils.ComponentUtils;
import cz.sengycraft.region.utils.MessageUtils;
import cz.sengycraft.region.utils.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WandManager implements Listener {

    private RegionPlugin plugin;

    private NamespacedKey key;

    public void setPlugin(RegionPlugin plugin) {
        this.plugin = plugin;
    }

    private static WandManager instance;

    public static WandManager getInstance() {
        if (instance == null) instance = new WandManager();
        return instance;
    }

    private final Map<UUID, Location[]> playerSelections = new HashMap<>();

    public ItemStack getWand() {
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        meta.displayName(ComponentUtils.deserialize(ConfigurationManager.getInstance().getConfiguration("config").getString("regions.wand.item-name")));

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        key = new NamespacedKey(plugin, "region_wand");
        pdc.set(key, PersistentDataType.BOOLEAN, true);

        wand.setItemMeta(meta);

        return wand;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.STICK) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey wandKey = key;

        if (!pdc.has(wandKey, PersistentDataType.BOOLEAN)) return;

        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null || clickedBlock.getType().isAir()) return;

        if (event.getHand() != EquipmentSlot.HAND) return;

        Location blockLocation = clickedBlock.getLocation();

        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK -> handleSelection(player, blockLocation, 0, event);
            case RIGHT_CLICK_BLOCK -> handleSelection(player, blockLocation, 1, event);
        }
    }

    private void handleSelection(Player player, Location blockLocation, int locIndex, Cancellable event) {
        setPlayerSelection(player, locIndex, blockLocation);
        MessageUtils.sendMessage(
                player,
                "location-set",
                new Pair<>("{loc}", String.valueOf(locIndex + 1)),
                new Pair<>("{x}", String.valueOf(blockLocation.getX())),
                new Pair<>("{y}", String.valueOf(blockLocation.getY())),
                new Pair<>("{z}", String.valueOf(blockLocation.getZ())),
                new Pair<>("{world}", blockLocation.getWorld().getName())
        );
        event.setCancelled(true);
    }

    public void setPlayerSelection(Player player, int index, Location location) {
        Location[] locations = playerSelections.get(player.getUniqueId());
        if (locations == null) {
            locations = new Location[2];
        }
        locations[index] = location;
        playerSelections.put(player.getUniqueId(), locations);
    }

    public Location[] getLocations(UUID uuid) {
        return playerSelections.get(uuid);
    }

    public void clearLocations(UUID uuid) {
        playerSelections.put(uuid, new Location[2]);
    }


}
