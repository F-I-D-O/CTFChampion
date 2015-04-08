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
	
	/**
	 * server for team communication
	 */
	public static UT2004TCServer tcServer;
	
    /**
     * This method is called when the bot is started either from IDE or from command line.
     * @param args
     */
    public static void main(String args[]) throws PogamutException {
		
		// Start TC (~ TeamCommunication) Server first.
    	tcServer = UT2004TCServer.startTCServer();
		
//        UT2004BotRunner br = new UT2004BotRunner(     // class that wrapps logic for bots executions, suitable to run single bot in single JVM
//                CTFChampion.class,  // which UT2004BotController it should instantiate
//                "CTF Champion"       // what name the runner should be using
//        ).setMain(true);          // tells runner that is is executed inside MAIN method, thus it may block the thread and watch whether agent/s are correctly executed
//		br.startAgents(3).;          // tells the runner to start 1 agent
		
		new UT2004BotRunner<UT2004Bot, UT2004BotParameters>(CTFChampion.class, "TeamCTF").setMain(true).setHost("localhost").setPort(3000).setLogLevel(Level.INFO).startAgents(
                new UT2004BotParameters().setTeam(2),
                new UT2004BotParameters().setTeam(2),
				new UT2004BotParameters().setTeam(2)
		);
    }
}
