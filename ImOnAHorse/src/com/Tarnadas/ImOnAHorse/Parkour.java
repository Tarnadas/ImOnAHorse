package com.Tarnadas.ImOnAHorse;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.Tarnadas.ImOnAHorse.Exceptions.CheckpointAlreadyExistsException;
import com.Tarnadas.ImOnAHorse.Exceptions.CheckpointDoesNotExistException;
import com.Tarnadas.ImOnAHorse.Exceptions.FinishNotSetException;
import com.Tarnadas.ImOnAHorse.Exceptions.NoItemInHandException;
import com.Tarnadas.ImOnAHorse.Exceptions.ParkourDoesNotExistException;
import com.Tarnadas.ImOnAHorse.Exceptions.PlayerAlreadyInParkourException;
import com.Tarnadas.ImOnAHorse.Exceptions.PlayerIsAlreadyAddingDispenserException;
import com.Tarnadas.ImOnAHorse.Exceptions.PlayerIsMountedException;
import com.Tarnadas.ImOnAHorse.Exceptions.PlayerIsNotAddingDispenserException;
import com.Tarnadas.ImOnAHorse.Exceptions.PlayerNotInParkourException;
import com.Tarnadas.ImOnAHorse.Listeners.ParkourListener;

public class Parkour extends BukkitRunnable {

	private static final double finishThreshold = 2;
	private static final double checkpointThreshold = 3;
	
	private static Map<String, Parkour> parkours;
	private static Collection<Parkour> collection;
	private static List<String> playersInParkour;
	private static Map<String, String> playersDispenser;
	
	private static boolean suppressExceptions;
	
	private String name;
	private Location start;
	private Location finish;
	private List<ItemStack> reward;
	private int rewardMoney;
	private File file;
	private FileConfiguration config;
	private List<Checkpoint> checkpoints;
	private Map<String, Checkpoint> playerCheckpoint;
	private Map<String, Integer> editCheckpoint;
	
	public Parkour() {
	}
	
	// called on loading data
	@SuppressWarnings("unchecked")
	public Parkour(String name, File file, FileConfiguration config) {
		this.name = name;
		this.start = Parkour.toLocation(config.getString("start"));
		if (!config.getString("finish").equals("")) this.finish = Parkour.toLocation(config.getString("finish"));
		
		this.checkpoints = new LinkedList<Checkpoint>();
		Checkpoint startCP = new Checkpoint(this, Parkour.toLocation(config.getString("start")), -1);
		this.checkpoints.add(startCP);
		ConfigurationSection playerList = config.getConfigurationSection("players");
		List<String> dispenserList = config.getStringList("dispenser");
		if (dispenserList != null) {
			for (String dis : dispenserList) {
				MagicDispenser dispenser = MagicDispenser.parseString(dis);
				startCP.addDispenser(dispenser);
			}
		} else {
			config.createSection("dispenser");
		}
		this.playerCheckpoint = new HashMap<String, Checkpoint>();
		if (playerList != null) {
			Set<String> pKeys = playerList.getKeys(false);
			if (pKeys != null && pKeys.size() > 0)
			for (MagicDispenser dispenser : startCP.getDispensers()) {
				dispenser.setEnabled(true);
			}
			for (String playerName : pKeys) {
				startCP.addPlayer(playerName.toLowerCase());
				playerCheckpoint.put(playerName.toLowerCase(), startCP);
			}
		} else {
			config.createSection("players");
		}
		ConfigurationSection checkpointList = config.getConfigurationSection("checkpoints");
		if (checkpointList != null) {
			Set<String> keys = checkpointList.getKeys(false);
			if (keys != null)
			for (String s : keys) {
				ConfigurationSection cpConfig = checkpointList.getConfigurationSection(s);
				Checkpoint checkpoint = new Checkpoint(this, Parkour.toLocation(cpConfig.getString("location")), Integer.parseInt(s));
				this.checkpoints.add(checkpoint);
				List<String> dispenserList0 = cpConfig.getStringList("dispenser");
				if (dispenserList != null) {
					for (String dis : dispenserList0) {
						MagicDispenser dispenser = MagicDispenser.parseString(dis);
						checkpoint.addDispenser(dispenser);
					}
				} else {
					checkpointList.createSection("dispenser");
				}

				List<String> players = cpConfig.getStringList("players");
				if (players != null && players.size() > 0) {
					for (MagicDispenser dispenser : checkpoint.getDispensers()) {
						dispenser.setEnabled(true);
					}
					for (String playerName : players) {
						checkpoint.addPlayer(playerName.toLowerCase());
						playerCheckpoint.put(playerName.toLowerCase(), checkpoint);
						if (!playersInParkour.contains(playerName)) playersInParkour.add(playerName);
					}
				}
			}
		} else {
			config.createSection("checkpoints");
		}
		this.reward = (List<ItemStack>) config.getList("reward");
		this.rewardMoney = config.getInt("rewardmoney");
		this.editCheckpoint = new HashMap<String, Integer>();
		this.file = file;
		this.config = config;
		parkours.put(name, this);
		collection = parkours.values();
	}
	
	// called on creation
	public Parkour(ImOnAHorse plugin, String name, Player player) {
		this.name = name;
		this.start = player.getLocation();
		this.file = new File(plugin.getDataFolder() + "\\Parkours", name + ".yml");
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		this.config = YamlConfiguration.loadConfiguration(file);
		config.set("start", Parkour.toString(player.getLocation()));
		config.createSection("players");
		config.createSection("reward");
		config.createSection("rewardmoney");
		this.rewardMoney = 0;
		try {
	        this.config.save(this.file);
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }
		this.checkpoints = new LinkedList<Checkpoint>();
		this.checkpoints.add(new Checkpoint(this, player.getLocation(), -1));
		this.playerCheckpoint = new HashMap<String, Checkpoint>();
		this.editCheckpoint = new HashMap<String, Integer>();
		parkours.put(name, this);
		collection = parkours.values();
	}
	
	public static void loadParkourData() {
		File folder = new File(ImOnAHorse.plugin.getDataFolder() + "\\Parkours");
		File[] fileList = folder.listFiles();
		parkours = new HashMap<String, Parkour>();
		playersInParkour = new LinkedList<String>();
		playersDispenser = new HashMap<String,String>();
		for (int i=0; i<fileList.length; i++) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(fileList[i]);
			String s = fileList[i].getName().toLowerCase();
			s = s.substring(0, s.length() - 4);
			new Parkour(s, fileList[i], config);
		}
		collection = parkours.values();
		suppressExceptions = false;
	}
	
	public static boolean reloadParkourData(Player player) {
		if (!playersInParkour.isEmpty()) return false;
		Parkour.loadParkourData();
		return true;
	}
	
	public static void startParkour(final Player player, String parkourName) throws ParkourDoesNotExistException,
	PlayerIsMountedException, FinishNotSetException, PlayerAlreadyInParkourException {
		final Parkour parkour = Parkour.getParkourByName(parkourName);
		if (parkour.finish == null) throw new FinishNotSetException();
		if (Parkour.isPlayerInParkour(player)) throw new PlayerAlreadyInParkourException();
		if (player.isInsideVehicle()) throw new PlayerIsMountedException();
		String playerName = player.getName().toLowerCase();
		parkour.checkpoints.get(0).addPlayer(playerName);
		parkour.playerCheckpoint.put(playerName, parkour.checkpoints.get(0));
		playersInParkour.add(playerName);
		parkour.config.getConfigurationSection("players").set(playerName, Parkour.toString(player.getLocation()));
		try {
	        parkour.config.save(parkour.file);
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }
		player.teleport(parkour.start);
		for (MagicDispenser dispenser : parkour.checkpoints.get(0).getDispensers()) {
			dispenser.setEnabled(true);
		}
		final Horse horse = (Horse) parkour.start.getWorld().spawnEntity(parkour.start, EntityType.HORSE);
		new BukkitRunnable() {
			@Override
		    public void run() {
		    	if (!player.isOnline()) {
		    		horse.remove();
		    		return;
		    	}
				horse.setPassenger(player);
				horse.setOwner(player);
				horse.setStyle(Horse.Style.values()[(int) (Math.random()*5)]);
				HorseInventory inventory = horse.getInventory();
				inventory.setArmor(MagicArmor.createMagicArmor(MagicArmor.Armor.PARKOUR));
				inventory.setSaddle(new ItemStack(Material.SADDLE));       
		    }
		}.runTask(ImOnAHorse.plugin);
	}
	
	public void restartParkour(final Player player) throws ParkourDoesNotExistException,
	PlayerIsMountedException, FinishNotSetException, PlayerAlreadyInParkourException {
		if (this.finish == null) throw new FinishNotSetException();
		final Horse horse = (Horse) player.getVehicle();
		player.teleport(start);
		new BukkitRunnable() {
			@Override
		    public void run() {
				if (horse != null) horse.remove();
				try {
					Checkpoint cp = Parkour.getParkour(player).playerCheckpoint.get(player.getName().toLowerCase());
					Location loc;
					if (cp == null) {
						loc = start;
					} else {
						loc = cp.getLocation();
					}
					Horse horse = (Horse) start.getWorld().spawnEntity(loc, EntityType.HORSE);
			    	if (!player.isOnline()) {
			    		horse.remove();
			    		return;
			    	}
					if (horse.setPassenger(player)) {
						horse.setOwner(player);
						horse.setStyle(Horse.Style.values()[(int) (Math.random()*5)]);
						HorseInventory inventory = horse.getInventory();
						inventory.setArmor(MagicArmor.createMagicArmor(MagicArmor.Armor.PARKOUR));
						inventory.setSaddle(new ItemStack(Material.SADDLE));  
					} else {
						horse.remove();
					}
				} catch (PlayerNotInParkourException e) {
					e.printStackTrace();
				}
		    }
		}.runTask(ImOnAHorse.plugin);
	}
	
	public void finishParkour(final Player player, boolean cleared, boolean sync) {
		Horse horse = (Horse) player.getVehicle();
		this.config = YamlConfiguration.loadConfiguration(this.file);
		String s = this.config.getConfigurationSection("players").getString(player.getName().toLowerCase());
		final Location loc = Parkour.toLocation(s);
		player.eject();
		String playerName = player.getName().toLowerCase();
		playerCheckpoint.get(playerName).removePlayer(player.getName().toLowerCase());
		playerCheckpoint.remove(playerName);
		if (sync) {
			new BukkitRunnable(){          
			    @Override
			    public void run() {
			    	Chunk c = loc.getChunk();
					if (!c.isLoaded())
						c.load();
					player.teleport(loc);
			    }
			}.runTaskLater(ImOnAHorse.plugin, 5);
		} else {
			Chunk c = loc.getChunk();
			if (!c.isLoaded())
				c.load();
			player.teleport(loc);
		}
		if (horse != null) horse.remove();
		
		if (cleared) {
			if (this.reward == null) this.reward = new LinkedList<ItemStack>();
			List<ItemStack> itemList = this.reward;
			for (ItemStack item : itemList) {
				player.getInventory().addItem(item);
			}
			if (ImOnAHorse.plugin.isEconomyEnabled()) {
				ImOnAHorse.plugin.getEconomy().depositPlayer(player.getName(), this.rewardMoney);
			}
			s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You have finished the parkour! Reward:" + getRewardString(player);
		} else {
			s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You have left the parkour!";
		}
		
		for (Checkpoint checkpoint : checkpoints) {
			List<String> temp = new LinkedList<String>();
			for (String name : checkpoint.getPlayers()) if (!name.equals(playerName)) temp.add(name);
			checkpoint.setPlayers(temp);
			playersInParkour.remove(playerName);
			if (checkpoint.getPlayers().isEmpty()) {
				for (MagicDispenser dispenser : checkpoint.getDispensers()) {
					dispenser.setEnabled(false);
				}
			}
		}
		
		this.config.getConfigurationSection("players").set(playerName, null);
		try {
			this.config.save(this.file);
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }
		player.sendMessage(s);
	}
	
	public static boolean isPlayerInParkour(Player player) {
		return playersInParkour.contains(player.getName().toLowerCase());
	}
	
	public static boolean isPlayerCreatingDispenser(Player player) {
		return playersDispenser.containsKey(player.getName().toLowerCase());
	}
	
	public static Parkour getParkour(Player player) throws PlayerNotInParkourException {
		Set<String> keys = parkours.keySet();
		String playerName = player.getName().toLowerCase();
		for (String s : keys) {
			Parkour parkour = parkours.get(s);
			for (Checkpoint checkpoint : parkour.checkpoints) {
				if (checkpoint.getPlayers().contains(playerName)) return parkour;
			}
		}
		throw new PlayerNotInParkourException();
	}
	
	public static void setStart(String name, Player player) throws ParkourDoesNotExistException {
		Parkour parkour = Parkour.getParkourByName(name);
		Location loc = player.getLocation();
		parkour.start = loc;
		parkour.config.set("start", Parkour.toString(loc));
		try {
			parkour.config.save(parkour.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void setFinish(String name, Player player) throws ParkourDoesNotExistException {
		Parkour parkour = Parkour.getParkourByName(name);
		Location loc = player.getLocation();
		parkour.finish = loc;
		parkour.config.set("finish", Parkour.toString(loc));
		try {
			parkour.config.save(parkour.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void addReward(String parkourName, Player player) throws ParkourDoesNotExistException,
	NoItemInHandException {
		if (player.getItemInHand().getAmount() == 0) throw new NoItemInHandException();
		Parkour parkour = Parkour.getParkourByName(parkourName);
		parkour.config = YamlConfiguration.loadConfiguration(parkour.file);
		
		@SuppressWarnings("unchecked")
		List<ItemStack> itemList = (List<ItemStack>) parkour.config.getList("reward");
		if (itemList == null) {
			parkour.config.createSection("reward");
			itemList = new LinkedList<ItemStack>();
		}
		if (parkour.reward == null) parkour.reward = new LinkedList<ItemStack>();
		ItemStack itemAdd = new ItemStack(player.getItemInHand());
		parkour.reward.add(itemAdd);
		itemList.add(itemAdd);
		ItemStack[] itemArray = new ItemStack[itemList.size()];
		int i = 0;
		for (ItemStack item : itemList) {
			itemArray[i++] = item;
		}
		parkour.config.set("reward", itemArray);
		
		try {
			parkour.config.save(parkour.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void clearReward(String parkourName, Player player) throws ParkourDoesNotExistException {
		Parkour parkour = Parkour.getParkourByName(parkourName);
		parkour.config = YamlConfiguration.loadConfiguration(parkour.file);
		parkour.config.set("reward", null);
		try {
			parkour.config.save(parkour.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void addRewardMoney(String parkourName, String amount) throws NumberFormatException, ParkourDoesNotExistException {
		Parkour parkour = Parkour.getParkourByName(parkourName);
		parkour.config = YamlConfiguration.loadConfiguration(parkour.file);
		int i = Integer.parseInt(amount);
		parkour.config.set("rewardmoney", i);
		parkour.rewardMoney = i;
		try {
			parkour.config.save(parkour.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void addDispenser(Player player, String name, String type, String delay) throws ParkourDoesNotExistException,
	PlayerIsAlreadyAddingDispenserException, NumberFormatException, IllegalArgumentException {
		if (!exists(name)) throw new ParkourDoesNotExistException();
		if (Parkour.isPlayerCreatingDispenser(player)) throw new PlayerIsAlreadyAddingDispenserException();
		Integer.parseInt(delay);
		MagicDispenser.ProjectileType.valueOf(type.toUpperCase());
		playersDispenser.put(player.getName().toLowerCase(), name.toLowerCase() + ":" + type.toUpperCase() + ":" + delay);
		String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "Leftclick a dispenser to add it to parkour " + name;
		player.sendMessage(s);
	}
	
	public static void cancelDispenser(Player player) throws PlayerIsNotAddingDispenserException {
		String s = player.getName().toLowerCase();
		for (String name : playersDispenser.keySet()) {
			if (name.equals(s)) {
				playersDispenser.remove(name);
				return;
			}
		}
		throw new PlayerIsNotAddingDispenserException();
	}
	
	public static void finishDispenser(Player player) {
		playersDispenser.remove(player.getName().toLowerCase());
		String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You successfully added a dispenser";
		player.sendMessage(s);
	}
	
	public void createDispenser(Block block, Player player) {
		String playerName = player.getName().toLowerCase();
		String[] sl = playersDispenser.get(playerName).split(":");
		MagicDispenser dispenser = new MagicDispenser(sl[1], Integer.parseInt(sl[2]), block);
		this.config = YamlConfiguration.loadConfiguration(this.file);
		Integer id = this.editCheckpoint.get(playerName);
		if (id == null) id = -1;
		List<String> dispenserList;
		if (id != -1) {
			ConfigurationSection conf = this.config.getConfigurationSection("checkpoints").getConfigurationSection("" + id);
			dispenserList = conf.getStringList("dispenser");
			if (dispenserList == null) dispenserList = new LinkedList<String>();
			dispenserList.add(dispenser.toString());
			Checkpoint.getCheckpoint(this.name + id).addDispenser(dispenser);
			conf.set("dispenser", dispenserList);
		} else {
			dispenserList = this.config.getStringList("dispenser");
			dispenserList.add(dispenser.toString());
			Checkpoint.getCheckpoint(this.name + id).addDispenser(dispenser);
			this.config.set("dispenser", dispenserList);
		}
		try {
			this.config.save(this.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void removeDispenser(Block block) {
		Vector vec = block.getLocation().toVector();
		World world = block.getLocation().getWorld();
		for (Checkpoint checkpoint : this.checkpoints) {
			for (MagicDispenser dispenser : checkpoint.getDispensers()) {
				if (dispenser.getWorld().equals(world) && dispenser.getLocation().toVector().distance(vec) == 0) {
					checkpoint.removeDispenser(dispenser);
					List<String> dispenserList;
					if (checkpoint.getPriority() == -1) {
						dispenserList = this.config.getStringList("dispenser");
						String out = "";
						for (String s : dispenserList) {
							Location loc = MagicDispenser.parseString(s).getLocation();
							if (loc.getWorld().equals(world) && loc.toVector().distance(vec) == 0) {
								out = s;
								break;
							}
						}
						if (!out.equals("")) {
							dispenserList.remove(out);
							this.config.set("dispenser", dispenserList);
							try {
								this.config.save(this.file);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} else {
						ConfigurationSection conf = this.config.getConfigurationSection("checkpoints").getConfigurationSection("" + checkpoint.getPriority());
						dispenserList = conf.getStringList("dispenser");
						String out = "";
						for (String s : dispenserList) {
							if (MagicDispenser.parseString(s).getLocation().toVector().equals(vec)) {
								out = s;
								break;
							}
						}
						if (!out.equals("")) {
							dispenserList.remove(out);
							conf.set("dispenser", dispenserList);
							try {
								this.config.save(this.file);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					return;
				}
			}
		}
	}
	
	public static void setCheckpoint(Player player, String name, String checkpoint) throws NumberFormatException,
	CheckpointDoesNotExistException, ParkourDoesNotExistException {
		if (!exists(name)) throw new ParkourDoesNotExistException();
		Parkour parkour = Parkour.getParkourByName(name.toLowerCase());
		Integer i;
		if (checkpoint.equals("start")) {
			i = -1;
		} else {
			i = Integer.parseInt(checkpoint);
		}
		for (Checkpoint cp : parkour.checkpoints) {
			if (cp.getPriority() == i) {
				parkour.editCheckpoint.put(player.getName().toLowerCase(), i);
				return;
			}
		}
		throw new CheckpointDoesNotExistException();
	}
	
	public static void addCheckpoint(Player player, String parkourName, String priority) throws ParkourDoesNotExistException,
	NumberFormatException, CheckpointAlreadyExistsException {
		Parkour parkour = Parkour.getParkourByName(parkourName);
		Location loc = player.getLocation();
		if (parkour.checkpoints == null) parkour.checkpoints = new LinkedList<Checkpoint>();
		Vector vec = new Vector(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
		for (Checkpoint checkpoint : parkour.checkpoints) {
			if (checkpoint.getLocation().toVector().equals(vec)) throw new CheckpointAlreadyExistsException();
		}
		parkour.checkpoints.add(new Checkpoint(parkour, loc, Integer.parseInt(priority)));
		parkour.config = YamlConfiguration.loadConfiguration(parkour.file);
		ConfigurationSection checkpointList = parkour.config.getConfigurationSection("checkpoints");
		if (checkpointList == null) checkpointList = parkour.config.createSection("checkpoints");
		checkpointList.createSection("" + priority).set("location", Parkour.toString(loc));
		parkour.config.set("checkpoints", checkpointList);
		try {
			parkour.config.save(parkour.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void clearCheckpoint(String parkourName) throws ParkourDoesNotExistException {
		Parkour parkour = Parkour.getParkourByName(parkourName);
		parkour.checkpoints = new LinkedList<Checkpoint>();
		parkour.config = YamlConfiguration.loadConfiguration(parkour.file);
		parkour.config.set("checkpoints", null);
		try {
			parkour.config.save(parkour.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Location getStart() {
		return this.start;
	}
	
	public Location getFinish() {
		return this.finish;
	}
	
	private static boolean exists(String name) {
		Set<String> keys = parkours.keySet();
		for (String s : keys) {
			Parkour parkour = parkours.get(s);
			if (parkour.name.equalsIgnoreCase(name)) return true;
		}
		return false;
	}
	
	private static Parkour getParkourByName(String name) throws ParkourDoesNotExistException {
		Set<String> keys = parkours.keySet();
		for (String s : keys) {
			Parkour parkour = parkours.get(s);
			if (parkour.name.equalsIgnoreCase(name)) return parkour;
		}
		throw new ParkourDoesNotExistException();
	}
	
	public static Parkour getParkourForDispenser(Player player) throws ParkourDoesNotExistException {
		String name = playersDispenser.get(player.getName().toLowerCase());
		name = name.split(":")[0];
		Set<String> keys = parkours.keySet();
		for (String s : keys) {
			Parkour parkour = parkours.get(s);
			if (parkour.name.equalsIgnoreCase(name)) {
				return parkour;
			}
		}
		throw new ParkourDoesNotExistException();
	}
	
	private static String toString(Location loc) {
		return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
	}
	
	private static Location toLocation(String s) {
		String[] sl = s.split(":");
		return new Location(ImOnAHorse.plugin.getServer().getWorld(sl[0]),
				Double.parseDouble(sl[1]), Double.parseDouble(sl[2]), Double.parseDouble(sl[3]));
	}
	
	private String getRewardString(Player player) {
		String s = "";
		if (reward != null)
			for (ItemStack item : reward) {
				s = s + "\n" + ChatColor.RED + item.getAmount() + " " + item.getType().toString().toLowerCase();
			}
		if (rewardMoney != 0) {
			s = s + "\n" + ChatColor.YELLOW + rewardMoney + "$ (current: " +
					(int) ImOnAHorse.plugin.getEconomy().getBalance(player.getName()) + "$)";
		}
		if (s.equals("")) return ChatColor.RED + "\nNo reward has been set yet!";
		return s;
	}
	
	public static String getParkourNames() {
		String out = "" + ChatColor.YELLOW;
		Set<String> keys = Parkour.parkours.keySet();
		for (String s : keys) {
			out = out + "\n" + s;
		}
		return out;
	}
	
	public boolean isDispenserRegistered(Block block) {
		Vector vec = block.getLocation().toVector();
		World world = block.getWorld();
		for (Checkpoint checkpoint : this.checkpoints) {
			for (MagicDispenser dispenser : checkpoint.getDispensers()) {
				if (dispenser.getWorld().equals(world) && dispenser.getLocation().toVector().distance(vec) == 0) return true;
			}
		}
		return false;
	}
	
	public static Parkour getDispenserParkour(Block block) {
		Vector vec = block.getLocation().toVector();
		World world = block.getWorld();
		for (Parkour parkour : Parkour.parkours.values()) {
			for (Checkpoint checkpoint : parkour.checkpoints) {
				for (MagicDispenser dispenser : checkpoint.getDispensers()) {
					if (dispenser.getWorld().equals(world) && dispenser.getLocation().toVector().distance(vec) == 0) return parkour;
				}
			}
		}
		return null;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public void run() {

		for (Parkour parkour : collection) {
			for (Checkpoint checkpoint : parkour.checkpoints) {
				for (String name : checkpoint.getPlayers()) {
					Player player = ImOnAHorse.plugin.getServer().getPlayer(name);
					if (player != null) {
						if (!player.isInsideVehicle()) {
							try {
								parkour.restartParkour(player);
							} catch (ParkourDoesNotExistException
									| PlayerIsMountedException
									| FinishNotSetException
									| PlayerAlreadyInParkourException e) {
								e.printStackTrace();
							}
							return;
						}
							
						Location finish = parkour.getFinish();
						Location playerLoc = player.getLocation();
						if (!finish.getWorld().equals(playerLoc.getWorld())) {
							parkour.finishParkour(player, false, true);
							return;
						}
						if (finish.distance(playerLoc) < finishThreshold) {
							parkour.finishParkour(player, true, true);
							return;
						}
	
						for (Checkpoint cp : parkour.checkpoints) {
							if ((cp.getPriority() > checkpoint.getPriority()) &&
									(cp.getLocation().distance(playerLoc) < checkpointThreshold)) {
								String playerName = player.getName().toLowerCase();
								parkour.playerCheckpoint.put(playerName, cp);
								checkpoint.removePlayer(playerName);
								List<String> players = checkpoint.getPlayers();
								if (players.size() == 0) {
									for (MagicDispenser dispenser : checkpoint.getDispensers()) {
										dispenser.setEnabled(false);
									}
								}
								cp.addPlayer(playerName);
								for (MagicDispenser disp : cp.getDispensers()) {
									disp.setEnabled(true);
								}
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.YELLOW + "You reached a checkpoint!";
								player.sendMessage(s);
								return;
							}
						}
						
							
						for (int x = -1; x <= 1; x++) {
				            for (int y = -1; y <= 1; y++) {
				                for (int z = -1; z <= 1; z++) {
				                    Block block = playerLoc.getBlock().getRelative(x, y, z);
				                    if (block.getType().equals(Material.WOOL)) {
				    					try {
											parkour.restartParkour(player);
											ParkourListener.cooldown.add(player);
											final Player plFinal = player;
											new BukkitRunnable() {
												@Override
											    public void run() {
													ParkourListener.cooldown.remove(plFinal);
											    }
											}.runTaskLater(ImOnAHorse.plugin, ParkourListener.delay);
					    					String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You got too close to the wool so you have to restart!";
					    					player.sendMessage(s);
					    					return;
										} catch (
												ParkourDoesNotExistException
												| PlayerIsMountedException
												| FinishNotSetException
												| PlayerAlreadyInParkourException e) {
											if (!suppressExceptions) e.printStackTrace();
											suppressExceptions = true;
										}
				                    }
				                }
				            }    
				        }
					}
				}
				
				for (MagicDispenser dispenser : checkpoint.getDispensers()) {
					if (dispenser.isEnabled()) dispenser.run();
				}
			}
		}
		
	}
	
}
