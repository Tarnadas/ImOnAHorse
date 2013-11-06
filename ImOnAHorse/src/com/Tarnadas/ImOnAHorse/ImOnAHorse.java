package com.Tarnadas.ImOnAHorse;

import java.io.File;
import java.io.IOException;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.Tarnadas.ImOnAHorse.Exceptions.CheckpointAlreadyExistsException;
import com.Tarnadas.ImOnAHorse.Exceptions.CheckpointDoesNotExistException;
import com.Tarnadas.ImOnAHorse.Exceptions.FinishNotSetException;
import com.Tarnadas.ImOnAHorse.Exceptions.InventoryFullException;
import com.Tarnadas.ImOnAHorse.Exceptions.NoItemInHandException;
import com.Tarnadas.ImOnAHorse.Exceptions.ParkourDoesNotExistException;
import com.Tarnadas.ImOnAHorse.Exceptions.PlayerAlreadyInParkourException;
import com.Tarnadas.ImOnAHorse.Exceptions.PlayerIsAlreadyAddingDispenserException;
import com.Tarnadas.ImOnAHorse.Exceptions.PlayerIsMountedException;
import com.Tarnadas.ImOnAHorse.Exceptions.PlayerIsNotAddingDispenserException;
import com.Tarnadas.ImOnAHorse.Exceptions.PlayerNotInParkourException;
import com.Tarnadas.ImOnAHorse.Listeners.CraftListener;
import com.Tarnadas.ImOnAHorse.Listeners.HorseListener;
import com.Tarnadas.ImOnAHorse.Listeners.LeashListener;
import com.Tarnadas.ImOnAHorse.Listeners.MagicDispenserListener;
import com.Tarnadas.ImOnAHorse.Listeners.ParkourListener;
import com.Tarnadas.ImOnAHorse.Listeners.ParkourSignListener;
import com.Tarnadas.ImOnAHorse.Listeners.WorldGuardListener;
import com.Tarnadas.ImOnAHorse.parkour.Checkpoint;
import com.Tarnadas.ImOnAHorse.parkour.Parkour;
import com.Tarnadas.ImOnAHorse.parkour.ParkourSign;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class ImOnAHorse extends JavaPlugin {
	
	private final static int period = 4;
	
	public static ImOnAHorse plugin;
	
	private Economy economy = null;
	private boolean economyEnabled;
	
	private WorldGuardPlugin wg = null;
	private boolean wgEnabled;
	
	@Override
	public void onEnable() {
		
		plugin = this;
		this.saveDefaultConfig();
		File file = new File(this.getDataFolder() + "\\Parkours");
		file.mkdir();
		file = new File(this.getDataFolder() + "\\Players");
		file.mkdir();
		new Config(this);
		MagicArmor.onInit(this, false);
		MagicSaddle.onInit(this);
		Checkpoint.onLoad();
		Parkour.loadParkourData();
		ParkourListener.onInit();
		MagicLeash.onLoad();
		ParkourSign.onLoad();
		getServer().getPluginManager().registerEvents(new HorseListener(), this);
		getServer().getPluginManager().registerEvents(new CraftListener(), this);
		getServer().getPluginManager().registerEvents(new ParkourListener(), this);
		getServer().getPluginManager().registerEvents(new MagicDispenserListener(), this);
		getServer().getPluginManager().registerEvents(new LeashListener(), this);
		getServer().getPluginManager().registerEvents(new ParkourSignListener(), this);
		economyEnabled = setupEconomy();
		wgEnabled = loadWorldGuard();
		if (wgEnabled) getServer().getPluginManager().registerEvents(new WorldGuardListener(), this);
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}
		new Parkour().runTaskTimer(this, 20, period);
		this.getLogger().info("Enabled");
		
	}
	
	@Override
	public void onDisable() {
		Player[] players = plugin.getServer().getOnlinePlayers();
		for (int i=0; i<players.length; i++) {
			Player player = players[i];
			if (!Parkour.isPlayerInParkour(player)) return;
			try {
				Parkour.getParkour(player).finishParkour(player, false, false);
			} catch (PlayerNotInParkourException e) {
			}
		}
	}
	
	public boolean reload(Player player) {
		new Config(this);
		MagicArmor.onInit(this, true);
		MagicSaddle.onInit(this);
		MagicLeash.onLoad();
		Checkpoint.onLoad();
		boolean b = Parkour.reloadParkourData(player);
		ParkourSign.onLoad();
		this.getLogger().info("Reloaded");
		return b;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		
		if(cmd.getName().equalsIgnoreCase("horse")) {
			
			if (args.length > 0) {
				if (sender instanceof ConsoleCommandSender ||
						(sender instanceof Player) && ((Player) sender).hasPermission("horse.reload")) {
					if (args[0].equalsIgnoreCase("reload")) {
						if (sender instanceof Player) {
							if (this.reload((Player) sender)) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "has been reloaded";
								sender.sendMessage(s);
							} else {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Could not reload parkours, because there are still players inside!";
								sender.sendMessage(s);
							}
						} else {
							this.reload(null);
						}
						return true;
					}
				}
			}
			
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be run by a player.");
			} else {
				
				Player player = (Player) sender;
				
				if (args.length == 0) {
					sender.sendMessage(ChatColor.DARK_GREEN + "ImOnAHorse v1.1.2\n" +
							ChatColor.AQUA + "by Tarnadas\nType " + ChatColor.RED + "/horse help" +
									ChatColor.AQUA + " for detailed information");
					return true;
				}
				
				if (args[0].equalsIgnoreCase("help")) {

					String s = ChatColor.DARK_GREEN + "------ ImOnAHorse help page ------\n" +
							ChatColor.RED + "/horse leash: " + ChatColor.AQUA + "Gives you the magical leash to store your horses\n" +
							ChatColor.RED + "/horse parkour: " + ChatColor.AQUA + "Shows a list of available parkours\n" +
							ChatColor.RED + "/horse parkour <parkourName>: " + ChatColor.AQUA + "Enters a parkour\n" + 
							ChatColor.RED + "/horse parkour leave: " + ChatColor.AQUA + "Leaves a parkour";
					player.sendMessage(s);

				} else if (args[0].equalsIgnoreCase("admin")) {

					String s = ChatColor.DARK_GREEN + "------ ImOnAHorse admin page ------\n" +
							ChatColor.AQUA + "Type any of the commands to get additional information\n" + ChatColor.YELLOW +
							"/horse parkour create\n" + 
							"/horse parkour setstart\n" +
							"/horse parkour setfinish\n" +
							"/horse parkour addreward\n" +
							"/horse parkour clearreward\n" +
							"/horse parkour rewardmoney\n" +
							"/horse parkour addcheckpoint\n" +
							"/horse parkour clearcheckpoint\n" +
							"/horse parkour editcheckpoint\n" +
							"/horse parkour dispenser\n" +
							"/horse cancel: " + ChatColor.AQUA + "Cancels the creation of a dispenser";
					player.sendMessage(s);
					
				} else if (args[0].equalsIgnoreCase("parkour")) {
					
					if (!player.hasPermission("horse.parkour")) {
						String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You do not have permission!";
						player.sendMessage(s);
						return true;
					}
					if (args.length == 1) {
						String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "list of available parkours:" +
								Parkour.getParkourNames();
						player.sendMessage(s);
						return true;
					}
					if (args[1].equalsIgnoreCase("leave")) {
						try {
							Parkour.getParkour(player).finishParkour(player, false, true);
						} catch (PlayerNotInParkourException e) {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You are not in any parkour!";
							sender.sendMessage(s);
						}
					} else if (args[1].equalsIgnoreCase("create")) {
						if (!player.hasPermission("horse.parkour.create")) {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You do not have permission!";
							sender.sendMessage(s);
							return true;
						}
						if (args.length > 2) {
							new Parkour(this, args[2], player);
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "Parkour " + args[2] + " created";
							sender.sendMessage(s);
						} else {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Usage: " +
									ChatColor.YELLOW + "/horse parkour create <parkourName>\n" + ChatColor.AQUA +
									"Creates a parkour with the given name at your current position";
							sender.sendMessage(s);
						}
					} else if (args[1].equalsIgnoreCase("setfinish")) {
						if (!player.hasPermission("horse.parkour.create")) {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You do not have permission!";
							sender.sendMessage(s);
							return true;
						}
						if (args.length > 2) {
							try {
								Parkour.setFinish(args[2], player);
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "Finish has been set for parkour " + args[2];
								sender.sendMessage(s);
							} catch (ParkourDoesNotExistException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Parkour " + args[2] + " does not exist!";
								sender.sendMessage(s);
							}
						} else {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Usage: " +
									ChatColor.YELLOW + "/horse parkour setfinish <parkourName>\n" + ChatColor.AQUA +
									"Sets the finish for the given parkour";
							sender.sendMessage(s);
						}
					} else if (args[1].equalsIgnoreCase("setstart")) {
						if (!player.hasPermission("horse.parkour.create")) {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You do not have permission!";
							sender.sendMessage(s);
							return true;
						}
						if (args.length > 2) {
							try {
								Parkour.setStart(args[2], player);
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "Start has been set for parkour " + args[1];
								sender.sendMessage(s);
							} catch (ParkourDoesNotExistException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Parkour " + args[2] + " does not exist!";
								sender.sendMessage(s);
							}
						} else {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Usage: " +
									ChatColor.YELLOW + "/horse parkour setstart <parkourName>\n" + ChatColor.AQUA +
									"Sets the start for the given parkour";
							sender.sendMessage(s);
						}
					} else if (args[1].equalsIgnoreCase("addreward")) {
						if (!player.hasPermission("horse.parkour.create")) {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You do not have permission!";
							sender.sendMessage(s);
							return true;
						}
						if (args.length > 2) {
							try {
								Parkour.addReward(args[2], player);
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA +
										"The item in your hand has been added as a reward for parkour " + args[2];
								player.sendMessage(s);
							} catch (ParkourDoesNotExistException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Parkour " + args[2] + " does not exist!";
								sender.sendMessage(s);
							} catch (NoItemInHandException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You don't have any item in your hand!";
								sender.sendMessage(s);
							}
						} else {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Usage: " +
									ChatColor.YELLOW + "/horse parkour addreward <parkourName>\n" + ChatColor.AQUA +
									"Adds the item in your hand as a reward for the given parkour";
							sender.sendMessage(s);
						}
					} else if (args[1].equalsIgnoreCase("clearreward")) {
						if (!player.hasPermission("horse.parkour.create")) {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You do not have permission!";
							sender.sendMessage(s);
							return true;
						}
						if (args.length > 2) {
							try {
								Parkour.clearReward(args[2], player);
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA +
										"Reward for parkour " + args[2] + " has been cleared";
								player.sendMessage(s);
							} catch (ParkourDoesNotExistException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Parkour " + args[2] + " does not exist!";
								sender.sendMessage(s);
							}
						} else {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Usage: " +
									ChatColor.YELLOW + "/horse parkour clearreward <parkourName>\n" + ChatColor.AQUA +
									"Clears all item rewards for the given parkour";
							sender.sendMessage(s);
						}
					} else if (args[1].equalsIgnoreCase("clearcheckpoint")) {
						if (!player.hasPermission("horse.parkour.create")) {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You do not have permission!";
							sender.sendMessage(s);
							return true;
						}
						if (args.length > 2) {
							try {
								Parkour.clearCheckpoint(args[2]);
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You cleared all checkpoints for parkour " + args[2];
								sender.sendMessage(s);
							} catch (ParkourDoesNotExistException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Parkour " + args[2] + " does not exist!";
								sender.sendMessage(s);
							}
						} else {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Usage: " +
									ChatColor.YELLOW + "/horse parkour clearcheckpoint <parkourName>\n" + ChatColor.AQUA +
									"Clears all checkpoints for the given parkour " + ChatColor.RED + "and all dispensers that are connected to it";
							sender.sendMessage(s);
						}
					} else if (args[1].equalsIgnoreCase("dispenser")) {
						if (!player.hasPermission("horse.parkour.create")) {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You do not have permission!";
							sender.sendMessage(s);
							return true;
						}
						if (args.length > 4) {
							try {
								Parkour.addDispenser(player, args[2], args[3], args[4]);
							} catch (ParkourDoesNotExistException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Parkour " + args[2] + " does not exist!";
								sender.sendMessage(s);
							} catch (PlayerIsAlreadyAddingDispenserException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You are already adding a dispenser!";
								sender.sendMessage(s);
							} catch (NumberFormatException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + args[4] + " is not a number!";
								sender.sendMessage(s);
							} catch (IllegalArgumentException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + args[3] + " is not a projectile type!\n" +
										ChatColor.YELLOW + "projectile types: " + ChatColor.AQUA + "snowball arrow largefireball\n";
								sender.sendMessage(s);
							}
						} else {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Usage: " +
									ChatColor.YELLOW + "/horse parkour dispenser <parkourName> <projectileType> <delay>\n" + ChatColor.AQUA +
									"Adds a dispenser to the given parkour and the active checkpoint\n" +
									ChatColor.RED + "projectile types: " + ChatColor.YELLOW + "snowball arrow largefireball\n" +
									ChatColor.AQUA + "delay of 1 equals 200ms (0.2s)";
							sender.sendMessage(s);
						}
						
					} else if (args[1].equalsIgnoreCase("addcheckpoint")) {
						if (!player.hasPermission("horse.parkour.create")) {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You do not have permission!";
							sender.sendMessage(s);
							return true;
						}
						if (args.length > 3) {
							try {
								Parkour.addCheckpoint(player, args[2], args[3]);
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You added a checkpoint for parkour " + args[2];
								sender.sendMessage(s);
							} catch (ParkourDoesNotExistException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Parkour " + args[2] + " does not exist!";
								sender.sendMessage(s);
							} catch (NumberFormatException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + args[3] + " is not a number!";
								sender.sendMessage(s);
							} catch (CheckpointAlreadyExistsException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "This checkpoint already exists!";
								sender.sendMessage(s);
							}
						} else {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Usage: " +
									ChatColor.YELLOW + "/horse parkour addcheckpoint <parkourName> <checkpointID>\n" + ChatColor.AQUA +
									"Adds a checkpoint to the given parkour at your current position";
							sender.sendMessage(s);
						}
					} else if (args[1].equalsIgnoreCase("rewardmoney")) {
						if (!player.hasPermission("horse.parkour.create")) {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You do not have permission!";
							sender.sendMessage(s);
							return true;
						}
						if (args.length > 3) {
							try {
								Parkour.addRewardMoney(args[2], args[3]);
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "Reward money for parkour " +
										args[2] + " has been set to " + args[3];
								sender.sendMessage(s);
							} catch (NumberFormatException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + args[3] + " is not a number!";
								sender.sendMessage(s);
							} catch (ParkourDoesNotExistException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Parkour " + args[2] + " does not exist!";
								sender.sendMessage(s);
							}
						} else {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Usage: " +
									ChatColor.YELLOW + "/horse parkour rewardmoney <parkourName> <amount>\n" + ChatColor.AQUA +
									"Sets the money reward for the given parkour (requires Vault)";
							sender.sendMessage(s);
						}
					} else if (args[1].equalsIgnoreCase("editcheckpoint")) {
						if (!player.hasPermission("horse.parkour.create")) {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You do not have permission!";
							sender.sendMessage(s);
							return true;
						}
						if (args.length > 3) {
							try {
								Parkour.setCheckpoint(player, args[2], args[3]);
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You are now configuring checkpoint " + args[3] + " for parkour " + args[2];
								sender.sendMessage(s);
							} catch (NumberFormatException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + args[3] + " is not a number!";
								sender.sendMessage(s);
							} catch (CheckpointDoesNotExistException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Checkpoint " + args[3] + " does not exist for parkour " + args[2] + "!";
								sender.sendMessage(s);
							} catch (ParkourDoesNotExistException e) {
								String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Parkour " + args[2] + " does not exist !";
								sender.sendMessage(s);
							}
						} else {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Usage: " +
									ChatColor.YELLOW + "/horse parkour editcheckpoint <parkourName> <checkpointId>\n" + ChatColor.AQUA +
									"Lets you add dispensers to the given parkour and checkpoint";
							sender.sendMessage(s);
						}
					} else if (args.length == 2) {
						try {
							Parkour.startParkour(player, args[1]);
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "Parkour " + args[1] + " started";
							sender.sendMessage(s);
						} catch (ParkourDoesNotExistException e) {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Parkour " + args[1] + " does not exist!";
							sender.sendMessage(s);
						} catch (PlayerIsMountedException e) {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You have to dismount in order to do that!";
							sender.sendMessage(s);
						} catch (FinishNotSetException e) {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "No finish has been set for this parkour!";
							sender.sendMessage(s);
						} catch (PlayerAlreadyInParkourException e) {
							String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You are already in a parkour!" +
									ChatColor.AQUA + "\nType " + ChatColor.RED + "/horse parkour leave " +
									ChatColor.AQUA + "to leave it";
							sender.sendMessage(s);
						}
					} else {
						String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Command not found!\n" +
								ChatColor.AQUA + "Type " + ChatColor.RED + "/horse help " + ChatColor.AQUA +
								"for a list of commands";
						sender.sendMessage(s);
					}
					
				} else if (args[0].equalsIgnoreCase("cancel")) {
					try {
						Parkour.cancelDispenser(player);
						String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.AQUA + "You stopped adding a dispenser";
						sender.sendMessage(s);
					} catch (PlayerIsNotAddingDispenserException e) {
						String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "You are not adding any dispenser!";
						sender.sendMessage(s);
					}
				} else if (args[0].equalsIgnoreCase("leash")) {
					try {
						MagicLeash.getPlayerLeash(player);
					} catch (InventoryFullException e) {
						String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Your inventory is full!";
						sender.sendMessage(s);
					}
				} else {
					String s = ChatColor.DARK_GREEN + "[ImOnAHorse] " + ChatColor.RED + "Command not found!\n" +
							ChatColor.AQUA + "Type " + ChatColor.RED + "/horse help " + ChatColor.AQUA +
							"for a list of commands";
					sender.sendMessage(s);
				}
				
			}
		}
		return true;
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
	
	private boolean loadWorldGuard() {
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return false;
	    } else {
		    wg = (WorldGuardPlugin) plugin;
		    return true;
	    }
	 
	}
	
	public boolean isEconomyEnabled() {
		return economyEnabled;
	}
	
	public Economy getEconomy() {
		return economy;
	}
	
	public boolean isWorldGuardEnabled() {
		return wgEnabled;
	}
	
	public WorldGuardPlugin getWorldGuard() {
		return wg;
	}
	
}
