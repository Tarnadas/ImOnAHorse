package com.Tarnadas.ImOnAHorse;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.Tarnadas.ImOnAHorse.Exceptions.NotEnoughFuelException;

public class MagicArmor extends Enchantment implements Runnable {
	
	private static final double maxFallSpeed = -0.5;
	private static final double adjustSpeed = 0.23;
	private static final int interval = 150;
	
	private static List<Horse> mountedList;

	private static ShapedRecipe ironRecipe;
	private static ShapedRecipe goldRecipe;
	private static ShapedRecipe diamondRecipe;
	private static ShapedRecipe ironRecipeMagic;
	private static ShapedRecipe goldRecipeMagic;
	private static ShapedRecipe diamondRecipeMagic;

	private static ShapelessRecipe ironRefill;
	private static ShapelessRecipe goldRefill;
	private static ShapelessRecipe diamondRefill;

	public MagicArmor(int id) {
		super(id);
	}
	
	public enum Armor {
		IRON_BARDING, GOLD_BARDING, DIAMOND_BARDING, IRON_BARDING_MAGIC, GOLD_BARDING_MAGIC, DIAMOND_BARDING_MAGIC, PARKOUR
	}
	
	public static void onInit(ImOnAHorse plugin, boolean isReload) {
		
		try {
		    Field f = Enchantment.class.getDeclaredField("acceptingNew");
		    f.setAccessible(true);
		    f.set(null, true);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		MagicArmor enchant = new MagicArmor(42);
		if (!isReload) Enchantment.registerEnchantment(enchant);

		ItemStack result = new ItemStack(Config.getResult(Armor.IRON_BARDING));
		ironRecipe = new ShapedRecipe(result);
		ironRecipe.shape(Config.getShape(Armor.IRON_BARDING));
		Material[] mats = Config.getIngredients(Armor.IRON_BARDING);
		char[] keys = Config.getIngredientKeys(Armor.IRON_BARDING);
		for (int i=0; i<mats.length; i++) {
			ironRecipe.setIngredient(keys[i], mats[i]);
		}
		plugin.getServer().addRecipe(ironRecipe);

		result = new ItemStack(Config.getResult(Armor.GOLD_BARDING));
		goldRecipe = new ShapedRecipe(result);
		goldRecipe.shape(Config.getShape(Armor.GOLD_BARDING));
		mats = Config.getIngredients(Armor.GOLD_BARDING);
		keys = Config.getIngredientKeys(Armor.GOLD_BARDING);
		for (int i=0; i<mats.length; i++) {
			goldRecipe.setIngredient(keys[i], mats[i]);
		}
		plugin.getServer().addRecipe(goldRecipe);

		result = new ItemStack(Config.getResult(Armor.DIAMOND_BARDING));
		diamondRecipe = new ShapedRecipe(result);
		diamondRecipe.shape(Config.getShape(Armor.DIAMOND_BARDING));
		mats = Config.getIngredients(Armor.DIAMOND_BARDING);
		keys = Config.getIngredientKeys(Armor.DIAMOND_BARDING);
		for (int i=0; i<mats.length; i++) {
			diamondRecipe.setIngredient(keys[i], mats[i]);
		}
		plugin.getServer().addRecipe(diamondRecipe);
		
		result = new ItemStack(Config.getResult(Armor.IRON_BARDING_MAGIC));
		ItemMeta meta = result.getItemMeta();
		List<String> lore = new LinkedList<String>();
		lore.add("Magical Armor");
		lore.add("Fuel: " + Config.getIronFuel() + "/" + Config.getIronMaxFuel());
		meta.setLore(lore);
		meta.addEnchant(enchant, 0, false);
		result.setItemMeta(meta);
		ironRecipeMagic = new ShapedRecipe(result);
		ironRecipeMagic.shape(Config.getShape(Armor.IRON_BARDING_MAGIC));
		mats = Config.getIngredients(Armor.IRON_BARDING_MAGIC);
		keys = Config.getIngredientKeys(Armor.IRON_BARDING_MAGIC);
		for (int i=0; i<mats.length; i++) {
			ironRecipeMagic.setIngredient(keys[i], mats[i]);
		}
		plugin.getServer().addRecipe(ironRecipeMagic);

		result = new ItemStack(Config.getResult(Armor.GOLD_BARDING_MAGIC));
		meta = result.getItemMeta();
		lore = new LinkedList<String>();
		lore.add("Magical Armor");
		lore.add("Fuel: " + Config.getGoldFuel() + "/" + Config.getGoldMaxFuel());
		meta.setLore(lore);
		meta.addEnchant(enchant, 0, false);
		result.setItemMeta(meta);
		goldRecipeMagic = new ShapedRecipe(result);
		goldRecipeMagic.shape(Config.getShape(Armor.GOLD_BARDING_MAGIC));
		mats = Config.getIngredients(Armor.GOLD_BARDING_MAGIC);
		keys = Config.getIngredientKeys(Armor.GOLD_BARDING_MAGIC);
		for (int i=0; i<mats.length; i++) {
			goldRecipeMagic.setIngredient(keys[i], mats[i]);
		}
		plugin.getServer().addRecipe(goldRecipeMagic);
		
		result = new ItemStack(Config.getResult(Armor.DIAMOND_BARDING_MAGIC));
		meta = result.getItemMeta();
		lore = new LinkedList<String>();
		lore.add("Magical Armor");
		lore.add("Fuel: " + Config.getDiamondFuel() + "/" + Config.getDiamondMaxFuel());
		meta.setLore(lore);
		meta.addEnchant(enchant, 0, false);
		result.setItemMeta(meta);
		diamondRecipeMagic = new ShapedRecipe(result);
		diamondRecipeMagic.shape(Config.getShape(Armor.DIAMOND_BARDING_MAGIC));
		mats = Config.getIngredients(Armor.DIAMOND_BARDING_MAGIC);
		keys = Config.getIngredientKeys(Armor.DIAMOND_BARDING_MAGIC);
		for (int i=0; i<mats.length; i++) {
			diamondRecipeMagic.setIngredient(keys[i], mats[i]);
		}
		plugin.getServer().addRecipe(diamondRecipeMagic);
		
		result = new ItemStack(Config.getResult(Armor.IRON_BARDING_MAGIC));
		meta = result.getItemMeta();
		lore = new LinkedList<String>();
		lore.add("Magical Armor");
		lore.add("Fuel +" + Config.getIronFuel());
		meta.setLore(lore);
		meta.addEnchant(enchant, 0, false);
		result.setItemMeta(meta);
		ironRefill = new ShapelessRecipe(result);
		ironRefill.addIngredient(Material.IRON_BARDING);
		ironRefill.addIngredient(Config.getIronRefill());
		plugin.getServer().addRecipe(ironRefill);
		
		result = new ItemStack(Config.getResult(Armor.GOLD_BARDING_MAGIC));
		meta = result.getItemMeta();
		lore = new LinkedList<String>();
		lore.add("Magical Armor");
		lore.add("Fuel +" + Config.getGoldFuel());
		meta.setLore(lore);
		meta.addEnchant(enchant, 0, false);
		result.setItemMeta(meta);
		goldRefill = new ShapelessRecipe(result);
		goldRefill.addIngredient(Material.GOLD_BARDING);
		goldRefill.addIngredient(Config.getGoldRefill());
		plugin.getServer().addRecipe(goldRefill);
		
		result = new ItemStack(Config.getResult(Armor.DIAMOND_BARDING_MAGIC));
		meta = result.getItemMeta();
		lore = new LinkedList<String>();
		lore.add("Magical Armor");
		lore.add("Fuel +" + Config.getDiamondFuel());
		meta.setLore(lore);
		meta.addEnchant(enchant, 0, false);
		result.setItemMeta(meta);
		diamondRefill = new ShapelessRecipe(result);
		diamondRefill.addIngredient(Material.DIAMOND_BARDING);
		diamondRefill.addIngredient(Config.getDiamondRefill());
		plugin.getServer().addRecipe(diamondRefill);
		
		mountedList = new LinkedList<Horse>();
		
		new Thread(enchant).start();
		
	}
	
	public static ItemStack createMagicArmor(Armor type) {
		ItemStack armor = null;
		MagicArmor enchant = new MagicArmor(42);
		switch(type) {
		case IRON_BARDING:
			armor = new ItemStack(Material.IRON_BARDING);
			ItemMeta meta = armor.getItemMeta();
			List<String> lore = new LinkedList<String>();
			lore.add("Magical Armor");
			lore.add("Fuel: " + Config.getIronFuel() + "/" + Config.getIronMaxFuel());
			meta.setLore(lore);
			meta.addEnchant(enchant, 0, false);
			armor.setItemMeta(meta);
			break;
		case GOLD_BARDING:
			armor = new ItemStack(Material.IRON_BARDING);
			meta = armor.getItemMeta();
			lore = new LinkedList<String>();
			lore.add("Magical Armor");
			lore.add("Fuel: " + Config.getGoldFuel() + "/" + Config.getGoldMaxFuel());
			meta.setLore(lore);
			meta.addEnchant(enchant, 0, false);
			armor.setItemMeta(meta);
			break;
		case DIAMOND_BARDING:
			armor = new ItemStack(Material.IRON_BARDING);
			meta = armor.getItemMeta();
			lore = new LinkedList<String>();
			lore.add("Magical Armor");
			lore.add("Fuel: " + Config.getDiamondFuel() + "/" + Config.getDiamondMaxFuel());
			meta.setLore(lore);
			meta.addEnchant(enchant, 0, false);
			armor.setItemMeta(meta);
			break;
		case PARKOUR:
			armor = new ItemStack(Material.IRON_BARDING);
			meta = armor.getItemMeta();
			lore = new LinkedList<String>();
			lore.add("Magical Armor");
			meta.setLore(lore);
			meta.addEnchant(enchant, 0, false);
			armor.setItemMeta(meta);
		default:
			break;
		}
		return armor;
	}
	
	public static boolean isMagicArmor(Recipe recipe) {
		return recipe.equals(ironRecipeMagic) || recipe.equals(goldRecipeMagic) || recipe.equals(diamondRecipeMagic);
	}
	
	public static boolean isMagicArmor(ItemStack item) {
		List<String> lore = item.getItemMeta().getLore();
		return (lore != null && lore.contains("Magical Armor"));
	}
	
	public static boolean isRefillArmor(Recipe recipe) {
		if (!(recipe instanceof ShapelessRecipe)) return false;
		ShapelessRecipe slRecipe = (ShapelessRecipe) recipe;
		List<ItemStack> ingredientList = slRecipe.getIngredientList();
		List<ItemStack> ironIngredientList = ironRefill.getIngredientList();
		List<ItemStack> goldIngredientList = goldRefill.getIngredientList();
		List<ItemStack> diamondIngredientList = diamondRefill.getIngredientList();
		boolean result = false;
		for (ItemStack item : ingredientList) {
			for (ItemStack compare : ironIngredientList) {
				if (item.getType().equals(compare.getType())) {
					result = true;
					break;
				}
			}
			if (result) break;
			for (ItemStack compare : goldIngredientList) {
				if (item.getType().equals(compare.getType())) {
					result = true;
					break;
				}
			}
			if (result) break;
			for (ItemStack compare : diamondIngredientList) {
				if (item.getType().equals(compare.getType())) {
					result = true;
					break;
				}
			}
			if (result) break;
		}
		return result;
	}
	
	public static boolean isHorseArmor(ItemStack item) {
		return item.getData().getItemType().equals(Material.IRON_BARDING) ||
				item.getData().getItemType().equals(Material.GOLD_BARDING) ||
				item.getData().getItemType().equals(Material.DIAMOND_BARDING);
	}
	
	public static boolean hasMagicArmor(Horse horse) {
		ItemStack armor = horse.getInventory().getArmor();
		List<String> lore;
		if (armor != null) {
			lore = armor.getItemMeta().getLore();
			if (lore != null && lore.contains("Magical Armor")) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isRidingMagicHorse(Player player) {
		if (!player.isInsideVehicle()) return false;
		Entity vehicle = player.getVehicle();
		if (!(vehicle instanceof Horse)) return false;
		return hasMagicArmor((Horse) vehicle);
	}
	
	public static boolean isRegistered(Horse horse) {
		return mountedList.contains(horse);
	}
	
	public static void mount(Horse horse) {
		mountedList.add(horse);
	}
	
	public static void dismount(Horse horse) {
		mountedList.remove(horse);
	}
	
	public static void setFuel(ItemStack armor, int amount) {

		ItemMeta meta = armor.getItemMeta();
		List<String> lore = meta.getLore();
		String s = lore.get(1);
		String fuel = s.substring(6);
		String[] split = fuel.split("/");
		fuel = Integer.toString(amount);
		s = "Fuel: " + fuel + "/" + split[1];
		lore.set(1, s);
		meta.setLore(lore);
		armor.setItemMeta(meta);
		
	}
	
	public static void decreaseFuel(ItemStack armor, Player player) throws NotEnoughFuelException {

		ItemMeta meta = armor.getItemMeta();
		List<String> lore = meta.getLore();
		String s = lore.get(1);
		String fuel = s.substring(6);
		String[] split = fuel.split("/");
		fuel = split[0];
		int fuelInt = Integer.parseInt(fuel);
		if (fuelInt == 0) throw new NotEnoughFuelException();
		fuel = Integer.toString(--fuelInt);
		if (fuelInt == 0) {
			String out = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "Your fuel is empty!";
			player.sendMessage(out);
		}
		s = "Fuel: " + fuel + "/" + split[1];
		lore.set(1, s);
		meta.setLore(lore);
		armor.setItemMeta(meta);
		
	}
	
	public static int getFuel(Recipe recipe) {
		ShapelessRecipe slRecipe = (ShapelessRecipe) recipe;
		List<ItemStack> ingredientList = slRecipe.getIngredientList();
//		List<ItemStack> ironIngredientList = ironRefill.getIngredientList();
//		List<ItemStack> goldIngredientList = goldRefill.getIngredientList();
//		List<ItemStack> diamondIngredientList = diamondRefill.getIngredientList();
//		for (ItemStack item : ingredientList) {
//			for (ItemStack compare : ironIngredientList) {
//				if (item.getType().equals(compare.getType())) {
//					return ironFuel;
//				}
//			}
//			for (ItemStack compare : goldIngredientList) {
//				if (item.getType().equals(compare.getType())) {
//					return goldFuel;
//				}
//			}
//			for (ItemStack compare : diamondIngredientList) {
//				if (item.getType().equals(compare.getType())) {
//					return diamondFuel;
//				}
//			}
//		}
		for (ItemStack item : ingredientList) {
			if (item.getType().equals(Material.IRON_BARDING)) return Config.getIronFuel();
			if (item.getType().equals(Material.GOLD_BARDING)) return Config.getGoldFuel();
			if (item.getType().equals(Material.DIAMOND_BARDING)) return Config.getDiamondFuel();
		}
		return 0;
	}
	
	public static int getMaxFuel(Recipe recipe) {
		ShapelessRecipe slRecipe = (ShapelessRecipe) recipe;
		List<ItemStack> ingredientList = slRecipe.getIngredientList();
//		List<ItemStack> ironIngredientList = ironRefill.getIngredientList();
//		List<ItemStack> goldIngredientList = goldRefill.getIngredientList();
//		List<ItemStack> diamondIngredientList = diamondRefill.getIngredientList();
//		for (ItemStack item : ingredientList) {
//			for (ItemStack compare : ironIngredientList) {
//				if (item.getType().equals(compare.getType())) {
//					return ironMaxFuel;
//				}
//			}
//			for (ItemStack compare : goldIngredientList) {
//				if (item.getType().equals(compare.getType())) {
//					return goldMaxFuel;
//				}
//			}
//			for (ItemStack compare : diamondIngredientList) {
//				if (item.getType().equals(compare.getType())) {
//					return diamondMaxFuel;
//				}
//			}
//		}
	for (ItemStack item : ingredientList) {
		if (item.getType().equals(Material.IRON_BARDING)) return Config.getIronMaxFuel();
		if (item.getType().equals(Material.GOLD_BARDING)) return Config.getGoldMaxFuel();
		if (item.getType().equals(Material.DIAMOND_BARDING)) return Config.getDiamondMaxFuel();
	}
		return 0;
	}
	
	public static String toString(ItemStack item) throws NullPointerException {
		return item.getType().toString() + ":" + item.getItemMeta().getLore().get(1).substring(6).split("/")[0];
	}
	
	@Override
	public void run() {
		
		while (true) {
			
			for (Horse horse : mountedList) {
				Vector vec = horse.getVelocity();
				double y = horse.getVelocity().getY();
				if (y < maxFallSpeed) {
					vec.setY(y + adjustSpeed);
					horse.setVelocity(vec);
				}
			}
			
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				
			}
			
		}
		
	}

	@Override
	public boolean canEnchantItem(ItemStack arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean conflictsWith(Enchantment arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EnchantmentTarget getItemTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStartLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

}
