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
import com.fido.ctfbot.InformationBase;
import com.fido.ctfbot.Strategy;
import com.fido.ctfbot.informations.FriendInfo;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathPlanner;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.CTF;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class StrategyPlanner extends CTFChampionModule{
	
	public static final Goal DEFAULT_STARTUP_GOAL = Goal.GUARD_OUR_FLAG;
	
	
	
	private final ComunicationModule comunicationModule;
	
	private final IPathPlanner mainPathPlanner;
	
	private final CTF ctfInfo;
	
	
	private Strategy currentStrategy;
	
	private boolean strategyApplied;
	

	
	public StrategyPlanner(CTFChampion bot, LogCategory log, ComunicationModule comunicationModule,
			InformationBase informationBase, IPathPlanner mainPathPlanner) {
		super(bot, log, informationBase);
		this.comunicationModule = comunicationModule;
		this.mainPathPlanner = mainPathPlanner;
		this.ctfInfo = informationBase.getCtf();
	}
	
	public void makeStrategy(){
		
		
		
		Strategy nextStrategy;
		if(informationBase.getCtf().isOurFlagHome()){
			nextStrategy = Strategy.STEAL_ENEMY_FLAG;
		}
		else{
			nextStrategy = Strategy.GET_BACK_OUR_FLAG;
		}
		
		if(nextStrategy != currentStrategy){
			strategyApplied = false;
		}
		
		// saving current strategy
		currentStrategy = nextStrategy;
		
		if(!strategyApplied){
			strategyApplied = applyStrategy(nextStrategy);
			if(!strategyApplied){
				log.log(Level.INFO, "Applying new strategy wasn't succesful: {0} [applyStrategy()]", nextStrategy);
			}
		}
		
//		if(bot.testHeatup.isCool()){
//			comunicationModule.sendCommand();
//		}
	}

	private boolean applyStrategy(Strategy nextStrategy) {
		log.log(Level.INFO, "Applying new strategy: {0} [applyStrategy()]", nextStrategy);
		
		boolean succes = false;
		
		switch(nextStrategy){
			case STEAL_ENEMY_FLAG:
				succes = steelEnemyFlag();
				break;
			case GET_BACK_OUR_FLAG:
				succes = true;
				break;
		}
		return succes;
	}

	private boolean steelEnemyFlag() {
		HashMap<UnrealId,FriendInfo> friendsTmp = getCopyFriendInfo();
		FriendInfo  nearestFriendToOurBae = informationBase.getFriends().get(
				informationBase.getNearestFriendTo(ctfInfo.getOurBase().getLocation()));
		boolean commandIssued = issueCommand(nearestFriendToOurBae, Goal.GUARD_OUR_FLAG);
		if(commandIssued){
			friendsTmp.remove(nearestFriendToOurBae.getId());
			FriendInfo  nearestFriendToEnemyBase = informationBase.getFriends().get(
				informationBase.getNearestFriendTo(ctfInfo.getEnemyBase().getLocation(), friendsTmp));
			commandIssued = issueCommand(nearestFriendToEnemyBase, Goal.GET_ENEMY_FLAG);
			if(commandIssued){
				friendsTmp.remove(nearestFriendToEnemyBase.getId());
				return issueCommand(new ArrayList<FriendInfo>(friendsTmp.values()), Goal.GUARD_OUR_FLAG);
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}

	private HashMap<UnrealId,FriendInfo> getCopyFriendInfo() {
		return new HashMap<UnrealId,FriendInfo>(informationBase.getFriends());
	}

	private boolean issueCommand(FriendInfo friend, Goal goal) {

		// we issue the command only if player goal has changed
		if(friend.getGoal() != goal){
			log.log(Level.INFO, "Issueing command: {0} for {1} [issueCommand()]", 
				new String[]{goal.toString(), friend.getName()});
			return comunicationModule.sendCommand(friend, goal);
		}
		else{
			return true;
		}
	}

	private boolean issueCommand(ArrayList<FriendInfo> friends, Goal goal) {
		for(FriendInfo friend : friends){
			if(issueCommand(friend, goal)){
				return false;
			}
		}
		return true;
	}
	
}
