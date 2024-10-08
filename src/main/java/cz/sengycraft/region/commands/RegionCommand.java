package cz.sengycraft.region.commands;

import cz.sengycraft.region.RegionPlugin;
import cz.sengycraft.region.regions.Region;
import cz.sengycraft.region.regions.RegionManager;
import cz.sengycraft.region.regions.wand.WandManager;
import cz.sengycraft.region.utils.MessageUtils;
import cz.sengycraft.region.utils.Pair;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RegionCommand implements TabExecutor {

    private RegionPlugin plugin;

    public RegionCommand(RegionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "only-players");
            return false;
        }

        switch (args.length) {
            case 1 -> {
                switch (args[0]) {
                    case "wand" -> {
                        if(Arrays.stream(player.getInventory().getStorageContents()).noneMatch(Objects::isNull)) {
                            MessageUtils.sendMessage(player, "no-inventory-space");
                            return false;
                        }

                        player.getInventory().addItem(WandManager.getInstance().getWand());
                        MessageUtils.sendMessage(player, "give-wand");
                    }
                }
            }
            case 2 -> {
                switch (args [0]) {
                    case "create" -> {
                        Location[] loc = WandManager.getInstance().getLocations(player.getUniqueId());

                        if(loc == null || loc[0] == null || loc[1] == null) {
                            MessageUtils.sendMessage(player, "locations-not-set");
                            return false;
                        }

                        if(!loc[0].getWorld().getName().equalsIgnoreCase(loc[1].getWorld().getName())) {
                            MessageUtils.sendMessage(player, "not-in-same-world");
                            return false;
                        }

                        String name = args[1].toLowerCase();
                        try {
                            if(!RegionManager.getInstance().addRegions(new Region(name, loc[0], loc[1]))) {
                                MessageUtils.sendMessage(player, "region-name-exists");
                                return false;
                            }
                            MessageUtils.sendMessage(player, "region-created", new Pair<>("{region}", name));
                            WandManager.getInstance().clearLocations(player.getUniqueId());
                        } catch (Exception e) {
                            MessageUtils.sendMessage(player, "region-error");
                            plugin.getComponentLogger().error("Error when creating the region!", e);
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 1 -> {
                return Arrays.asList("wand", "create");
            }
            case 2 -> {
                switch (args[0]) {
                     case "create" -> {
                        return List.of("<name>");
                    }
                }
            }
        }

        return new ArrayList<>();
    }
}
