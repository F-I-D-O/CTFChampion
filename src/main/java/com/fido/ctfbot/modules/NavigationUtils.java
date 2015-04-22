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
import com.fido.ctfbot.Direction;
import com.fido.ctfbot.informations.InformationBase;
import com.fido.ctfbot.informations.players.FriendInfo;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.base3d.worldview.object.Velocity;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.floydwarshall.FloydWarshallMap;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import cz.cuni.amis.pogamut.ut2004.bot.command.AdvancedLocomotion;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class NavigationUtils extends CTFChampionModule {
	
	private static final int JUMP_DISTANCE = 200;
	
	
	
	
	private final FloydWarshallMap fwMap;
	
	private final NavPoints navPoints;
	
	private final UT2004Navigation navigation;
			
	private final NavMeshNavigation nmNav;
	
	private final AgentInfo info;
	
	private final AdvancedLocomotion move;
	
	
	
	
	

	public NavigationUtils(CTFChampion bot, LogCategory log, InformationBase informationBase, FloydWarshallMap fwMap, 
			NavPoints navPoints, UT2004Navigation navigation, NavMeshNavigation nmNav) {
		super(bot, log, informationBase);
		this.fwMap = fwMap;
		this.navPoints = navPoints;
		this.navigation = navigation;
		this.nmNav = nmNav;
		info = bot.getInfo();
		move = bot.getMove();
	}
	
	public double getDistance(Location location1, Location location2){
		return fwMap.getDistance(getNearestNavpoint(location1), getNearestNavpoint(location2));
	}
	
	public NavPoint getNearestNavpoint(Location location){
		return navPoints.getNearestNavPoint(location);
	}

	public boolean pathExist(NavPoint navPoint1, NavPoint navPoint2) {
		return fwMap.reachable(navPoint1, navPoint2);
	}

	private boolean useNavMesh(){
		return bot.getMainNavigation() instanceof NavMeshNavigation;
	}
	
	public void navigate(Location target){
		log.log(Level.INFO, "Navigating to {0} [navigate()]", target);
		bot.getMainNavigation().navigate(target);
	}

	public boolean isNavPointOccupied(NavPoint navpoint) {
		for (Player player : bot.getPlayers().getVisiblePlayers().values()) {
			if(navpoint.getLocation().equals(player.getLocation())){
				log.log(Level.INFO, "Navpoint at {0} is occupied - we see it [isNavPointOccupied()]", navpoint.getLocation());
				return true;
			}
		}
		for (FriendInfo friendInfo : informationBase.getFriends().values()) {
			if(navpoint.getLocation().equals(friendInfo.getBestLocation())){
				log.log(Level.INFO, "Navpoint at {0} is occupied - by friend [isNavPointOccupied()]", navpoint.getLocation());
				return true;
			}
		}
		log.log(Level.INFO, "Navpoint at {0} is free [isNavPointOccupied()]", navpoint.getLocation());
		return false;
	}

	public Location getDodgeFallLocation(Direction direction) {
		double x = 0, y = 0;
		switch(direction){
			case BACK:
				x = -(navigation.getFocus().getLocation().x - info.getLocation().x);
				y = -(navigation.getFocus().getLocation().y - info.getLocation().y);
				break;
			case RIGHT:
				x = -(navigation.getFocus().getLocation().y - info.getLocation().y);
				y = (navigation.getFocus().getLocation().x - info.getLocation().x);
				break;
			case LEFT:
				x = (navigation.getFocus().getLocation().y - info.getLocation().y);
				y = -(navigation.getFocus().getLocation().x - info.getLocation().x);
				break;
		}
		
		return info.getLocation().add(new Velocity(x, y, 0).normalize().scale(JUMP_DISTANCE));
	}

	public void dodgeInDirection(Direction direction) {
		switch(direction){
			case BACK:
				move.dodgeBack(info, false);
				return;
			case RIGHT:
				move.dodgeRight(info, false);
				return;
			case LEFT:
				move.dodgeLeft(info, false);
				return;
		}
	}
	
}
