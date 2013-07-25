package net.illegalprime.zeus.runnables;

import net.illegalprime.zeus.Zeus;

public final class ZeusAutosaveRunnable implements Runnable {
	private Zeus plugin;
	
	public ZeusAutosaveRunnable(Zeus passedPlugin) {
		this.plugin = passedPlugin;
	}

	@Override
	public void run() {
		plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "save-all");
	}
}