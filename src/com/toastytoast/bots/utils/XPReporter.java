package com.toastytoast.bots.utils;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.osbot.rs07.api.ui.Skill;


public class XPReporter {
	private static final String BASE_URL = "http://osrs-bot-metrics.azurewebsites.net/";
	
	public static void reportXP(Skill skill, int xpGained) throws IOException {
		int skillID = SkillUtils.getSkillID(skill);
		String skillName = SkillUtils.getSkillName(skill);
		
		String url = BASE_URL + "api/skills/" + skillID;
		URL urlObject = new URL(url);
		
		HttpURLConnection conn = (HttpURLConnection) urlObject.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestMethod("POST");
		
		String jsonOutput = "{id:" + skillID + ",skill:\"" + skillName + "\",xp:" + xpGained + "}";
		
		OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
		os.write(jsonOutput);
		os.flush();
		os.close();
		
		conn.getResponseCode();
		
		conn.disconnect();
	}
}
