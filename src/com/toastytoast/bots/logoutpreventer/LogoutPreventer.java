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
		moveCamera();
		waitForLogoutTimer();
		
		return 0;
	}
	
	/**
	 * Move the camera to a random position to reset the logout timer
	 */
	private void moveCamera() {
		camera.movePitch(random(22, 67));
		camera.moveYaw(random(0, 270));
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
