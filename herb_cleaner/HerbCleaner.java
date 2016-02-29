import java.io.IOException;

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author="ToastyToast", info = "", logo = "", name = "Herb Cleaner", version = 0.1)
public class HerbCleaner extends Script {
	private static final String[] GRIMY_HERBS = {
		"Grimy guam leaf",
		"Grimy marrentill",
		"Grimy tarromin",
		"Grimy harralander",
		"Grimy ranarr weed",
		"Grimy toadflax",
		"Grimy irit leaf",
		"Grimy avantoe",
		"Grimy kwuarm",
		"Grimy snapdragon",
		"Grimy cadantine",
		"Grimy lantadyme",
		"Grimy dwarf weed",
		"Grimy torstol"
	};
	
	private static final int[] HERB_LEVELS = {
		3,
		5,
		11,
		20,
		25,
		30,
		40,
		48,
		54,
		59,
		65,
		67,
		70,
		75
	};
	
	private State state;

	private enum State {
		CLEANING,
		BANKING
	}
	
	@Override
	public void onStart() {
		state = State.BANKING;
		experienceTracker.start(Skill.HERBLORE);
	}
	
	@Override
	public int onLoop() throws InterruptedException {
		switch(state) {
			case CLEANING:
				cleanHerbs();
				break;
			case BANKING:
				bankHerbs();
				break;
			default:
		}
		return random(250, 2000);
	}
	
	@Override
	public void onExit() {
		try {
			XPReporter.reportXP(Skill.HERBLORE, experienceTracker.getGainedXP(Skill.HERBLORE));
		} catch (IOException e) {}
	}
	
	private void cleanHerbs() throws InterruptedException {
		if(!myPlayer().isAnimating()) {
			int herbCount = (int) inventory.getAmount(GRIMY_HERBS);
			for(int inventorySlot = 0; inventorySlot < herbCount; inventorySlot++) {
				mouse.move(inventory.getMouseDestination(inventorySlot));
				mouse.click(false);
				sleep(random(150, 400));
			}
			state = State.BANKING;
		}
	}
	
	private void bankHerbs() throws InterruptedException {
		if(!myPlayer().isAnimating()) {
			if(bank.isOpen()) {
				if(!inventory.isEmpty()) {
					bank.depositAll();
					sleep(random(500, 1500));
				}
				for(int i = 0; i < GRIMY_HERBS.length; i++) {
					if(bank.contains(GRIMY_HERBS[i]) && skills.getDynamic(Skill.HERBLORE) >= HERB_LEVELS[i]) {
						bank.withdrawAll(GRIMY_HERBS[i]);
						if(inventory.isFull()) {
							state = State.CLEANING;
							break;
						}
					}
				}
				
				sleep(random(500, 2000));
				bank.close();
				if(inventory.isEmpty()) {
					stop(false);
				} else {
					state = State.CLEANING;
				}
			} else {
				NPC banker = npcs.closest("Banker", "Bank booth");
				if(banker != null) {
					banker.interact("Bank");
				}
			}
		}
	}
}
