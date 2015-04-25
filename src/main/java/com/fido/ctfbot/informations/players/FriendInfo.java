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
package com.fido.ctfbot.informations.players;

import com.fido.ctfbot.Goal;
import com.fido.ctfbot.informations.InformationBase;
import com.fido.ctfbot.modules.StrategyPlanner;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Players;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;

/**
 *
 * @author Fido
 */
public class FriendInfo extends PlayerInfo{
	
	
	private final AgentInfo info;
	
	private Goal goal;
	
	private boolean readyForAttack;
	

	

	public Goal getGoal() {
		return goal;
	}

	public void setGoal(Goal goal) {
		this.goal = goal;
	}
	
	public boolean isReadyForAttack() {
		return readyForAttack;
	}

	public void setReadyForAttack(boolean readyForAttack) {
		this.readyForAttack = readyForAttack;
	}
	
	
	
	

	public FriendInfo(InformationBase informationBase, Player player, Players players) {
		super(informationBase, null, 0, player.getId(), player, players, null);
		this.info = null;
		init();
	}
	
	public FriendInfo(InformationBase informationBase, UnrealId playerId, Players players) {
		super(informationBase, null, 0, playerId, null, players, null);
		this.info = null;
		init();
	}
	
	public FriendInfo(InformationBase informationBase, AgentInfo info, Players players) {
		super(informationBase, info.getLocation(), info.getTime(), info.getId(), null, players, null);
		this.info = info;
		init();
	}
	
	private void init(){
		this.goal = StrategyPlanner.DEFAULT_STARTUP_GOAL;
	}

	@Override
	public Location getBestLocation() {
		if(info == null){
			return super.getBestLocation();
		}
		else{
			return info.getLocation();
		}
	}

	@Override
	public String getName() {
		if(info == null){
			return super.getName();
		}
		else{
			return info.getName();
		}
	}

	@Override
	public UnrealId getId() {
		if(info == null){
			return super.getId();
		}
		else{
			return info.getId();
		}
	}
	
	public boolean isMe(){
		return info != null;
	}

	


}
