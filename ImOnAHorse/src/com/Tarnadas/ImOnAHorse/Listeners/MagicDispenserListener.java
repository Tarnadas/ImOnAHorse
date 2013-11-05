package com.Tarnadas.ImOnAHorse.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;

import com.Tarnadas.ImOnAHorse.Exceptions.DispenserAlreadyRegisteredException;
import com.Tarnadas.ImOnAHorse.Exceptions.ParkourDoesNotExistException;
import com.Tarnadas.ImOnAHorse.parkour.Parkour;

public class MagicDispenserListener implements Listener {
	
	@EventHandler(priority=EventPriority.NORMAL)
    public void onBlockDamageEvent(BlockDamageEvent event) {
		
		if (!(event.getBlock().getState() instanceof Dispenser)) return;
		if (!Parkour.isPlayerCreatingDispenser(event.getPlayer())) return;
		Parkour parkour;
		try {
			parkour = Parkour.getParkourForDispenser(event.getPlayer());
			try {
				if (parkour.isDispenserRegistered(event.getBlock())) throw new DispenserAlreadyRegisteredException(event.getPlayer());
			} catch (DispenserAlreadyRegisteredException e) {
				return;
			}
			parkour.createDispenser(event.getBlock(), event.getPlayer());
			Parkour.finishDispenser(event.getPlayer());
		} catch (ParkourDoesNotExistException e) {
			e.printStackTrace();
		}
		
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
    public void onBlockBreakEvent(BlockBreakEvent event) throws ParkourDoesNotExistException {
		
		if (!(event.getBlock().getState() instanceof Dispenser)) return;
		if (Parkour.isPlayerCreatingDispenser(event.getPlayer())) {
			Parkour parkour = Parkour.getParkourForDispenser(event.getPlayer());
			event.setCancelled(true);
			try {
				if (parkour.isDispenserRegistered(event.getBlock())) throw new DispenserAlreadyRegisteredException(event.getPlayer());
			} catch (DispenserAlreadyRegisteredException e) {
				return;
			}
			parkour.createDispenser(event.getBlock(), event.getPlayer());
			Parkour.finishDispenser(event.getPlayer());
		} else {
			Parkour parkour = Parkour.getDispenserParkour(event.getBlock());
			if (parkour != null) {
				parkour.removeDispenser(event.getBlock());
				String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "The dispenser has been removed from the parkour";
				event.getPlayer().sendMessage(s);
			}
		}
		
	}
	
}
