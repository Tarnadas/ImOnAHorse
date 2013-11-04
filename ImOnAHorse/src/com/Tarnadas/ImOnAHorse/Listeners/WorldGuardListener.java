package com.Tarnadas.ImOnAHorse.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.Tarnadas.ImOnAHorse.ImOnAHorse;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class WorldGuardListener implements Listener {

	@EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) ||
				!event.getPlayer().getItemInHand().getData().getItemType().equals(Material.MONSTER_EGG)) return;
		Location loc = event.getClickedBlock().getLocation();
		RegionManager manager = ImOnAHorse.plugin.getWorldGuard().getRegionManager(loc.getWorld());
		ApplicableRegionSet regions = manager.getApplicableRegions(loc);
		if (!regions.allows(DefaultFlag.MOB_SPAWNING)) {
			String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You cannot spawn mobs here!";
			event.getPlayer().sendMessage(s);
			event.setCancelled(true);
		}
	}

}
