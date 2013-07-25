package net.illegalprime.zeus.jails;

//import java.util.ArrayList;
//import java.util.Iterator;

import org.bukkit.Location;

public final class ZeusCell {
	
	private Location cellLocate;
	private int      cellOccupy;
	private int      cellVacant;
	private int      cellNumber;
	
	//private ArrayList<String>   cellPlayers;
	
	public ZeusCell(Location cellLocation, int vacancy, int number, int occupancy) {
		this.cellLocate = cellLocation;
		this.cellOccupy = 0;
		this.cellVacant = vacancy;
		this.cellNumber = number;
		//this.cellPlayers = new ArrayList<String>();
	}
	
	public void incrementOccupancy() {
		this.cellOccupy++;
	}
	
	public void decrementOccupancy() {
		this.cellOccupy--;
	}
	
	/*public void addPlayer(String playerName) {
		this.cellPlayers.add(playerName);
		this.incrementOccupancy();
	}
	
	public boolean removePlayer(String playerName) {
		if (this.cellPlayers.remove(playerName)) {
			this.decrementOccupancy();
			return true;
		}
		return false;
	}
	
	public boolean playerExists(String playerName) {
		return this.cellPlayers.contains(playerName);
	}*/
	
	public Location getLocation()  { return this.cellLocate; }
	public int      getOccupancy() { return this.cellOccupy; }
	public int      getVacancy()   { return this.cellVacant; }
	public int      getNumber()    { return this.cellNumber; }
	
	public void setLocation(Location newLocation)  { this.cellLocate = newLocation;  }
	public void setOccupancy(int newOccupancy)     { this.cellOccupy = newOccupancy; }
	public void setVacancy(int newVacancy)         { this.cellVacant = newVacancy;   }
	public void setNumber(int newNumber)           { this.cellNumber = newNumber;    }
}