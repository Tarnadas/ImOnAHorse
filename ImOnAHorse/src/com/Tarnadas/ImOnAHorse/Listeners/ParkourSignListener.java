package com.Tarnadas.ImOnAHorse.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.Tarnadas.ImOnAHorse.Exceptions.ParkourDoesNotExistException;
import com.Tarnadas.ImOnAHorse.parkour.Parkour;
import com.Tarnadas.ImOnAHorse.parkour.ParkourSign;

public class ParkourSignListener implements Listener {

	private static final String firstLine = ChatColor.DARK_RED + "[ImOnAHorse]";
	private static final String secondLine = ChatColor.AQUA + "join parkour";
	private static final String secondLineLeave = ChatColor.AQUA + "leave parkour";
	
	@EventHandler(priority=EventPriority.NORMAL)
    public void onSignChangeEvent(SignChangeEvent event) {
		
		if (!event.getLine(0).equalsIgnoreCase("[horse]")) return;
		if (!event.getPlayer().hasPermission("horse.parkour.sign.create")) return;
		event.setCancelled(true);
		if (event.getLine(1).equalsIgnoreCase("parkour join")) {
			if (!event.getLine(2).equals("")) {
				try {
					Parkour parkour = Parkour.getParkour(event.getLine(2));
					new ParkourSign(event.getBlock().getLocation(), parkour);
					Sign sign = (Sign) event.getBlock().getState();
					sign.setLine(0, firstLine);
					sign.setLine(1, secondLine);
					sign.setLine(2, ChatColor.AQUA + event.getLine(2));
					sign.setLine(3, "");
					sign.update();
					String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "Parkour sign created for parkour " + event.getLine(2);
					event.getPlayer().sendMessage(s);
				} catch (ParkourDoesNotExistException e) {
					String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Parkour " + event.getLine(2) + " does not exist!";
					event.getPlayer().sendMessage(s);
				}
			} else {
				String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You have to define the parkours name!";
				event.getPlayer().sendMessage(s);
			}
		} else if (event.getLine(1).equalsIgnoreCase("parkour leave")) {
			new ParkourSign(event.getBlock().getLocation(), null);
			Sign sign = (Sign) event.getBlock().getState();
			sign.setLine(0, firstLine);
			sign.setLine(1, secondLineLeave);
			sign.setLine(2, "");
			sign.setLine(3, "");
			sign.update(true);
			String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "Parkour leave sign created";
			event.getPlayer().sendMessage(s);
		} else {
			String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You did not specify anything!\n" +
					"Second line must be 'parkour join' or 'parkour leave'";
			event.getPlayer().sendMessage(s);
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {

		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || !ParkourSign.isParkourSign(event.getClickedBlock().getLocation())) return;
		if (!event.getPlayer().hasPermission("horse.parkour.sign.use")) {
			String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You do not have permission!";
			event.getPlayer().sendMessage(s);
			return;
		}
		ParkourSign.getSign(event.getClickedBlock().getLocation()).execute(event.getPlayer());

	}
	
	@EventHandler(priority=EventPriority.NORMAL)
    public void onBlockBreakEvent(BlockBreakEvent event) {
		
		if (!event.getBlock().getType().equals(Material.SIGN) && !event.getBlock().getType().equals(Material.SIGN_POST)) return;
		if (!ParkourSign.isParkourSign(event.getBlock().getLocation())) return;
		ParkourSign.getSign(event.getBlock().getLocation()).release();
		String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You have deleted a parkour sign";
		event.getPlayer().sendMessage(s);
		
	}
		
}
