package com.toastytoast.bots.splasher;

import java.io.IOException;

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import com.toastytoast.bots.utils.XPReporter;


/**
 * This class contains a script for OSBot that will splash on
 * spiders in Lumbridge basement for as long as possible.
 * 
 * @author Brian McDonald
 */
@ScriptManifest(author="ToastyToast", info = "", logo = "", name = "Splasher", version = 0.1)
public class Splasher extends Script {
	private State state;	// current state of bot
	
	/**
	 * States used to control logical flow of bot
	 */
	private enum State {
		/**
		 * Make sure equipment is ok for splashing
		 */
		INIT,
		
		/**
		 * Start attacking a spider
		 */
		ATTACK,
		
		/**
		 * Interact with the game to keep from logging out
		 */
		INTERACT,
		
		/**
		 * Sleep thread until it is time to interact with the game
		 */
		WAIT
	}
	
	/**
	 * Perform setup before bot begins running
	 * 
	 * This method is automatically called by the OSBot client.  Do not call explicitly.
	 */
	@Override
	public void onStart() {
		// start tracking magic XP gained
		experienceTracker.start(Skill.MAGIC);
	}
	
	/**
	 * Main logic loop for bot
	 * 
	 * Perform tasks based on current state of bot
	 * 
	 * This method is automatically called by the OSBot client.  Do not call explicitly.
	 */
	@Override
	public int onLoop() throws InterruptedException {
		switch(state) {
			case INIT:
				break;
			case ATTACK:
				attack();
				break;
			case INTERACT:
				break;
			case WAIT:
				break;
		}
		return random(250, 500);
	}
	
	/**
	 * Perform cleanup and exit operations on bot exit
	 * 
	 * Report XP gained to overall tracking to server and print other relevant info
	 * about the bot run to the logger
	 * 
	 * This method is automatically called by the OSBot client.  Do not call explicitly.
	 */
	@Override
	public void onExit() {
		try {
			XPReporter.reportXP(Skill.MAGIC, experienceTracker.getGainedXP(Skill.MAGIC));
		} catch(IOException e) {}
		
		log("Magic XP Gained: " + experienceTracker.getGainedXP(Skill.MAGIC));
		log("Magic Levels Gained: " + experienceTracker.getGainedLevels(Skill.MAGIC));
		log("Magic XP/hr: " + experienceTracker.getGainedXPPerHour(Skill.MAGIC));
	}
	
	private void attack() {
		
	}
}
