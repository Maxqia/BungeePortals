package net.yofuzzy3.BungeePortals.Commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.yofuzzy3.BungeePortals.BungeePortals;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class CommandBPortals implements CommandExecutor {

    private BungeePortals plugin;
    public static Map<String, List<String>> selections = new HashMap<>();

    public CommandBPortals(BungeePortals plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("BPortals")) {
            if (sender.hasPermission("BungeePortals.command.BPortals")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    String playerName = sender.getName();
                    if (args.length >= 1) {
                        switch (args[0].toLowerCase()) {
                            case "reload":
                                plugin.loadConfigFiles();
                                plugin.loadPortalsData();
                                sender.sendMessage(ChatColor.GREEN + "All configuration files and data have been reloaded.");
                                break;
                            case "forcesave":
                                plugin.savePortalsData();
                                sender.sendMessage(ChatColor.GREEN + "Portal data saved!");
                                break;
                            case "create":
                                if (select(player, ((args.length >= 3) ? args[2] : null) )) {
                                    List<String> selection = selections.get(playerName);
                                    for (String block : selection) {
                                        plugin.portalData.put(block, args[1]);
                                    }
                                    player.sendMessage(ChatColor.GREEN + String.valueOf(selection.size()) + " portals have been created.");
                                    selections.remove(playerName);
                                }
                                break;
                            case "remove":
                                if (select(player, null)) {
                                    int count = 0;
                                    for (String block : selections.get(playerName)) {
                                        if (plugin.portalData.containsKey(block)) {
                                            plugin.portalData.remove(block);
                                            count++;
                                        }
                                    }
                                    sender.sendMessage(ChatColor.GREEN + String.valueOf(count) + " portals have been removed.");
                                    selections.remove(playerName);
                                }
                                break;
                            default: help(sender);
                        }
                    } else help(sender);
                } else sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            } else sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            return true;
        }
        return false;
    }

    private void help(CommandSender sender) {
        sender.sendMessage(ChatColor.BLUE + "BungeePortals v" + plugin.getDescription().getVersion() + " by YoFuzzy3");
        sender.sendMessage(ChatColor.GREEN + "/BPortals reload " + ChatColor.RED + "Reload all files and data.");
        sender.sendMessage(ChatColor.GREEN + "/BPortals forcesave " + ChatColor.RED + "Force-save portals.");
        sender.sendMessage(ChatColor.GREEN + "/BPortals create <destination> <filter,list> " + ChatColor.RED + "Create portals.");
        sender.sendMessage(ChatColor.GREEN + "/BPortals remove " + ChatColor.RED + "Remove portals.");
        sender.sendMessage(ChatColor.BLUE + "Visit www.spigotmc.org/resources/bungeeportals.19 for help.");
    }

    private List<Location> getLocationsFromCuboid(CuboidSelection cuboid) {
        List<Location> locations = new ArrayList<>();
        Location minLocation = cuboid.getMinimumPoint();
        Location maxLocation = cuboid.getMaximumPoint();
        for (int i1 = minLocation.getBlockX(); i1 <= maxLocation.getBlockX(); i1++) {
            for (int i2 = minLocation.getBlockY(); i2 <= maxLocation.getBlockY(); i2++) {
                for (int i3 = minLocation.getBlockZ(); i3 <= maxLocation.getBlockZ(); i3++) {
                    locations.add(new Location(cuboid.getWorld(), i1, i2, i3));
                }
            }
        }
        return locations;
    }

    private boolean select(CommandSender sender, String args) {
        Player player = (Player) sender;
        String playerName = player.getName();
        Selection selection = plugin.worldEdit.getSelection(player);
        if (selection != null) {
            if (selection instanceof CuboidSelection) {
                List<Location> locations = getLocationsFromCuboid((CuboidSelection) selection);
                List<String> blocks = new ArrayList<>();
                String[] ids = {};
                int count = 0;
                int filtered = 0;
                boolean filter = false;
                if (args != null) {
                    ids = args.split(",");
                    filter = true;
                }
                for (Location location : locations) {
                    Block block = player.getWorld().getBlockAt(location);
                    if (filter) {
                        boolean found = false;
                        for (int i = 0; i < ids.length; i++) {
                            String[] parts = ids[i].split(":");
                            if (parts.length == 2) {
                                if (parts[0].equals(String.valueOf(block.getTypeId())) && parts[1].equals(String.valueOf(block.getData()))) {
                                    found = true;
                                    break;
                                }
                            } else {
                                if (parts[0].equals(String.valueOf(block.getTypeId()))) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (found) {
                            blocks.add(block.getWorld().getName() + "#" + String.valueOf(block.getX()) + "#" + String.valueOf(block.getY()) + "#" + String.valueOf(block.getZ()));
                            count++;
                        } else {
                            filtered++;
                        }
                    } else {
                        blocks.add(block.getWorld().getName() + "#" + String.valueOf(block.getX()) + "#" + String.valueOf(block.getY()) + "#" + String.valueOf(block.getZ()));
                        count++;
                    }
                }
                selections.put(playerName, blocks);
                sender.sendMessage(ChatColor.GREEN + String.valueOf(count) + " blocks have been selected, " + String.valueOf(filtered) + " filtered.");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Must be a cuboid selection!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You have to first create a WorldEdit selection!");
        }
        return false;
    }
}
