package net.illegalprime.zeus;

import java.util.ArrayList;
import java.util.Set;

import net.illegalprime.zeus.jails.ZeusJailManager;
import net.illegalprime.zeus.worldedit.ZeusWERunnable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.sk89q.worldedit.InvalidItemException;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.UnknownItemException;


public class ZeusCommandExecutor implements CommandExecutor {
	private Zeus plugin;
	
	public ZeusCommandExecutor(Zeus plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		int lenArgs = args.length;
		
		if (cmd.getName().equalsIgnoreCase("regionedit")) {
			// TODO: Fix dependency checking for WG & WE
			
			if (lenArgs < 2) {
				sender.sendMessage(ChatColor.RED + "[ZeusWE] /regionedit <region id> (-a|-c|-e|-t|-p <# players>|-d <delay>|) <worldedit command>");
				return true;
			}
			if (args[0].equalsIgnoreCase("help")) {
				sender.sendMessage(ChatColor.RED +
				"Usage: /regionedit <region id> (flags) <worldedit command>" +  "\n" +
				"Flags: -a   Include the air block when copying."            +  "\n" +
				       "-e   Check if command already ran before running it again."   +  "\n" +
				       "-t   Toggle the region between two types of blocks."   +  "\n" +
				       "-p (# of players)  Only run if there are this many players in the Region."   +  "\n" +
				       "-d (delay1,delay2,...) Create delays between commands."   +  "\n" +
				"Commands: set      <block> set region to a block or pattern."    +  "\n" +
				          "replace  <block from> <block to> replace a block with another or a pattern"  +  "\n" +
				          "copyfrom <region from> copy blocks from one region into another");
			}
			
			String concatenatedArgs = "";
			String selectedRegion   = "";
			
			ArrayList<Integer> delays     = new ArrayList<Integer>();
			int playerTolerance     =  0;
			
			boolean regionFound     = false;
			boolean isAir           = false;
			boolean isEffecient     = false;
			boolean isToggle        = false;
			boolean isPlayerAware   = false;
			boolean hasDelay        = false;
			
			for (int i = 0; i < lenArgs; i++) {
				
				args[i] = args[i].replace('=', '-');
				
				if (args[i].equalsIgnoreCase("-a")) {
					isAir = true;
				}
				else if (args[i].equalsIgnoreCase("-c")) {
					plugin.zWorldEdit.cancelDelay(selectedRegion);
					sender.sendMessage(ChatColor.GREEN + "[ZeusWE] Pending commands on " + selectedRegion + " have stopped.");
					return true;
				}
				else if (args[i].equalsIgnoreCase("-e")) {
					isEffecient = true;
				}
				else if (args[i].equalsIgnoreCase("-t")) {
					isToggle = true;
				}
				else if (args[i].equalsIgnoreCase("-p")) {
					isPlayerAware = true;
					try {
						playerTolerance = Integer.parseInt(args[i + 1]);
						i++;
					}
					catch (NumberFormatException e) {}
				}
				else if (args[i].equalsIgnoreCase("-d")) {
					if (plugin.zWorldEdit.regionHasDelaySet(selectedRegion)) {
						sender.sendMessage(ChatColor.RED + "[ZeusWE] Region already has some pending commands.");
						return true;
					}
					for (String litNum : args[++i].split(",")) {
						try {
							delays.add(Integer.parseInt(litNum));
						}
						catch (NumberFormatException e) {
							sender.sendMessage(ChatColor.RED + "[ZeusWE] You must specify your delays: delay1,delay2,...");
							return true;
						}
					}
					hasDelay = true;
				}
				else if (!regionFound) {
					selectedRegion = args[i];
					regionFound = true;
				}
				else {
					concatenatedArgs += (args[i] + " ");
				}
			}
			
			try {
				if (hasDelay) {
					int delay = delays.get(0);
					delays.remove(0);
					plugin.zWorldEdit.logDelay(selectedRegion, Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new ZeusWERunnable(this.plugin, selectedRegion, concatenatedArgs, isAir, isEffecient, isToggle, isPlayerAware, playerTolerance, delays), delay));
				}
				else {
					String err = plugin.zWorldEdit.editregion(selectedRegion, concatenatedArgs, isAir, isEffecient, isToggle, isPlayerAware, playerTolerance, false); 
					if (err != null) {
						sender.sendMessage(ChatColor.RED + "[ZeusWE] " + err);
					}
				}
			} catch (MaxChangedBlocksException e) {
				sender.sendMessage(ChatColor.RED + "[ZeusWE] You tried to change to many blocks!! # Blocks > " + e.getBlockLimit());
			} catch (InvalidItemException e) {
				sender.sendMessage(ChatColor.RED + "[ZeusWE] Invalid syntax for specified item: " + e.getMessage()); 
			} catch (UnknownItemException e) {
				sender.sendMessage(ChatColor.RED + "[ZeusWE] Unknown item: " + e.getMessage());
			}
			return true;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Player expected.");
			return false;
		}
		
		else if (cmd.getName().equalsIgnoreCase("jailtime")) {
			if (plugin.jailMan.prisonerExists(((Player) sender).getName())) {
				sender.sendMessage(ChatColor.RED + "You have " + Integer.toString(plugin.jailMan.getPrisonerTime(((Player) sender).getName())) + " minutes remaining in jail. Good Luck."); 
			}
			else {
				sender.sendMessage(ChatColor.YELLOW + "You are not in jail, therefore your jail time is zero.");
			}
			return true;
		}
		
		else if (cmd.getName().equalsIgnoreCase("invite")) {
			if (lenArgs == 0) {
				if (!plugin.warpMan.acceptGoto( ((Player) sender).getName() )) {
					sender.sendMessage(ChatColor.RED + "No one is requesting to join you.");
				}
			}
			else if (Bukkit.getPlayerExact(args[0]) != null) {
				Bukkit.getPlayerExact(args[0]).sendMessage(ChatColor.YELLOW + ((Player) sender).getName() + " invited you to warp! Type /goto to accept.");
				plugin.warpMan.invitePlayer(args[0], ((Player) sender).getLocation());
				sender.sendMessage(ChatColor.GREEN + "Invited " + args[0] + " to join you here.");
			}
			else {
				sender.sendMessage(ChatColor.RED + "Player not found.");
			}
			return true;
		}
		else if (cmd.getName().equalsIgnoreCase("goto")) {
			if (lenArgs == 0) {
				if (!plugin.warpMan.acceptInvite(((Player) sender).getName())) {
					sender.sendMessage(ChatColor.RED + "No one has invited you to warp.");
				}
			}
			else if (lenArgs == 1) {
				if (Bukkit.getPlayerExact(args[0]) != null) {
					plugin.warpMan.gotoPlayer(args[0], ((Player) sender).getName());
					sender.sendMessage(ChatColor.GREEN + "You have asked to join " + args[0] + " at his location.");
					Bukkit.getPlayerExact(args[0]).sendMessage(ChatColor.YELLOW + ((Player) sender).getName() + " has requested to join you here. Type /invite to accept.");
				}
				else {
					sender.sendMessage(ChatColor.RED + "Could not find player '" + args[0] + "'");
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "/goto <player name>");
			}
			return true;
		}
		
		
		else if (cmd.getName().equalsIgnoreCase("tactical")) {
			if (lenArgs == 0) {
				//if (plugin.tactical.hasStock(((Player) sender).getName())) {
					if ((((Player) sender).getInventory().contains(Material.REDSTONE_TORCH_ON) && 
							((Player) sender).getInventory().contains(Material.EYE_OF_ENDER)) ||
							sender.isOp()) {
						
						if (plugin.tactical.addInsertion(sender.getName(), ((Player) sender).getLocation())) {
							sender.sendMessage(ChatColor.GREEN + "[Tactical] A tactical has been placed at your location. If you die, you respawn here!");
							
							if (!sender.isOp()) {
								((Player) sender).getInventory().removeItem(new ItemStack(Material.REDSTONE_TORCH_ON));
								((Player) sender).getInventory().removeItem(new ItemStack(Material.EYE_OF_ENDER));
							}
						}
						else {
							sender.sendMessage(ChatColor.RED + "[Tactical] You cannot put a tactical there.");
						}
						
					}
					else {
						sender.sendMessage(ChatColor.RED + "[Tactical] 1 Tactical: 1 Redstone Torch & 1 Eye of Ender.");
					}
				//}
				//else {
				//	sender.sendMessage(ChatColor.RED + "[Tactical] You do not have any tactical insertions in stock. Go buy some!");
				//}
			}
			/*else if (args[0].equalsIgnoreCase("view")) {
				int torches = ((Player) sender).getInventory().getItem(Material.REDSTONE_TORCH_ON.getId()).getAmount();
				int eyes    = ((Player) sender).getInventory().getItem(Material.EYE_OF_ENDER.getId()).getAmount();
				
				if (torches > eyes) {
					sender.sendMessage(ChatColor.YELLOW + "[Tactical] You have " + Integer.toString(eyes) + " tactical insertions available.");
				}
				else {
					sender.sendMessage(ChatColor.YELLOW + "[Tactical] You have " + Integer.toString(torches) + " tactical insertions available.");
				}
			}*/
			else if (args[0].equalsIgnoreCase("buy")) {
				
			}
			else if (args[0].equalsIgnoreCase("cost")) {
				//sender.sendMessage(ChatColor.YELLOW + "1 Tactical: $" + plugin.tactical.getItemCost());
				sender.sendMessage(ChatColor.RED + "[Tactical] 1 Tactical: 1 Redstone Torch & 1 Eye of Ender.");
			}
			else {
				sender.sendMessage(ChatColor.RED + "/tactical [|cost]");
			}
			return true;
		}
		else if (cmd.getName().equalsIgnoreCase("book")) {
			Material iteminhand = ((Player) sender).getItemInHand().getType();
			if (iteminhand == Material.BOOK_AND_QUILL || iteminhand == Material.WRITTEN_BOOK) {
				String catBook = "";
				for (String page : ((BookMeta) ((Player) sender).getItemInHand().getItemMeta()).getPages()) {
					catBook += page;
				}
				Bukkit.dispatchCommand(sender, catBook);
				sender.sendMessage(ChatColor.GREEN + "Executed Command: " + catBook);
			}
		}
		
		//-----------------------------------------------------------------//
		//																   //
		//                          ADMIN TIME!!!                          //
		//                                                                 //
		//-----------------------------------------------------------------//
		
		
		if (!(sender.isOp())) {
			sender.sendMessage("[Zeus] You must be an admin to use these commands.");
			return false;
		}
		
		if (cmd.getName().equalsIgnoreCase("zeus")) {
			if (lenArgs == 0) {
				sender.sendMessage("[Zeus] Lightning with STICK set to ON.");
				plugin.lightActive = true;
				plugin.tntActive = false;
			}
			else if (lenArgs == 1) {
				if (args[0].equalsIgnoreCase("tnt")) {
					sender.sendMessage("[Zeus] Lightning with STICK set to ON with TNT.");
					plugin.lightActive = true;
					plugin.tntActive = true;
				}
				else if (args[0].equalsIgnoreCase("off")) {
					sender.sendMessage("[Zeus] Turned everything OFF.");
					plugin.lightActive = false;
					plugin.tntActive = false;
					//plugin.haloActive = false;	TODO: Make halographic feature
					//plugin.cmdActive = false;		TODO: Make cmd stream.
				}
				else if (args[0].equalsIgnoreCase("help")) {
					sender.sendMessage("--- Zeus Help ---\n" + 
					"zeus     - turn on lightning.\n" + 
					"zeus tnt - turn on with tnt.\n" + 
					"zeus off - turn everything off.\n" + 
					"--- Redstone Command --- \n" + 
					"zCommPlayer / zCP  - Player\n" + 
					"zCommServer / zCS  - Server\n" + 
					"--- Zeus Jump ---\n" +
					"[ZeusJump]; x; y; z;\n" + 
					"--- Invite/Goto ---\n" + 
					"/invite <player name>; /goto" 
					);
				}
			}
		}
		else if (cmd.getName().equalsIgnoreCase("jail")) {
			ZeusJailManager jailManager = plugin.jailMan;
			if (lenArgs == 0) {
				sender.sendMessage(ChatColor.RED + "[Zeus] Not enough arguments!");
				return true;
			}
			if (args[0].equalsIgnoreCase("release")) {
				if (lenArgs != 2) {
					sender.sendMessage(ChatColor.RED + "[Zeus] This command takes 2 arguments exactly");
				}
				else if (jailManager.prisonerExists(args[1])) {
					if (plugin.getServer().getPlayerExact(args[1]) instanceof Player) {
						jailManager.releasePrisoner(args[1]);
						sender.sendMessage(ChatColor.GREEN + "[Zeus] Player '" + args[1] + "' has been released.");
					}
					else {
						jailManager.timeoutPrisoner(args[1]);
 						sender.sendMessage(ChatColor.GREEN + "[Zeus] Player not online, the jail time of '" + args[1] + "' has been waived.");
					}
				}
				else {
					sender.sendMessage(ChatColor.RED + "[Zeus] Could not find player in jail records.");
				}
			}
			else if (args[0].equalsIgnoreCase("view")) {
				if (lenArgs == 2) {
					if (args[1].equalsIgnoreCase("prisoners")) {
						Set<String> prisonerNames = jailManager.getPrisonerNames();
						for (String prisonerN : prisonerNames) {
							sender.sendMessage(prisonerN + " for " + jailManager.getPrisonerTime(prisonerN) + " min. @ " + jailManager.getJailOfPrisoner(prisonerN));
						}
					}
					else if (args[1].equalsIgnoreCase("jails")) {
						Set<String> jailNames = jailManager.getJailNames();
						for (String jailN : jailNames) {
							sender.sendMessage(jailN + " - " + Integer.toString(jailManager.getNumberOfCells(jailN)) + " Cells");
						}
					}
					else if (args[1].equalsIgnoreCase("spawn")) {
						((Player) sender).teleport(jailManager.getSpawnLocation());
					}
					else {
						sender.sendMessage(ChatColor.RED + "Choose between viewing jails or prisoners or spawn.");
					}
				}
				else {
					sender.sendMessage(ChatColor.RED + "Tooo Many Arguments, or to little arguments.");
				}
			}
			else if (args[0].equalsIgnoreCase("help")) {
				sender.sendMessage("--- Jail Manager ---\n" +  
								   "jail <player> (<jail>) (<time>)\n" + 
								   "jail set [<jail>|<time>|spawn]\n" + 
								   "jail view [jails|prisoners|spawn]\n" + 
								   "jail release <player>\n" + 
								   "jail save\n" + 
								   "jailtime\n" + 
								   "--- Jail Signs ---\n" + 
								   "[Jail Cell]; <Jail Name>; <Occupancy>; (; is new line)\n" +
								   "Defaults are replaced with blank lines, fourth is ID!\n"
				);
			}
			else if (args[0].equalsIgnoreCase("save")) {
				plugin.jailMan.saveDATA();
				plugin.saveConfig();
				sender.sendMessage(ChatColor.GREEN + "[Zeus] Saved jail information.");
			}
			else if (args[0].equalsIgnoreCase("set")) {
				if (lenArgs != 2) {
					sender.sendMessage(ChatColor.RED + "[Zeus] This command takes 2 arguments exactly");
					return true;
				}
				if (args[1].equalsIgnoreCase("spawn")) {
					jailManager.setSpawnLocation(((Player)sender).getLocation());
					sender.sendMessage(ChatColor.GREEN + "[Zeus] Set default spawn location to your position");
				}
				else if (plugin.isInteger(args[1])) {
					jailManager.setDefaultTime(Integer.parseInt(args[1]));
					sender.sendMessage(ChatColor.GREEN + "[Zeus] Set default jail time to " + args[1] + " minutes.");
				}
				else {
					jailManager.setDefaultJail(args[1]);
					sender.sendMessage(ChatColor.GREEN + "[Zeus] Set default jail to " + args[1] + ".");
				}
			}
			else {
				if (plugin.getServer().getPlayerExact(args[0]) == null) {
					sender.sendMessage(ChatColor.RED + "[Zeus] No such player online.");
					return true;
				}
				if (jailManager.prisonerExists(args[0])) {
					sender.sendMessage(ChatColor.YELLOW + "[Zeus] Player '" + args[0] + "' already jailed.");
					return true;
				}
				String jailName = jailManager.getDefaultJail();
				int    jailTime = jailManager.getDefaultTime();
				if (lenArgs > 1) {
					if (plugin.isInteger(args[1])) {
						jailTime = Integer.valueOf(args[1]);
					}
					else {
						jailName = args[1];
					}
					if (lenArgs > 2) {
						if (plugin.isInteger(args[2])) {
							jailTime = Integer.valueOf(args[2]);
						}
						else {
							jailName = args[2];
						}
					}
					else if (lenArgs > 3) {
						sender.sendMessage(ChatColor.RED + "[Zeus] Too many arguments!");
						return true;
					}
				}
				if (!jailManager.jailExists(jailName)) {
					sender.sendMessage(ChatColor.RED + "[Zeus] Jail '" + jailManager.getDefaultJail() + "' does not exist.");
				}
				else if (!jailManager.jail(args[0], jailName, jailTime)) {
					sender.sendMessage(ChatColor.YELLOW + "[Zeus] Jail is Full.");
				}
				else {
					sender.sendMessage(ChatColor.GREEN + "[Zeus] Player Jailed.");
				}
				return true;
			}
		}
		else if (cmd.getName().equalsIgnoreCase("autosave")) {
			if (lenArgs == 0) {
				sender.sendMessage("[Zeus] The autosave interval is set to " + Integer.toString(plugin.autosaveTime) + " minutes.");
			}
			else if (lenArgs == 1){
				if (plugin.isInteger(args[0])) {
					plugin.autosaveTime = Integer.valueOf(args[0]);
					plugin.autosaveActive = true;
					plugin.newAutosave();
					
					sender.sendMessage("[Zeus] The autosave interval is set to " + Integer.toString(plugin.autosaveTime) + " minutes.");
				}
				else if (args[0].equalsIgnoreCase("disable")) {
					plugin.autosaveActive = false;
					plugin.cancelAutosave();
					
					sender.sendMessage("[Zeus] Autosave has been disabled.");
				}
				else {
					sender.sendMessage("/autosave [<save interval>|disable|]");
				}
			}
		}
		return true;
	}
}