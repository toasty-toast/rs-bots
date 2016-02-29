import java.util.HashMap;
import java.util.Map;

import org.osbot.rs07.api.ui.Skill;


public class SkillUtils {
	@SuppressWarnings("serial")
	private static final Map<Skill, Integer> SKILL_ID_LOOKUP = new HashMap<Skill, Integer>() {{
		put(Skill.ATTACK, 1);
		put(Skill.STRENGTH, 2);
		put(Skill.DEFENCE, 3);
		put(Skill.RANGED, 4);
		put(Skill.PRAYER, 5);
		put(Skill.MAGIC, 6);
		put(Skill.RUNECRAFTING, 7);
		put(Skill.HITPOINTS, 8);
		put(Skill.CRAFTING, 9);
		put(Skill.MINING, 10);
		put(Skill.SMITHING, 11);
		put(Skill.FISHING, 12);
		put(Skill.COOKING, 13);
		put(Skill.FIREMAKING, 14);
		put(Skill.WOODCUTTING, 15);
		put(Skill.AGILITY, 16);
		put(Skill.HERBLORE, 17);
		put(Skill.THIEVING, 18);
		put(Skill.FLETCHING, 19);
		put(Skill.SLAYER, 20);
		put(Skill.FARMING, 21);
		put(Skill.CONSTRUCTION, 22);
		put(Skill.HUNTER, 23);
	}};
	
	@SuppressWarnings("serial")
	private static final Map<Skill, String> SKILL_NAME_LOOKUP = new HashMap<Skill, String>() {{
		put(Skill.ATTACK, "Attack");
		put(Skill.STRENGTH, "Strength");
		put(Skill.DEFENCE, "Defence");
		put(Skill.RANGED, "Ranged");
		put(Skill.PRAYER, "Prayer");
		put(Skill.MAGIC, "Magic");
		put(Skill.RUNECRAFTING, "Runecrafting");
		put(Skill.HITPOINTS, "Hitpoints");
		put(Skill.CRAFTING, "Crafting");
		put(Skill.MINING, "Mining");
		put(Skill.SMITHING, "Smithing");
		put(Skill.FISHING, "Fishing");
		put(Skill.COOKING, "Cooking");
		put(Skill.FIREMAKING, "Firemaking");
		put(Skill.WOODCUTTING, "Woodcutting");
		put(Skill.AGILITY, "Agility");
		put(Skill.HERBLORE, "Herblore");
		put(Skill.THIEVING, "Thieving");
		put(Skill.FLETCHING, "Fletching");
		put(Skill.SLAYER, "Slayer");
		put(Skill.FARMING, "Farming");
		put(Skill.CONSTRUCTION, "Construction");
		put(Skill.HUNTER, "Hunter");
	}};
	
	public static int getSkillID(Skill skill) {
		return SKILL_ID_LOOKUP.get(skill);
	}
	
	public static String getSkillName(Skill skill) {
		return SKILL_NAME_LOOKUP.get(skill);
	}
}
