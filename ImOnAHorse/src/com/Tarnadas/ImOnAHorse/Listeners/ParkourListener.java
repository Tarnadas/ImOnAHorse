package com.Tarnadas.ImOnAHorse.Listeners;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.Tarnadas.ImOnAHorse.ImOnAHorse;
import com.Tarnadas.ImOnAHorse.Parkour;
import com.Tarnadas.ImOnAHorse.Exceptions.FinishNotSetException;
import com.Tarnadas.ImOnAHorse.Exceptions.ParkourDoesNotExistException;
import com.Tarnadas.ImOnAHorse.Exceptions.PlayerAlreadyInParkourException;
import com.Tarnadas.ImOnAHorse.Exceptions.PlayerIsMountedException;
import com.Tarnadas.ImOnAHorse.Exceptions.PlayerNotInParkourException;

public class ParkourListener implements Listener {

	public static final int delay = 20;
	
	public static List<Entity> cooldown;
	
	public static void onInit() {
		cooldown = new LinkedList<Entity>();
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
		
		Player player = event.getPlayer();
		if (!Parkour.isPlayerInParkour(player)) return;
		try {
			Parkour.getParkour(player).finishParkour(player, false, true);
		} catch (PlayerNotInParkourException e) {
		}
		
	}
	
	@EventHandler(priority=EventPriority.HIGH)
    public void onInventoryInteractEvent(InventoryClickEvent event) {
		
		Inventory inventory = event.getInventory();
		if (inventory.getHolder() instanceof Horse) {
			Horse horse = (Horse) inventory.getHolder();
			if (horse.getPassenger() instanceof Player && Parkour.isPlayerInParkour((Player) horse.getPassenger())) {
				ItemStack item = event.getCurrentItem();
				if (((HorseInventory) inventory).getArmor().equals(item) || ((HorseInventory) inventory).getSaddle().equals(item))
					event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGH)
    public void onVehicleExitEvent(VehicleExitEvent event) {

		if (cooldown.contains(event.getExited())) {
			final Player player = (Player) event.getExited();
			final Horse horse = (Horse) player.getVehicle();
			new BukkitRunnable() {
				@Override
			    public void run() {
			    	cooldown.remove(player);
			    	restartOnDismount(player, horse);
			    }
			}.runTaskLater(ImOnAHorse.plugin, delay);
			return;
		}
		if (event.getExited() instanceof Player) {
			final Player player = (Player) event.getExited();
			if (Parkour.isPlayerInParkour(player)) {
				try {
					final Horse horse = (Horse) player.getVehicle();
					Parkour.getParkour(player).restartParkour(player);
					cooldown.add(player);
					new BukkitRunnable() {
						@Override
					    public void run() {
					    	cooldown.remove(player);
					    	restartOnDismount(player, horse);
					    }
					}.runTaskLater(ImOnAHorse.plugin, delay);
					String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You have been dismounted so you have to restart the parkour!";
					player.sendMessage(s);
				} catch (ParkourDoesNotExistException
						| PlayerIsMountedException | FinishNotSetException
						| PlayerAlreadyInParkourException
						| PlayerNotInParkourException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		
		if (event.getDamager() instanceof Player) return;
		if (cooldown.contains(event.getEntity())) return;
		if (event.getEntity() instanceof Horse) {
			final Horse horse = (Horse) event.getEntity();
			if (horse.getPassenger() instanceof Player && Parkour.isPlayerInParkour((Player) horse.getPassenger())) {
				final Player player = (Player) horse.getPassenger();
				if (cooldown.contains(player)) return;
				try {
					event.setCancelled(true);
					Parkour.getParkour(player).restartParkour(player);
					cooldown.add(player);
					new BukkitRunnable() {
						@Override
					    public void run() {
					    	cooldown.remove(player);     
					    	restartOnDismount(player, horse); 
					    }
					}.runTaskLater(ImOnAHorse.plugin, delay);
					String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Your horse has taken damage so you have to restart the parkour!";
					player.sendMessage(s);
				} catch (ParkourDoesNotExistException
						| PlayerIsMountedException | FinishNotSetException
						| PlayerAlreadyInParkourException
						| PlayerNotInParkourException e) {
					e.printStackTrace();
				}
			}
		} else if (event.getEntity() instanceof Player  && Parkour.isPlayerInParkour((Player) event.getEntity())) {
			final Player player = (Player) event.getEntity();
			try {
				event.setCancelled(true);
				if (event.getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK)) player.setFireTicks(0);
				final Horse horse = (Horse) player.getVehicle();
				Parkour.getParkour(player).restartParkour(player);
				cooldown.add(player);
				new BukkitRunnable() {
					@Override
				    public void run() {
				    	cooldown.remove(player);
				    	restartOnDismount(player, horse);
				    }
				}.runTaskLater(ImOnAHorse.plugin, delay);
				String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You have taken damage so you have to restart the parkour!";
				player.sendMessage(s);
			} catch (ParkourDoesNotExistException
					| PlayerIsMountedException | FinishNotSetException
					| PlayerAlreadyInParkourException
					| PlayerNotInParkourException e) {
				e.printStackTrace();
			}
		}
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
    public void onEntityCombustEvent(EntityCombustEvent event) {

		if (cooldown.contains(event.getEntity())) return;
		if (event.getEntity() instanceof Player) {
			final Player player = (Player) event.getEntity();
			if (Parkour.isPlayerInParkour(player)) {
				event.setCancelled(true);
				try {
					final Horse horse = (Horse) player.getVehicle();
					Parkour.getParkour(player).restartParkour(player);
					cooldown.add(player);
					new BukkitRunnable() {
						@Override
					    public void run() {
					    	cooldown.remove(player);
					    	restartOnDismount(player, horse);
					    }
					}.runTaskLater(ImOnAHorse.plugin, delay);
					String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You have taken damage so you have to restart the parkour!";
					player.sendMessage(s);
				} catch (ParkourDoesNotExistException
						| PlayerIsMountedException | FinishNotSetException
						| PlayerAlreadyInParkourException
						| PlayerNotInParkourException e) {
					e.printStackTrace();
				}
			}
		} else if (event.getEntity() instanceof Horse) {
			final Horse horse = (Horse) event.getEntity();
			if (horse.getPassenger() instanceof Player && Parkour.isPlayerInParkour((Player) horse.getPassenger())) {
				final Player player = (Player) horse.getPassenger();
				try {
					Parkour.getParkour(player).restartParkour(player);
					cooldown.add(player);
					new BukkitRunnable() {
						@Override
					    public void run() {
					    	cooldown.remove(player);
					    	restartOnDismount(player, horse);
					    }
					}.runTaskLater(ImOnAHorse.plugin, delay);
					String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Your horse has taken damage so you have to restart the parkour!";
					player.sendMessage(s);
				} catch (ParkourDoesNotExistException
						| PlayerIsMountedException | FinishNotSetException
						| PlayerAlreadyInParkourException
						| PlayerNotInParkourException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void restartOnDismount(final Player player, final Horse horse) {
		if ((horse != null && player.isInsideVehicle() && !player.getVehicle().equals(horse)) ||
				(horse != null && !player.isInsideVehicle())) horse.remove();
		if (player.isInsideVehicle()) return;
		try {
			Parkour.getParkour(player).restartParkour(player);
			cooldown.add(player);
			new BukkitRunnable() {
				@Override
			    public void run() {
			    	cooldown.remove(player); 
			    	restartOnDismount(player, null);     
			    }
			}.runTaskLater(ImOnAHorse.plugin, delay);
			String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You have been dismounted so you have to restart the parkour!";
			player.sendMessage(s);
		} catch (ParkourDoesNotExistException | PlayerIsMountedException
				| FinishNotSetException | PlayerAlreadyInParkourException e) {
			e.printStackTrace();
		} catch (PlayerNotInParkourException e) {
			
		}
	}
	
}
