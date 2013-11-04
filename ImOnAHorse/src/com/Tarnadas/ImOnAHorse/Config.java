package com.Tarnadas.ImOnAHorse;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

	private static FileConfiguration config;

	private static int ironFuel;
	private static int goldFuel;
	private static int diamondFuel;
	
	private static int ironMaxFuel;
	private static int goldMaxFuel;
	private static int diamondMaxFuel;

	private static Material ironRefill;
	private static Material goldRefill;
	private static Material diamondRefill;

	private static String[] saddleShape;
	private static String[] ironShape;
	private static String[] goldShape;
	private static String[] diamondShape;
	private static String[] ironShapeMagic;
	private static String[] goldShapeMagic;
	private static String[] diamondShapeMagic;

	private static Material[] saddleIngredients;
	private static Material[] ironIngredients;
	private static Material[] goldIngredients;
	private static Material[] diamondIngredients;
	private static Material[] ironIngredientsMagic;
	private static Material[] goldIngredientsMagic;
	private static Material[] diamondIngredientsMagic;

	private static char[] saddleIngredientKeys;
	private static char[] ironIngredientKeys;
	private static char[] goldIngredientKeys;
	private static char[] diamondIngredientKeys;
	private static char[] ironIngredientKeysMagic;
	private static char[] goldIngredientKeysMagic;
	private static char[] diamondIngredientKeysMagic;
	
	// called on loading config
	public Config(ImOnAHorse plugin) {
		
		Config.config = plugin.getConfig();
		
		ConfigurationSection conf = config.getConfigurationSection("fuel");
		ironFuel = conf.getInt("ironFuel");
		goldFuel = conf.getInt("goldFuel");
		diamondFuel = conf.getInt("diamondFuel");
		ironMaxFuel = conf.getInt("ironMaxFuel");
		goldMaxFuel = conf.getInt("goldMaxFuel");
		diamondMaxFuel = conf.getInt("diamondMaxFuel");
		conf = conf.getConfigurationSection("refill_item");
		ironRefill = Material.valueOf(conf.getString("iron").toUpperCase());
		goldRefill = Material.valueOf(conf.getString("gold").toUpperCase());
		diamondRefill = Material.valueOf(conf.getString("diamond").toUpperCase());
		
		conf = config.getConfigurationSection("recipes").getConfigurationSection("saddle");
		ConfigurationSection shape = conf.getConfigurationSection("shape");
		int max = shape.getKeys(false).size();
		saddleShape = new String[max];
		for (int i=0; i < max; i++) {
			saddleShape[i] = shape.getString(Integer.toString(i));
		}
		ConfigurationSection ingredients = conf.getConfigurationSection("ingredients");
		Object[] obj = ingredients.getKeys(false).toArray();
		saddleIngredientKeys = new char[obj.length];
		for (int i=0; i<obj.length; i++) {
			saddleIngredientKeys[i] = obj[i].toString().charAt(0);
		}
		max = ingredients.getKeys(false).size();
		saddleIngredients = new Material[max];
		for (int i=0; i < max; i++) {
			saddleIngredients[i] = Material.getMaterial(ingredients.getString("" + saddleIngredientKeys[i]).toUpperCase());
		}
		
		conf = config.getConfigurationSection("recipes").getConfigurationSection("iron_barding");
		shape = conf.getConfigurationSection("shape");
		max = shape.getKeys(false).size();
		ironShape = new String[max];
		for (int i=0; i < max; i++) {
			ironShape[i] = shape.getString(Integer.toString(i));
		}
		ingredients = conf.getConfigurationSection("ingredients");
		obj = ingredients.getKeys(false).toArray();
		ironIngredientKeys = new char[obj.length];
		for (int i=0; i<obj.length; i++) {
			ironIngredientKeys[i] = obj[i].toString().charAt(0);
		}
		max = ingredients.getKeys(false).size();
		ironIngredients = new Material[max];
		for (int i=0; i < max; i++) {
			ironIngredients[i] = Material.getMaterial(ingredients.getString("" + ironIngredientKeys[i]).toUpperCase());
		}
		
		conf = config.getConfigurationSection("recipes").getConfigurationSection("gold_barding");
		shape = conf.getConfigurationSection("shape");
		max = shape.getKeys(false).size();
		goldShape = new String[max];
		for (int i=0; i < max; i++) {
			goldShape[i] = shape.getString(Integer.toString(i));
		}
		ingredients = conf.getConfigurationSection("ingredients");
		obj = ingredients.getKeys(false).toArray();
		goldIngredientKeys = new char[obj.length];
		for (int i=0; i<obj.length; i++) {
			goldIngredientKeys[i] = obj[i].toString().charAt(0);
		}
		max = ingredients.getKeys(false).size();
		goldIngredients = new Material[max];
		for (int i=0; i < max; i++) {
			goldIngredients[i] = Material.getMaterial(ingredients.getString("" + goldIngredientKeys[i]).toUpperCase());
		}
		
		conf = config.getConfigurationSection("recipes").getConfigurationSection("diamond_barding");
		shape = conf.getConfigurationSection("shape");
		max = shape.getKeys(false).size();
		diamondShape = new String[max];
		for (int i=0; i < max; i++) {
			diamondShape[i] = shape.getString(Integer.toString(i));
		}
		ingredients = conf.getConfigurationSection("ingredients");
		obj = ingredients.getKeys(false).toArray();
		diamondIngredientKeys = new char[obj.length];
		for (int i=0; i<obj.length; i++) {
			diamondIngredientKeys[i] = obj[i].toString().charAt(0);
		}
		max = ingredients.getKeys(false).size();
		diamondIngredients = new Material[max];
		for (int i=0; i < max; i++) {
			diamondIngredients[i] = Material.getMaterial(ingredients.getString("" + diamondIngredientKeys[i]).toUpperCase());
		}
		
		conf = config.getConfigurationSection("recipes").getConfigurationSection("iron_barding_magic");
		shape = conf.getConfigurationSection("shape");
		max = shape.getKeys(false).size();
		ironShapeMagic = new String[max];
		for (int i=0; i < max; i++) {
			ironShapeMagic[i] = shape.getString(Integer.toString(i));
		}
		ingredients = conf.getConfigurationSection("ingredients");
		obj = ingredients.getKeys(false).toArray();
		ironIngredientKeysMagic = new char[obj.length];
		for (int i=0; i<obj.length; i++) {
			ironIngredientKeysMagic[i] = obj[i].toString().charAt(0);
		}
		max = ingredients.getKeys(false).size();
		ironIngredientsMagic = new Material[max];
		for (int i=0; i < max; i++) {
			ironIngredientsMagic[i] = Material.getMaterial(ingredients.getString("" + ironIngredientKeysMagic[i]).toUpperCase());
		}
		
		conf = config.getConfigurationSection("recipes").getConfigurationSection("gold_barding_magic");
		shape = conf.getConfigurationSection("shape");
		max = shape.getKeys(false).size();
		goldShapeMagic = new String[max];
		for (int i=0; i < max; i++) {
			goldShapeMagic[i] = shape.getString(Integer.toString(i));
		}
		ingredients = conf.getConfigurationSection("ingredients");
		obj = ingredients.getKeys(false).toArray();
		goldIngredientKeysMagic = new char[obj.length];
		for (int i=0; i<obj.length; i++) {
			goldIngredientKeysMagic[i] = obj[i].toString().charAt(0);
		}
		max = ingredients.getKeys(false).size();
		goldIngredientsMagic = new Material[max];
		for (int i=0; i < max; i++) {
			goldIngredientsMagic[i] = Material.getMaterial(ingredients.getString("" + goldIngredientKeysMagic[i]).toUpperCase());
		}
		
		conf = config.getConfigurationSection("recipes").getConfigurationSection("diamond_barding_magic");
		shape = conf.getConfigurationSection("shape");
		max = shape.getKeys(false).size();
		diamondShapeMagic = new String[max];
		for (int i=0; i < max; i++) {
			diamondShapeMagic[i] = shape.getString(Integer.toString(i));
		}
		ingredients = conf.getConfigurationSection("ingredients");
		obj = ingredients.getKeys(false).toArray();
		diamondIngredientKeysMagic = new char[obj.length];
		for (int i=0; i<obj.length; i++) {
			diamondIngredientKeysMagic[i] = obj[i].toString().charAt(0);
		}
		max = ingredients.getKeys(false).size();
		diamondIngredientsMagic = new Material[max];
		for (int i=0; i < max; i++) {
			diamondIngredientsMagic[i] = Material.getMaterial(ingredients.getString("" + diamondIngredientKeysMagic[i]).toUpperCase());
		}
		
	}
	
	public static int getIronFuel() {
		return ironFuel;
	}
	
	public static int getGoldFuel() {
		return goldFuel;
	}
	
	public static int getDiamondFuel() {
		return diamondFuel;
	}
	
	public static int getIronMaxFuel() {
		return ironMaxFuel;
	}
	
	public static int getGoldMaxFuel() {
		return goldMaxFuel;
	}
	
	public static int getDiamondMaxFuel() {
		return diamondMaxFuel;
	}
	
	public static Material getIronRefill() {
		return ironRefill;
	}
	
	public static Material getGoldRefill() {
		return goldRefill;
	}
	
	public static Material getDiamondRefill() {
		return diamondRefill;
	}
	
	public static Material getResult(MagicArmor.Armor armor) {
		switch (armor) {
		case IRON_BARDING:
			return Material.IRON_BARDING;
		case GOLD_BARDING:
			return Material.GOLD_BARDING;
		case DIAMOND_BARDING:
			return Material.DIAMOND_BARDING;
		case IRON_BARDING_MAGIC:
			return Material.IRON_BARDING;
		case GOLD_BARDING_MAGIC:
			return Material.GOLD_BARDING;
		case DIAMOND_BARDING_MAGIC:
			return Material.DIAMOND_BARDING;
		default:
			break;
		}
		return Material.IRON_BARDING;
	}
	
	public static Material getResult(MagicSaddle.Saddle saddle) {
		switch (saddle) {
		case SADDLE:
			return Material.SADDLE;
		}
		return Material.SADDLE;
	}
	
	public static String[] getShape(MagicArmor.Armor armor) {
		switch (armor) {
		case IRON_BARDING:
			return ironShape;
		case GOLD_BARDING:
			return goldShape;
		case DIAMOND_BARDING:
			return diamondShape;
		case IRON_BARDING_MAGIC:
			return ironShapeMagic;
		case GOLD_BARDING_MAGIC:
			return goldShapeMagic;
		case DIAMOND_BARDING_MAGIC:
			return diamondShapeMagic;
		default:
			break;
		}
		return null;
	}
	
	public static String[] getShape(MagicSaddle.Saddle saddle) {
		switch (saddle) {
		case SADDLE:
			return saddleShape;
		}
		return null;
	}
	
	public static Material[] getIngredients(MagicArmor.Armor armor) {
		switch (armor) {
		case IRON_BARDING:
			return ironIngredients;
		case GOLD_BARDING:
			return goldIngredients;
		case DIAMOND_BARDING:
			return diamondIngredients;
		case IRON_BARDING_MAGIC:
			return ironIngredientsMagic;
		case GOLD_BARDING_MAGIC:
			return goldIngredientsMagic;
		case DIAMOND_BARDING_MAGIC:
			return diamondIngredientsMagic;
		default:
			break;
		}
		return null;
	}
	
	public static Material[] getIngredients(MagicSaddle.Saddle saddle) {
		switch (saddle) {
		case SADDLE:
			return saddleIngredients;
		}
		return null;
	}
	
	public static char[] getIngredientKeys(MagicArmor.Armor armor) {
		switch (armor) {
		case IRON_BARDING:
			return ironIngredientKeys;
		case GOLD_BARDING:
			return goldIngredientKeys;
		case DIAMOND_BARDING:
			return diamondIngredientKeys;
		case IRON_BARDING_MAGIC:
			return ironIngredientKeysMagic;
		case GOLD_BARDING_MAGIC:
			return goldIngredientKeysMagic;
		case DIAMOND_BARDING_MAGIC:
			return diamondIngredientKeysMagic;
		default:
			break;
		}
		return null;
	}
	
	public static char[] getIngredientKeys(MagicSaddle.Saddle saddle) {
		switch (saddle) {
		case SADDLE:
			return saddleIngredientKeys;
		}
		return null;
	}
	
}
