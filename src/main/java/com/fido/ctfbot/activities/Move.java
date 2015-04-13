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
import com.fido.ctfbot.informations.RecentSpotedItems;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Players;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.floydwarshall.FloydWarshallMap;
import cz.cuni.amis.pogamut.ut2004.bot.command.ImprovedShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.utils.flag.FlagListener;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class Move extends Activity implements FlagListener<NavigationState> {
	
	private static final double MAX_DISTANCE_INCREASE = 200;
	
	
	
	
	private Location currentTarget;

	private Location mainTarget;
	
	private final IUT2004Navigation navigation;

	private final Players players;
	
	private final ImprovedShooting shoot;
	
	private final WeaponPrefs weaponPrefs;
	
	private final AgentInfo info;
	
	private final FloydWarshallMap fwMap;
	
	
	private final RecentSpotedItems recentSpotedItems;
	
			
	
	
	public Move(InformationBase informationBase, LogCategory log, Location target) {
		super(informationBase, log);
		this.navigation = informationBase.getNavigation();
		this.players = informationBase.getPlayers();
		this.shoot = informationBase.getShoot();
		this.weaponPrefs = informationBase.getWeaponPrefs();
		this.info = informationBase.getInfo();
		this.recentSpotedItems = informationBase.getRecentSpotedItems();
		this.fwMap = informationBase.getFwMap();
        
        this.currentTarget = target;
		this.mainTarget = target;
		navigation.addStrongNavigationListener(this);
	}

	@Override
	public void run() {
		if(currentTarget == null){
			log.log(Level.SEVERE, "Cannot navigate to null target [Move.start()]");
		}
		
		// try to recount the path across newly spoted items
		if(!recentSpotedItems.isEmpty() && mainTarget == currentTarget){
			tryToRecountPathAcrossItems();
		}
		
		// navigovate only if we don't navigating, or we navigating to different target
		if(!navigation.isNavigating() || !navigation.getCurrentTarget().getLocation().equals(currentTarget)){
			navigation.navigate(currentTarget);
		}
		
		if(players.canSeeEnemies()){
			Player enemy = players.getNearestVisibleEnemy();
			navigation.setFocus(enemy);
			shoot.shoot(weaponPrefs, enemy);
		}
		else{
			navigation.setFocus(null);
			if(info.isShooting()){
				shoot.stopShooting();
			}
		}
		
	}

	@Override
	protected void close() {
		navigation.stopNavigation();
	}

	@Override
	protected boolean activityParametrsEquals(Object obj) {
		return currentTarget.equals(((Move) obj).currentTarget);
	}

	private void tryToRecountPathAcrossItems() {
		ArrayList<Item> spotedItemsSorted = recentSpotedItems.getAllSorted();
		
		for(Item item : spotedItemsSorted){
			ItemInfo itemInfo = informationBase.getItemInfo(item);
			
			boolean isWorthTaking = 
					itemInfo == null ? ItemInfo.isWorthTakeWhileNavigating(item) : 
						itemInfo.isWorthTakeWhileNavigating();
	
			if(isWorthTaking){
				NavPoint itemNavPoint = 
						itemInfo == null ? informationBase.getNearestNavpoint(item.getLocation()) : item.getNavPoint();

				NavPoint targetNavPoint = informationBase.getNearestNavpoint(currentTarget);
				
				double distanceToItem = fwMap.getDistance(info.getNearestNavPoint(), itemNavPoint);
				double distanceFromItem = fwMap.getDistance(itemNavPoint, targetNavPoint);
				double pathDistance = fwMap.getDistance(info.getNearestNavPoint(), targetNavPoint);
				if(distanceToItem + distanceFromItem <= pathDistance + MAX_DISTANCE_INCREASE){
					currentTarget = item.getLocation();
					recentSpotedItems.remove(item);
					break;
				}
			}
			recentSpotedItems.remove(item);
		}
	}

	@Override
	public void flagChanged(NavigationState changedValue) {
		switch (changedValue) {
			case STUCK:
				log.log(Level.WARNING, "We are stucked [Move.flagChanged()]");
				break;
			case STOPPED:
				break;
			case TARGET_REACHED:
				if(currentTarget == mainTarget){
					
				}
				else {
					currentTarget = mainTarget;
				}
				break;
			case PATH_COMPUTATION_FAILED:
				log.log(Level.WARNING, "Path computation failed [Move.flagChanged()]");
				break;
			case NAVIGATING:
				break;
		}
	}
	
	
	
}
