package cz.sengycraft.region.listeners;

import cz.sengycraft.region.common.Permissions;
import cz.sengycraft.region.regions.Region;
import cz.sengycraft.region.regions.RegionManager;
import cz.sengycraft.region.regions.flags.Flag;
import cz.sengycraft.region.regions.flags.FlagState;
import cz.sengycraft.region.utils.MessageUtils;
import cz.sengycraft.region.utils.Pair;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class FlagsListeners implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        handleEvent(event.getPlayer(), event.getBlock().getLocation(), "block-break", event);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        handleEvent(event.getPlayer(), event.getBlock().getLocation(), "block-place", event);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        handleEvent(event.getPlayer(), event.getPlayer().getLocation(), "interact", event);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            handleEvent(player, event.getEntity().getLocation(), "entity-damage", event);
        }
    }

    private void handleEvent(Player player, Location location, String action, Cancellable event) {
        Region region = getRegion(location);
        if (region == null) {
            return;
        }

        FlagState state = region.getFlags().get(new Flag(action));


        if ((state == FlagState.NONE && isPlayerBypassed(player)) ||
                (state == FlagState.WHITELIST && isPlayerWhitelistedOrBypassed(region, player))) {
            return;
        }

        MessageUtils.sendMessage(player, "denied",
                new Pair<>("{flag}", action),
                new Pair<>("{region}", region.getName())
        );
        event.setCancelled(true);
    }

    private boolean isPlayerWhitelistedOrBypassed(Region region, Player player) {
        return region.isPlayerWhitelisted(player.getName()) || isPlayerBypassed(player);
    }

    private boolean isPlayerBypassed(Player player) {
        return player.hasPermission(Permissions.BYPASS.permission());
    }

    private Region getRegion(Location location) {
        return RegionManager.getInstance().getRegions()
                .stream()
                .filter(region -> region.contains(location))
                .findFirst()
                .orElse(null);
    }
}
