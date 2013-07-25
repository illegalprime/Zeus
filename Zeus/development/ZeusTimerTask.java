package net.illegalprime.zeus;

import java.util.TimerTask;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public final class ZeusTimerTask extends TimerTask {
	private String pipeName = "/home/mde/Desktop/pipedream";
	private BufferedReader pipeReader;
	private Logger logger;
	
	private Zeus plugin;
	
	public ZeusTimerTask(Zeus zeus_plugin) {
		plugin = zeus_plugin;
		logger = plugin.getLogger();
		
		try {
			Runtime.getRuntime().exec("mkfifo", new String[]{pipeName});
		}
		catch (IOException ex) {
			logger.warning("Could not create a FIFO Pipe; command server disabled.");
			//plugin.commServerActive = false;
			return;
		}
		try {
			pipeReader = new BufferedReader(new FileReader(pipeName));
		}
		catch (FileNotFoundException ex) {
			logger.warning("Could not open the pipe for reading; command server disabled.");
			//plugin.commServerActive = false;
			return;
		}
	}
	
	@Override
	public void run() {
		String nextCommand;
		try {
			while (pipeReader.ready()) {
				nextCommand = pipeReader.readLine();
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), nextCommand);
			}
		} catch (IOException e) {
			logger.warning("Could not execute command.");
			return;
		}
		//plugin.commCheck.schedule(this, 1000);
		return;
	}
	
	public void cleanUp() {
		try {
			Runtime.getRuntime().exec("rm", new String[]{pipeName});
		}
		catch (IOException ex) { 
			logger.warning("Could not delete pipe, do it yourself.");
		}
	}
}