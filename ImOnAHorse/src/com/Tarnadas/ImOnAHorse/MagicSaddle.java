package com.Tarnadas.ImOnAHorse;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class MagicSaddle {

	private static ShapedRecipe saddleRecipe;
	
	public enum Saddle {
		SADDLE
	}
	
	public static void onInit(ImOnAHorse plugin) {
		
		ItemStack result = new ItemStack(Config.getResult(Saddle.SADDLE));
		saddleRecipe = new ShapedRecipe(result);
		saddleRecipe.shape(Config.getShape(Saddle.SADDLE));
		Material[] mats = Config.getIngredients(Saddle.SADDLE);
		char[] keys = Config.getIngredientKeys(Saddle.SADDLE);
		for (int i=0; i<mats.length; i++) {
			saddleRecipe.setIngredient(keys[i], mats[i]);
		}
		plugin.getServer().addRecipe(saddleRecipe);
		
	}
	
}
