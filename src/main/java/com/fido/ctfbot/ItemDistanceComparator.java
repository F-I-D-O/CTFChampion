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

import com.fido.ctfbot.informations.InformationBase;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.floydwarshall.FloydWarshallMap;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import java.util.Comparator;

/**
 *
 * @author Fido
 */
public class ItemDistanceComparator implements Comparator{
	
	private final InformationBase informationBase;
	
	private final FloydWarshallMap fwMap;
	
	private final AgentInfo info;
	
	

	public ItemDistanceComparator(InformationBase informationBase) {
		this.informationBase = informationBase;
		fwMap = informationBase.getFwMap();
		info = informationBase.getInfo();
	}
	
	
	
	

	@Override
	public int compare(Object item1, Object item2) {
		NavPoint item1NavPoint = informationBase.getItemNavPoint(item1);
		NavPoint item2NavPoint = informationBase.getItemNavPoint(item1);
		
		double distanceToItem1 = fwMap.getDistance(info.getNearestNavPoint(), item1NavPoint);
		double distanceToItem2 = fwMap.getDistance(info.getNearestNavPoint(), item2NavPoint);
		
		return (int) (distanceToItem1 - distanceToItem2);
	}
	
}
