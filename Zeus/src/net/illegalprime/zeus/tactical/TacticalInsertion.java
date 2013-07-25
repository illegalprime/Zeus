package net.illegalprime.zeus.tactical;

import java.util.HashMap;
import java.util.Set;

import net.illegalprime.zeus.Zeus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class TacticalInsertion {
	
	private HashMap<String, Location> Insertions;
	//private HashMap<String, Integer>  Stock;
	
	//private final int    itemCost         = 500;
	private final String tacticalSignText = ChatColor.DARK_RED + "[Tactical]" + ChatColor.DARK_RED + "[Insertion]";
	private final String useMessage       = ChatColor.GREEN + "[Tactical] Used tactical insertion.";
	//private final String shopSignText     = "[ZeusShop]";
	
	private final Zeus plugin;
	
	public TacticalInsertion(Zeus plugin) {
		this.plugin = plugin;
		Insertions = new HashMap<String, Location>();
		//Stock      = new HashMap<String, Integer>();
		
		this.loadDATA();
	}
	
	public void saveDATA() {
		this.plugin.getConfig().set("ZeusTactical.Insertions", null);
		
		Set<String> player_names = Insertions.keySet();
		for (String player_name : player_names) {
			this.plugin.getConfig().set("ZeusTactical.Insertions." + player_name + ".location", Insertions.get(player_name).toVector());
			this.plugin.getConfig().set("ZeusTactical.Insertions." + player_name + ".world", Insertions.get(player_name).getWorld().getName());
		}
	}
	
	public void loadDATA() {
		if (!plugin.getConfig().contains("ZeusTactical.Insertions")) return;
		
		Set<String> player_names = plugin.getConfig().getConfigurationSection("ZeusTactical.Insertions").getKeys(false);
		if (player_names == null) return;
		
		for (String player_name : player_names) {
			Vector insertion_location = plugin.getConfig().getVector("ZeusTactical.Insertions." + player_name + ".location");
			String insertion_world    = plugin.getConfig().getString("ZeusTactical.Insertions." + player_name + ".world");
			
			Insertions.put(player_name, insertion_location.toLocation(Bukkit.getWorld(insertion_world)));
		}
	}
	
	//	Called on Player Respawn Event
	public void useInsertion(Player player) {
			//  Remove insertion, event will handle respawn location.
			this.removeInsertion(player.getName());
			this.sendUseMessage(player);
			
	}
	
	//  Called on Player Buy Event
	public void buyInsertion(String player_name, Integer amount) {
		
		
		//Stock.put(player_name, Stock.get(player_name) + amount);
	}
	
	//  Called on Player Right Click Event.
	public boolean addInsertion(String player_name, Location tactical_location) {
		//  If player already has one insertion..
		BlockFace sign_direction = canPlaceInsertion(tactical_location);
		if (sign_direction == BlockFace.SELF) {
			//Bukkit.broadcastMessage(" No we cannot. ");
			return false;							// Placement Unsuccessful
		}
		
		if (Insertions.containsKey(player_name)) {
			this.removeInsertion(player_name);
		}
		
		//  Place it in the world.
		if (placeInsertion(player_name, tactical_location, sign_direction)) {
			//Bukkit.broadcastMessage(" Reached adding insertion. ");
			//  Add this new tactical to the index.
			Insertions.put(player_name, tactical_location);
			//Bukkit.broadcastMessage(" added and decremented stock. ");
			//  Remove one stock from player.
			//decrementStock(player_name);
			return true;
		}
		//Bukkit.broadcastMessage(" no place to put torch. ");
		return false;
	}
	
	private boolean placeInsertion(String player_name, Location tactical_location, BlockFace sign_direction) {
		//Bukkit.broadcastMessage(" can we place an insertion? ");
		
		//Bukkit.broadcastMessage(" Yes we Can! ");
		//  Creates a torch
		tactical_location.getBlock().setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(), (byte) 0x6, true);
		tactical_location.getBlock().getState().update();
		//Bukkit.broadcastMessage(" Created Torch ");
		//  Sets type of block and direction of the Wall Sign
		
		Block sign_block = tactical_location.add(sign_direction.getModX(), 0, sign_direction.getModZ()).getBlock();
		sign_block.setTypeIdAndData(Material.WALL_SIGN.getId(), (byte)(Math.ceil(((2.5 + ((float)sign_direction.getModZ()/2)) + (4.5 + ((float)sign_direction.getModX()/2))))), false);
		Sign sign_state = ((Sign) sign_block.getState());
		
		sign_state.setLine(0, ChatColor.DARK_RED + "[Tactical]");
		sign_state.setLine(1, ChatColor.DARK_RED + "[Insertion]");
		sign_state.setLine(3, player_name);
		
		tactical_location.add(-sign_direction.getModX(), 0, -sign_direction.getModZ());
		sign_state.update();
		//Bukkit.broadcastMessage("Created Sign");
		
		return true;
	}
	
	private BlockFace canPlaceInsertion(Location tactical_location) {
		//Bukkit.broadcastMessage(tactical_location.getBlock().getType().toString());
		if (tactical_location.getBlock().getType() == Material.AIR && !tactical_location.add(0, -1, 0).getBlock().isBlockPowered()) {
			if (tactical_location.add(0, 1, 1).getBlock().getType() == Material.AIR) {
				tactical_location.add(0, 0, -1);
				return BlockFace.SOUTH;
			}
			else if (tactical_location.add(0, 0, -2).getBlock().getType() == Material.AIR) {
				tactical_location.add(0, 0, 1);
				return BlockFace.NORTH;
			}
			else if (tactical_location.add(1, 0, 1).getBlock().getType() == Material.AIR) {
				tactical_location.add(-1, 0, 0);
				return BlockFace.EAST;
			}
			else if (tactical_location.add(-2, 0, 0).getBlock().getType() == Material.AIR) {
				tactical_location.add(1, 0, 0);
				return BlockFace.WEST;
			}
			else {
				tactical_location.add(1, 0, 0);
				return BlockFace.SELF;  	// No Space around the torch.
			}
		}
		else {
			tactical_location.add(0, 1, 0);
			return BlockFace.SELF;  	    // There is a block in the way!
		}
	}
	
	private void removeInsertion(String player_name) {
		
		destroyInsertion(Insertions.get(player_name));
		Insertions.remove(player_name);
		
		return;
	}
	
	private void destroyInsertion(Location tactical_location) {
		BlockFace sign_direction = isInsertion(tactical_location);
		if (sign_direction == BlockFace.SELF) {
			return;
		}
		
		tactical_location.add(sign_direction.getModX(), 0, sign_direction.getModZ()).getBlock().setType(Material.AIR);
		tactical_location.add(-sign_direction.getModX(), 0, -sign_direction.getModZ());
		tactical_location.getBlock().setType(Material.AIR);
		return;
	}
	
	private BlockFace isInsertion(Location tactical_location) {
		BlockFace sign_side;
		Sign      tactical_sign;
		
		if (tactical_location.getBlock().getType() == Material.REDSTONE_TORCH_ON ||
				tactical_location.getBlock().getType() == Material.AIR) {
			if (tactical_location.add(0, 0, 1).getBlock().getType() == Material.WALL_SIGN) {
				tactical_sign = (Sign) tactical_location.getBlock().getState();
				sign_side =  BlockFace.SOUTH;
				tactical_location.add(0, 0, -1);
			}
			else if (tactical_location.add(0, 0, -2).getBlock().getType() == Material.WALL_SIGN) {
				tactical_sign = (Sign) tactical_location.getBlock().getState();
				sign_side =  BlockFace.NORTH;
				tactical_location.add(0, 0, 1);
			}
			else if (tactical_location.add(1, 0, 1).getBlock().getType() == Material.WALL_SIGN) {
				tactical_sign = (Sign) tactical_location.getBlock().getState();
				sign_side =  BlockFace.EAST;
				tactical_location.add(-1, 0, 0);
			}
			else if (tactical_location.add(-2, 0, 0).getBlock().getType() == Material.WALL_SIGN) {
				tactical_sign = (Sign) tactical_location.getBlock().getState();
				sign_side =  BlockFace.WEST;
				tactical_location.add(1, 0, 0);
			}
			else {
				tactical_location.add(1, 0, 0);
				return BlockFace.SELF;  // No Sign around torch.
			}
		}
		else {
			return BlockFace.SELF;  	// No torch.
		}
		
		if (isTacticalSign(tactical_sign)) {
			return sign_side;
		}
		else {
			return BlockFace.SELF;
		}
	}
	
	private boolean isTacticalSign(Sign sign_block) {
		return this.tacticalSignText.equalsIgnoreCase(sign_block.getLine(0) + sign_block.getLine(1));
	}
	
	public boolean hasInsertion(String player_name) {
		return Insertions.containsKey(player_name);
	}
	
	public Location getNewSpawnLocation(String player_name) {
		return Insertions.get(player_name);
	}
	
	/*
	public boolean isShopSign(Sign sign_block) {
		return this.shopSignText.equalsIgnoreCase(sign_block.getLine(0) + sign_block.getLine(1));
	}
	
	public int getStock(String player_name) {
		if (Stock.containsKey(player_name)) {
			return Stock.get(player_name);
		}
		return 0;
	}
	
	public boolean hasStock(String player_name) {
		return (Stock.containsKey(player_name) || this.isOp(player_name));
	}

	private void decrementStock(String player_name) {
		if (this.isOp(player_name)) {
			return;
		}
		if (Stock.get(player_name) < 2) {
			Stock.remove(player_name);
		}
		else {
			Stock.put(player_name, Stock.get(player_name) - 1);
		}
	}
	
	private boolean isOp(String player_name) {
		return Bukkit.getPlayerExact(player_name).isOp();
	}
	
	public int getItemCost() {
		return this.itemCost;
	}
	*/
	
	public void sendUseMessage(Player player) {
		player.sendMessage(this.useMessage);
	}
}