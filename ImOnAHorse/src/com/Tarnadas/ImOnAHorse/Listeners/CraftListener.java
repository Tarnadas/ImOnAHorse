package com.Tarnadas.ImOnAHorse.Listeners;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.Tarnadas.ImOnAHorse.MagicArmor;

public class CraftListener implements Listener {

	@EventHandler(priority=EventPriority.NORMAL)
    public void onCraftEvent(CraftItemEvent event) {
		//TODO crafting recipe permission
		if (!MagicArmor.isRefillArmor(event.getRecipe())) return;
		ItemStack[] matrix = event.getInventory().getMatrix();
		ItemStack armor = matrix[0];
		for (int i=0; i < matrix.length; i++) {
			if (matrix[i] != null && MagicArmor.isHorseArmor(matrix[i])) armor = matrix[i];
		}
		ItemMeta meta = armor.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null || lore.size()<2 || !lore.get(1).substring(4, 5).equals(":")) {
			meta.setLore(lore);
			event.getInventory().getResult().setItemMeta(meta);
			return;
		}
		String s = lore.get(1);
		String fuel = s.substring(6);
		String[] split = fuel.split("/");
		fuel = split[0];
		int fuelInt = Integer.parseInt(fuel);
		fuelInt = fuelInt + MagicArmor.getFuel(event.getRecipe());
		int max = MagicArmor.getMaxFuel(event.getRecipe());
		if (fuelInt > max) fuelInt = max;
		fuel = Integer.toString(fuelInt);
		s = "Fuel: " + fuel + "/" + split[1];
		lore.set(1, s);
		meta.setLore(lore);
		event.getInventory().getResult().setItemMeta(meta);
		
	}
	
}
