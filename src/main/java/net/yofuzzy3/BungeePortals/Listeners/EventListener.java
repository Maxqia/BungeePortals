package net.yofuzzy3.BungeePortals.Listeners;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.yofuzzy3.BungeePortals.BungeePortals;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class EventListener implements Listener {

    private BungeePortals plugin;
    private Map<String, Boolean> statusData = new HashMap<>();
    private HashMap<Player, Long> cooldown = new HashMap<Player, Long>();

    public EventListener(BungeePortals plugin) {
        this.plugin = plugin;
    }

    private boolean CheckCooldown(Player player) {
        final int cooldelay = plugin.getConfig().getInt("CooldownSeconds");
        int diff = (int) ((System.currentTimeMillis() - cooldown.get(player)) / 1000);
        if (diff < cooldelay) {
            player.sendMessage(ChatColor.RED + "Please wait " + ChatColor.YELLOW + (cooldelay - diff) + ChatColor.RED + " seconds until attempting to teleport again.");
            return true;
        }
        return false;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        cooldown.put(event.getPlayer(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) throws IOException {
        Player player = event.getPlayer();
        String playerName = player.getName();
        if (!statusData.containsKey(playerName)) {
            statusData.put(playerName, false);
        }
        Block block = player.getWorld().getBlockAt(player.getLocation());
        String data = block.getWorld().getName() + "#" + String.valueOf(block.getX()) + "#" + String.valueOf(block.getY()) + "#" + String.valueOf(block.getZ());
        if (plugin.portalData.containsKey(data)) {
            if (!statusData.get(playerName)) {
                statusData.put(playerName, true);
                if (CheckCooldown(player)) return;
                String destination = plugin.portalData.get(data);
                if (player.hasPermission("BungeePortals.portal." + destination) || player.hasPermission("BungeePortals.portal.*")) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(baos);
                    dos.writeUTF("Connect");
                    dos.writeUTF(destination);
                    player.sendPluginMessage(plugin, "BungeeCord", baos.toByteArray());
                    baos.close();
                    dos.close();
                    cooldown.put(player, System.currentTimeMillis());
                } else {
                    player.sendMessage(plugin.configFile.getString("NoPortalPermissionMessage").replace("{destination}", destination).replaceAll("(&([a-f0-9l-or]))", "\u00A7$2"));
                }
            }
        } else {
            if (statusData.get(playerName)) {
                statusData.put(playerName, false);
            }
        }
    }
}
