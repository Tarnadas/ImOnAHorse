package com.Tarnadas.ImOnAHorse;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MagicEgg {
	
	private boolean released;
	private String name;
	private String owner;
	private int age;
	private Horse.Color color;
	private Horse.Style style;
	private Horse.Variant variant;
	private ItemStack saddle;
	private ItemStack armor;
	
	public MagicEgg(boolean released, String name, String owner, int age, String colorString, String styleString, String variantString,
			ItemStack saddle, ItemStack armor) {
		this.released = released;
		this.name = name;
		this.owner = owner;
		this.age = age;
		this.color = Horse.Color.valueOf(colorString);
		this.style = Horse.Style.valueOf(styleString);
		this.variant = Horse.Variant.valueOf(variantString);
		this.saddle = saddle;
		this.armor = armor;
	}
	
	public MagicEgg(String name, Player player, Horse horse) {
		this.released = false;
		this.name = name;
		this.owner = player.getName().toLowerCase();
		this.age = horse.getAge();
		this.color = horse.getColor();
		this.style = horse.getStyle();
		this.variant = horse.getVariant();
		this.saddle = horse.getInventory().getSaddle();
		this.armor = horse.getInventory().getArmor();
	}
	
	public boolean isReleased() {
		return released;
	}
	
	public void setReleased(boolean released) {
		this.released = released;
	}
	
	public String getName() {
		return name;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public int getAge() {
		return age;
	}
	
	public Horse.Color getColor() {
		return color;
	}
	
	public Horse.Style getStyle() {
		return style;
	}
	
	public Horse.Variant getVariant() {
		return variant;
	}
	
	public ItemStack getSaddle() {
		return saddle;
	}
	
	public ItemStack getArmor() {
		return armor;
	}

}
