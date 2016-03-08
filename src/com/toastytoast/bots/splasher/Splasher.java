package com.toastytoast.bots.splasher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

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
				if(readyToSplash()) {
					state = State.ATTACK;
				} else {
					JOptionPane.showMessageDialog(null, "Your magic attack bonus is not low enough to splash");
					stop(false);
				}
				break;
			case ATTACK:
				attack();
				break;
			case INTERACT:
				moveMouse();
				break;
			case WAIT:
				waitUntilInteract();
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
	
	/**
	 * Check if player has a low enough magic bonus to splash
	 * 
	 * @return True if player has low enough magic bonus to splash
	 */
	private boolean readyToSplash() {
		// TODO: check if player actually low enough magic bonus to splash
		return true;
	}
	
	/**
	 * Attack a spider
	 * 
	 * Attack a random spider that is not fighting someone else
	 */
	@SuppressWarnings("deprecation")
	private void attack() {
		List<NPC> allNPCs = npcs.getAll();
		ArrayList<NPC> spiders = new ArrayList<NPC>();
		ArrayList<NPC> availableSpiders = new ArrayList<NPC>();
		NPC spiderToAttack = null;
		
		// set autocast
		if(tabs.getOpen() != Tab.ATTACK) {
			tabs.open(Tab.ATTACK);
		}
		
		RS2Widget autocastWidget = widgets.get(593, 25);
		// make sure we are wielding a staff that can autocast
		if(autocastWidget == null) {
			JOptionPane.showMessageDialog(null, "You must be wielding a staff capable of autocast");
			stop(false);
		} else {
			autocastWidget.interact();
			
			// choose spell
			RS2Widget airStrikeWidget = null;
			do {
				try {
					sleep(250);
				} catch (InterruptedException e) {}
				airStrikeWidget= widgets.get(201, 1);
			} while(airStrikeWidget == null);
		}
		
		tabs.open(Tab.INVENTORY);
		
		// get all spiders in area
		for(NPC npc : allNPCs) {
			if("Spider".equals(npc.getName())) {
				spiders.add(npc);
			}
		}
		
		// get spiders we can fight
		for(NPC spider : spiders) {
			if(spider.exists() && spider.getHealth() > 0 && !spider.isUnderAttack()) {
				availableSpiders.add(spider);
			}
		}
		
		if(availableSpiders.size() < 1) {
			// notify user that there are no spiders to attack and exit
			JOptionPane.showMessageDialog(null, "No available spiders here");
			stop(false);
		}
		
		// just attack the first spider in the list
		spiderToAttack = availableSpiders.get(0);
		if(spiderToAttack.interact("Attack")) {
			state = State.WAIT;
		}
	}
	
	/**
	 * Move the mouse to a random location on the screen then off the screen
	 */
	private void moveMouse() {
		mouse.move(random(0, 500), random(0, 500));
		
		try {
			sleep(random(250, 500));
		} catch (InterruptedException e) {}
		
		mouse.moveOutsideScreen();
	}
	
	/**
	 * Sleep until out combat or until it is time to interact
	 * 
	 * Sleep for a max of 3.5 to 4.5 minutes or until out of combat.
	 * If out of combat assume we ran out of runes and are done.
	 */
	private void waitUntilInteract() {
		new ConditionalSleep(random(210000, 270000), 2000) {
			@Override
			public boolean condition() throws InterruptedException {
				return !combat.isFighting();
			}
		}.sleep();
		
		state = State.INTERACT;
	}
}
