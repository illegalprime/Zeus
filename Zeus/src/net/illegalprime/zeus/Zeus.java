package net.illegalprime.zeus;

import net.illegalprime.zeus.jails.ZeusJailManager;
import net.illegalprime.zeus.jails.ZeusJailRunnable;
import net.illegalprime.zeus.runnables.ZeusAutosaveRunnable;
import net.illegalprime.zeus.tactical.TacticalInsertion;
import net.illegalprime.zeus.warps.ZeusWarpManager;
import net.illegalprime.zeus.worldedit.ZeusWorldEdit;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Zeus extends JavaPlugin {
	private static final String playerCommLine =      "[zCommPlayer]";
	private static final String serverCommLine =      "[zCommServer]";
	private static final String playerCommLineShort = "[zCP]";
	private static final String serverCommLineShort = "[zCS]";
	private static final String signUnlimited =       "[ZeusUnlimited]";
	private static final String spaceJump =           "[ZeusJump]";
	// TODO: private static final String NoBlock =			  "[ZeusNoBlock]";
	
	// TODO: Implement NoBlock on Zeus
	// TODO: Implement reverse invite/goto commands
	// TODO: Implement Slot Machine feature
	// TODO: Set up Permissions in this plugin.
	
	private static final String[] bukkitComms = {
		"ban",
		"ban-ip",
		"banlist",
		"clear",
		"defaultgamemode",
		"deop",
		"difficulty",
		"enchant",
		"gamemode",
		"gamerule",
		"give",
		"help",
		"kick",
		"kill",
		"list", 
		"me",
		"op",
		"pardon",
		"pardon-ip",
		"plugins",
		"reload",
		"save-all",
		"save-off",
		"save-on",
		"say",
		"seed",
		"spawnpoint",
		"stop",
		"tell",
		"time",
		"timings",
		"toggledownfall",
		"tp",
		"version",
		"weather",
		"whitelist",
		"xp"
	};
	
	//protected Timer commCheck;
	//protected ZeusTimerTask commTask; 
	
	protected ZeusCommandExecutor commEXE;
	protected ZeusJailManager     jailMan;
	public    ZeusJailRunnable    jailTick;
	protected ZeusWarpManager     warpMan;
	protected TacticalInsertion   tactical;
	public    ZeusWorldEdit       zWorldEdit;
	
	public int autosaveTime;
	public int jailID;
	public int autosaveID;
	public boolean lightActive;
	public boolean tntActive;
	public boolean autosaveActive;
	private boolean WorldEditExists;
	//public boolean commServerActive = true;
	//public boolean haloActive;
	
	@Override
	public void onEnable() {
		this.getConfig();
		
		this.autosaveActive = true;	
		this.commEXE = new ZeusCommandExecutor(this);
		this.jailMan = new ZeusJailManager(this);
		this.jailTick = new ZeusJailRunnable(jailMan);
		this.jailID = this.getServer().getScheduler().runTaskTimerAsynchronously(this, this.jailTick, 200L, 1200L).getTaskId();
		this.autosaveActive = this.getConfig().getBoolean("Autosave.enabled", false);
		this.autosaveTime = this.getConfig().getInt("Autosave.time", 120);
		this.warpMan = new ZeusWarpManager();
		this.tactical = new TacticalInsertion(this);
		
		if (this.autosaveActive) {
			this.autosaveID = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new ZeusAutosaveRunnable(this), 200L, this.autosaveTime*1200L);
		}
		
		if (!(Bukkit.getPluginManager().getPlugin("WorldEdit") == null ||  Bukkit.getPluginManager().getPlugin("WorldGuard") == null)) {
			this.zWorldEdit = new ZeusWorldEdit();
			getCommand("regionedit").setExecutor(commEXE);
			this.WorldEditExists = true;
		}
		else {
			Bukkit.getLogger().info("[Zeus] Could not find WorldEdit and WorldGuard, regionedit will be disabled.");
		}
		
		getCommand("zeus").setExecutor(commEXE);
		getCommand("autosave").setExecutor(commEXE);
		getCommand("jailtime").setExecutor(commEXE);
		getCommand("jail").setExecutor(commEXE);
		getCommand("invite").setExecutor(commEXE);
		getCommand("goto").setExecutor(commEXE);
		getCommand("tactical").setExecutor(commEXE);
		getCommand("book").setExecutor(commEXE);
		getLogger().info("Zeus Plugin Loaded!");
		
		getServer().getPluginManager().registerEvents(new ZeusEventHandler(this), this);
	}
	
	public void cancelAutosave() {
		if (this.getServer().getScheduler().isQueued(this.autosaveID) || this.getServer().getScheduler().isCurrentlyRunning(this.autosaveID)) {
			this.getServer().getScheduler().cancelTask(this.autosaveID);
		}
	}
	
	public void newAutosave() {
		this.cancelAutosave();
		this.autosaveID = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new ZeusAutosaveRunnable(this), 200L, this.autosaveTime*1200L);
	}
	
	@Override
	public void onDisable() {
		getCommand("zeus").setExecutor((CommandExecutor)null);
		getCommand("autosave").setExecutor((CommandExecutor)null);
		getCommand("jailtime").setExecutor((CommandExecutor)null);
		getCommand("jail").setExecutor((CommandExecutor)null);
		getCommand("invite").setExecutor((CommandExecutor)null);
		getCommand("goto").setExecutor((CommandExecutor)null);
		getCommand("tactical").setExecutor((CommandExecutor)null);
		if (this.WorldEditExists)
			getCommand("regionedit").setExecutor((CommandExecutor)null);
		
		this.getServer().getScheduler().cancelTask(this.jailID);
		
		this.getConfig().set("Autosave.enabled", this.autosaveActive);
		this.getConfig().set("Autosave.time", this.autosaveTime);
		jailMan.saveDATA();
		tactical.saveDATA();
		this.saveConfig();
		getLogger().info("Zeus Data Saved!");
	}
	
	public boolean isCommBlock(String signStr) {
		if (signStr == (String)null) {
			return false;
		}
		return (playerCommLine.equalsIgnoreCase(signStr) || serverCommLine.equalsIgnoreCase(signStr) || playerCommLineShort.equalsIgnoreCase(signStr) || serverCommLineShort.equalsIgnoreCase(signStr))? true : false ;  
	}
	
	public boolean isSenderPlayer(String signStr) {
		return (playerCommLine.equalsIgnoreCase(signStr) || playerCommLineShort.equalsIgnoreCase(signStr))? true : false;
	}
	
	public boolean commExists(String commName) {
		int i = 0;
		while (i < bukkitComms.length) {
			if (bukkitComms[i].equalsIgnoreCase(commName))
				return true;
			i++;
		}
		if (this.getServer().getPluginCommand(commName) != (PluginCommand)null) {
			return true;
		}
		return false;
	}
	
	public boolean isUnlimitedSign(String strInQuestion) {
		return strInQuestion.equalsIgnoreCase(signUnlimited);
	}
	
	public boolean isJumpSign(String strInQuestion) {
		return strInQuestion.equalsIgnoreCase(spaceJump);
	}
	
	public boolean isInteger(String intStr) {
		try {
			Integer.valueOf(intStr);
			return true;
		}
		catch (NumberFormatException err) {
			return false;
		}
	}
	
	public float signInteger(String signLine) {
		float output = 0f;
		try {
			output = Float.valueOf(signLine);
		}
		catch (NumberFormatException err) {   }
		
		return output;
	}
}

