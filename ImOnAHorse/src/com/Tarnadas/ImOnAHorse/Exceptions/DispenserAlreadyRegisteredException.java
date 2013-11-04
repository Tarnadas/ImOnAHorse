package com.Tarnadas.ImOnAHorse.Exceptions;

import org.bukkit.entity.Player;

@SuppressWarnings("serial")
public class DispenserAlreadyRegisteredException extends Exception {

	public DispenserAlreadyRegisteredException(Player player) {
		String s ="This dispenser is already registered!";
		player.sendMessage(s);
	}
	
}
