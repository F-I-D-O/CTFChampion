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

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.floydwarshall.FloydWarshallMap;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;

/**
 *
 * @author Fido
 */
public class NavigationUtils {
	
	private final FloydWarshallMap fwMap;
	
	private final NavPoints navPoints;

	public NavigationUtils(FloydWarshallMap fwMap, NavPoints navPoints) {
		this.fwMap = fwMap;
		this.navPoints = navPoints;
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

	
	
}
