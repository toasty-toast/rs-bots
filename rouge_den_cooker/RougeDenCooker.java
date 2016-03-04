import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.IOException;

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

@ScriptManifest(author="ToastyToast", info = "", logo = "", name = "Rouge's Den Cooker", version = 0.1)
public class RougeDenCooker extends Script {
	private int tunaCount;
	private int swordfishCount;
	private String currentItem;
	private State state;
	
	private final ConditionalSleep cooking = new ConditionalSleep(random(1500, 2000)) {
		@Override
		public boolean condition() throws InterruptedException {
			return myPlayer().isAnimating();
		}
	};
	
	private enum State {
		COOK,
		BANK
	}
	
	@Override
	public void onStart() {
		state = State.BANK;
		tunaCount = 0;
		swordfishCount = 0;
		currentItem = null;
		experienceTracker.start(Skill.COOKING);
	}
	
	@Override
	public int onLoop() throws InterruptedException {
		switch(state) {
			case BANK:
				bank();
				break;
			case COOK:
				cook();
				break;
			default:
		}
		return random(500, 2000);
	}
	
	@Override
	public void onExit() {
		try {
			XPReporter.reportXP(Skill.COOKING, experienceTracker.getGainedXP(Skill.COOKING));
		} catch (IOException e) {}
	}
	
	@Override
	public void onMessage(Message m) {
		if(m != null) {
			if("You manage to cook a tuna.".equals(m.getMessage())) {
				tunaCount++;
			} else if("You successfully cook a swordfish.".equals(m.getMessage())) {
				swordfishCount++;
			}
		}
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		int xpGained;
		int xpPerHour;
		int levelsGained;
		
		xpGained = experienceTracker.getGainedXP(Skill.COOKING);
		xpPerHour = experienceTracker.getGainedXPPerHour(Skill.COOKING);
		levelsGained = experienceTracker.getGainedLevels(Skill.COOKING);
		
		g.setColor(new Color(0x093145));
		g.fillRect(0, 0, 515, 50);
		
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 14));
		
		g.drawString("Tuna Cooked: " + formatXpVal(tunaCount), 5, 20);
		g.drawString("Swordfish Cooked: " + formatXpVal(swordfishCount), 5, 40);
		g.drawString("Current Cooking Level: " + skills.getStatic(Skill.FISHING), 180, 20);
		g.drawString("Levels Gained: " + levelsGained, 180, 40);
		g.drawString("XP Gained: " + formatXpVal(xpGained), 380, 20);
		g.drawString("XP/hr: " + formatXpVal(xpPerHour), 380, 40);
	}
	
	private void cook() throws InterruptedException {
		if(!cooking.sleep()) {
			if(dialogues.inDialogue()) {
				RS2Widget cookWidget = widgets.getWidgetContainingText(currentItem);
				if(cookWidget != null) {
					cookWidget.interact("Cook All");
				}
			} else {
				if(inventory.contains(currentItem)) {
					if(inventory.getItem(currentItem).interact("Use")) {
						RS2Object fire = objects.closest("Fire");
						if(fire != null) {
							fire.interact("Use");
						}
					}
				} else {
					currentItem = null;
					state = State.BANK;
				}
			}
		}
	}
	
	private void bank() throws InterruptedException {
		if(!myPlayer().isAnimating()) {
			if(bank.isOpen()) {
				if(!inventory.isEmpty()) {
					bank.depositAll();
					sleep(random(500, 1500));
				}
				
				if(bank.contains("Raw tuna")) {
					currentItem = "Raw tuna";
				} else if (bank.contains("Raw swordfish")) {
					currentItem = "Raw swordfish";
				}
				
				if(currentItem != null) {
					bank.withdrawAll(currentItem);
					sleep(random(500, 2000));
					bank.close();
					state = State.COOK;
				} else {
					bank.close();
					stop();
				}
			} else {
				NPC banker = npcs.closest("Emerald Benedict");
				if(banker != null) {
					banker.interact("Bank");
				}
			}
		}
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
