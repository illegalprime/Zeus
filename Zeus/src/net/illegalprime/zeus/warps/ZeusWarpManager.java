package net.illegalprime.zeus.warps;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public final class ZeusWarpManager {
	private HashMap<String, Location> Guests;
	private HashMap<String, String>	  RudeGuests;
	
	public ZeusWarpManager() { 
		Guests     = new HashMap<String, Location>();
		RudeGuests = new HashMap<String, String>();
	} 
	
	public void invitePlayer(String playerName, Location playerLocation) {
		Guests.put(playerName, playerLocation);
	}
	
	public void gotoPlayer(String playerHost, String playerRudeGuest) {
		RudeGuests.put(playerHost, playerRudeGuest);
	}
	
	public boolean acceptGoto(String playerHost) {
		if (RudeGuests.containsKey(playerHost)) {
			String playerRudeGuest = RudeGuests.get(playerHost);
			Player entityRudeGuest = Bukkit.getPlayerExact(playerRudeGuest); 
			if (entityRudeGuest != null) {
				
				entityRudeGuest.teleport(Bukkit.getPlayerExact(playerHost));
				RudeGuests.remove(playerHost);
				
				return true;
			}
		}
		return false;
	}
	
	public boolean acceptInvite(String playerName) {
		Player entityName = Bukkit.getPlayerExact(playerName); 
		if (entityName != null) {
			
			if (Guests.containsKey(playerName)) {
				entityName.teleport(Guests.get(playerName));
				Guests.remove(playerName);
				return true;
			}
		}
		return false;
	}
}