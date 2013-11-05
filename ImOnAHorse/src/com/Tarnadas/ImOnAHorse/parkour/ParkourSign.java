package com.Tarnadas.ImOnAHorse.parkour;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.Tarnadas.ImOnAHorse.ImOnAHorse;
import com.Tarnadas.ImOnAHorse.Exceptions.FinishNotSetException;
import com.Tarnadas.ImOnAHorse.Exceptions.ParkourDoesNotExistException;
import com.Tarnadas.ImOnAHorse.Exceptions.PlayerAlreadyInParkourException;
import com.Tarnadas.ImOnAHorse.Exceptions.PlayerIsMountedException;
import com.Tarnadas.ImOnAHorse.Exceptions.PlayerNotInParkourException;

public class ParkourSign {

	private static Map<Location,ParkourSign> signs;
	
	private Location loc;
	private Parkour parkour;
	
	// called on creation
	public ParkourSign(Location loc, Parkour parkour) {
		this.loc = loc;
		this.parkour = parkour;
		File file = new File(ImOnAHorse.plugin.getDataFolder(), "signs.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		List<String> signList = config.getStringList("signs");
		signList.add(this.toString());
		config.set("signs", signList);
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		signs.put(loc, this);
	}

	// called on loading data
	public ParkourSign(String s) {
		this.loc = this.toLocation(s);
		String parkourName = s.split(":")[4];
		if (parkourName.equals("leave")) {
			this.parkour = null;
		} else {
			try {
				this.parkour = Parkour.getParkour(parkourName);
			} catch (ParkourDoesNotExistException e) {
				e.printStackTrace();
			}
		}
		signs.put(this.loc, this);
	}
	
	public static void onLoad() {
		signs = new HashMap<Location,ParkourSign>();
		File file = new File(ImOnAHorse.plugin.getDataFolder(), "signs.yml");
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		List<String> signList = config.getStringList("signs");
		for (String s : signList)
			new ParkourSign(s);
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void release() {
		signs.remove(this.loc);
	}
	
	public void execute(Player player) {
		if (parkour != null) {
			try {
				parkour.startParkour(player);
			} catch (PlayerIsMountedException e) {
				String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You have to dismount in order to do that!";
				player.sendMessage(s);
			} catch (FinishNotSetException e) {
				String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "No finish has been set for this parkour!";
				player.sendMessage(s);
			} catch (PlayerAlreadyInParkourException e) {
				String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You are already in a parkour!" +
						ChatColor.AQUA + "\nType " + ChatColor.RED + "/horse parkour leave " +
						ChatColor.AQUA + "to leave it";
				player.sendMessage(s);
			}
		} else {
			try {
				Parkour.getParkour(player).finishParkour(player, false, true);
			} catch (PlayerNotInParkourException e) {
				String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You are not in any parkour!";
				player.sendMessage(s);
			}
		}
	}
	
	public static boolean isParkourSign(Location loc) {
		return signs.keySet().contains(loc);
	}
	
	public static ParkourSign getSign(Location loc) {
		return signs.get(loc);
	}
	
	public String toString() {
		return (this.parkour != null) ? this.loc.getWorld().getName() + ":" + this.loc.getBlockX() + ":" + this.loc.getBlockY() + ":" + this.loc.getBlockZ() + ":" + this.parkour.getName() :
			this.loc.getWorld().getName() + ":" + this.loc.getBlockX() + ":" + this.loc.getBlockY() + ":" + this.loc.getBlockZ() + ":" + "leave";
	}
	
	private Location toLocation(String s) {
		String[] sl = s.split(":");
		return new Location(ImOnAHorse.plugin.getServer().getWorld(sl[0]),
				Double.parseDouble(sl[1]), Double.parseDouble(sl[2]), Double.parseDouble(sl[3]));
	}
	
}
