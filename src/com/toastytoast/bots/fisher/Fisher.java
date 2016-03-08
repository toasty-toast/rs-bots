package com.toastytoast.bots.fisher;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.util.LocalPathFinder;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import com.toastytoast.bots.utils.XPReporter;

/**
 * This class contains a script for use in OSBot that will fish
 * in multiple possible locations and track experience gained and fish caught.
 * 
 * @author Brian McDonald
 */
@ScriptManifest(author="ToastyToast", info = "", logo = "", name = "Fisher", version = 0.1)
public class Fisher extends Script {
	// fishing spot IDs
	private static final int CAGE_HARPOON_FISH_SPOT = 1519;
	private static final int NET_BAIT_FISH_SPOT = 1525;
	
	// individual areas to go to before looking for a fishing spot
	private static final Area DRAYNOR_FISHING_AREA_1 = new Area(3084, 3231, 3088, 3226);
	private static final Area CATHERBY_FISHING_AREA_1 = new Area(2835, 3434, 2841, 3432);
	private static final Area CATHERBY_FISHING_AREA_2 = new Area(2843, 3433, 2848, 3430);
	private static final Area CATHERBY_FISHING_AREA_3 = new Area(2851, 3428, 2857, 3423);
	private static final Area CATHERBY_FISHING_AREA_4 = new Area(2857, 3429, 2862, 3426);
	
	// all fishing locations for each area
	private static final Area[] DRAYNOR_FISHING_AREA = {DRAYNOR_FISHING_AREA_1};
	private static final Area[] CATHERBY_FISHING_AREA = {CATHERBY_FISHING_AREA_1, CATHERBY_FISHING_AREA_2, CATHERBY_FISHING_AREA_3, CATHERBY_FISHING_AREA_4};
	
	// banks
	private static final Area DRAYNOR_BANK = Banks.DRAYNOR;
	private static final Area CATHERBY_BANK = Banks.CATHERBY;
	
	// count fish caught
	private int shrimpCount;
	private int anchovieCount;
	private int tunaCount;
	private int swordfishCount;
	
	private boolean singleFishingArea;		// true if multiple areas for current location
	private int fishSpotId;					// id for current type of fishing spot
	private String fishingTool;				// what is needed to fish (like a harpoon)
	private String fishSpotInteractAction;	// action to use on fishing spot to begin fishing
	private Area currentBank;				// bank to use for current location
	private Area[] legalFishingAreas;		// areas to be in before looking for fishing spots
	
	private Object uiLock;	// block script until fishing mode is chosen in UI
	
	private State state;	// track state of bot
	private Mode mode;		// type of fishing to do - chosen in pop-up UI
	
	/**
	 * Modes of fishing that specify location and fish to catch
	 */
	public enum Mode {
		/**
		 * Catch shrimp and anchovies with a small net at Draynor and bank them
		 */
		DRAYNOR_SHRIMP_ANCHOVIES,
		
		/**
		 * Catch tuna and swordfish with a harpoon at Catherby and bank them 
		 */
		CATHERBY_TUNA_SWORDFISH
	}
	
	/**
	 * States used to control logical flow of bot
	 */
	private enum State {
		/**
		 * Initialize inventory and prepare for fishing
		 */
		INIT,
		
		/**
		 * Go to bank and deposit all fish
		 */
		BANK,
		
		/**
		 * Go to fishing area and begin fishing
		 */
		FISH,
		
		/**
		 * Sleep process until player is idle
		 */
		WAIT
	}
	
	/**
	 * Perform setup before bot begins running
	 * 
	 * Show a UI allowing the user to choose the location to fish at 
	 * and perform general setup for that location.
	 * 
	 * This method is automatically called by the OSBot client.  Do not call explicitly.
	 */
	@Override
	public void onStart() {
		// create a new UI to choose the mode of fishing
		uiLock = new Object();
		FisherUI ui = new FisherUI(this, uiLock);
		
		// initialize mode to null so we know if the UI sets it or not
		mode = null;
		
		// block current thread until UI chooses mode
		synchronized(uiLock) {
			try {
				uiLock.wait();
			} catch (InterruptedException e) {
				log(e.getMessage());
			}
		}
		
		// UI closed without choosing a mode, so just exit
		if(mode == null) {
			stop(false);
		}
		
		// get rid of the UI since we don't need it anymore
		ui.dispose();
		
		
		// initialize fish caught counters
		shrimpCount = 0;
		anchovieCount = 0;
		tunaCount = 0;
		swordfishCount = 0;
		
		// start tracking fishing XP gained
		experienceTracker.start(Skill.FISHING);

		state = State.INIT;
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
				initInventory();
				break;
			case BANK:
				bank();
				break;
			case FISH:
				fish();
				break;
			case WAIT:
				waitUntilIdle();
				// bank if we have full inventory of fish
				if(inventory.isFull()) {
					state = State.BANK;
				} else {
					state = State.FISH;
				}
				break;
			default:
		}
		return random(500, 2000);
	}
	
	/**
	 * Perform cleanup and exit operations on bot exit
	 * 
	 * Report XP gained to overall tracking server and print other relevant
	 * information about the bot run to the logger.
	 * 
	 * This method is automatically called by the OSBot client.  Do not call explicitly.
	 */
	@Override
	public void onExit() {
		// report XP gained to server
		try {
			XPReporter.reportXP(Skill.FISHING, experienceTracker.getGainedXP(Skill.FISHING));
		} catch (IOException e) {}
		
		switch(mode) {
			case DRAYNOR_SHRIMP_ANCHOVIES:
				log("Shrimp caught: " + shrimpCount);
				log("Anchovies caught: " + anchovieCount);
				break;
			case CATHERBY_TUNA_SWORDFISH:
				log("Tuna caught: " + tunaCount);
				log("Swordfish caught: " + swordfishCount);
				break;
		}
		
		log("Fishing XP Gained: " + experienceTracker.getGainedXP(Skill.FISHING));
		log("Fishing Levels Gained: " + experienceTracker.getGainedLevels(Skill.FISHING));
		log("Fishing XP/hr: " + experienceTracker.getGainedXPPerHour(Skill.FISHING));
	}
	
	/**
	 * Process messages in the Runescape message dialog
	 * 
	 * Watch for messages about catching fish and use them to count the total
	 * number of fish caught.
	 * 
	 * This method is automatically called by the OSBot client.  Do not call explicitly.
	 */
	@Override
	public void onMessage(Message m) {
		switch(m.getMessage()) {
			case "You catch some shrimps.":
				shrimpCount++;
				break;
			case "You catch some anchovies.":
				anchovieCount++;
				break;
			case "You catch a tuna.":
				tunaCount++;
				break;
			case "You catch a swordfish.":
				swordfishCount++;
				break;
		}
	}
	
	/**
	 * Draw information about the bot's status in a banner on the top of the screen.
	 * 
	 * This method is automatically called by the OSBot client.  Do not call explicitly.
	 */
	@Override
	public void onPaint(Graphics2D g) {
		// don't start drawing the banner until a mode has been chosen
		if(mode == null) {
			return;
		}
		
		// get status of skill level and XP
		int xpGained = experienceTracker.getGainedXP(Skill.FISHING);
		int xpPerHour = experienceTracker.getGainedXPPerHour(Skill.FISHING);
		int levelsGained = experienceTracker.getGainedLevels(Skill.FISHING);
		
		super.onPaint(g);
		
		// draw background of banner
		g.setColor(new Color(0x093145));
		g.fillRect(0, 0, 515, 50);
		
		// set color of words on banner
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 14));
		
		// draw fish caught
		switch(mode) {
			case DRAYNOR_SHRIMP_ANCHOVIES:
				g.drawString("Shrimp Caught: " + formatXpVal(shrimpCount), 5, 20);
				g.drawString("Anchovies Caught: " + formatXpVal(anchovieCount), 5, 40);
				break;
			case CATHERBY_TUNA_SWORDFISH:
				g.drawString("Tuna Caught: " + formatXpVal(tunaCount), 5, 20);
				g.drawString("Swordfish Caught: " + formatXpVal(swordfishCount), 5, 40);
				break;
		}
		
		// draw generic info about levels and XP
		g.drawString("Current Fishing Level: " + skills.getStatic(Skill.FISHING), 180, 20);
		g.drawString("Levels Gained: " + levelsGained, 180, 40);
		g.drawString("XP Gained: " + formatXpVal(xpGained), 380, 20);
		g.drawString("XP/hr: " + formatXpVal(xpPerHour), 380, 40);
	}
	
	/**
	 * Set mode of fishing for bot
	 * 
	 * Set the current location and type of fishing to do.  This method is
	 * intended to be called only before the bot actually begins running.
	 * 
	 * @param mode Mode indicating location and type of fishing to do
	 */
	public void setMode(Mode mode) {
		this.mode = mode;
		
		switch(mode) {
			case DRAYNOR_SHRIMP_ANCHOVIES:
				singleFishingArea = true;
				fishSpotId = NET_BAIT_FISH_SPOT;
				fishingTool = "Small fishing net";
				fishSpotInteractAction = "Net";
				currentBank = DRAYNOR_BANK;
				legalFishingAreas = DRAYNOR_FISHING_AREA;
				break;
			case CATHERBY_TUNA_SWORDFISH:
				singleFishingArea = false;
				fishSpotId = CAGE_HARPOON_FISH_SPOT;
				fishingTool = "Harpoon";
				fishSpotInteractAction = "Harpoon";
				currentBank = CATHERBY_BANK;
				legalFishingAreas = CATHERBY_FISHING_AREA;
				break;
		}
	}
	
	/**
	 * Go to bank and prepare inventory to start fishing
	 * 
	 * Move player to bank and deposit all items except what is
	 * needed for fishing.  Once complete proceed to the FISH state.
	 * 
	 * @throws InterruptedException
	 */
	private void initInventory() throws InterruptedException {
		walkToBank();
		
		if(!bank.isOpen()) {
			bank.open();
			sleep(random(500, 1000));
		}
		
		// empty inventory
		bank.depositAllExcept(fishingTool);
		sleep(random(500, 1000));
		
		// check if tool already in inventory
		if(!inventory.contains(fishingTool)) {
			// check if player has a fishing tool in bank
			if(bank.contains(fishingTool)) {
				bank.withdraw(fishingTool, 1);
				sleep(random(500, 1000));
			} else {
				// notify player that they don't have the items to fish and exit
				JOptionPane.showMessageDialog(null, "No " + fishingTool + " found in inventory or bank");
				stop(false);
			}
		}
		
		bank.close();
		
		state = State.FISH;
	}
	
	/**
	 * Walk to bank
	 * 
	 * Walk player to bank and only return once player is successfully standing
	 * in the bank
	 */
	@SuppressWarnings("deprecation")
	private void walkToBank() {
		while(!currentBank.contains(myPlayer())) {
			LocalPathFinder pathFinder = new LocalPathFinder(this.bot);
			LinkedList<Position> path = pathFinder.findPath(myPlayer().getPosition(), currentBank.getRandomPosition());
			if(path != null) {
				localWalker.walkPath(path);
				waitUntilIdle();
			}
		}
	}
	
	/**
	 * Go to bank and deposit all fish
	 * 
	 * Everything in the inventory except the fishing tool will be deposited.
	 * 
	 * @throws InterruptedException
	 */
	private void bank() throws InterruptedException {
		walkToBank();
		
		if(!bank.isOpen()) {
			bank.open();
			sleep(random(500, 1000));
		}
		
		// deposit all fish
		bank.depositAllExcept(fishingTool);
		sleep(random(500, 1000));
		bank.close();
		
		state = State.FISH;
	}
	
	/**
	 * Go to fishing area and begin fishing
	 * 
	 * Walk to the fishing area and look for fishing spots.  Once fishing
	 * proceed to IDLE state.
	 * 
	 * @throws InterruptedException
	 */
	private void fish() throws InterruptedException {
		// check if we're in a fishing area
		if(inFishingArea()) {
			NPC fishingSpot = npcs.closest(fishSpotId);
			
			// fish is a fishing spot is available
			if(fishingSpot != null) {
				fishingSpot.interact(fishSpotInteractAction);
				state = State.WAIT;
			} else {
				// check if there is another fishing area to go to
				if(singleFishingArea) {
					// no other fishing area so just wait for a spot to appear
					waitForFishingSpot();
				} else {
					goToNextFishingArea();
				}
			}
		} else {
			// not in fishing area so go there
			goToNextFishingArea();
		}
	}
	
	/**
	 * Wait until the player is idle
	 * 
	 * Return only when the player is not moving or animating
	 */
	private void waitUntilIdle() {
		new ConditionalSleep(Integer.MAX_VALUE, random(2000, 3000)) {
			@Override
			public boolean condition() throws InterruptedException {
				return !myPlayer().isMoving() && !myPlayer().isAnimating();
			}
		}.sleep();
	}
	
	/**
	 * Wait for a fishing spot to appear
	 * 
	 * Return only when a usable fishing spot has appeared
	 */
	private void waitForFishingSpot() {
		new ConditionalSleep(Integer.MAX_VALUE, random(2000, 3000)) {
			@Override
			public boolean condition() throws InterruptedException {
				return npcs.closest(fishSpotId) != null;
			}
		}.sleep();
	}
	
	/**
	 * Check if a player is in a legal fishing area
	 * 
	 * @return True if the player is standing in one of the legal fishing areas
	 */
	private boolean inFishingArea() {
		for(Area area : legalFishingAreas) {
			if(area.contains(myPlayer())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Walk to the next fishing area in the list of legal fishing areas
	 * 
	 * Move the player to the next fishing area and return only when the
	 * player is successfully standing in that area
	 */
	@SuppressWarnings("deprecation")
	private void goToNextFishingArea() {
		Area next = getNextFishingArea();
		
		if(next != null) {
			while(!next.contains(myPlayer())) {
				LocalPathFinder pathFinder = new LocalPathFinder(this.bot);
				LinkedList<Position> path = pathFinder.findPath(myPlayer().getPosition(), next.getRandomPosition());
				if(path != null) {
					localWalker.walkPath(path);
					waitUntilIdle();
				}
			}
		}
	}
	
	/**
	 * Get the next fishing area in the list of legal fishing areas
	 * 
	 * Find the next fishing area the player should go to.  If at the end
	 * of the array, loop back to the first location in the array.
	 * 
	 * @return Next fishing area player should walk to
	 */
	private Area getNextFishingArea() {
		Area next = legalFishingAreas[0];
		
		for(int i = 0; i < legalFishingAreas.length; i++) {
			if(legalFishingAreas[i].contains(myPlayer())) {
				next = legalFishingAreas[(i + 1) % legalFishingAreas.length];
				break;
			}
		}
		
		return next;
	}
	
	
	/**
	 * Format a value to use Runescape "k" notation if large enough
	 * 
	 * If the value is greater than 1000, it will be divided by 1000 and a
	 * "k" will be appended.  If not, the value will remain unchanged.
	 * 
	 * @param val Value to format
	 * @return Input value formatted to Runescape's "k" notation
	 */
	private String formatXpVal(int val) {
		String ret;
		
		if(val > 1000) {
			ret = String.format("%.1fk", (double) val / 1000);
		} else {
			ret = String.valueOf(val);
		}
		
		return ret;
	}
}
