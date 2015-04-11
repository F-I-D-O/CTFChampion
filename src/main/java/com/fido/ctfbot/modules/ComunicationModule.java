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
package com.fido.ctfbot.modules;

import com.fido.ctfbot.CTFChampion;
import com.fido.ctfbot.Goal;
import com.fido.ctfbot.informations.InformationBase;
import com.fido.ctfbot.informations.players.FriendInfo;
import com.fido.ctfbot.informations.InfoType;
import com.fido.ctfbot.messages.CommandMessage;
import com.fido.ctfbot.messages.LocationMessage;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004TCClient;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class ComunicationModule extends CTFChampionModule {
	
	private UT2004TCClient teamComClient;

	public ComunicationModule(CTFChampion bot, LogCategory log, UT2004TCClient tcClient, InformationBase informationBase) {
		super(bot, log, informationBase);
		teamComClient = tcClient;
	}


	
	public boolean sendCommand(FriendInfo friend, Goal goal){
		if(!isTeamComReady()){
			return false;
		}
		if(!bot.isLeader()){
			log.log(Level.WARNING, "Only leader can send commands: [sendCommand()]");
			return false;
		}
		
		log.log(Level.INFO, "Sending command: {0} for {1} [sendCommand()]", 
				new String[]{goal.toString(), friend.getName()});
		return teamComClient.sendToTeam(new CommandMessage(friend, goal));
	}

	private boolean isTeamComReady() {
		if(teamComClient.isConnected()){
			return true;
		}
		else{
			log.log(Level.INFO, "TeamCom clien is not ready yet: [isTeamComReady()]");
			return false;
		}
	}

	public void sendMyLocationMessage() {
		if(!isTeamComReady()){
			return;
		}
		
		log.log(Level.INFO, "Sending bot location message: [sendMyLocationMessage()]");
		teamComClient.sendToTeam(new LocationMessage(informationBase.getInfo().getLocation(), 
				informationBase.getInfo().getId(), InfoType.FRIEND));
	}
}
