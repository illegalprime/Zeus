package net.illegalprime.zeus.worldedit;

import java.util.ArrayList;

import net.illegalprime.zeus.Zeus;

import org.bukkit.Bukkit;

import com.sk89q.worldedit.InvalidItemException;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.UnknownItemException;

public class ZeusWERunnable implements Runnable {
	
	Zeus plugin;
	String region_name;
	String catCommands;
	boolean isAir;
	boolean isEffecient;
	boolean isToggle;
	boolean isPlayerAware;
	int playerTolerance;
	ArrayList<Integer> delays;

	public ZeusWERunnable(Zeus plugin, String region_name, String catCommands, boolean isAir, boolean isEffecient, boolean isToggle, boolean isPlayerAware, int playerTolerance, ArrayList<Integer> delays) {
		this.plugin = plugin;
		this.region_name = region_name;
		this.catCommands = catCommands;
		this.isAir = isAir;
		this.isEffecient = isEffecient;
		this.isToggle = isToggle;
		this.isPlayerAware = isPlayerAware;
		this.playerTolerance = playerTolerance;
		this.delays = delays;
	}

	@Override
	public void run() {
		try {
			
			this.plugin.zWorldEdit.editregion(this.region_name, this.catCommands, this.isAir, this.isEffecient, this.isToggle, this.isPlayerAware, this.playerTolerance, true);
			
			if (catCommands.contains("&&")) {
				catCommands = catCommands.split("[ \t]*\\&\\&[ \t]*", 2)[1];
			}
			else {
				this.plugin.zWorldEdit.removeDelay(this.region_name);
				return;
			}
			
			if (delays.size() != 0) {
				int delay = delays.get(0);
				delays.remove(0);
				
				this.plugin.zWorldEdit.logDelay(this.region_name, Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new ZeusWERunnable(this.plugin, region_name, catCommands, isAir, isEffecient, isToggle, isPlayerAware, playerTolerance, delays), delay));
			}
			else {
				this.plugin.zWorldEdit.editregion(this.region_name, this.catCommands, this.isAir, this.isEffecient, this.isToggle, this.isPlayerAware, this.playerTolerance, false);
			}
			
			
		} catch (InvalidItemException | MaxChangedBlocksException
				| UnknownItemException e) {}
	}

}