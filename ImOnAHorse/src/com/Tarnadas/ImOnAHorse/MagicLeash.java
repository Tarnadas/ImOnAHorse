package com.Tarnadas.ImOnAHorse;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.Tarnadas.ImOnAHorse.Exceptions.HorseAlreadyRegisteredException;
import com.Tarnadas.ImOnAHorse.Exceptions.InventoryFullException;
import com.Tarnadas.ImOnAHorse.Exceptions.LeashInventoryFullException;
import com.Tarnadas.ImOnAHorse.Exceptions.NoCustomNameSetException;
import com.Tarnadas.ImOnAHorse.Exceptions.NotYourHorseException;

public class MagicLeash {

	private static final int inventorySize = 9;
	
	private static Map<String, MagicLeash> leashs;
	private static Map<String, MagicEgg> eggs;
	
	private File file;
	private FileConfiguration config;
	private String owner;
	private ItemStack[] items;
	private Inventory inventory;
	
	public MagicLeash(File file) {
		this.file = file;
		this.config = YamlConfiguration.loadConfiguration(file);
		String name = file.getName().substring(0, file.getName().length()-4);
		this.owner = name;
		this.items = new ItemStack[inventorySize];
		this.inventory = Bukkit.getServer().createInventory(null, inventorySize, "Magical Leash");
		
		Set<String> keys = config.getKeys(false);
		int i = 0;
		for (String s : keys) {
			ConfigurationSection conf = config.getConfigurationSection(s);
			boolean released = conf.getBoolean("released");
			int age = conf.getInt("age");
			String color = conf.getString("color");
			String style = conf.getString("style");
			String variant = conf.getString("variant");
			ItemStack saddle = null;
			if (conf.getString("saddle") != null && !conf.getString("saddle").equals("")) {
				saddle = new ItemStack(Material.valueOf(conf.getString("saddle")));
			}
			ItemStack armor = null;
			if (conf.getString("armor") != null && !conf.getString("armor").equals("")) {
				String[] armorString = conf.getString("armor").split(":");
				if (armorString.length == 1) {
					armor = new ItemStack(Material.valueOf(armorString[0]));
				} else if (armorString.length == 2) {
					armor = MagicArmor.createMagicArmor(MagicArmor.Armor.valueOf(armorString[0]));
					MagicArmor.setFuel(armor, Integer.parseInt(armorString[1]));
				}
			}
			eggs.put(s, new MagicEgg(released, s, name, age, color, style, variant, saddle, armor));
			
			if (!released) {
				items[i] = new ItemStack(Material.MONSTER_EGG);
				List<String> lore = new LinkedList<String>();
				lore.add(s);
				ItemMeta meta = items[i].getItemMeta();
				meta.setLore(lore);
				items[i].setItemMeta(meta);
				this.inventory.addItem(items[i]);
				i++;
			}
		}
		this.inventory.setContents(items);
		leashs.put(this.owner, this);
	}
	
	public MagicLeash(Player player) {
		String playerName = player.getName().toLowerCase();
		this.file = new File(ImOnAHorse.plugin.getDataFolder() + "\\Players", playerName + ".yml");
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		this.config = YamlConfiguration.loadConfiguration(file);
		this.owner = playerName;
		this.items = new ItemStack[inventorySize];
		this.inventory = Bukkit.getServer().createInventory(null, inventorySize, "Magical Leash");
		leashs.put(this.owner, this);
	}
	
	public static void onLoad() {
		leashs = new HashMap<String, MagicLeash>();
		eggs = new HashMap<String, MagicEgg>();
		File folder = new File(ImOnAHorse.plugin.getDataFolder() + "\\Players");
		File[] fileList = folder.listFiles();
		for (int i=0; i<fileList.length; i++) {
			new MagicLeash(fileList[i]);
		}
	}
	
	public static void getPlayerLeash(Player player) throws InventoryFullException {
		ItemStack[] items = player.getInventory().getContents();
		boolean hasLeash = false;
		int id = 0;
		for (int i=0; i<items.length; i++) {
			if (items[i] != null && items[i].getType().equals(Material.LEASH) && items[i].getItemMeta().getEnchants().containsKey(Enchantment.DURABILITY)) {
				hasLeash = true;
				id = i;
				break;
			}
		}
		if (hasLeash) {
			player.getInventory().remove(items[id]);
		} else {
			if (player.getInventory().firstEmpty() < 0) throw new InventoryFullException();
		}
		ItemStack leash = new ItemStack(Material.LEASH);
		leash.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		List<String> lore = new LinkedList<String>();
		lore.add("Magical Leash");
		lore.add("Rightclick to store a horse");
		lore.add("Leftclick to release");
		ItemMeta meta = leash.getItemMeta();
		meta.setLore(lore);
		leash.setItemMeta(meta);
		player.getInventory().addItem(leash);
	}
	
	public static void addHorse(final Horse horse, Player player) throws NoCustomNameSetException, HorseAlreadyRegisteredException,
	NotYourHorseException, LeashInventoryFullException {
		if (horse.getOwner() == null || !horse.getOwner().equals(player)) throw new NotYourHorseException();
		if (horse.getCustomName() == null) throw new NoCustomNameSetException();
		String name = horse.getCustomName();
		MagicLeash leash = MagicLeash.getLeash(player);
		if (leash == null) leash = new MagicLeash(player);
		if (eggs.get(name) != null) throw new HorseAlreadyRegisteredException();
		if (leash.inventory.firstEmpty() < 0) throw new LeashInventoryFullException();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				horse.remove();
			}
		}.runTask(ImOnAHorse.plugin);
		MagicEgg egg = new MagicEgg(name, player, horse);
		eggs.put(name, egg);
		ConfigurationSection conf = leash.config.createSection(name);
		conf.set("released", false);
		conf.set("age", egg.getAge());
		conf.set("color", egg.getColor().toString());
		conf.set("style", egg.getStyle().toString());
		conf.set("variant", egg.getVariant().toString());
		if (horse.getInventory().getSaddle() != null) conf.set("saddle", egg.getSaddle().getType().toString());
		if (MagicArmor.hasMagicArmor(horse)) {
			conf.set("armor", MagicArmor.toString(egg.getArmor()));
		} else if (horse.getInventory().getArmor() != null) {
			conf.set("armor", egg.getArmor().getType().toString());
		}
		try {
			leash.config.save(leash.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ItemStack item = new ItemStack(Material.MONSTER_EGG);
		List<String> lore = new LinkedList<String>();
		lore.add(name);
		ItemMeta meta = item.getItemMeta();
		meta.setLore(lore);
		item.setItemMeta(meta);
		leash.inventory.addItem(item);
		
		String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You added a horse named " + egg.getName() + " to your magic leash";
		player.sendMessage(s);
		
	}
	
	public static void spawnHorse(final Player player, final ItemStack item, final Location loc) {
		new BukkitRunnable() {
			@Override
			public void run() {
				loc.setY(loc.getY() + 1);
				Horse horse = (Horse) loc.getWorld().spawn(loc, Horse.class);
				MagicEgg egg = eggs.get(item.getItemMeta().getLore().get(0));
				horse.setCustomName(egg.getName());
				horse.setOwner(Bukkit.getServer().getPlayer(egg.getOwner()));
				horse.setAge(egg.getAge());
				horse.setColor(egg.getColor());
				horse.setStyle(egg.getStyle());
				horse.setVariant(egg.getVariant());
				horse.getInventory().setSaddle(egg.getSaddle());
				horse.getInventory().setArmor(egg.getArmor());
				player.getInventory().remove(player.getItemInHand());
				String name = item.getItemMeta().getLore().get(0);
				MagicLeash leash = MagicLeash.getLeash(player);
				leash.config.set(name, null);
				try {
					leash.config.save(leash.file);
				} catch (IOException e) {
					e.printStackTrace();
				}
				eggs.put(name, null);
			}
		}.runTask(ImOnAHorse.plugin);
	}
	
	public static void openMenu(Player player) {
		MagicLeash leash = MagicLeash.getLeash(player);
		if (leash == null) leash = new MagicLeash(player);
		player.openInventory(leash.inventory);
	}
	
	public void releaseEgg(ItemStack item) {
		if (!item.getType().equals(Material.MONSTER_EGG) || !item.getItemMeta().hasLore()) return; 
		String name = item.getItemMeta().getLore().get(0);
		config.getConfigurationSection(name).set("released", true);
		eggs.get(name).setReleased(true);
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static MagicLeash getLeash(Player player) {
		try {
			return leashs.get(player.getName().toLowerCase());
		} catch (NullPointerException e) {
			return new MagicLeash(player);
		}
	}
	
	public static boolean isMagicLeash(ItemStack item) {
		try {
			return item.getItemMeta().hasEnchant(Enchantment.DURABILITY);
		} catch (NullPointerException e) {
			return false;
		}
	}
	
}
