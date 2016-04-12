package com.toastytoast.bots.logoutpreventer;

import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

/**
 * This class contains a script for use in OSBot that performs actions
 * regularly to prevent the player from being logged out by the 6 minute timer
 * 
 * @author Brian McDonald
 */
@ScriptManifest(author="ToastyToast", info = "", logo = "", name = "Logout Preventer", version = 0.1)
public class LogoutPreventer extends Script {
	/**
	 * Main logic loop for bot
	 * 
	 * Move the mouse every 4.5-5.5 minutes to reset the logout timer
	 * 
	 * This method is automatically called by the OSBot client.  Do not call explicitly.
	 */
	@Override
	public int onLoop() {
		moveMouse();
		waitForLogoutTimer();
		
		return 0;
	}
	
	/**
	 * Move the mouse randomly on the screen the reset the logout timer
	 */
	private void moveMouse() {
		mouse.moveRandomly();
		try {
			sleep(random(250, 500));
		} catch (InterruptedException e) {}
		mouse.moveOutsideScreen();
	}
	
	/**
	 * Sleep for a random time between 4.5 and 5.5 minutes
	 * 
	 * The logout timer kicks after 6 minutes, so this method sleeps
	 * for a time less than that.
	 */
	private void waitForLogoutTimer() {
		try {
			sleep(random(270000, 330000));
		} catch (InterruptedException e) {}
	}
}
