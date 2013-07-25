package net.illegalprime.zeus.runnables;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public final class ZeusGamemodeRunnable implements Runnable {
	private Player creativePlayer;
	
	public ZeusGamemodeRunnable(Player givenPlayer) {
		this.creativePlayer = givenPlayer;
	}
	
	@Override
	public void run() {
		this.creativePlayer.setGameMode(GameMode.CREATIVE);
	}
}