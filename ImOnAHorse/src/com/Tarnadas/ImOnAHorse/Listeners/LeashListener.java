package com.Tarnadas.ImOnAHorse.Listeners;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Horse;
//import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
//import org.bukkit.scheduler.BukkitRunnable;

//import com.Tarnadas.ImOnAHorse.ImOnAHorse;
import com.Tarnadas.ImOnAHorse.MagicLeash;
import com.Tarnadas.ImOnAHorse.Exceptions.HorseAlreadyRegisteredException;
import com.Tarnadas.ImOnAHorse.Exceptions.InventoryFullException;
//import com.Tarnadas.ImOnAHorse.Exceptions.InventoryFullException;
import com.Tarnadas.ImOnAHorse.Exceptions.LeashInventoryFullException;
import com.Tarnadas.ImOnAHorse.Exceptions.NoCustomNameSetException;
import com.Tarnadas.ImOnAHorse.Exceptions.NotYourHorseException;

public class LeashListener implements Listener {


	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerLeashEntityEvent(PlayerLeashEntityEvent event) {
		if (!(event.getLeashHolder() instanceof Player)) return;
		final Player player = event.getPlayer();
		if (!MagicLeash.isMagicLeash((player.getInventory().getItemInHand()))) return;
		if (!player.hasPermission("horse.leash")) {
			String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You don't have permission to use this!";
			player.sendMessage(s);
			return;
		}
		event.setCancelled(true);
		if (!(event.getEntity() instanceof Horse)) {
			String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "This is not a horse!";
			event.getPlayer().sendMessage(s);
			return;
		}
		try {
			MagicLeash.getPlayerLeash(player);
			MagicLeash.addHorse((Horse) event.getEntity(), event.getPlayer());
		} catch (NoCustomNameSetException e) {
			String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "No custom name has been set!";
			event.getPlayer().sendMessage(s);
		} catch (HorseAlreadyRegisteredException e) {
			String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "A horse with this name is already registered!";
			event.getPlayer().sendMessage(s);
		} catch (NotYourHorseException e) {
			String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "This is not your horse!";
			event.getPlayer().sendMessage(s);
		} catch (LeashInventoryFullException e) {
			String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Your leash inventory is full!";
			event.getPlayer().sendMessage(s);
		} catch (InventoryFullException e) {
			e.printStackTrace();
		} finally {
			player.updateInventory();
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (!event.getAction().equals(Action.LEFT_CLICK_AIR) && !event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;
		if (!MagicLeash.isMagicLeash(event.getPlayer().getInventory().getItemInHand())) return;
		MagicLeash.openMenu(event.getPlayer());
	}

	@EventHandler(priority=EventPriority.HIGHEST)
    public void onHorseSpawnEvent(PlayerInteractEvent event) {
		if (event.isCancelled()) return;
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
		if (!event.getPlayer().getItemInHand().getData().getItemType().equals(Material.MONSTER_EGG)) return;
		if (!event.getPlayer().getItemInHand().getItemMeta().hasLore()) return;
		MagicLeash.spawnHorse(event.getPlayer(), event.getPlayer().getItemInHand(), event.getClickedBlock().getLocation());
	}

	@EventHandler(priority=EventPriority.HIGH)
    public void onInventoryClickEvent(InventoryClickEvent event) {
		if (!event.getInventory().getName().equals("Magical Leash")) return;
		if (event.getClick().equals(ClickType.SHIFT_LEFT) && event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
			event.setCancelled(true);
			return;
		}
		if (event.getSlotType().equals(InventoryType.SlotType.OUTSIDE)) return;
		if (event.getRawSlot() < event.getView().getTopInventory().getSize() && !event.getCursor().getType().equals(Material.AIR)) {
			event.setCancelled(true);
			return;
		}
		MagicLeash leash = MagicLeash.getLeash((Player) event.getWhoClicked());
		leash.releaseEgg(event.getCurrentItem());
	}

	@EventHandler(priority=EventPriority.HIGH)
    public void onInventoryDragEvent(InventoryDragEvent event) {
		if (!event.getInventory().getName().equals("Magical Leash")) return;
		Set<Integer> set = event.getRawSlots();
		for (int i : set) {
			if (i < event.getView().getTopInventory().getSize()) {
				event.setCancelled(true);
				return;
			}
		}
	}
	
}
