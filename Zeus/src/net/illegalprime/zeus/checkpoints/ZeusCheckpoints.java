package net.illegalprime.zeus.checkpoints;

import java.util.HashMap;

import net.illegalprime.zeus.Zeus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.sk89q.worldguard.bukkit.WGBukkit;

public class ZeusCheckpoints implements CommandExecutor, Listener {
	
	private HashMap<String, Location> Checks  = new HashMap<String, Location>(); // Players
	private HashMap<String, Location> Regions = new HashMap<String, Location>(); // Check Points
	private Zeus plugin;
	
	public ZeusCheckpoints(Zeus plugin) {
		this.plugin = plugin;
		loadDATA();
	}
	
	private void loadDATA() {
		// Regions
		if (!plugin.getConfig().contains("ZeusCheckpoints.Regions")) return;
		for (String region_id : this.plugin.getConfig().getConfigurationSection("ZeusCheckpoints.Regions").getKeys(false)) {			
			Location loc = getLocation("ZeusCheckpoints.Regions." + region_id);
			if (loc != null)
				addRegion(region_id, loc);
		}
		
		// Players
		if (!plugin.getConfig().contains("ZeusCheckpoints.Checks")) return;
		for (String player_name : this.plugin.getConfig().getConfigurationSection("ZeusCheckpoints.Checks").getKeys(false)) {
			Location loc = getLocation("ZeusCheckpoints.Checks." + player_name);
			if (loc != null)
				setCheck(player_name, loc);
		}
	}
	
	public void saveDATA() {
		this.plugin.getConfig().set("ZeusCheckpoints", null);
		
		// Regions
		for (String region_id : Regions.keySet()) {
			setLocation("ZeusCheckpoints.Regions." + region_id, Regions.get(region_id));
		}
		
		// Players
		for (String player_name : Checks.keySet()) {
			setLocation("ZeusCheckpoints.Checks." + player_name, Checks.get(player_name));
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String cmd_name = cmd.getName();
		int    args_len = args.length;
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "[Checkpoints] Player expected.");
			return false;
		}
		if (cmd_name.equals("giveup")) {
			clearCheck(((Player) sender).getName());
			((Player) sender).setHealth(0);
			return true;
		}
		if (!((Player) sender).isOp()) {
			sender.sendMessage(ChatColor.RED + "[Checkpoints] Need to be OP.");
			return false;
		}
	
		if (cmd_name.equals("checkpoint")) {
			if (args_len >= 2) {
				if (args[0].equals("set")) {
					if (regionExists(args[1])) {
						
						if (args_len == 3) {
							
							if (args[2].equals("purge")) {
								addRegion(args[1], new Location(((Player) sender).getLocation(), Location.PURGE_REGION));
								sender.sendMessage(ChatColor.GREEN + "[Checkpoints] Checkpoints of player will be lost upon entering region: '" + args[1] + "'");
							}
							else if (args[2].equals("qwarp")) {
								addRegion(args[1], new Location(((Player) sender).getLocation(), Location.QUIET_WARP));
								sender.sendMessage(ChatColor.GREEN + "[Checkpoints] A player will warp quietly to the Checkpoint once he enters.");
							}
							else {
								sender.sendMessage(ChatColor.RED + "[Checkpoints] Not recognized.. Options: 'purge' or 'qwarp'");
							}
							
						}
						else {
							addRegion(args[1], new Location(((Player) sender).getLocation(), Location.CHECKPOINT));
							
							sender.sendMessage(ChatColor.GREEN + "[Checkpoints] If a player enters region: '" + args[1] + "', he will respawn here!");
						}
						return true;
					}
					else {
						
						sender.sendMessage(ChatColor.RED + "[Checkpoints] Region " + args[1] + " does not exist.");
						return true;
					}
				}
				else if (args[0].equals("unset")){
					removeRegion(args[1]);
					
					sender.sendMessage(ChatColor.GREEN + "[Checkpoints] Removed checkpoint of region " + args[1]);
					return true;
				}
			}
			
		sender.sendMessage(ChatColor.RED + "[Checkpoints] /checkpoint [set|unset] <region_id> (purge)");
		return true;
		}
		
		return false;
		
	}
	
	@EventHandler
	public void onRegionEnter(RegionEnterEvent event) {
		if (Regions.containsKey(event.getRegion().getId())) {
			Location checkpoint = Regions.get(event.getRegion().getId());
			
 			switch (checkpoint.tag) {
 				case Location.CHECKPOINT:
 					setCheck(event.getPlayer().getName(), checkpoint);
 					break;
 				case Location.PURGE_REGION:
 					clearCheck(event.getPlayer().getName());
					break;
				case Location.QUIET_WARP:
					event.getPlayer().teleport(checkpoint);
					break;
				default:
					setCheck(event.getPlayer().getName(), checkpoint);
 			}
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Location respawn = getCheck(event.getPlayer().getName());
		if (respawn != null) {
			event.setRespawnLocation(respawn);
			event.getPlayer().sendMessage(ChatColor.YELLOW + "[Checkpoints] To clear your checkpoints and respawn use:\n" + 
					                                         "/giveup");
		}
	}
	
	private void setCheck(String player, Location loc) {
		Checks.put(player, loc);
	}
	
	private Location getCheck(String player) {
		return Checks.get(player);
	}
	
	private void clearCheck(String player) {
		Checks.remove(player);
	}
	
	private void addRegion(String region_id, Location check_loc) {
		Regions.put(region_id, check_loc);
	}
	
	private void removeRegion(String region_id) {
		Regions.remove(region_id);
	}
	
	private boolean regionExists(String region_id) {
		for (World world : plugin.getServer().getWorlds()) {
			if (WGBukkit.getRegionManager(world).hasRegion(region_id)) {
				return true;
			}
		}
		
		return false;
	}
	
	private void setLocation(String path, Location location) {
		plugin.getConfig().set(path, String.format("%s,%s,%s,%s,%s,%s,%s",
				location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), location.tag ));
	}

	private Location getLocation(String path) {
		String[] location = plugin.getConfig().getString(path).split(",");
		return new Location( Bukkit.getWorld(location[0]),
							 Double.parseDouble(location[1]), Double.parseDouble(location[2]), Double.parseDouble(location[3]),
							 Float.parseFloat(location[4]), Float.parseFloat(location[5]),
							 Integer.parseInt(location[6]));
	}
	
	protected class Location extends org.bukkit.Location {
		public int tag;
		public static final int CHECKPOINT   = 0;
		public static final int PURGE_REGION = 1;
		public static final int QUIET_WARP   = 2;
		
		public Location(org.bukkit.Location location, int tag) {
			super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
			this.tag = tag;
		}

		public Location(World world, double x, double y, double z, float yaw, float pitch, int tag) {
			super(world, x, y, z, yaw, pitch);
			this.tag = tag;
		}
	}
}