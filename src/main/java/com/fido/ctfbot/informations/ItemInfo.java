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
package com.fido.ctfbot.informations;

import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.floydwarshall.FloydWarshallMap;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;

/**
 *
 * @author Fido
 */
public class ItemInfo extends Info implements Comparable<ItemInfo>{
	
	private static final double STANDARD_RESPAWN_TIME = 27.5;
	private static final double DOUBLE_RESPAWN_TIME = 55;
	private static final double TRIPLE_RESPAWN_TIME = 82;
    
    private static Items items;
    
    
	public static boolean isWorthTakeWhileNavigating(Item item) {
		return items.isPickable(item) && items.isPickupSpawned(item);
	}

    public static void setInfo(Items items) {
       ItemInfo.items = items;
    }
	
	
	
	
	private final FloydWarshallMap fwMap;
	
	private final AgentInfo info;
	
	private final IUT2004Navigation navigation;
	
	private final LogCategory log;
	
//	private final Items items;
	
	
	private final Item item;
	
	private double timeToRespawn;
	
	private double distancePriority;
	
	private double overallPriority;

	public Item getItem() {
		return item;
	}

	public double getTimeToRespawn() {
		return timeToRespawn;
	}

	public double getOverallPriority() {
		return overallPriority;
	}
	
	
	public void restartRespawnTime() {
		if(item.getType() == UT2004ItemType.U_DAMAGE_PACK){
			timeToRespawn = TRIPLE_RESPAWN_TIME;
		}
		else if(item.getType() == UT2004ItemType.SUPER_HEALTH_PACK || 
				item.getType() == UT2004ItemType.SUPER_SHIELD_PACK){
			timeToRespawn = DOUBLE_RESPAWN_TIME;
		}
		else {
			timeToRespawn = STANDARD_RESPAWN_TIME;
		}
	}
	
	public void decreaseRespawnTime(){
		if(timeToRespawn > 0.25){
			timeToRespawn -= 0.25;
		}
		else{
			timeToRespawn = 0;
		}
	}
	

	public ItemInfo(InformationBase informationBase, Item item, FloydWarshallMap fwMap, AgentInfo info, 
			IUT2004Navigation navigation, LogCategory log) {
		super(informationBase);
		this.item = item;
		this.timeToRespawn = 0;
		this.fwMap = fwMap;
		this.info = info;
		this.navigation = navigation;
		this.log = log;
		items = informationBase.getItems();
	}

    @Override
    public int compareTo(ItemInfo o) {
        return (int) Math.round((overallPriority - o.overallPriority) * 10);
    }

	public void countDistancePriority() {
		double distance = fwMap.getDistance(info.getNearestNavPoint(), navigation.getNearestNavPoint(item));
//		log.log(Level.INFO, "Distance to: {0} counted as {1}[countAmountPriority()]", new Object[]{item, distance});
		distancePriority = 80 / Math.pow((distance / 100), 3);
	}

	public void countOverallPriority(ItemTypeInfo statisticForItemType) {
		countDistancePriority();
		
		// null priority for items with unknown item type
		double itemTypePriority = statisticForItemType == null ? 0.0 : statisticForItemType.getOverallPriority();
		overallPriority = itemTypePriority + distancePriority;
	}
	
	public boolean isWorthTakeWhileNavigating(){
		return items.isPickable(item) && items.isPickupSpawned(item);
	}
	
	
}
