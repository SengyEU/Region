package cz.sengycraft.region.commands;

import cz.sengycraft.region.RegionPlugin;
import cz.sengycraft.region.common.Permissions;
import cz.sengycraft.region.regions.Region;
import cz.sengycraft.region.regions.RegionManager;
import cz.sengycraft.region.regions.flags.Flag;
import cz.sengycraft.region.regions.flags.FlagRegistry;
import cz.sengycraft.region.regions.flags.FlagState;
import cz.sengycraft.region.regions.wand.WandManager;
import cz.sengycraft.region.utils.MessageUtils;
import cz.sengycraft.region.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

        RegionManager regionManager = RegionManager.getInstance();
        WandManager wandManager = WandManager.getInstance();
        FlagRegistry flagRegistry = FlagRegistry.getInstance();

        if (args.length < 1) {
            MessageUtils.sendMessage(player, "invalid-usage");
            return false;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "wand" -> {
                if (!player.hasPermission(Permissions.WAND.permission())) {
                    MessageUtils.sendMessage(player, "no-permission");
                    return false;
                }
                if (args.length == 1) {
                    if (isInventoryFull(player)) {
                        MessageUtils.sendMessage(player, "no-inventory-space");
                        return false;
                    }

                    player.getInventory().addItem(wandManager.getWand());
                    MessageUtils.sendMessage(player, "give-wand");
                    return true;
                }
            }
            case "create" -> {
                if (!player.hasPermission(Permissions.CREATE.permission())) {
                    MessageUtils.sendMessage(player, "no-permission");
                    return false;
                }
                if (args.length == 2) {
                    return handleCreateCommand(player, args[1].toLowerCase(), wandManager, regionManager);
                }
            }
            case "whitelist" -> {
                if (!player.hasPermission(Permissions.WHITELIST.permission())) {
                    MessageUtils.sendMessage(player, "no-permission");
                    return false;
                }
                if (args.length == 2) {
                    return handleWhitelistCommand(player, args[1].toLowerCase(), regionManager);
                }
            }
            case "add", "remove" -> {
                if (!player.hasPermission(subCommand.equals("add") ? Permissions.ADD.permission() : Permissions.REMOVE.permission())) {
                    MessageUtils.sendMessage(player, "no-permission");
                    return false;
                }
                if (args.length == 3) {
                    return handleWhitelistModification(player, subCommand, args[1].toLowerCase(), args[2], regionManager);
                }
            }
            case "flag" -> {
                if (!player.hasPermission(Permissions.FLAG.permission())) {
                    MessageUtils.sendMessage(player, "no-permission");
                    return false;
                }
                if (args.length == 4) {
                    return handleFlagCommand(player, args[1].toLowerCase(), args[2], args[3], regionManager, flagRegistry);
                }
            }
        }

        MessageUtils.sendMessage(player, "invalid-usage");
        return true;
    }


    private boolean isInventoryFull(Player player) {
        return Arrays.stream(player.getInventory().getStorageContents()).noneMatch(Objects::isNull);
    }

    private boolean handleCreateCommand(Player player, String regionName, WandManager wandManager, RegionManager regionManager) {
        Location[] loc = wandManager.getLocations(player.getUniqueId());

        if (loc == null || loc[0] == null || loc[1] == null) {
            MessageUtils.sendMessage(player, "locations-not-set");
            return false;
        }

        if (!loc[0].getWorld().getName().equalsIgnoreCase(loc[1].getWorld().getName())) {
            MessageUtils.sendMessage(player, "not-in-same-world");
            return false;
        }

        try {
            if (!regionManager.addRegions(new Region(regionName, loc[0], loc[1]))) {
                MessageUtils.sendMessage(player, "region-name-exists");
                return false;
            }
            MessageUtils.sendMessage(player, "region-created", new Pair<>("{region}", regionName));
            wandManager.clearLocations(player.getUniqueId());
            return true;
        } catch (Exception e) {
            MessageUtils.sendMessage(player, "region-error");
            plugin.getComponentLogger().error("Error when creating the region!", e);
            return false;
        }
    }

    private boolean handleWhitelistCommand(Player player, String regionName, RegionManager regionManager) {
        if (regionDoesntExist(regionName, regionManager)) {
            MessageUtils.sendMessage(player, "region-not-exists");
            return false;
        }

        Region region = regionManager.getRegion(regionName);
        MessageUtils.sendMessage(player, "whitelist.title", new Pair<>("{region}", regionName));
        for (String whitelistedPlayer : region.getWhitelistedPlayers()) {
            MessageUtils.sendMessage(player, "whitelist.player", new Pair<>("{player}", whitelistedPlayer));
        }
        return true;
    }

    private boolean handleWhitelistModification(Player player, String action, String regionName, String targetPlayer, RegionManager regionManager) {
        if (regionDoesntExist(regionName, regionManager)) {
            MessageUtils.sendMessage(player, "region-not-exists");
            return false;
        }

        Region region = regionManager.getRegion(regionName);

        if (action.equals("add")) {
            if (region.getWhitelistedPlayers().contains(targetPlayer)) {
                MessageUtils.sendMessage(player, "whitelist.already");
                return false;
            }

            try {
                region.addWhitelistedPlayer(targetPlayer);
                MessageUtils.sendMessage(player, "whitelist.added", new Pair<>("{region}", regionName), new Pair<>("{player}", targetPlayer));
                return true;
            } catch (Exception e) {
                MessageUtils.sendMessage(player, "region-error");
                plugin.getComponentLogger().error("Error adding player to whitelist!", e);
                return false;
            }
        } else if (action.equals("remove")) {
            if (!region.getWhitelistedPlayers().contains(targetPlayer)) {
                MessageUtils.sendMessage(player, "whitelist.not");
                return false;
            }

            try {
                region.removeWhitelistedPlayer(targetPlayer);
                MessageUtils.sendMessage(player, "whitelist.removed", new Pair<>("{region}", regionName), new Pair<>("{player}", targetPlayer));
                return true;
            } catch (Exception e) {
                MessageUtils.sendMessage(player, "region-error");
                plugin.getComponentLogger().error("Error removing player from whitelist!", e);
                return false;
            }
        }

        return false;
    }

    private boolean handleFlagCommand(Player player, String regionName, String flag, String state, RegionManager regionManager, FlagRegistry flagRegistry) {
        if (regionDoesntExist(regionName, regionManager)) {
            MessageUtils.sendMessage(player, "region-not-exists");
            return false;
        }

        if (!flagRegistry.getFlags().stream().map(Flag::getName).toList().contains(flag)) {
            MessageUtils.sendMessage(player, "flag.invalid-flag");
            return false;
        }

        if (!List.of("EVERYONE", "WHITELIST", "NONE").contains(state.toUpperCase())) {
            MessageUtils.sendMessage(player, "flag.invalid-state");
            return false;
        }

        try {
            regionManager.getRegion(regionName).changeState(flag, FlagState.valueOf(state.toUpperCase()));
            MessageUtils.sendMessage(player, "flag.changed", new Pair<>("{region}", regionName), new Pair<>("{flag}", flag), new Pair<>("{state}", state.toUpperCase()));
            return true;
        } catch (Exception e) {
            MessageUtils.sendMessage(player, "region-error");
            plugin.getComponentLogger().error("Error when changing flag state!", e);
            return false;
        }
    }

    private boolean regionDoesntExist(String regionName, RegionManager regionManager) {
        return !regionManager.getNames().contains(regionName.toLowerCase());
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) return Collections.emptyList();

        RegionManager regionManager = RegionManager.getInstance();
        FlagRegistry flagRegistry = FlagRegistry.getInstance();

        switch (args.length) {
            case 1 -> {
                return List.of("wand", "create", "whitelist", "add", "remove", "flag");
            }
            case 2 -> {
                return switch (args[0]) {
                    case "create" -> List.of("<name>");
                    case "whitelist", "add", "remove", "flag" -> regionManager.getNames();
                    default -> Collections.emptyList();
                };
            }
            case 3 -> {
                return switch (args[0]) {
                    case "add" -> Bukkit.getServer().getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(playerName -> !regionManager.getRegion(args[1]).getWhitelistedPlayers().contains(playerName))
                            .collect(Collectors.toList());
                    case "remove" -> regionManager.getRegion(args[1]).getWhitelistedPlayers();
                    case "flag" -> flagRegistry.getFlags().stream()
                            .map(Flag::getName)
                            .collect(Collectors.toList());
                    default -> Collections.emptyList();
                };
            }
            case 4 -> {
                if ("flag".equals(args[0])) {
                    return List.of("EVERYONE", "WHITELIST", "NONE");
                }
            }
        }

        return Collections.emptyList();
    }

}
