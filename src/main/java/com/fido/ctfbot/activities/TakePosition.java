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

import com.fido.ctfbot.informations.HidingSpot;
import com.fido.ctfbot.informations.InformationBase;
import com.fido.ctfbot.modules.NavigationUtils;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.floydwarshall.FloydWarshallMap;
import cz.cuni.amis.pogamut.ut2004.bot.command.AdvancedLocomotion;
import cz.cuni.amis.pogamut.ut2004.bot.command.ImprovedShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.utils.IFilter;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class TakePosition extends HighLevelActivity {
	
	private final FloydWarshallMap fwMap;
	
	private final NavPoints navPoints;
	
	private final AgentInfo info;
	
	private final AdvancedLocomotion move;
	
	private final Weaponry weaponry;
	
	private final ImprovedShooting shoot;
	
	
	private final NavigationUtils navigationUtils;
	
	private final Location nearTo;
	
	private double maxDistance = 0;
	
	private NavPoint position;
	
	private int maxNumberOfEdges = 2;
	
	private int rotationIteartor = 0;
	
	private boolean knownHidingSpotsAlreadyTried;
	
	private HidingSpot hidingSpot;
	
	
	
	

	public TakePosition(InformationBase informationBase, LogCategory log, ICaller caller, Location nearTo) {
		this(informationBase, log, caller, nearTo, 0);
	}
	
	public TakePosition(InformationBase informationBase, LogCategory log, ICaller caller, Location nearTo, 
			double maxDistance) {
		super(informationBase, log, caller);
		fwMap = informationBase.getFwMap();
		navPoints = informationBase.getNavPoints();
		info = informationBase.getInfo();
		move = bot.getMove();
		weaponry = bot.getWeaponry();
		shoot = bot.getShoot();
		
		navigationUtils = bot.getNavigationUtils();
		this.nearTo = nearTo;
		this.maxDistance = maxDistance;
		
	}

	@Override
	public void run() {
		if(position == null){
			if(knownHidingSpotsAlreadyTried){
				// we are at the point near whitch we wat to hide - calculate spot position.
				if(info.atLocation(nearTo)){
					calculatePosition();
				}
				// we are not at the point near whitch we wat to hide - going to the point.
				else{
//					log.log(Level.INFO, "Going to the point of interest: {0} [TakePosition.run()]", nearTo);
					runChildActivity(new Move(informationBase, log, this, nearTo));
				}
			}
			else{
				knownHidingSpotsAlreadyTried = true;
				tryKnownHiddingSpots();
			}
		}
		else {
			// now we are in hiding spot - going to watch the area
			if(info.atLocation(position)){
//				log.log(Level.INFO, "Watching area: {0} from {1} [TakePosition.run()]", 
//						new String[]{nearTo.toString(), position.toString()});
				watchArea();
			}
			// know the hiding spotn - going htere
			else{
//				log.log(Level.INFO, "Going to the point hiding spot: {0} [TakePosition.run()]", nearTo);
				runChildActivity(new Move(informationBase, log, this, position.getLocation()));
			}
		}
	}

	@Override
	protected void close() {
		if(hidingSpot != null){
			hidingSpot.setUsed(false);
		}
	}

	@Override
	protected boolean activityParametrsEquals(Object obj) {
		return nearTo.equals(((TakePosition) obj).nearTo);
	}

	private void calculatePosition() {
//		log.log(Level.INFO, "Calculating position for hide: {0} [TakePosition.calculatePosition()]", nearTo);
		
		if(maxNumberOfEdges < 5){
			if(rotationIteartor < 8){
				if(bot.getLogicIterationNumber() % 4 == 0){
//					log.log(Level.INFO, "Searching for hideouts: {0} [TakePosition.calculatePosition()]", nearTo);
					position = fwMap.getNearestFilteredNavPoint(navPoints.getNavPoints().values(), info.getNearestNavPoint(), 
							new IFilter<NavPoint>() {
						int numberOfEdges;		
						
						@Override
						public boolean isAccepted(NavPoint navpoint) {
							if(navpoint.isVisible()){
//								log.log(Level.INFO, "Checking visible navpoint: {0}[TakePosition.calculatePosition()]", 
//										navpoint.getLocation());
								
								return  // distance test
										(maxDistance == 0 || 
											bot.getNavigationUtils().getDistance(nearTo, navpoint.getLocation()) 
												< maxDistance) && 
										
										// point quality test
										(navpoint.isAIMarker() || navpoint.getIncomingEdges().size() <= numberOfEdges) &&
								
										// point not used test
										!navigationUtils.isNavPointOccupied(navpoint);
							}
							return false;
						}

						public IFilter setNumberOfEdges(int numberOfEdges){
							this.numberOfEdges = numberOfEdges;
							return this;
						}
					}.setNumberOfEdges(maxNumberOfEdges));
					if(position != null){
						hidingSpot = informationBase.addNewHidingSpot(nearTo, position);
						run();
					}
					else{
						move.turnHorizontal(45);
						rotationIteartor++;
					}
				}
			}
			else{
				rotationIteartor = 0;
				maxNumberOfEdges += 2;
			}
		}
		else{
			log.log(Level.WARNING, "No hiding spot found - gonna wait in nearTo: {0} [TakePosition.calculatePosition()]", 
				nearTo);
			position = info.getNearestNavPoint(nearTo);
		}
	}
	
	@Override
	public void childActivityFinished() {
		super.childActivityFinished();
		this.run();
	}

	private void watchArea() {
		move.turnTo(nearTo);
		if(weaponry.hasLoadedWeapon(UT2004ItemType.BIO_RIFLE) && !bot.isBioRifleCharged()){
			shoot.changeWeapon(UT2004ItemType.BIO_RIFLE);
			shoot.shootSecondary(navPoints.getNearestNavPoint());
			bot.setBioRifleCharged(true);
		}
//		move.turnHorizontal(120);
//		move.turnHorizontal(-240);
//		move.turnTo(nearTo);
	}

	private void tryKnownHiddingSpots() {
		ArrayList<HidingSpot> hidingSpots = informationBase.getHidingSpots(nearTo);
		if(hidingSpots != null){
			for (HidingSpot hidingSpot : hidingSpots) {
				if(!hidingSpot.isUsed()){
					position = hidingSpot.getPosition();
					this.hidingSpot = hidingSpot;
					hidingSpot.setUsed(true);
					run();
					return;
				}
			}
		}
	}

	@Override
	protected void init() {
		
	}
	
}
