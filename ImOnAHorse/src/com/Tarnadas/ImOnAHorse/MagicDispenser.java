package com.Tarnadas.ImOnAHorse;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
//import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
//import org.bukkit.entity.WitherSkull;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class MagicDispenser extends BukkitRunnable {

	private static final double arrowX = 1.6;
	private static final double arrowY = 1.6;
	private static final double arrowZ = 1.6;

//	private static final double smallFireballX = 0.01;
//	private static final double smallFireballY = 0.01;
//	private static final double smallFireballZ = 0.01;

	private static final double largeFireballX = 2;
	private static final double largeFireballY = 2;
	private static final double largeFireballZ = 2;

//	private static final double witherSkullX = 2;
//	private static final double witherSkullY = 2;
//	private static final double witherSkullZ = 2;

	private static final double snowballX = 1;
	private static final double snowballY = 1;
	private static final double snowballZ = 1;

	private static final double offset = 1.0;
	private static final double spawnOffset = 0.5;
	private static final double arrowOffset = 0.25;
	private static final double snowballOffset = 0.3;
	
	final private ProjectileType type;
	private int delay;
	final private Location loc;
	final private Location spawnLoc;
	final private World world;
	final private BlockFace blockFace;
	private boolean enabled;
	private int tick;
	
	public enum ProjectileType {
		ARROW, /*SMALLFIREBALL,*/ LARGEFIREBALL, /*WITHERSKULL,*/ SNOWBALL
	}
	
	public MagicDispenser(String type, int delay, Block block) {
		this.type = ProjectileType.valueOf(type.toUpperCase());
		this.delay = delay;
		this.loc = block.getLocation();
		this.world = loc.getWorld();
		MaterialData d = block.getState().getData();
		org.bukkit.material.Dispenser disp = (org.bukkit.material.Dispenser) d;
		this.blockFace = disp.getFacing();
		float yaw = 0;
		if (blockFace.equals(BlockFace.NORTH)) yaw = -180;
		if (blockFace.equals(BlockFace.EAST)) yaw = -90;
		if (blockFace.equals(BlockFace.WEST)) yaw = 90;
		float pitch = 0;
		if (blockFace.equals(BlockFace.UP)) pitch = -90;
		if (blockFace.equals(BlockFace.DOWN)) pitch = 90;
		this.spawnLoc = new Location(loc.getWorld(), spawnOffset + loc.getX() + blockFace.getModX() * offset, spawnOffset + loc.getY() +
				blockFace.getModY() * offset, spawnOffset + loc.getZ()  + blockFace.getModZ() * offset, yaw, pitch);
		this.enabled = false;
		this.tick = 0;
	}
	
	public MagicDispenser(ProjectileType type, int delay, World world, int x, int y, int z, BlockFace face) {
		this.type = type;
		this.delay = delay;
		this.loc = new Location(world, x, y, z);
		this.world = world;
		this.blockFace = face;
		float yaw = 0;
		if (blockFace.equals(BlockFace.NORTH)) yaw = -180;
		if (blockFace.equals(BlockFace.EAST)) yaw = -90;
		if (blockFace.equals(BlockFace.WEST)) yaw = 90;
		float pitch = 0;
		if (blockFace.equals(BlockFace.UP)) pitch = -90;
		if (blockFace.equals(BlockFace.DOWN)) pitch = 90;
		this.spawnLoc = new Location(loc.getWorld(), spawnOffset + loc.getX() + face.getModX() * offset, spawnOffset + loc.getY() +
				face.getModY() * offset, spawnOffset + loc.getZ()  + face.getModZ() * offset, yaw, pitch);
		this.enabled = false;
		this.tick = 0;
	}
	
	public String toString() {
		String s = type.toString() + ":" + delay + ":" + loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() +
				":" + loc.getBlockZ() + ":" + blockFace.toString();
		return s;
	}
	
	public static MagicDispenser parseString(String s) {
		String[] sl = s.split(":");
		return new MagicDispenser(ProjectileType.valueOf(sl[0]), Integer.parseInt(sl[1]), ImOnAHorse.plugin.getServer().getWorld(sl[2]),
				Integer.parseInt(sl[3]), Integer.parseInt(sl[4]), Integer.parseInt(sl[5]), BlockFace.valueOf(sl[6]));
	}
	
	public void setEnabled(boolean b) {
		this.enabled = b;
	}
	
	public boolean isEnabled() {
		return this.enabled && (++tick == delay);
	}
	
	private Vector getVelocity(ProjectileType type) {
		switch(type) {
		case ARROW:
			return new Vector(blockFace.getModX() * arrowX, arrowOffset + blockFace.getModY() * arrowY, blockFace.getModZ() * arrowZ);
//		case SMALLFIREBALL:
//			return new Vector(blockFace.getModX() * smallFireballX, blockFace.getModY() * smallFireballY, blockFace.getModZ() * smallFireballZ);
		case LARGEFIREBALL:
			return new Vector(blockFace.getModX() * largeFireballX, blockFace.getModY() * largeFireballY, blockFace.getModZ() * largeFireballZ);
//		case WITHERSKULL:
//			return new Vector(blockFace.getModX() * witherSkullX, blockFace.getModY() * witherSkullY, blockFace.getModZ() * witherSkullZ);
		case SNOWBALL:
			return new Vector(blockFace.getModX() * snowballX, snowballOffset + blockFace.getModY() * snowballY, blockFace.getModZ() * snowballZ);
		default:
			return null;
		}
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public World getWorld() {
		return world;
	}
	
	public int getX() {
		return loc.getBlockX();
	}
	
	public int getY() {
		return loc.getBlockY();
	}
	
	public int getZ() {
		return loc.getBlockZ();
	}
	
	public void run() {

		tick = 0;
		switch(type) {
		case ARROW:
			Entity entity = world.spawn(spawnLoc, Arrow.class);
			entity.setVelocity(getVelocity(type));
			entity.setFireTicks(200);
			break;
//		case SMALLFIREBALL:
//			Fireball fireball = (Fireball) world.spawn(spawnLoc, SmallFireball.class);
//			fireball.setDirection(getVelocity(type));;
//			fireball.setIsIncendiary(false);
//			fireball.setBounce(false);
//			break;
		case LARGEFIREBALL:
			Fireball fireball = (Fireball) world.spawn(spawnLoc, Fireball.class);
			fireball.setDirection(getVelocity(type));;
			fireball.setIsIncendiary(false);
			fireball.setBounce(false);
			break;
//		case WITHERSKULL:
//			fireball = (Fireball) world.spawn(spawnLoc, WitherSkull.class);
//			fireball.setDirection(getVelocity(type));;
//			fireball.setIsIncendiary(false);
//			fireball.setBounce(false);
//			break;
		case SNOWBALL:
			world.spawn(spawnLoc, Snowball.class).setVelocity(getVelocity(type));
		default:
		}
	}
	
}
