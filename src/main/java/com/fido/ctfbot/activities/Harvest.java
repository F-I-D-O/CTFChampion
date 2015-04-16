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

import com.fido.ctfbot.informations.InformationBase;
import com.fido.ctfbot.informations.ItemInfo;
import com.fido.ctfbot.informations.ItemTypeInfo;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class Harvest extends HighLevelActivity {
	
	
	
	
	
	private final IUT2004Navigation navigation;
	
	private final Weaponry weaponry;
	
	private final Game game;
	
	private final Items items;
			
	
	
	private Location searchingAreaCenter;
	
	private double maxDistance;
	
	private ArrayList<ItemInfo> harvestingPriorities;
	
	private ItemInfo chosenItem;
	

	
	public Harvest(InformationBase informationBase, LogCategory log, ICaller caller) {
		super(informationBase, log, caller);
		navigation = informationBase.getNavigation();
		weaponry = informationBase.getWeaponry();
		game = informationBase.getGame();
		items = informationBase.getItems();
	}
	
	public Harvest(InformationBase informationBase, LogCategory log, ICaller caller, Location searchingAreaCenter, 
			double maxDistance) {
		super(informationBase, log, caller);
		navigation = informationBase.getNavigation();
		weaponry = informationBase.getWeaponry();
		game = informationBase.getGame();
		items = informationBase.getItems();
		
		this.searchingAreaCenter = searchingAreaCenter;
		this.maxDistance = maxDistance;
	}

	
	
	
	@Override
	public void run() {
		if(currentChildActivity == null){
			callculateHarvestingPriority();
			if(harvestingPriorities.isEmpty()){
				log.log(Level.INFO, "Nothing to harvest [Harvest.run()]");
			}
			else{
				chosenItem = harvestingPriorities.get(0);
                
				log.log(Level.INFO, "Item for harvest chosen: {0} [Harvest.run()]", chosenItem);
				runChildActivity(new Move(informationBase, log, this, chosenItem.getItem().getLocation()));
			}
		}
		else{
			log.log(Level.INFO, "Item for harvesting allready chosen [Harvest.run()]");
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
	
	private void callculateHarvestingPriority() {
		log.log(Level.INFO, "Calculating harvesting priorities - start [Harvest.callculateHarvestingPriority()]");
        for (ItemTypeInfo itemTypeStatistic : informationBase.getItemTypesInfo().values()) {
            itemTypeStatistic.countOverallPriority();
        }
		
		harvestingPriorities = new ArrayList<ItemInfo>(informationBase.getItemsInfo().values());
		
		Iterator<ItemInfo> iterator = harvestingPriorities.iterator();
		while (iterator.hasNext()) {
			ItemInfo itemInfo = iterator.next(); 
		  
			// ze seznamu priorit zcela vyřadíme věci které nemůžemne sebrat, 
			if(!items.isPickable(itemInfo.getItem()) || 
					// které jsou nedosažitelné
					!informationBase.isReachable(itemInfo.getItem().getNavPoint()) ||
					// nebo které ještě nejsou respawnované               
					!itemInfo.isItemSpawned() ||
					// or item is too far
					searchingAreaCenter != null && bot.getNavigationUtils().getDistance(
							searchingAreaCenter, itemInfo.getItem().getLocation()) > maxDistance
					){
				iterator.remove();
				debugRemovalCause(itemInfo);
			}
			else {
				itemInfo.countOverallPriority();
			}
		}
		
        Collections.sort(harvestingPriorities, Collections.reverseOrder());
		log.log(Level.INFO, "Calculating harvesting priorities - end [Harvest.callculateHarvestingPriority()]");
	}

	@Override
	public void childActivityFinished() {
		super.childActivityFinished();
		this.run();
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
		else{
			log.log(Level.INFO, "Item removed for unknown reason: {0} [Harvest.debugRemovalCause()]", itemInfo);
		}
	}
	
}
