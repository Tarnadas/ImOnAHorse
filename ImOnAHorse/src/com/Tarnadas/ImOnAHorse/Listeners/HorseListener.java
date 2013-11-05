package com.Tarnadas.ImOnAHorse.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.util.Vector;

import com.Tarnadas.ImOnAHorse.MagicArmor;
import com.Tarnadas.ImOnAHorse.Exceptions.NotEnoughFuelException;
import com.Tarnadas.ImOnAHorse.parkour.Parkour;

public class HorseListener implements Listener {
	
	private static final double yVelocity = 0.55;
	private static final double xVelocity = 0.2;
	private static final double zVelocity = 0.2;
	
	@EventHandler(priority=EventPriority.HIGH)
    public void onVehicleExitEvent(VehicleExitEvent event) {

		Entity entity = event.getVehicle();
		if (entity instanceof Horse && MagicArmor.hasMagicArmor((Horse) entity)) {
			
			Player player = (Player) event.getExited();
			Material material = ((Horse) entity).getInventory().getArmor().getData().getItemType();
			boolean hasArmor = false;
			if (material.equals(Material.IRON_BARDING) && player.hasPermission("horse.use.iron")) {
				hasArmor = true;
			} else {
				if (material.equals(Material.GOLD_BARDING) && player.hasPermission("horse.use.gold")) {
					hasArmor = true;
				} else {

					if (material.equals(Material.DIAMOND_BARDING) && player.hasPermission("horse.use.diamond")) {
						hasArmor = true;
					}
				}
			}
			if (hasArmor) MagicArmor.dismount((Horse) entity);
		}
		
	}

	@EventHandler(priority=EventPriority.NORMAL)
    public void onJumpEvent(HorseJumpEvent event) {

		Horse horse = event.getEntity();
		if (!MagicArmor.hasMagicArmor(horse)) {
			if (MagicArmor.isRegistered(horse)) MagicArmor.dismount(horse);
			return;
		}
		if (!MagicArmor.isRegistered(horse)) MagicArmor.mount(horse);
		
		Player player = (Player) horse.getPassenger();
		Material material = horse.getInventory().getArmor().getType();
		boolean permission = false;
		if (material.equals(Material.IRON_BARDING) && player.hasPermission("horse.use.iron")) {
			permission = true;
		} else {
			if (material.equals(Material.GOLD_BARDING) && player.hasPermission("horse.use.gold")) {
				permission = true;
			} else {
				if (material.equals(Material.DIAMOND_BARDING) && player.hasPermission("horse.use.diamond")) {
					permission = true;
				}
			}
		}
		if (!permission) return;
		
		if (!player.hasPermission("horse.nofuel") && !Parkour.isPlayerInParkour(player)) {
			try {
				MagicArmor.decreaseFuel(horse.getInventory().getArmor(), player);
			} catch (NotEnoughFuelException e) {
				return;
			}
		}
		
		Vector vec = horse.getVelocity();
		double yaw = Math.toRadians(player.getLocation().getYaw() + 90);
		vec.setY(vec.getY() + yVelocity);
		vec.setX(vec.getX() + Math.cos(yaw) * xVelocity);
		vec.setZ(vec.getZ() + Math.sin(yaw) * zVelocity);
		horse.setVelocity(vec);
		event.setCancelled(true);
		
    }
	
	@EventHandler(priority=EventPriority.NORMAL)
    public void onDamageEvent(EntityDamageEvent event) {
		
		DamageCause cause = event.getCause();
		if (cause.equals(DamageCause.FALL)) {
			Entity entity = event.getEntity();
			if (entity instanceof Horse && MagicArmor.hasMagicArmor((Horse) entity)) {
				event.setCancelled(true);
			} else if (entity instanceof Player && MagicArmor.isRidingMagicHorse((Player) entity)) {
				event.setCancelled(true);
			}
		}
		
    }
	
}
