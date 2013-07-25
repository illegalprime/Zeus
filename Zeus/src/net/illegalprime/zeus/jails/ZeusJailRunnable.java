package net.illegalprime.zeus.jails;


public final class ZeusJailRunnable implements Runnable {
	private ZeusJailManager jailMan;
	
	public ZeusJailRunnable(ZeusJailManager jailManager) {
		this.jailMan = jailManager;
	}
	
	@Override
	public void run() {
		this.jailMan.Tick();
	}
}