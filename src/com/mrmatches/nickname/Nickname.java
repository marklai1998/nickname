package com.mrmatches.nickname;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;


public class Nickname extends JavaPlugin implements Listener {

    private FileConfiguration config = this.getConfig();
    private Scoreboard scoreboard;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.saveDefaultConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("nickname") && sender instanceof Player && sender.isOp()) {
            if (args.length == 3) {
                setPrefix(sender, args[0], args[1], args[2]);
                return true;
            } else if (args.length == 2) {
                setPrefix(sender, args[0], args[1], "normal");
                return true;
            } else if (args.length == 1) {
                setPrefix(sender, args[0], null, "normal");
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String prefix = this.getPrefix(player);
        ChatColor color = getColor(player);
        if (prefix != null)
            event.setFormat("" + color + '[' + prefix + "]" + ChatColor.RESET + " %1$s : %2$s");
        else event.setFormat("%1$s : %2$s");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String prefix = this.getPrefix(player);
        ChatColor color = getColor(player);
        if (prefix != null) {
            setScoreboard(player.getName(), prefix, color);
            event.setJoinMessage(ChatColor.YELLOW + "[" + prefix + "] " + player.getDisplayName() + " 已上線");
        } else event.setJoinMessage(ChatColor.YELLOW + player.getDisplayName() + " 已上線");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String prefix = this.getPrefix(player);
        if (prefix != null)
            event.setQuitMessage(ChatColor.YELLOW + "[" + prefix + "] " + player.getDisplayName() + " 已下線");
        else event.setQuitMessage(ChatColor.YELLOW + player.getDisplayName() + " 已下線");
    }

    private String getPrefix(Player player) {
        String prefix = config.getString("Users." + player.getUniqueId() + ".nickname");
        if (prefix == null || prefix.equals("")) return null;
        else return prefix;
    }

    private ChatColor getColor(Player player) {
        String role = config.getString("Users." + player.getUniqueId() + ".role");
        if (role == null || role.equals("")) return null;
        switch (role) {
            case "owner":
                return ChatColor.YELLOW;
            case "admin":
                return ChatColor.RED;
            case "ganso":
                return ChatColor.GREEN;
            default:
                return ChatColor.WHITE;
        }
    }

    private void setPrefix(CommandSender sender, String player, String prefix, String role) {
        Player player_ob = Bukkit.getServer().getPlayer(player);
        if (player_ob != null) {
            String uuid = player_ob.getUniqueId().toString();
            String player_name = player_ob.getName();
            config.set("Users." + uuid + ".nickname", prefix);
            config.set("Users." + uuid + ".role", role);
            this.saveConfig();
            ChatColor color = getColor(player_ob);
            setScoreboard(player_name, prefix, color);
        } else sender.sendMessage(ChatColor.RED + "該玩家必須在線!");
    }

    private void setScoreboard(String player_name, String prefix, ChatColor color) {
        Team team = scoreboard.getTeam(player_name);
        if (team == null) team = scoreboard.registerNewTeam(player_name);
        team.setPrefix("" + color + '[' + prefix + "] " + ChatColor.RESET);
        team.addEntry(player_name);
        for (Player all : Bukkit.getOnlinePlayers()) {
            all.setScoreboard(scoreboard);
        }
    }
}