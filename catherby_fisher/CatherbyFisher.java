import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.IOException;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author="ToastyToast", info = "", logo = "", name = "Catherby Fisher", version = 0.1)
public class CatherbyFisher extends Script {
	private static final int CAGE_HARPOON_FISH_SPOT = 1519;
	private static final int NET_HARPOON_FISH_SPOT = 1520;
	private static final Area FISHING_AREA_1 = new Area(2835, 3434, 2841, 3432);
	private static final Area FISHING_AREA_2 = new Area(2843, 3433, 2848, 3430);
	private static final Area FISHING_AREA_3 = new Area(2851, 3428, 2857, 3423);
	private static final Area FISHING_AREA_4 = new Area(2857, 3429, 2862, 3426);
	private static final Area CATHERBY_BANK = Banks.CATHERBY;
	
	private Area currentFishingArea;
	private int tunaCount;
	private int swordfishCount;

	private enum State {
		FISH,
		BANK
	}
	
	@Override
	public void onStart() {
		tunaCount = 0;
		swordfishCount = 0;
		experienceTracker.start(Skill.FISHING);
	}
	
	@Override
	public int onLoop() throws InterruptedException {
		switch(getState()) {
			case BANK:
				bank();
				break;
			case FISH:
				fish();
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
		
		log("Tuna caught: " + tunaCount);
		log("Swordfish caught: " + swordfishCount);
		log("Fishing XP Gained: " + experienceTracker.getGainedXP(Skill.FISHING));
		log("Fishing Levels Gained: " + experienceTracker.getGainedLevels(Skill.FISHING));
		log("Fishing XP/hr: " + experienceTracker.getGainedXPPerHour(Skill.FISHING));
	}
	
	@Override
	public void onMessage(Message m) {
		if(m != null) {
			if("You catch a tuna.".equals(m.getMessage())) {
				tunaCount++;
			} else if("You catch a swordfish.".equals(m.getMessage())) {
				swordfishCount++;
			}
		}
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		int xpGained;
		int xpPerHour;
		int levelsGained;
		
		xpGained = experienceTracker.getGainedXP(Skill.FISHING);
		xpPerHour = experienceTracker.getGainedXPPerHour(Skill.FISHING);
		levelsGained = experienceTracker.getGainedLevels(Skill.FISHING);
		
		g.setColor(new Color(0x093145));
		g.fillRect(0, 0, 515, 50);
		
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 14));
		
		g.drawString("Tuna Caught: " + formatLargeValue(tunaCount), 5, 20);
		g.drawString("Swordfish Caught: " + formatLargeValue(swordfishCount), 5, 40);
		g.drawString("Current Fishing Level: " + skills.getStatic(Skill.FISHING), 180, 20);
		g.drawString("Levels Gained: " + levelsGained, 180, 40);
		g.drawString("XP Gained: " + formatLargeValue(xpGained), 380, 20);
		g.drawString("XP/hr: " + formatLargeValue(xpPerHour), 380, 40);
	}
	
	@SuppressWarnings("deprecation")
	private void bank() throws InterruptedException {
		currentFishingArea = null;
		if(!myPlayer().isAnimating()) {
			if(CATHERBY_BANK.contains(myPlayer())) {
				if(bank.isOpen()) {
					bank.depositAllExcept("Harpoon");
					sleep(random(500, 1000));
					bank.close();
				} else {
					bank.open();
				}
			} else {
				localWalker.walk(CATHERBY_BANK.getRandomPosition(), false);
			}
		}
	}
	
	private void fish() {
		if(!myPlayer().isAnimating()) {
			if(inFishingArea()) {
				NPC fishingSpot = npcs.closest(CAGE_HARPOON_FISH_SPOT);
				if(fishingSpot != null) {
					fishingSpot.interact("Harpoon");
				} else {
					goToNextFishingArea();
				}
			} else {
				goToNextFishingArea();
			}
		}
	}
	
	private boolean inFishingArea() {
		return FISHING_AREA_1.contains(myPlayer()) || FISHING_AREA_2.contains(myPlayer()) ||
				FISHING_AREA_3.contains(myPlayer()) || FISHING_AREA_4.contains(myPlayer());
	}
	
	@SuppressWarnings("deprecation")
	private void goToNextFishingArea() {
		if(currentFishingArea == null) {
			localWalker.walk(FISHING_AREA_1.getRandomPosition(), true);
			currentFishingArea = FISHING_AREA_1;
		} else if(currentFishingArea.equals(FISHING_AREA_1)) {
			localWalker.walk(FISHING_AREA_2.getRandomPosition(), true);
			currentFishingArea = FISHING_AREA_2;
		} else if(currentFishingArea.equals(FISHING_AREA_2)) {
			localWalker.walk(FISHING_AREA_3.getRandomPosition(), true);
			currentFishingArea = FISHING_AREA_3;
		} else if(currentFishingArea.equals(FISHING_AREA_3)) {
			localWalker.walk(FISHING_AREA_4.getRandomPosition(), true);
			currentFishingArea = FISHING_AREA_4;
		} else if(currentFishingArea.equals(FISHING_AREA_4)) {
			localWalker.walk(FISHING_AREA_1.getRandomPosition(), true);
			currentFishingArea = FISHING_AREA_1;
		}
	}
	
	private State getState() {
		if(getInventory().isFull()) {
			return State.BANK;
		} else {
			return State.FISH;
		}
	}
	
	private String formatLargeValue(int val) {
		String ret;
		
		if(val > 1000) {
			ret = String.format("%.1fk", (double) val / 1000);
		} else {
			ret = String.valueOf(val);
		}
		
		return ret;
	}
}
