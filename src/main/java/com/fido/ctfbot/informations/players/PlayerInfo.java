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

import com.fido.ctfbot.CTFChampion;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Players;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public abstract class PlayerInfo {
	
	/**
	 * Id of player. Cannot be null.
	 */
	protected final Players players;
	
	protected final UnrealId playerId;
	
	/**
	 * Player message from players. Can be null, if player was loaded after self
	 */
	protected Player player;
	
	protected Location lastKnownLocation;
	
	private double lastKnownLocationTime;
	
	
	
	public Player getPlayer() {
		return player;
	}

	public void setLastKnownLocation(Location lastKnownLocation) {
		this.lastKnownLocation = lastKnownLocation;
	}

	public double getLastKnownLocationTime() {
		return lastKnownLocationTime;
	}

	public void setLastKnownLocationTime(double lastKnownLocationTime) {
		this.lastKnownLocationTime = lastKnownLocationTime;
	}
	
	
	
	
	

	public PlayerInfo(UnrealId playerId, Player player, Location lastKnownLocation, Players players) {
		this.players = players;
		this.player = player;
		this.playerId = playerId;
		
		if(lastKnownLocation == null){
			this.lastKnownLocation = new Location(0, 0, 0);
			CTFChampion.logStatic.log(Level.INFO, 
					"Creating player with null location, zero location added instead [PlayerInfo()]"); 
		}
		else{
			this.lastKnownLocation = lastKnownLocation;
		}
	}
	
	
	public Location getBestLocation(){
		
		// we don't see the player, returns last known location
		if(players.getVisiblePlayer(playerId) == null){
			return lastKnownLocation;
		}
		// return exact location
		else{
			return players.getVisiblePlayer(playerId).getLocation();
		}
	}
	
	
	public String getName(){
		
		// player instance wasn't created yet in players
		if(player == null){
			return "Unknown";
		}
		return player.getName();
	}
	
	public UnrealId getId(){
		return playerId;
	}

	public void connectPlayer(Player player) {
		this.player = player;
		CTFChampion.logStatic.log(Level.INFO, 
					"Player {0} connected. [connectPlayer()]", getName()); 
	}
	
	
}
