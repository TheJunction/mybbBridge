/*
 * Copyright (c) 2015 CubeXMC. All Rights Reserved.
 * Created by PantherMan594.
 */

package net.cubexmc.mybbbridge;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	
	private static Connection conn;
	
	@Override
	public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
		try {
			Class.forName("com.mysql.jdbc.Driver");
		    conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/MyBB", "david", "DavidShen");
		} catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public synchronized boolean playerDataContainsPlayer(Player player) {
		try {
			ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM mybb_users WHERE username='" + player.getName() + "';");
			return rs.next();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

    public synchronized boolean playerDataContainsUuid(String uuid) {
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM mybb_users WHERE uuid='" + uuid + "';");
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
	
	public synchronized void sync() {
		for ( Player player : Bukkit.getOnlinePlayers()) {
			String uuidstr = String.valueOf(player.getUniqueId());
			try {
				if (playerDataContainsPlayer(player)) {
					ResultSet rs = conn.createStatement().executeQuery("SELECT uuid FROM mybb_users WHERE username='" + player.getName() + "';");
					rs.next();
					String uuid = rs.getString("uuid");
					if (uuid == null || uuid.equals("")) conn.createStatement().executeUpdate("UPDATE mybb_users SET uuid='" + uuidstr + "' WHERE username='" + player.getName() + "';"); //Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "sync console all pex user" + uuidstr + "group add RMember");
                    conn.createStatement().executeUpdate("UPDATE mybb_users SET avatar='http://cravatar.eu/helmhead/" + uuidstr + "/100.png' WHERE username='" + player.getName() + "';");
					conn.createStatement().executeUpdate("UPDATE mybb_users SET avatardimensions='100|100' WHERE username='" + player.getName() + "';");
                    conn.createStatement().executeUpdate("UPDATE mybb_users SET avatartype='remote' WHERE username='" + player.getName() + "';");
                } else if (playerDataContainsUuid(uuidstr)) conn.createStatement().executeUpdate("UPDATE mybb_users SET username='" + player.getName() + "' WHERE uuid='" + uuidstr + "';");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		getLogger().info("Data has been successfully re-synced.");
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		sync();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = (Player)sender;
		if (commandLabel.equalsIgnoreCase("mybbsync") && player.hasPermission("mybbb.admin")) {
			sync();
			player.sendMessage(ChatColor.GREEN + "Data has been successfully re-synced.");
		}
		return false;
	}
}