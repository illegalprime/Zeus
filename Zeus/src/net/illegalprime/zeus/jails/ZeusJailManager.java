package net.illegalprime.zeus.jails;

//import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
import java.util.Set;

import net.illegalprime.zeus.Zeus;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
//import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class ZeusJailManager {
	private Zeus plugin;
	
	private String defaultJail;
	private int defaultTime;
	private int defaultOccupancy;
	private Location spawnLoc;
	
	private HashMap<String, ZeusJail> Jails;
	private HashMap<String, ZeusPrisoner> Prisoners;
	
	private String jailSign = "[Jail Cell]";
	
	public ZeusJailManager(Zeus zplugin) {
		plugin = zplugin;
		
		Jails = new HashMap<String, ZeusJail>();
		Prisoners = new HashMap<String, ZeusPrisoner>();
		
		/*defaultTime = 10;
		defaultOccupancy = 1;
		defaultJail = "nonexistent_jail";
		spawnLoc = plugin.getServer().getWorlds().get(0).getSpawnLocation();*/
		
		loadJailConfig();
	}
	
	public void loadJailConfig() {
		World currWorld = plugin.getServer().getWorlds().get(0);
		
		this.defaultJail = plugin.getConfig().getString("ZeusJail.dJail");
		this.defaultTime = plugin.getConfig().getInt("ZeusJail.dTime");
		this.defaultOccupancy = plugin.getConfig().getInt("ZeusJail.dMaxP");
		Vector location = plugin.getConfig().getVector("ZeusJail.dSpawn");
		this.spawnLoc = location.toLocation(currWorld);
		
		if (plugin.getConfig().getConfigurationSection("ZeusJail.Prisoners") == null) {
			plugin.getLogger().info("No Prisoners.");
		}
		else {
			Set<String> people = plugin.getConfig().getConfigurationSection("ZeusJail.Prisoners").getKeys(false);
			for (String person : people) {
				plugin.getLogger().info("Person Jailed: " + person);
				List<String> jailInfo = plugin.getConfig().getStringList("ZeusJail.Prisoners." + person);
				Prisoners.put(person, new ZeusPrisoner(Integer.valueOf(jailInfo.get(0)), Integer.valueOf(jailInfo.get(1)), jailInfo.get(2)));
			}
		}
		
		if (plugin.getConfig().getConfigurationSection("ZeusJail.Jails") == null) {
			plugin.getLogger().info("No Jails.");
		}
		else {
			Set<String> prisons = plugin.getConfig().getConfigurationSection("ZeusJail.Jails").getKeys(false);
			for (String prison : prisons) {
				Jails.put(prison, new ZeusJail());
				Set<String> cells = plugin.getConfig().getConfigurationSection("ZeusJail.Jails." + prison).getKeys(false);
				for (String cell : cells) {
					Vector cellLocation = plugin.getConfig().getVector("ZeusJail.Jails." + prison + "." + cell + ".location");
					List<Integer> cellInfo = plugin.getConfig().getIntegerList("ZeusJail.Jails." + prison + "." + cell + ".info");
					Jails.get(prison).addCell(cellLocation.toLocation(currWorld), cellInfo.get(0), cellInfo.get(1), cellInfo.get(2));
				}
			}
		}
	}
	
	public void saveDATA() {
		plugin.getConfig().set("ZeusJail.dMaxP", this.defaultOccupancy);
		plugin.getConfig().set("ZeusJail.dJail", this.defaultJail);
		plugin.getConfig().set("ZeusJail.dTime", this.defaultTime);
		Vector newLocation = this.spawnLoc.toVector();
		plugin.getConfig().set("ZeusJail.dSpawn", newLocation);
		
		plugin.getConfig().set("ZeusJail.Prisoners", null);
		
		Set<String> prisonerNames = Prisoners.keySet();
		for (String prisonerN : prisonerNames) {
			ZeusPrisoner prisonerO = Prisoners.get(prisonerN);
			List<String> prisonerInfo = new ArrayList<String>();
				prisonerInfo.add(Integer.toString(prisonerO.getCell()));
				prisonerInfo.add(Integer.toString(prisonerO.getTimeLeft()));
				prisonerInfo.add(prisonerO.getJail());
				
			plugin.getConfig().set("ZeusJail.Prisoners." + prisonerN, prisonerInfo);
		}
		
		plugin.getConfig().set("ZeusJail.Jails", null);
		
		Set<String> jailNames = Jails.keySet();
		for (String jailN : jailNames) {
			Jails.get(jailN).saveCells("ZeusJail.Jails." + jailN, plugin);
		}
	}
	
	public void Tick() {
		Server server = plugin.getServer();
		Iterator<String> prisonerIt = Prisoners.keySet().iterator();
		
		while (prisonerIt.hasNext()) {
			String nprisoner = prisonerIt.next();
			if (server.getPlayerExact(nprisoner) != null) {
				if (Prisoners.get(nprisoner).tickTime()) {
					this.releasePrisoner(nprisoner);
				}
			}
		}
	}
	
	public void addJail(String jailName) {
		this.Jails.put(jailName, new ZeusJail());
	}
	
	public void addCell(Location cellLoc, String jailName, int maxNumber) {
		if (maxNumber == 0) {
			return;
		}
		if (Jails.containsKey(jailName)) {
			Jails.get(jailName).addCell(cellLoc, maxNumber);
		}
		else {
			Jails.put(jailName, new ZeusJail());
			Jails.get(jailName).addCell(cellLoc, maxNumber);
		}
	}
	
	public boolean prisonerExists(String Prisoner) {
		return Prisoners.containsKey(Prisoner);
	}
	
	public void timeoutPrisoner(String Prisoner) {
		Prisoners.get(Prisoner).timeout();
	}
	
	public void releasePrisoner(String Prisoner) {
		Player prisoner = plugin.getServer().getPlayer(Prisoner);
		prisoner.setVelocity(new Vector(0, 0, 0));
		prisoner.teleport(this.spawnLoc);
		try {
			Jails.get(Prisoners.get(Prisoner).getJail()).decrementOccupancy(Prisoners.get(Prisoner).getCell());
		}
		catch (NullPointerException err) {}
		Prisoners.remove(Prisoner);
		plugin.getConfig().set("ZeusJail.Prisoners."+Prisoner, null);
		
		prisoner.sendMessage(ChatColor.GREEN + "You have been released from Jail. Come back soon!");
		
		if (Prisoners.isEmpty()) {
			plugin.getServer().getScheduler().cancelTask(plugin.jailID);
		}
	}
	
	public boolean jail(String playerName, String jail, int time) {
		ZeusJail currJail = Jails.get(jail);
		int currCell = currJail.vacantCell();
		if (currCell == -1) {
			return false;
		}
		Prisoners.put(playerName, new ZeusPrisoner(currCell, time, jail));
		currJail.incrementOccupancy(currCell);
		plugin.getServer().getPlayer(playerName).teleport(currJail.getCellLocation(currCell));
		plugin.getServer().getPlayer(playerName).sendMessage(ChatColor.RED + "You have been put in jail for violating the constitution of The Block Project, for " + Integer.toString(time) + " minutes. To see how much time you have remaining, use /jailtime");
		
		if (!(plugin.getServer().getScheduler().isCurrentlyRunning(plugin.jailID) || plugin.getServer().getScheduler().isQueued(plugin.jailID))) {
			plugin.jailID = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, plugin.jailTick, 200L, 1200L).getTaskId();
		}
		return true;
	}
	
	public boolean destroyCell(String jailName, int cellIndex) {
		if (Jails.get(jailName) == null) {
			return true;
		}
		Jails.get(jailName).destroyCell(cellIndex);
		if (Jails.get(jailName).jailIsEmpty()) {
			Jails.remove(jailName);
			return true;
		}
		return false;
	}
	
	public boolean jailExists(String jailn) {
			return Jails.containsKey(jailn);
	}
	
	public boolean isJailSign(String signStr) {
		return this.jailSign.equalsIgnoreCase(signStr);
	}
	
	public int nextCell(String jailName) {
		return Jails.get(jailName).nextCell();
	}
	
	public int getPrisonerTime(String prisonerName) {
		if (this.prisonerExists(prisonerName)) {
			return Prisoners.get(prisonerName).getTimeLeft();
		}
		return 0;
	}
	
	public int getNumberOfCells(String jailName) {
		if (this.jailExists(jailName)) {
			return this.Jails.get(jailName).getContainedCells();
		}
		return 0;
	}
	
	public String getJailOfPrisoner(String prisonerName) {
		if (this.prisonerExists(prisonerName)) {
			return this.Prisoners.get(prisonerName).getJail();
		}
		return "No Jail";
	}
	
	public Set<String> getJailNames() {
		return Jails.keySet();
	}
	
	public Set<String> getPrisonerNames() {
		return Prisoners.keySet(); 
	}
	
	public void setSpawnLocation(Location spwnLoc) { this.spawnLoc = spwnLoc; }
	public void setDefaultTime(int newTime)        { this.defaultTime = newTime; }
	public void setDefaultJail(String defJailName) { this.defaultJail = defJailName; }
	public void setDefaultNumber(int newOccupancy) { this.defaultOccupancy = newOccupancy; }
	
	public String   getDefaultJail()   { return this.defaultJail;      }
	public int      getDefaultNumber() { return this.defaultOccupancy; }
	public int      getDefaultTime()   { return this.defaultTime;      }
	public Location getSpawnLocation() { return this.spawnLoc;         }
}