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

import com.fido.ctfbot.informations.InformationBase;
import com.fido.ctfbot.informations.LocationInfo;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Players;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public abstract class PlayerInfo extends LocationInfo {
	
	public static final double PLAYER_LOCATION_EXPIRE_TIME = 5;
	
	
	
	protected final Players players;
	
	
	
	/**
	 * Id of player. Cannot be null.
	 */
	protected final UnrealId playerId;
	
	/**
	 * Player message from players. Can be null, if player was loaded after self
	 */
	protected Player player;
	
	
	
	
	public Player getPlayer() {
		return player;
	}

	
	

	public PlayerInfo(InformationBase informationBase, Location lastKnownLocation, double lastKnownLocationTime, 
			UnrealId playerId, Player player, Players players) {
		super(informationBase, lastKnownLocation, lastKnownLocationTime);
		this.players = players;
		this.player = player;
		this.playerId = playerId;
		
		if(lastKnownLocation == null){
			this.lastKnownLocation = new Location(0, 0, 0);
			log.log(Level.INFO, 
					"Creating player with null location, zero location added instead [PlayerInfo()]"); 
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
		log.log(Level.INFO, 
					"Player {0} connected. [connectPlayer()]", getName()); 
	}
	
	@Override
	protected Location getActualLocation() {
		if(player != null && player.isVisible()){
			return player.getLocation();
		}
		return null;
	}
	
	@Override
	protected boolean lastKnownLocationTimeExpired() {
//		log.log(Level.INFO, 
//					"Player {0} location expiration tested. Game time: {1}, location time {2} [lastKnownLocationTimeExpired()]", 
//					new String[]{getName(), 
//						Double.toString(informationBase.getInfo().getTime()), 
//						Double.toString(lastKnownLocationTime)}); 
		return informationBase.getInfo().getTime() - lastKnownLocationTime > PLAYER_LOCATION_EXPIRE_TIME;
	}
}
