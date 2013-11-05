package com.Tarnadas.ImOnAHorse.parkour;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;


public class Checkpoint {

	private static Map<String,Checkpoint> checkpoints;
	
	private String name;
	private Location loc;
	private int priority;
	private List<String> players;
	private List<MagicDispenser> dispensers;
	
	public Checkpoint(Parkour parkour, Location loc, int priority) {
		this.name = parkour.getName();
		this.loc = loc;
		this.priority = priority;
		this.players = new LinkedList<String>();
		this.dispensers = new LinkedList<MagicDispenser>();
		checkpoints.put(name + priority, this);
	}
	
	public static void onLoad() {
		checkpoints = new HashMap<String,Checkpoint>();
	}
	
	public static Checkpoint getCheckpoint(String id) {
		return checkpoints.get(id);
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void addDispenser(MagicDispenser dispenser) {
		this.dispensers.add(dispenser);
	}
	
	public void removeDispenser(MagicDispenser dispenser) {
		this.dispensers.remove(dispenser);
	}
	
	public List<MagicDispenser> getDispensers() {
		return dispensers;
	}
	
	public void addPlayer(String player) {
		this.players.add(player);
	}
	
	public void removePlayer(String player) {
		this.players.remove(player);
	}
	
	public List<String> getPlayers() {
		return players;
	}
	
	public void setPlayers(List<String> players) {
		this.players = players;
	}
	
}
