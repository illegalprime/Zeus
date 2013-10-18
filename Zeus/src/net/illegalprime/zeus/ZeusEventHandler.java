package net.illegalprime.zeus;

import java.util.HashMap;

import net.illegalprime.zeus.runnables.ZeusGamemodeRunnable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.material.Button;

public class ZeusEventHandler implements Listener {
	public Zeus plugin;
	
	private HashMap<Location, Boolean> commandSigns;

	public ZeusEventHandler(Zeus plugin) {
		this.plugin = plugin;
		
		this.commandSigns = new HashMap<Location, Boolean>();
	}

	public void tryCommand(BlockState currBlockState) {
		String command = 	((Sign) currBlockState).getLine(0).substring(5) +
							((Sign) currBlockState).getLine(1) +
							((Sign) currBlockState).getLine(2) +
							((Sign) currBlockState).getLine(3);
		try {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
		} catch (CommandException ex) {
			Bukkit.getLogger().info( "[Zeus] Could not run command: " + command);
		}
	}

	public void tryCommand(BlockState currBlockState, Player playerSender) {
		if (currBlockState instanceof Sign) {
			String command = 	((Sign) currBlockState).getLine(0).substring(5) +
					((Sign) currBlockState).getLine(1) +
					((Sign) currBlockState).getLine(2) +
					((Sign) currBlockState).getLine(3);
			try {
				playerSender.performCommand(command);
			} catch (CommandException ex) {
				playerSender.sendMessage( "[Zeus] Could not run command: " + command);
			}
		}
	}

	@EventHandler
	public void onPlayerInteractBlock(PlayerInteractEvent evt) {
		
		if (evt.getAction() == Action.PHYSICAL) {
			Block pressurePlate = evt.getClickedBlock();
			if (pressurePlate.getState().getType() == Material.STONE_PLATE
					|| pressurePlate.getState().getType() == Material.WOOD_PLATE) {
					Block maybeSign = pressurePlate.getLocation().add(0, -2, 0)
							.getBlock();
					
					if ((maybeSign.getState() instanceof Sign)) {
						Sign signBlock = ((Sign) maybeSign.getState());
						String signLine = signBlock.getLine(0);
						
						if (plugin.isJumpSign(signLine)) {
							float Velocities[] = {plugin.signInteger(signBlock.getLine(1)), plugin.signInteger(signBlock.getLine(2)), plugin.signInteger(signBlock.getLine(3))};
							evt.getPlayer().setVelocity(new Vector(Velocities[0], Velocities[1], Velocities[2]));
						}
					}
			}
			return;
		}
			
		if (!evt.getPlayer().isOp()) { return; }
		
		//////////////////////////////////////////////////////////////
		//															//
		//					ADMIN TIME!!!							//
		//															//
		//////////////////////////////////////////////////////////////		
		
		Block block = evt.getPlayer().getTargetBlock(null, 200);
		Location location = block.getLocation();
		World world = evt.getPlayer().getWorld();
		if (evt.getPlayer().getItemInHand().getTypeId() == Material.STICK
				.getId()) {
			if (plugin.lightActive) {
				world.strikeLightning(location);
				if (plugin.tntActive) {
					world.createExplosion(location, 2);
				}
			}
		}
		
		else if (!evt.hasBlock()) {
			return;
		}

		else if ((evt.getClickedBlock().getState().getType() == Material.STONE_BUTTON)
				|| (evt.getClickedBlock().getState().getType() == Material.WOOD_BUTTON)) {
			BlockFace initialFace = ((Button) block.getState().getData())
					.getAttachedFace();
			if (initialFace.getModX() == 0) {
				int movZ = initialFace.getModZ();
				BlockState currBlock = world.getBlockAt(
						block.getLocation().add(-1, 0, movZ)).getState();
				this.tryCommand(currBlock, evt.getPlayer());
				currBlock = world.getBlockAt(
						block.getLocation().add(0, 0, 2 * movZ)).getState();
				this.tryCommand(currBlock, evt.getPlayer());
				currBlock = world.getBlockAt(
						block.getLocation().add(1, 0, movZ)).getState();
				this.tryCommand(currBlock, evt.getPlayer());
				currBlock = world.getBlockAt(
						block.getLocation().add(0, 1, movZ)).getState();
				this.tryCommand(currBlock, evt.getPlayer());
			} else {
				int movX = initialFace.getModX();
				BlockState currBlock = world.getBlockAt(
						block.getLocation().add(movX, 0, -1)).getState();
				this.tryCommand(currBlock, evt.getPlayer());
				currBlock = world.getBlockAt(
						block.getLocation().add(2 * movX, 0, 0)).getState();
				this.tryCommand(currBlock, evt.getPlayer());
				currBlock = world.getBlockAt(
						block.getLocation().add(movX, 0, 1)).getState();
				this.tryCommand(currBlock, evt.getPlayer());
				currBlock = world.getBlockAt(
						block.getLocation().add(movX, 1, 0)).getState();
				this.tryCommand(currBlock, evt.getPlayer());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (event.getPlayer().isOp()) {
			event.getPlayer().setGameMode(GameMode.CREATIVE);
		}
		
		//  Handle Tactical Insertions:
		if (plugin.tactical.hasInsertion(event.getPlayer().getName())) {
			event.setRespawnLocation(plugin.tactical.getNewSpawnLocation(event.getPlayer().getName()));
			plugin.tactical.useInsertion(event.getPlayer());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		if (event.getPlayer().isOp()) {
			event.getPlayer().setGameMode(GameMode.CREATIVE);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		if (event.getPlayer().isOp()) {
			plugin.getServer()
					.getScheduler()
					.scheduleSyncDelayedTask(this.plugin,
							new ZeusGamemodeRunnable(event.getPlayer()), 20);
			// event.getPlayer().setGameMode(GameMode.CREATIVE);
		}
	}

	@EventHandler
	public void onBlockPhysicsChange(BlockPhysicsEvent event) {
		if (event.getBlock().getState() instanceof Sign) {
			Sign droppedSign = (Sign) event.getBlock().getState();
			if (plugin.jailMan.isJailSign(droppedSign.getLine(0))) {
				if (plugin.jailMan.jailExists(droppedSign.getLine(1))) {
					plugin.jailMan.destroyCell(droppedSign.getLine(1),
							Integer.valueOf(droppedSign.getLine(2)));
					plugin.getLogger().info("[Zeus] Cell Destroyed");
				}
			}
		}

		/*
		 * Block pressurePlate = event.getBlock(); if
		 * (pressurePlate.getState().getType() == Material.WOOD_PLATE ||
		 * pressurePlate.getState().getType() == Material.STONE_PLATE) {
		 * 
		 * }
		 */
	}

	@EventHandler
	public void onSignChange(SignChangeEvent evt) {
		// Check if new sign pertains to plugin & if the second line contains
		// anything.
		String signLine = evt.getLine(0);
		try {
		if ((!plugin.jailMan.isJailSign(signLine)
				&& !plugin.isCommBlock(signLine.substring(0, 5))
				&& !plugin.isUnlimitedSign(signLine)
				&& !plugin.isJumpSign(signLine))
				|| evt.getLine(1) == null
				|| evt.getLine(1) == "")
			return;
		} catch (IndexOutOfBoundsException error) {}

		if (!evt.getPlayer().isOp()) {
			evt.getPlayer().sendMessage(
					ChatColor.RED + "You don't want no part of these signs!");
			evt.getBlock().breakNaturally();
			return;
		}

		if (plugin.isCommBlock(evt.getLine(0).substring(0, 5))) {
			// Check if the second line is a real command.
			String command = evt.getLine(0).substring(5) + evt.getLine(1) + evt.getLine(2) + evt.getLine(3);
			String commName;

			if (command.indexOf(' ') == -1)
				commName = command;
			else
				commName = command.substring(0, command.indexOf(' '));

			if (plugin.commExists(commName)) {
				// If so, notify the player and keep the sign.
				evt.getPlayer().sendMessage(
						ChatColor.GREEN
								+ "[Zeus] [Active] Added command sign: '"
								+ command + "'");
				return;
			}
			// If not, break the sign and try again!
			evt.setCancelled(true);
			evt.getBlock().breakNaturally();
			evt.getPlayer().sendMessage("[Zeus] [Error] No such command.");
			return;
			// All signs which still exist are either not part of this plugin,
			// or are syntactically correct.
		} else if (plugin.jailMan.isJailSign(evt.getLine(0))) { // For the jail
																// manager.
			String jailName = evt.getLine(1);
			if (jailName == null || jailName.equalsIgnoreCase("")
					|| jailName == (String) null) {
				jailName = plugin.jailMan.getDefaultJail();
				evt.setLine(1, jailName);
				evt.getPlayer().sendMessage(
						ChatColor.YELLOW + "[Zeus] Using Default Jail: "
								+ jailName);
			} else if (!plugin.jailMan.jailExists(jailName)) {
				evt.getPlayer().sendMessage(
						ChatColor.YELLOW + "[Zeus] Will add new jail: "
								+ evt.getLine(1));
			}

			Location currCellLoc = evt.getBlock().getLocation();
			if (evt.getBlock().getType() == Material.WALL_SIGN) {
				BlockFace signFacing = ((org.bukkit.material.Sign) evt
						.getBlock().getState().getData()).getFacing();
				currCellLoc.add(new Vector(signFacing.getModX(), signFacing
						.getModY() - 1, signFacing.getModZ())); // TODO 
			}

			if (plugin.isInteger(evt.getLine(2))) {
				plugin.jailMan.addCell(currCellLoc, jailName,
						Integer.valueOf(evt.getLine(2)));
			} else {
				String defOcc = Integer.toString(plugin.jailMan
						.getDefaultNumber());
				evt.getPlayer().sendMessage(
						ChatColor.YELLOW + "[Zeus] Using Default Occupancy: "
								+ defOcc);
				evt.setLine(2, defOcc);
				plugin.jailMan.addCell(currCellLoc, jailName,
						plugin.jailMan.getDefaultNumber());
			}
			evt.setLine(3, Integer.toString(plugin.jailMan.nextCell(jailName)));
			evt.getPlayer().sendMessage(
					ChatColor.GREEN + "[Zeus] Jail Cell Added.");
		} else if (plugin.isUnlimitedSign(evt.getLine(0))) {
			evt.getPlayer()
					.sendMessage(
							ChatColor.GREEN
									+ "[Zeus] Place this sign under a dispenser for unlimited items!");
		}

	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (!(event.getBlock().getState() instanceof Sign)) {
			return;
		}
		Sign cSign = (Sign) event.getBlock().getState();
		if (!plugin.jailMan.isJailSign(cSign.getLine(0))) {
			return;
		}

		String jailName = cSign.getLine(1);
		plugin.jailMan.destroyCell(jailName,
				Integer.valueOf(cSign.getLine(3)) - 1);
		event.getPlayer().sendMessage(
				ChatColor.GREEN + "[Zeus] Cell Destroyed.");
		if (!plugin.jailMan.jailExists(jailName)) {
			event.getPlayer().sendMessage(
					ChatColor.GREEN + "[Zues] Jail " + jailName
							+ " has been destroyed.");
		}
	}

	@EventHandler
	public void onBlockDispense(BlockDispenseEvent event) {
		if (event.getBlock().getType() == Material.DISPENSER) {
			Dispenser diq = (Dispenser) event.getBlock().getState();
			Block maybeSign = event
					.getBlock()
					.getWorld()
					.getBlockAt(event.getBlock().getX(),
							event.getBlock().getY() - 1,
							event.getBlock().getZ()); // TODO
			if (maybeSign.getState() instanceof Sign) {
				if (plugin.isUnlimitedSign(((Sign) maybeSign.getState())
						.getLine(0))) {
					diq.getInventory().addItem(new ItemStack(event.getItem()));
				}
			}
		}
	}

	@EventHandler
	public void onBlockRedstoneChange(BlockRedstoneEvent evt) {
		if (!(evt.getBlock().getState() instanceof Sign))
			return;
		Sign siq = (Sign) evt.getBlock().getState();
		try {
			if (!plugin.isCommBlock(siq.getLine(0).substring(0, 5)) || plugin.isSenderPlayer(siq.getLine(0).substring(0, 5)))
				return;
		} catch (IndexOutOfBoundsException error) {}
		
		if (evt.getBlock().isBlockIndirectlyPowered()
				|| evt.getBlock().isBlockPowered()) {
			
			Boolean hasRun = commandSigns.get(siq.getLocation());
			if (hasRun == null || !hasRun) {
				this.tryCommand(evt.getBlock().getState());
				commandSigns.put(siq.getLocation(), true);
			}
		}
		else {
			commandSigns.put(siq.getLocation(), false);
		}
	}
	
	/*
	@EventHandler
	public void onInventoryOpenEvent(InventoryOpenEvent event) {
		if (!(event.getInventory().getHolder() instanceof Chest || event.getInventory().getHolder() instanceof DoubleChest)) {
			return; }
		
		Block treasureChest = (Block)  event.getInventory().getHolder();
		
		if (plugin.Treasure.isTreasureChest(treasureChest.getLocation())) {
			Player 	    treasureMan     = (Player) event.getPlayer();
			ItemStack[] playerInventory =          event.getInventory().getContents();
			
			if (plugin.Treasure.playerHasSpace(treasureMan, playerInventory)) {
				plugin.Treasure.giveTreasure(treasureMan, playerInventory, treasureChest.getLocation());
				
				String contents = new String();
				for (ItemStack trItem : playerInventory) {
					contents += trItem.toString() + ", "; }
				treasureMan.sendMessage(ChatColor.GREEN + "You discovered: " + contents + "as treasure!");
			}
			else {
				treasureMan.sendMessage(ChatColor.RED + "You do not have enough space in your inventory to receive the treasure!");
			}
		}
	}
	*/
}
