package net.illegalprime.zeus.jails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.illegalprime.zeus.Zeus;

import org.bukkit.Location;

public final class ZeusJail {
	private HashMap<Integer, ZeusCell> Cells;
	
	public ZeusJail() {
		Cells = new HashMap<Integer, ZeusCell>();
	}
	
	public int nextCell() {
		int max = 0;
		Iterator<Integer> cellIt = Cells.keySet().iterator();
		while (cellIt.hasNext()) {
			int curr = cellIt.next();
			if (curr > max) {
				max = curr;
			}
		}
		return max+1;
	}
	
	public void addCell(Location cellLocation, int cellOccupancy) {
		int nextID = this.nextCell();
		Cells.put(nextID, new ZeusCell(cellLocation, cellOccupancy, nextID, 0));
	}
	
	public void addCell(Location cellLocation, int cellVacancy, int cellID, int cellOccupancy) {
		Cells.put(cellID, new ZeusCell(cellLocation, cellVacancy, cellID, cellOccupancy));
	}
	
	public void removeCell(int cellID) {
		if (Cells.containsKey(cellID)) {
			Cells.remove(cellID);
		}
	}
	
	public int getContainedCells() {
		return this.Cells.size();
	}
	
	public void decrementOccupancy(int cellID) {
		Cells.get(cellID).decrementOccupancy();
	}
	
	public void incrementOccupancy(int cellID) {
		Cells.get(cellID).incrementOccupancy();
	}
	
	public void destroyCell(int cellID) {
		Cells.remove(cellID);
	}
	
	public boolean jailIsEmpty() {
		return Cells.isEmpty();
	}
	
	public Location getCellLocation(int cellID) {
		return Cells.get(cellID).getLocation();
	}
	
	public int vacantCell() {
		Iterator<ZeusCell> cellIt = Cells.values().iterator();
		while (cellIt.hasNext()) {
			ZeusCell currCell = cellIt.next();
			if (currCell.getVacancy() > currCell.getOccupancy()) {
				return currCell.getNumber();
			}
		}
		return -1;
	}
	
	public void saveCells(String pathToJail, Zeus plugin) {
		plugin.getConfig().set(pathToJail, null);
		
		int i = 0;
		Iterator<ZeusCell> cellInCells = Cells.values().iterator();
		while (cellInCells.hasNext()) {
			ZeusCell currCell = cellInCells.next();
			List<Integer> cellInfo = new ArrayList<Integer>();
				cellInfo.add(currCell.getVacancy());
				cellInfo.add(currCell.getNumber());
				cellInfo.add(currCell.getOccupancy());
				
			plugin.getConfig().set(pathToJail + ".Cell" + Integer.toString(i) + ".info", cellInfo);
			plugin.getConfig().set(pathToJail + ".Cell" + Integer.toString(i) + ".location", currCell.getLocation().toVector());
			i++;
		}
	}
}