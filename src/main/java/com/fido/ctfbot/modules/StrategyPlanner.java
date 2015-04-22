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
import com.fido.ctfbot.messages.RequestMessage;
import com.fido.ctfbot.RequestType;
import com.fido.ctfbot.informations.InformationBase;
import com.fido.ctfbot.Strategy;
import com.fido.ctfbot.informations.players.FriendInfo;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.CTF;
import cz.cuni.amis.utils.Cooldown;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class StrategyPlanner extends CTFChampionModule{
	
	public static final Goal DEFAULT_STARTUP_GOAL = Goal.GUARD_OUR_FLAG;
	
	
	
	
	private final CTF ctf;
	
	
	private final ComunicationModule comunicationModule;
	
	private Strategy currentStrategy;
	
	private boolean strategyApplied;
	
	private final ArrayList<RequestMessage> playerRequests;
	
	private final Cooldown exchangeGuardingRolesCooldown = new Cooldown(10000);
	

	
	public StrategyPlanner(CTFChampion bot, LogCategory log, ComunicationModule comunicationModule,
			InformationBase informationBase) {
		super(bot, log, informationBase);
		this.comunicationModule = comunicationModule;
		this.ctf = informationBase.getCtf();
		playerRequests = new ArrayList<RequestMessage>();
	}
	
	public void makeStrategy(){
		log.log(Level.INFO, "Start making strategy: [applyStrategy()]");
		Strategy nextStrategy;
		if(ctf.isOurFlagHome()){
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
				log.log(Level.INFO, "Applying new strategy wasn not succesful: {0} [applyStrategy()]", nextStrategy);
			}
		}
		else{
			// we only process bot requests if strategy wasn|t changed
			processRequests();
		}
		
//		if(bot.testHeatup.isCool()){
//			comunicationModule.sendCommand();
//		}
		log.log(Level.INFO, "End making strategy: [applyStrategy()]");
	}

	private boolean applyStrategy(Strategy nextStrategy) {
		log.log(Level.INFO, "Applying new strategy: {0} [applyStrategy()]", nextStrategy);
		
		boolean succes = false;
		
		switch(nextStrategy){
			case STEAL_ENEMY_FLAG:
				succes = steelEnemyFlag();
				break;
			case GET_BACK_OUR_FLAG:
				succes = getBackOurFlag();
				break;
		}
		return succes;
	}

	private boolean steelEnemyFlag() {
		HashMap<UnrealId,FriendInfo> friendsTmp = getCopyFriendInfo();
		
		// firs assign one bot to guard our flag
		FriendInfo  nearestFriendToOurBae = informationBase.getFriends().get(
				informationBase.getNearestFriendTo(ctf.getOurBase().getLocation()));
		boolean commandIssued = issueCommand(nearestFriendToOurBae, Goal.GUARD_OUR_FLAG);
		if(commandIssued){
			friendsTmp.remove(nearestFriendToOurBae.getId());
			
			// then other to attack
			FriendInfo  nearestFriendToEnemyBase = informationBase.getFriends().get(
				informationBase.getNearestFriendTo(ctf.getEnemyBase().getLocation(), friendsTmp));
			commandIssued = issueCommand(nearestFriendToEnemyBase, Goal.GET_ENEMY_FLAG);
			if(commandIssued){
				friendsTmp.remove(nearestFriendToEnemyBase.getId());
				
				// then the last one to harvest near our base
				return issueCommand(new ArrayList<FriendInfo>(friendsTmp.values()), Goal.HARVEST_NEAR_OUR_BASE);
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
			boolean commadIssued = comunicationModule.sendCommand(friend, goal);
			if(commadIssued){
				if(friend.isMe()){
					bot.setGoal(goal);
				}
				friend.setGoal(goal);
			}
			return commadIssued;
		}
		else{
			return true;
		}
	}

	private boolean issueCommand(ArrayList<FriendInfo> friends, Goal goal) {
		for(FriendInfo friend : friends){
			
			// if one command issue is not succesfull, we stop issuing other commands
			if(!issueCommand(friend, goal)){
				return false;
			}
		}
		return true;
	}

    private boolean getBackOurFlag() {
        HashMap<UnrealId,FriendInfo> friendsTmp = getCopyFriendInfo();
		
		boolean commandIssued = issueCommand(new ArrayList<FriendInfo>(friendsTmp.values()), Goal.GET_BACK_OUR_FLAG);
        
        return commandIssued;
    }

	private void processRequests() {
		for (RequestMessage playerRequest : playerRequests) {
			boolean requestAccepted = processRequest(playerRequest);
			
			// we only accept one request a turn
			if(requestAccepted){
				break;
			}
		}
	}

	private boolean processRequest(RequestMessage playerRequest) {
		log.log(Level.WARNING, "Procesing request: {0} [processRequest()]", playerRequest.getRequestType());
		switch(playerRequest.getRequestType()){
			case END_HARVEST:
				return processEndHarvestRequest(playerRequest);
			default:
				log.log(Level.WARNING, "Illegal request: {0} [processRequest()]", playerRequest.getRequestType());
				return false;
		}
	}
	
	public boolean processRequest(RequestType playerRequest) {
		return processEndHarvestRequest(new RequestMessage(playerRequest, informationBase.getInfo().getId()));
	}

	private boolean processEndHarvestRequest(RequestMessage playerRequest) {
		log.log(Level.WARNING, "Procesing end harvest request: {0} [processRequest()]", playerRequest.getRequestType());
		boolean commandIssued;
		if(exchangeGuardingRolesCooldown.isCool()){
			log.log(Level.WARNING, "End harvest request - roles will be exchanged [processEndHarvestRequest()]");
			exchangeGuardingRolesCooldown.use();
			commandIssued = issueCommand(informationBase.getFrindByGoal(Goal.GUARD_OUR_FLAG), Goal.HARVEST_NEAR_OUR_BASE);
			
			if(commandIssued){
				commandIssued = issueCommand(informationBase.getFriends().get(playerRequest.getPlayerId()), 
									Goal.GUARD_OUR_FLAG);
			}
		}
		else{
			log.log(Level.WARNING, "End harvest request - cannot exchange roles - there will be two guards [processEndHarvestRequest()]");
			commandIssued = issueCommand(informationBase.getFriends().get(playerRequest.getPlayerId()), 
					Goal.GUARD_OUR_FLAG);
		}
		return commandIssued;
	}

	public void queueRequest(RequestMessage requestMessage) {
		playerRequests.add(requestMessage);
		log.log(Level.INFO, "Request: {0} from player {1} added to queue [queueRequest()]", 
				new String[]{requestMessage.getRequestType().toString(), requestMessage.getPlayerId().toString()});
	}
	
}
