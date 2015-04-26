/*
 * Copyright (C) 2015 AMIS research group, Faculty of Mathematics and Physics, Charles University in Prague, Czech Republic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.fido.ctfbot;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.bot.params.UT2004BotParameters;
import cz.cuni.amis.pogamut.ut2004.teamcomm.server.UT2004TCServer;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class AgentRunner {
	
	public static final String TEAM_NAME = "TeamCTF";
	
	/**
	 * server for team communication
	 */
	public static UT2004TCServer tcServer;
	
	private static String year;
	
	private static int team;
	
	private static int skill;
	
	private static int numberOfBotsInTeam;
	
	private static String server;
	
	private static int port = 3011;
	
    /**
     * This method is called when the bot is started either from IDE or from command line.
     * @param args
     */
    public static void main(String args[]) throws PogamutException {
		
//		for (int i = 0; i < args.length; i++) {
//			System.out.println(args[i]);
//		}
		
		year = args[0];
		team = Integer.parseInt(args[1]);
		skill =  Integer.parseInt(args[2]);
		numberOfBotsInTeam = Integer.parseInt(args[3]);
		server = args[4];
    	tcServer = UT2004TCServer.startTCServer(server, port);

		new UT2004BotRunner<UT2004Bot, UT2004BotParameters>(CTFChampion.class, TEAM_NAME).setMain(true).setHost(server).setPort(3000).setLogLevel(Level.WARNING).startAgents(           
                new CTFChampionBotParams().setSkillLevel(skill).setTeam(team),
                new CTFChampionBotParams().setSkillLevel(skill).setTeam(team),
				new CTFChampionBotParams().setSkillLevel(skill).setTeam(team)
		);
    }
}
