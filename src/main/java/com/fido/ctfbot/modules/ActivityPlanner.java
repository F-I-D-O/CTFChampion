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

import com.fido.ctfbot.activities.Activity;
import com.fido.ctfbot.activities.FightEnemy;
import com.fido.ctfbot.CTFChampion;
import com.fido.ctfbot.InformationBase;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.CTF;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Players;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.bot.command.AdvancedLocomotion;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.FlagInfo;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class ActivityPlanner extends CTFChampionModule{
	
	private ActionPlanner actionPlanner;
	
	private final AdvancedLocomotion move;
	
	private final CTF ctf;
	
	private final AgentInfo info;
	
	private final IUT2004Navigation navigation;
	
	private final Players players;
	
	
	private Activity currentActivity;
	
	

	public ActivityPlanner(CTFChampion bot, LogCategory log, AdvancedLocomotion move, CTF ctf, AgentInfo info,
		IUT2004Navigation navigation, InformationBase informationBase) {
		super(bot, log, informationBase);
		this.move = move;
		this.ctf = ctf;
		this.info = info;
		this.navigation = navigation;
		this.players = informationBase.getPlayers();
	}

	public void init(ActionPlanner actionPlanner){
		this.actionPlanner = actionPlanner;
	}
	
	public void takeOver(){
		log.log(Level.INFO, "Activity planner start [takeOver()]");
		switch(bot.getGoal()){
			case GUARD_OUR_FLAG:
				guardFlag();
				break;
			case GET_ENEMY_FLAG:
				getEnemyFlag();
				break;
		}
	}

	private void guardFlag() {
//		bot.searchForUsefullObject();
		if(ctf.isOurFlagHome()){
			guardBase();
		}
		else{
			
		}
	}

	private void getEnemyFlag() {
		log.log(Level.INFO, "getting enemy flag [getEnemyFlag()]");
		
		FlagInfo enemyFlag = ctf.getEnemyFlag();
		
		// ctfService has not been notified about enemy flag yet
		if(enemyFlag == null){
			log.log(Level.INFO, "ctfService has not been notified about enemy flag yet [getEnemyFlag()]");
			navigateToEnemyBase();
		}
		
		// flag is home
		else if(ctf.isEnemyFlagHome()){
			log.log(Level.INFO, "enemy flag is home [getEnemyFlag()]");
			navigateToEnemyBase();
		}
		else{
			UnrealId flagHolderId = enemyFlag.getHolder();
			
			// bot has the glag
			if (bot.getInfo().getId().equals(flagHolderId)) {
				log.log(Level.INFO, "bot allready have the flag [getEnemyFlag()]");
				navigateToOurBase();
			} 
			else {
				Location enemyFlagLocation = enemyFlag.getLocation();
				
				// bot can't see the flag
				if (enemyFlagLocation == null) {
					log.log(Level.INFO, "We don't know the the enemy flag location [getEnemyFlag()]");
					navigateToEnemyBase();
				} 
				// bot see the flag
				else {
					navigateTo(enemyFlagLocation);
				}
			}
		}
		
	}
	
	private void navigateToOurBase() {
		log.log(Level.INFO, "navigating to our base [goToEnemyBase()]");
		navigateTo(ctf.getOurBase());
	}

	private void navigateToEnemyBase() {
		log.log(Level.INFO, "navigating to enemy base [goToEnemyBase()]");
		navigateTo(ctf.getEnemyBase());
	}
	
	private boolean navigateTo(ILocated target){
		if (target == null) {
			log.log(Level.INFO, "Navigation target is null [navigateTo()]");
			return false;
		}
		
		log.log(Level.INFO, "Navigating from: {0} to: {1}", new Object[]{target, info.getLocation()});

		navigation.navigate(target);
		
		return true;
	}

	private void guardBase() {
		if(players.canSeeEnemies()){
			runActivity(new FightEnemy(informationBase, log));
		}
		else{
			
		}
	}
	
	private void runActivity(Activity activity){
		bot.setName(activity.getName());
		activity.start();
		currentActivity = activity;
	}
}
