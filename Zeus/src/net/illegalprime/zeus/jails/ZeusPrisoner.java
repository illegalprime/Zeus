package net.illegalprime.zeus.jails;

public final class ZeusPrisoner {
	private int    cellID;
	private String jailID;
	private int    jailTime;
	
	public ZeusPrisoner(int cellID, int jailTime, String jailID) {
		this.cellID = cellID;
		this.jailID = jailID;
		this.jailTime = jailTime;
	}
	
	public boolean tickTime() {
		this.jailTime--;
		if (this.jailTime <= 0) {
			return true;
		}
		return false;
	}
	
	public void timeout() {
		this.jailTime = 0;
	}
	
	public String getJail() {
		return this.jailID;
	}
	
	public int getCell() {
		return this.cellID;
	}
	
	public int getTimeLeft() {
		return this.jailTime;
	}
}