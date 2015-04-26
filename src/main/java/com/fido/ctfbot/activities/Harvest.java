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
package com.fido.ctfbot.activities;

import com.fido.ctfbot.Goal;
import com.fido.ctfbot.RequestType;
import com.fido.ctfbot.informations.InformationBase;
import com.fido.ctfbot.informations.ItemInfo;
import com.fido.ctfbot.informations.ItemTypeInfo;
import com.fido.ctfbot.modules.ActivityPlanner;
import com.fido.ctfbot.modules.NavigationUtils;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class Harvest extends HighLevelActivity {
	
	public static final double HARVEST_NEAR_BASE_DISTANCE_LIMIT = 3000;
	
	
	
	
	
	private final IUT2004Navigation navigation;
	
	private final Weaponry weaponry;
	
	private final Game game;
	
	private final Items items;
			
	
	
	private final Location searchingAreaCenter;
	
	private final double maxDistance;
	
	private final double minDistance;
	
	/**
	 * recalculating on every main target pickup
	 */
	protected ArrayList<ItemInfo> harvestingPriorities;
	
	/**
	 * recalculating only on harvest init
	 */
	private ArrayList<ItemInfo> posibleItemSForHarvest;
	
	protected ItemInfo chosenItem;
	
	private final NavigationUtils navigationUtils;
	

	
	public Harvest(InformationBase informationBase, LogCategory log, ICaller caller) {
		this(informationBase, log, caller, null, 0, 0);
	}
	
	public Harvest(InformationBase informationBase, LogCategory log, ICaller caller, Location searchingAreaCenter, 
			double maxDistance) {
		this(informationBase, log, caller, searchingAreaCenter, maxDistance, 0);
	}
	
	public Harvest(InformationBase informationBase, LogCategory log, ICaller caller, Location searchingAreaCenter, 
			double maxDistance, double minDistance) {
		super(informationBase, log, caller);
		navigation = informationBase.getNavigation();
		weaponry = informationBase.getWeaponry();
		game = informationBase.getGame();
		items = informationBase.getItems();
		
		navigationUtils = bot.getNavigationUtils();
		this.searchingAreaCenter = searchingAreaCenter;
		this.maxDistance = maxDistance;
		this.minDistance = minDistance;
	}

	
	
	
	@Override
	public void run() {
		if(currentChildActivity == null){
			callculateHarvestingPriority();
			if(harvestingPriorities.isEmpty()){
				log.log(Level.INFO, "Nothing to harvest [Harvest.run()]");
				if(bot.getGoal() == Goal.GUARD_OUR_FLAG){
					((ActivityPlanner) caller).tryToHarvestWhileGuardingCoolDown.use();
					caller.childActivityFinished();
				}
				else if(informationBase.amIArmed()){
					bot.request(RequestType.END_HARVEST);
					caller.childActivityFinished();
				}
			}
			else{
				chosenItem = harvestingPriorities.get(0);
                
////				log.log(Level.INFO, "Item for harvest chosen: {0}, location: {1}[Harvest.run()]", 
//						new String[]{chosenItem.getItem().toString(), chosenItem.getItem().getLocation().toString()});
				runChildActivity(new Move(informationBase, log, this, chosenItem.getItem().getLocation()));
			}
		}
		else{
//			log.log(Level.INFO, "Item for harvesting already chosen: {0} [Harvest.run()]", chosenItem.getItem());
			currentChildActivity.run();
		}
	}

	@Override
	protected void close() {
		navigation.stopNavigation();
	}

	@Override
	protected boolean activityParametrsEquals(Object obj) {
		Harvest action = (Harvest) obj;
		
		// both parametrs has to be null
		if(searchingAreaCenter == null){
			return action.searchingAreaCenter == null;
		}
		// or equal
		else{
			return searchingAreaCenter.equals(action.searchingAreaCenter) && maxDistance == action.maxDistance;
		}
			
	}
	
	protected void callculateHarvestingPriority() {
//		log.log(Level.INFO, "Calculating harvesting priorities - start [Harvest.callculateHarvestingPriority()]");
        for (ItemTypeInfo itemTypeStatistic : informationBase.getItemTypesInfo().values()) {
            itemTypeStatistic.countOverallPriority();
        }
		
		harvestingPriorities = new ArrayList<ItemInfo>(posibleItemSForHarvest);
		
		Iterator<ItemInfo> iterator = harvestingPriorities.iterator();
		while (iterator.hasNext()) {
			ItemInfo itemInfo = iterator.next(); 
			
			NavPoint itemNavPoint = itemInfo.getItem().getNavPoint();
		  
			// we remove from the list items, that is not pickable, 
			if(!items.isPickable(itemInfo.getItem()) || 
					// or is not spwawned yet              
					!itemInfo.isItemSpawned() ||
					// point not used test
					navigationUtils.isNavPointOccupied(itemNavPoint)
					){
				iterator.remove();
//				debugRemovalCause(itemInfo);
			}
			else {
				itemInfo.countOverallPriority();
			}
		}
		
        Collections.sort(harvestingPriorities, Collections.reverseOrder());
//		log.log(Level.INFO, "Calculating harvesting priorities - end [Harvest.callculateHarvestingPriority()]");
	}

	private void debugRemovalCause(ItemInfo itemInfo) {
		if(!items.isPickable(itemInfo.getItem())){
			log.log(Level.INFO, "Item removed because it's not pickable: {0} [Harvest.debugRemovalCause()]", itemInfo.getItem());
		} 
		else if(!informationBase.isReachable(itemInfo.getItem().getNavPoint())){
			log.log(Level.INFO, "Item removed because it's not reachable: {0} [Harvest.debugRemovalCause()]", itemInfo.getItem());
		}
		else if(!items.isPickupSpawned(itemInfo.getItem())){
			log.log(Level.INFO, "Item removed because it's not spawned: {0} [Harvest.debugRemovalCause()]", itemInfo);
		}
		else if(searchingAreaCenter != null && bot.getNavigationUtils().getDistance(
							searchingAreaCenter, itemInfo.getItem().getLocation()) > maxDistance){
			log.log(Level.INFO, "Item removed because it's too far: {0} [Harvest.debugRemovalCause()]", itemInfo);
		}
		else if(// or item is too close 
					minDistance != 0 && bot.getNavigationUtils().getDistance(
							searchingAreaCenter, itemInfo.getItem().getLocation()) < minDistance){
			log.log(Level.INFO, "Item removed because it's too close: {0} [Harvest.debugRemovalCause()]", itemInfo);
		}
		else{
			log.log(Level.INFO, "Item removed for unknown reason: {0} [Harvest.debugRemovalCause()]", itemInfo);
		}
	}

	private void initPossibleItemsForHarvest() {
		log.log(Level.INFO, "Initalizing possible items for harvest - start [Harvest.initPossibleItemsForHarvest()]");
		
		posibleItemSForHarvest = new ArrayList<ItemInfo>(informationBase.getItemsInfo().values());
		
		Iterator<ItemInfo> iterator = posibleItemSForHarvest.iterator();
		while (iterator.hasNext()) {
			ItemInfo itemInfo = iterator.next(); 
			
			NavPoint itemNavPoint = itemInfo.getItem().getNavPoint();
		  
			
			if(		// we gonna remove items that is not reachable
					!informationBase.isReachable(itemNavPoint) ||					
					// or is too far
					searchingAreaCenter != null && bot.getNavigationUtils().getDistance(
							searchingAreaCenter, itemInfo.getItem().getLocation()) > maxDistance ||
					// or is too close 
					minDistance != 0 && bot.getNavigationUtils().getDistance(
							searchingAreaCenter, itemInfo.getItem().getLocation()) < minDistance ||
					
					// unusable items test
					informationBase.getItemTypesInfo().get((UT2004ItemType) itemInfo.getItem().getType()) == null ||
					informationBase.getItemTypesInfo().get((UT2004ItemType) itemInfo.getItem().getType())
							.getStaticPriority() == 0
					){
				iterator.remove();
//				debugRemovalCause(itemInfo);
			}
		}
		log.log(Level.INFO, "Initalizing possible items for harvest - end [Harvest.initPossibleItemsForHarvest()]");
	}

	@Override
	protected void init() {
		initPossibleItemsForHarvest();
	}
	
}
