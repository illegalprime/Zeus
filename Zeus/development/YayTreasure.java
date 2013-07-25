package net.illegalprime.zeus;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.HashMap;

public final class YayTreasure {
	
	HashMap<Location, List<String>> TreasureInventory;
	
	YayTreasure() {
		this.TreasureInventory = new HashMap<Location, List<String>>();
	}
	
	public boolean isTreasureChest(Location treasure_location) {
		return TreasureInventory.containsKey(treasure_location);
	}
	
	public boolean playerHasSpace(Player treasureHunter, ItemStack[] treasure) {
		return treasureHunter.getInventory().getContents().length <= (treasureHunter.getInventory().getSize() - treasure.length); 
	}
	
	public void giveTreasure(Player treasureHunter, ItemStack[] treasure, Location treasureLocation) {
		treasureHunter.getInventory().addItem(treasure);
		addToTreasureInventory(treasureLocation, treasureHunter.getName());
	}

	public void addTreasureChest(Location treasure_location) {
		TreasureInventory.put(treasure_location, null);
	}
	
	public void removeTreasureChest(Location treasure_location) { 
		TreasureInventory.remove(treasure_location);
	}
	
	public void resetTreasureChest(Location treasure_location) {
		TreasureInventory.get(treasure_location).clear();
	}
	
	// TODO:	Find out if this is dangerous or not.
	public void removeAllNonChests() {
		for (Location treasure : TreasureInventory.keySet()) {
			if (treasure.getBlock().getType() != Material.CHEST) {
				TreasureInventory.remove(treasure);
			}
		}
	}
	
	public boolean canReceiveTreasure(Location treasure_location, Player treasure_hunter) {
		return !TreasureInventory.get(treasure_location).contains(treasure_hunter.getName()) || treasure_hunter.isOp();
	}
	
	private void addToTreasureInventory(Location referenceLoc, String player_name) {
		TreasureInventory.get(referenceLoc).add(player_name);
	}
}