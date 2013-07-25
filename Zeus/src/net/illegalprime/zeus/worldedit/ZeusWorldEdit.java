package net.illegalprime.zeus.worldedit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.InvalidItemException;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.MobType;
import com.sk89q.worldedit.UnknownItemException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.blocks.NoteBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.blocks.SkullBlock;
import com.sk89q.worldedit.blocks.ClothColor;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.patterns.RandomFillPattern;
import com.sk89q.worldedit.patterns.BlockChance;
import com.sk89q.worldedit.regions.AbstractRegion;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ZeusWorldEdit {
	
	private WorldEditPlugin we;
	private int maxBlocks;
	
	private HashMap<String, Integer> Delays; 
	
	public ZeusWorldEdit() {
		this.we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		this.maxBlocks = we.getConfig().getInt("limits.max-blocks-changed.maximum");
		this.Delays = new HashMap<String, Integer>();
	};
	
	public String editregion(String region_name, String catCommands, boolean isAir, boolean isEffecient, boolean isToggle, boolean isPlayerAware, int playerTolerance, boolean hasDelay) throws MaxChangedBlocksException, InvalidItemException, UnknownItemException {
		
		World world = findWorldByRegionId(region_name);
		if (world == null)
			return "Region not found.";
		
		LocalWorld       curr_world  = (LocalWorld) (new BukkitWorld(world));
		ProtectedRegion  wg_region   = getWGRegion(region_name, world);
		AbstractRegion   we_region   = wg2we(wg_region, curr_world);
		for (String command : catCommands.split("[ \t]*\\&\\&[ \t]*")) {
			
			String[] args      = command.split(" ");
			String   temp_comm = args[0].replace("/", "");
			int      argLen    = args.length;
			
			if (isPlayerAware) {
				int players = 0;
				for (Player player : world.getPlayers()) {
					if (wg_region.contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ())) {
						if (++players > playerTolerance)
							return null;
					}
				}
				if (players != playerTolerance)
					return null;
			}
			
			if (temp_comm.equalsIgnoreCase("set")) {
				if (argLen == 2) {
					if (!set(args[1], (LocalWorld) curr_world, we_region, isEffecient)) {
						return "You can only set one type of block effecientley (-e).";
					}						
				}
				else if (isToggle) {
					if (argLen == 3) {
						if (!tset(args[1], args[2], (LocalWorld) curr_world, we_region)) {
							return "You can only toggle (-t) set one type of block.";
						}
					}
					else {
						return "/tset <block1> <block2>";
					}
				}
				else {
					return "/set block";
				}
			}
			else if (temp_comm.equalsIgnoreCase("replace")) {
				if (argLen == 3) {
					replace(args[1], args[2], (LocalWorld) curr_world, we_region);
				}
				else {
					return "//replace <from> <to>";					
				}
			}
			else if (temp_comm.equalsIgnoreCase("copyfrom")) {
				if (argLen == 2) {
					if (wg_region.getTypeName() != "cuboid") {
						return "Regions must be a cuboid.";
					}
					
					World world_from = findWorldByRegionId(args[1]);
					if (world_from == null) {
						return "Could not find the specified region to copy from";
					}
					LocalWorld localw_from = (LocalWorld) new BukkitWorld(world_from);
					
					ProtectedRegion region_from = getWGRegion(args[1], world_from);
					if (region_from.getTypeName() != "cuboid") {
						return "Regions must be a cuboid.";
					}
					
					if (!transferRegion((CuboidRegion) wg2we(region_from, localw_from), (CuboidRegion) we_region, (LocalWorld) curr_world, !isAir, isEffecient)) {
						return "Regions are not of the same dimensions."; 
					}
				}
				else {
					return "//copyfrom <region id>";					
				}
			}
			else {
				return "Unknowd command: " + temp_comm;
			}
			
			if (hasDelay)
				break;
		}		

		return null;
	}
	
	public void logDelay(String region_name, int delay_id) {
		Delays.put(region_name, delay_id);			
	}
	
	public void removeDelay(String region_name) {
		Delays.remove(region_name);
	}
	
	public void cancelDelay(String region_name) {
		if (Delays.containsKey(region_name)) {
			Bukkit.getScheduler().cancelTask(Delays.get(region_name));
			removeDelay(region_name);
		}
	}
	
	public boolean regionHasDelaySet(String region_name) {
		return Delays.containsKey(region_name);
	}
	
	private World findWorldByRegionId(String region_name) {
		for (World world : Bukkit.getWorlds()) {
			if (WGBukkit.getRegionManager(world).hasRegion(region_name)) {
				return world;
			}
		}
		return null;
	}
	
	private ProtectedRegion getWGRegion(String region_name, World region_world) {
		return WGBukkit.getRegionManager(region_world).getRegionExact(region_name);
	}
	
	private AbstractRegion wg2we(ProtectedRegion wg_region, LocalWorld local_world) {
		AbstractRegion we_region;
		
		if (wg_region.getTypeName() == "cuboid") {
			we_region = new CuboidRegion((Vector) wg_region.getMaximumPoint(), (Vector) wg_region.getMinimumPoint());
			we_region.setWorld(local_world);				// Eclipse didn't recognize the constructor that included a world.
		}
		else {
			we_region = new Polygonal2DRegion(local_world, wg_region.getPoints(), wg_region.getMinimumPoint().getBlockY(), wg_region.getMaximumPoint().getBlockY());
		}
		
		return we_region;
	}
	
	private BaseBlock getBlockByXYZ(LocalWorld curr_world, int x, int y, int z) {
		return curr_world.getBlock(new Vector(x, y, z));
	}
	
	private boolean transferRegion(CuboidRegion region_from, CuboidRegion region_to, LocalWorld local_world, boolean noAir, boolean isEffecient) throws MaxChangedBlocksException {
		if (isEffecient) {
			if (local_world.getBlock(region_from.getMinimumPoint()) == local_world.getBlock(region_to.getMinimumPoint())) {
				return true;
			}
		}
		
		
		Vector size_to = region_to.getMaximumPoint().subtract(region_to.getMinimumPoint()).add(new Vector(1, 1, 1));
		if ((region_to.getHeight() != region_from.getHeight()) || (region_to.getLength() != region_from.getLength()) || (region_to.getWidth() != region_from.getWidth()))   
			return false;
		
		int originalx = region_from.getMinimumPoint().getBlockX();
		int originaly = region_from.getMinimumPoint().getBlockY();
		int originalz = region_from.getMinimumPoint().getBlockZ();
		
		int newx      = region_to.getMinimumPoint().getBlockX();
		int newy      = region_to.getMinimumPoint().getBlockY();
		int newz      = region_to.getMinimumPoint().getBlockZ();		
		
		size_to.add(new Vector(1, 1, 1));
		for (int x = 0; x < size_to.getBlockX(); ++x) {
            for (int y = 0; y < size_to.getBlockY(); ++y) {
                for (int z = 0; z < size_to.getBlockZ(); ++z) {
                	BaseBlock curr_block = getBlockByXYZ(local_world, originalx + x, originaly + y, originalz + z);
                	
                    if (noAir && curr_block.isAir())
                        continue;
                    
                    we.getWorldEdit().getEditSessionFactory().getEditSession(local_world, maxBlocks).setBlock(new Vector(newx + x, newy + y, newz + z), curr_block);
                }
            }
        }
	return true;
	}
	
	private Pattern getBlockPattern(String blockArg) throws InvalidItemException, UnknownItemException {
		String[] items = blockArg.split(",");
		
		if (items.length == 1) {
			return new SingleBlockPattern(getBlock(items[0]));
		}
		
		List<BlockChance> blockChances = new ArrayList<BlockChance>();

	        for (String s : items) {
	            BaseBlock block;

	            double chance;

	            // Parse special percentage syntax
	            if (s.matches("[0-9]+(\\.[0-9]*)?%.*")) {
	                String[] p = s.split("%");
	                chance = Double.parseDouble(p[0]);
	                block = getBlock(p[1]);
	            } else {
	                chance = 1;
	                block = getBlock(s);
	            }

	            blockChances.add(new BlockChance(block, chance));
	        }

	        return new RandomFillPattern(blockChances);
	}
	
	private BaseBlock getBlock(String arg)
					throws UnknownItemException, InvalidItemException {
		
		BlockType blockType;
		arg = arg.replace("_", " ");
		arg = arg.replace(";", "|");
		final String[] blockAndExtraData = arg.split("\\|");
		final String[] typeAndData = blockAndExtraData[0].split(":", 2);
		final String testID = typeAndData[0];
		int blockId = -1;
		boolean allAllowed = true;
		boolean allowNoData = false;
		int data = -1;

		// Attempt to parse the item ID or otherwise resolve an item/block
		// name to its numeric ID
		try {
			blockId = Integer.parseInt(testID);
			blockType = BlockType.fromID(blockId);
		} catch (final NumberFormatException e) {
			blockType = BlockType.lookup(testID);
			if (blockType == null) {
				final int t = we.getWorldEdit().getServer().resolveItem(testID);
				if (t > 0) {
					blockType = BlockType.fromID(t); // Could be null
					blockId = t;
				}
			}
		}

		if (blockId == -1 && blockType == null) {
			// Maybe it's a cloth
			final ClothColor col = ClothColor.lookup(testID);

			if (col != null) {
				blockType = BlockType.CLOTH;
				data = col.getID();
			} else {
				throw new UnknownItemException(arg);
			}
		}

		// Read block ID
		if (blockId == -1) {
			blockId = blockType.getID();
		}

		if (data == -1) { // Block data not yet detected
			// Parse the block data (optional)
			try {
				data = (typeAndData.length > 1 && typeAndData[1].length() > 0) ? Integer.parseInt(typeAndData[1]) : (allowNoData ? -1 : 0);
				if ((data > 15                                 ) || (data < 0 && !(allAllowed && data == -1))) {
					//         && !config.allowExtraDataValues
					data = 0;
				}
			} catch (final NumberFormatException e) {
				if (blockType != null) {
					switch (blockType) {
					case CLOTH:
						final ClothColor col = ClothColor.lookup(typeAndData[1]);

						if (col != null) {
							data = col.getID();
						} else {
							throw new InvalidItemException(arg, "Unknown cloth color '" + typeAndData[1] + "'");
						}
						break;

					case STEP:
					case DOUBLE_STEP:
						final BlockType dataType = BlockType.lookup(typeAndData[1]);

						if (dataType != null) {
							switch (dataType) {
							case STONE:
								data = 0;
								break;
							case SANDSTONE:
								data = 1;
								break;
							case WOOD:
								data = 2;
								break;
							case COBBLESTONE:
								data = 3;
								break;
							case BRICK:
								data = 4;
								break;
							case STONE_BRICK:
								data = 5;
								break;
							case NETHER_BRICK:
								data = 6;
								break;
							case QUARTZ_BLOCK:
								data = 7;
								break;

							default:
								throw new InvalidItemException(arg, "Invalid step type '" + typeAndData[1] + "'");
							}
						} else {
							throw new InvalidItemException(arg, "Unknown step type '" + typeAndData[1] + "'");
						}
						break;

					default:
						throw new InvalidItemException(arg, "Unknown data value '" + typeAndData[1] + "'");
					}
				} else {
					throw new InvalidItemException(arg, "Unknown data value '" + typeAndData[1] + "'");
				}
			}
		}
		if (blockType != null) {
			switch (blockType) {
			case SIGN_POST:
			case WALL_SIGN:
				// Allow special sign text syntax
				final String[] text = new String[4];
				text[0] = blockAndExtraData.length > 1 ? blockAndExtraData[1] : "";
				text[1] = blockAndExtraData.length > 2 ? blockAndExtraData[2] : "";
				text[2] = blockAndExtraData.length > 3 ? blockAndExtraData[3] : "";
				text[3] = blockAndExtraData.length > 4 ? blockAndExtraData[4] : "";
				return new SignBlock(blockType.getID(), data, text);

			case MOB_SPAWNER:
				// Allow setting mob spawn type
				if (blockAndExtraData.length > 1) {
					String mobName = blockAndExtraData[1];
					for (final MobType mobType : MobType.values()) {
						if (mobType.getName().toLowerCase().equals(mobName.toLowerCase())) {
							mobName = mobType.getName();
							break;
						}
					}
					if (!we.getWorldEdit().getServer().isValidMobType(mobName)) {
						throw new InvalidItemException(arg, "Unknown mob type '" + mobName + "'");
					}
					return new MobSpawnerBlock(data, mobName);
				} else {
					return new MobSpawnerBlock(data, MobType.PIG.getName());
				}

			case NOTE_BLOCK:
				// Allow setting note
				if (blockAndExtraData.length > 1) {
					final byte note = Byte.parseByte(blockAndExtraData[1]);
					if (note < 0 || note > 24) {
						throw new InvalidItemException(arg, "Out of range note value: '" + blockAndExtraData[1] + "'");
					} else {
						return new NoteBlock(data, note);
					}
				} else {
					return new NoteBlock(data, (byte) 0);
				}

			case HEAD:
				// allow setting type/player/rotation
				if (blockAndExtraData.length > 1) {
					// and thus, the format shall be "|type|rotation" or "|type" or "|rotation"
					byte rot = 0;
					String type = "";
					try {
						rot = Byte.parseByte(blockAndExtraData[1]);
					} catch (final NumberFormatException e) {
						type = blockAndExtraData[1];
						if (blockAndExtraData.length > 2) {
							try {
								rot = Byte.parseByte(blockAndExtraData[2]);
							} catch (final NumberFormatException e2) {
								throw new InvalidItemException(arg, "Second part of skull metadata should be a number.");
							}
						}
					}
					byte skullType = 0;
					// type is either the mob type or the player name
					// sorry for the four minecraft accounts named "skeleton", "wither", "zombie", or "creeper"
					if (!type.isEmpty()) {
						if (type.equalsIgnoreCase("skeleton")) skullType = 0;
						else if (type.equalsIgnoreCase("wither")) skullType = 1;
						else if (type.equalsIgnoreCase("zombie")) skullType = 2;
						else if (type.equalsIgnoreCase("creeper")) skullType = 4;
						else skullType = 3;
					}
					if (skullType == 3) {
						return new SkullBlock(data, rot, type.replace(" ", "_")); // valid MC usernames
					} else {
						return new SkullBlock(data, skullType, rot);
					}
				} else {
					return new SkullBlock(data);
				}

			default:
				return new BaseBlock(blockId, data);
			}
		} else {
			return new BaseBlock(blockId, data);
		}
	}

	private boolean set(String blockPattern, LocalWorld curr_world, AbstractRegion we_region, boolean isEffecient) throws InvalidItemException, UnknownItemException, MaxChangedBlocksException {
		
		Pattern pattern = getBlockPattern(blockPattern);
		
		if (isEffecient) {
			if (pattern instanceof RandomFillPattern) {
				return false;
			}
			if (curr_world.getBlock(we_region.getMinimumPoint()) == ((SingleBlockPattern) pattern).getBlock()) {
				return true;
			}
		}
		
		if (pattern instanceof SingleBlockPattern) {
			we.getWorldEdit().getEditSessionFactory().getEditSession(curr_world, maxBlocks).setBlocks(we_region, ((SingleBlockPattern) pattern).getBlock());
		}
		else {
			we.getWorldEdit().getEditSessionFactory().getEditSession(curr_world, maxBlocks).setBlocks(we_region, pattern);
		}
		
		return true;
	}
	
	private boolean tset(String block1Pattern, String block2Pattern, LocalWorld curr_world, AbstractRegion tset_region) throws InvalidItemException, UnknownItemException, MaxChangedBlocksException {
		Pattern patternOne = getBlockPattern(block1Pattern);
		Pattern patternTwo = getBlockPattern(block2Pattern);
		
		if ((patternOne instanceof RandomFillPattern) || (patternTwo instanceof RandomFillPattern)) {
			return false;
		}
		
		if (curr_world.getBlock(tset_region.getMinimumPoint()).equalsFuzzy(((SingleBlockPattern) patternOne).getBlock())) {
			we.getWorldEdit().getEditSessionFactory().getEditSession(curr_world, maxBlocks).setBlocks(tset_region, ((SingleBlockPattern) patternTwo).getBlock());
		}
		else {
			we.getWorldEdit().getEditSessionFactory().getEditSession(curr_world, maxBlocks).setBlocks(tset_region, ((SingleBlockPattern) patternOne).getBlock());
		}
		return true;
	}

	private Set<BaseBlock> getBlocks(String list) throws UnknownItemException, InvalidItemException {
		String[] items = list.split(",");
		Set<BaseBlock> blocks = new HashSet<BaseBlock>();
		for (String id : items) {
			blocks.add(getBlock(id));
		}
		return blocks;
	}
	
	private void replace(String patternFrom, String patternTo, LocalWorld curr_world, AbstractRegion we_region) throws InvalidItemException, UnknownItemException, MaxChangedBlocksException {
		Set<BaseBlock> from  = getBlocks(patternFrom);
		Pattern        to    = getBlockPattern(patternTo);
		
		if (to instanceof SingleBlockPattern) {
			we.getWorldEdit().getEditSessionFactory().getEditSession(curr_world, maxBlocks).replaceBlocks(we_region, from, ((SingleBlockPattern) to).getBlock());
		}
		else {
			we.getWorldEdit().getEditSessionFactory().getEditSession(curr_world, maxBlocks).replaceBlocks(we_region, from, to);
		}
	}
}