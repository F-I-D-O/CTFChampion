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

import com.fido.ctfbot.Goal;
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
	private Goal goal;
	
	private final AgentInfo info;


	

	public Goal getGoal() {
		return goal;
	}

	public void setGoal(Goal goal) {
		this.goal = goal;
	}
	

	public FriendInfo(Player player, Players players) {
		super(player, player.getLocation(), players);
		this.info = null;
		init();
	}
	
	public FriendInfo(AgentInfo info, Players players) {
		super(null, info.getLocation(), players);
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
	
	
	
	
	
	
	
	
	
	
}
