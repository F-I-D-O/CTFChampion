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

import com.fido.ctfbot.informations.FriendInfo;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathPlanner;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.CTF;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Players;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.floydwarshall.FloydWarshallMap;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import java.util.HashMap;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class InformationBase {
	
	private static double MAX_DISTANCE = 999999999;
	
	private final CTFChampion bot;
	
	private final LogCategory log;
	
	private final Players players;
	
	private final CTF ctfInfo;
	
	private final AgentInfo info;
	
	private final FloydWarshallMap fwMap;
	
	private final IUT2004Navigation navigation;
	
	
	private final HashMap<UnrealId, FriendInfo> friends;
	
	private final IPathPlanner mainPathPlanner;
	
	
	
	
	

	public CTFChampion getBot() {
		return bot;
	}

	public Players getPlayers() {
		return players;
	}

	public CTF getCtf() {
		return ctfInfo;
	}

	public IUT2004Navigation getNavigation() {
		return navigation;
	}
	
	
	

	public HashMap<UnrealId, FriendInfo> getFriends() {
		return friends;
	}
	
	
	
	
	
	public InformationBase(CTFChampion bot, LogCategory log, Players players, CTF ctfInfo, IPathPlanner mainPathPlanner,
			AgentInfo info, FloydWarshallMap fwMap, IUT2004Navigation navigation) {
		this.log = log;
		this.bot = bot;
		this.players = players;
		this.ctfInfo = ctfInfo;
		this.info = info;
		this.fwMap = fwMap;
		this.navigation = navigation;
		
		this.mainPathPlanner = mainPathPlanner;
		friends = new HashMap<UnrealId, FriendInfo>();
	}

	
	
	
	public void addPlayer(UnrealId id) {
		Player friend = players.getPlayer(id);
		if(players.getFriends().get(id) != null || info.getId() == id){
			friends.put(id, new FriendInfo(friend, players));
		}
	}
	
	public void addSelf() {
		friends.put(info.getId(), new FriendInfo(info, players));
	}
	
	public void addPlayersAlreadyInGame(){
		for(Player friend : players.getFriends().values()){
			friends.put(friend.getId(), new FriendInfo(friend, players));
		}
	}
	
	public UnrealId getNearestFriendTo(Location nearestTo, HashMap<UnrealId, FriendInfo> friends){
		double minDistance = getDistance(info.getLocation(), nearestTo);
		UnrealId minDistanceFriendId = info.getId();
		for(FriendInfo friend : friends.values()){
			log.log(Level.INFO, "player {0} checked for distance: [getNearestFriendTo()]", friend.getName());
			double distance = getDistance(friend.getBestLocation(), nearestTo);
			if(distance < minDistance){
				minDistance = distance;
				minDistanceFriendId = friend.getId();
			}
		}
		return minDistanceFriendId;
	}
	
	public UnrealId getNearestFriendTo(Location nearestTo){ 
		return getNearestFriendTo(nearestTo, friends);
	}
	
	public double getDistance(ILocated from, ILocated to){
//		if(mainPathPlanner instanceof FloydWarshallMap){		
			return fwMap.getDistance(info.getNearestNavPoint(from), info.getNearestNavPoint(to));
//		}
		
	}
	
	
	
}
