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

@ScriptManifest(author="ToastyToast", info = "", logo = "", name = "Fisher", version = 0.1)
public class Fisher extends Script {
	private static final int CAGE_HARPOON_FISH_SPOT = 1519;
	private static final int NET_BAIT_FISH_SPOT = 1525;
	
	private static final Area DRAYNOR_FISHING_AREA_1 = new Area(3084, 3231, 3088, 3226);
	private static final Area CATHERBY_FISHING_AREA_1 = new Area(2835, 3434, 2841, 3432);
	private static final Area CATHERBY_FISHING_AREA_2 = new Area(2843, 3433, 2848, 3430);
	private static final Area CATHERBY_FISHING_AREA_3 = new Area(2851, 3428, 2857, 3423);
	private static final Area CATHERBY_FISHING_AREA_4 = new Area(2857, 3429, 2862, 3426);
	
	private static final Area[] DRAYNOR_FISHING_AREA = {DRAYNOR_FISHING_AREA_1};
	private static final Area[] CATHERBY_FISHING_AREA = {CATHERBY_FISHING_AREA_1, CATHERBY_FISHING_AREA_2, CATHERBY_FISHING_AREA_3, CATHERBY_FISHING_AREA_4};
	
	private static final Area DRAYNOR_BANK = Banks.DRAYNOR;
	private static final Area CATHERBY_BANK = Banks.CATHERBY;
	
	private int shrimpCount;
	private int anchovieCount;
	private int tunaCount;
	private int swordfishCount;
	
	private boolean singleFishingArea;
	private int fishSpotId;
	private String fishingTool;
	private String fishSpotInteractAction;
	private Area currentBank;
	private Area[] legalFishingAreas;
	
	private Object uiLock;
	
	private State state;
	private Mode mode;
	
	public enum Mode {
		DRAYNOR_SHRIMP_ANCHOVIES,
		CATHERBY_TUNA_SWORDFISH
	}
	
	private enum State {
		INIT,
		BANK,
		FISH,
		WAIT
	}
	
	@Override
	public void onStart() {
		uiLock = new Object();
		mode = null;
		FisherUI ui = new FisherUI(this, uiLock);
		
		synchronized(uiLock) {
			try {
				uiLock.wait();
			} catch (InterruptedException e) {
				log(e.getMessage());
			}
		}
		
		if(mode == null) {
			stop(false);
		}
		
		ui.dispose();
		
		shrimpCount = 0;
		anchovieCount = 0;
		tunaCount = 0;
		swordfishCount = 0;
		
		state = State.INIT;
		
		experienceTracker.start(Skill.FISHING);
	}
	
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
	
	@Override
	public void onExit() {
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
	
	@Override
	public void onPaint(Graphics2D g) {
		if(mode == null) {
			return;
		}
		
		int xpGained;
		int xpPerHour;
		int levelsGained;
		
		xpGained = experienceTracker.getGainedXP(Skill.FISHING);
		xpPerHour = experienceTracker.getGainedXPPerHour(Skill.FISHING);
		levelsGained = experienceTracker.getGainedLevels(Skill.FISHING);
		
		super.onPaint(g);
		
		g.setColor(new Color(0x093145));
		g.fillRect(0, 0, 515, 50);
		
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 14));
		
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
		
		g.drawString("Current Fishing Level: " + skills.getStatic(Skill.FISHING), 180, 20);
		g.drawString("Levels Gained: " + levelsGained, 180, 40);
		g.drawString("XP Gained: " + formatXpVal(xpGained), 380, 20);
		g.drawString("XP/hr: " + formatXpVal(xpPerHour), 380, 40);
	}
	
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
	
	private void initInventory() throws InterruptedException {
		walkToBank();
		
		if(!bank.isOpen()) {
			bank.open();
			sleep(random(500, 1000));
		}
		
		bank.depositAllExcept(fishingTool);
		sleep(random(500, 1000));
		
		if(!inventory.contains(fishingTool)) {
			stop(false);
			if(bank.contains(fishingTool)) {
				bank.withdraw(fishingTool, 1);
				sleep(random(500, 1000));
			} else {
				JOptionPane.showMessageDialog(null, "No " + fishingTool + " found in inventory or bank");
			}
		}
		
		bank.close();
		
		state = State.FISH;
	}
	
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
	
	private void bank() throws InterruptedException {
		walkToBank();
		
		if(!bank.isOpen()) {
			bank.open();
			sleep(random(500, 1000));
		}
		
		bank.depositAllExcept(fishingTool);
		sleep(random(500, 1000));
		bank.close();
		
		state = State.FISH;
	}
	
	private void fish() throws InterruptedException {
		if(inFishingArea()) {
			NPC fishingSpot = npcs.closest(fishSpotId);
			if(fishingSpot != null) {
				fishingSpot.interact(fishSpotInteractAction);
				state = State.WAIT;
			} else {
				if(singleFishingArea) {
					waitForFishingSpot();
				} else {
					goToNextFishingArea();
				}
			}
		} else {
			goToNextFishingArea();
		}
	}
	
	private void waitUntilIdle() {
		new ConditionalSleep(Integer.MAX_VALUE, random(2000, 3000)) {
			@Override
			public boolean condition() throws InterruptedException {
				return !myPlayer().isMoving() && !myPlayer().isAnimating();
			}
		}.sleep();
	}
	
	private void waitForFishingSpot() {
		new ConditionalSleep(Integer.MAX_VALUE, random(2000, 3000)) {
			@Override
			public boolean condition() throws InterruptedException {
				return npcs.closest(fishSpotId) != null;
			}
		}.sleep();
	}
	
	private boolean inFishingArea() {
		for(Area area : legalFishingAreas) {
			if(area.contains(myPlayer())) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	private void goToNextFishingArea() {
		Area next = getNextFishingArea();
		
		if(next != null) {
			while(!next.contains(myPlayer())) {
				LocalPathFinder pathFinder = new LocalPathFinder(this.bot);
				LinkedList<Position> path = pathFinder.findPath(myPlayer().getPosition(), next.getRandomPosition());
				if(path != null) {
					localWalker.walkPath(path);
				}
				waitUntilIdle();
			}
		}
	}
	
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
