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
import com.fido.ctfbot.activities.ICaller;
import com.fido.ctfbot.informations.InformationBase;
import com.fido.ctfbot.activities.Move;
import com.fido.ctfbot.informations.flags.EnemyFlagInfo;
import com.fido.ctfbot.informations.flags.OurFlagInfo;
import com.fido.ctfbot.informations.players.EnemyInfo;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
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
public class ActivityPlanner extends CTFChampionModule implements ICaller{
	
	private static final double OUR_FLAG_LOCATION_EXPIRE_TIME = 5;
	
	private static final double ENEMY_FLAG_LOCATION_EXPIRE_TIME = 5;
	
	private static final double OUR_FLAG_MAX_DISTANCE_TO_LAST_KNOW_LOCATION = 1000;
	
	
	private final AdvancedLocomotion move;
	
	private final CTF ctf;
	
	private final AgentInfo info;
	
	private final IUT2004Navigation navigation;
	
	private final Players players;
    
	
	private ActionPlanner actionPlanner;
    
	private Activity currentActivity;
	
	private OurFlagInfo ourFlagInfo;
	
	private EnemyFlagInfo enemyFlagInfo;
	
	
	
	

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
            case GET_BACK_OUR_FLAG:
                getBackOurFlag();
                break;
			default:
				log.log(Level.WARNING, "Not implemented goal [takeOver()]");
				
		}
		log.log(Level.INFO, "Activity planner end [takeOver()]");
	}

	private void guardFlag() {
		if(ctf.isOurFlagHome()){
			guardBase();
		}
		else{
			getBackOurFlag();
		}
	}

	private void getEnemyFlag() {
		log.log(Level.INFO, "getting enemy flag [getEnemyFlag()]");
		
		FlagInfo enemyFlag = ctf.getEnemyFlag();
		
		// ctf service has not been notified about enemy flag yet
		if(enemyFlag == null){
			log.log(Level.INFO, "ctfService has not been notified about enemy flag yet - going to enemy base [getEnemyFlag()]");
			runActivity(new Move(informationBase, log, this, ctf.getEnemyBase().getLocation()));
		}
		
		// flag is home
		else if(ctf.isEnemyFlagHome()){
			log.log(Level.INFO, "enemy flag is home - going to enemy base [getEnemyFlag()]");
			runActivity(new Move(informationBase, log, this, ctf.getEnemyBase().getLocation()));
		}
		else{
			UnrealId flagHolderId = enemyFlag.getHolder();
			
			// bot has the glag
			if (bot.getInfo().getId().equals(flagHolderId)) {
				log.log(Level.INFO, "bot allready have the flag - going back home [getEnemyFlag()]");
				runActivity(new Move(informationBase, log, this, ctf.getOurBase().getLocation()));
			} 
			else {
				Location enemyFlagLocation = enemyFlagInfo.getRecentLocation();
				
				//TODO - napsat akci pro pripad, ze nese vlajku dalsi clen tymu
				
				
				// flag is dropped somewhere
				
				// bot can't see the flag
				if (enemyFlagLocation == null) {
					log.log(Level.INFO, "We don't know the enemy flag location - going to enemy base [getEnemyFlag()]");
					runActivity(new Move(informationBase, log, this, ctf.getEnemyBase().getLocation()));
				} 
				// bot see the flag
				else {
					log.log(Level.INFO, "We know the enemy flag location, going to location [getEnemyFlag()]");
					runActivity(new Move(informationBase, log, this, enemyFlagLocation));
				}
			}
		}
		
	}

	private void guardBase() {
		// is our flag home ?
		if(ctf.isOurFlagHome()){
			// am I in our base ?
			if(informationBase.AmIInOurBase()){
				// I see an enemy in our base!
				if(players.canSeeEnemies()){
					log.log(Level.INFO, "Enemy in our base - going to kill him [guardBase()]");
					runActivity(new FightEnemy(informationBase, log, this));
				}
				else{
					EnemyInfo enemyInfo = informationBase.getEnemyInOurBase();
					if(enemyInfo == null){
						
					}
					else{
						log.log(Level.INFO, "Enemy in our base - don't see him, but going to kill him [guardBase()]");
						runActivity(new FightEnemy(informationBase, log, this, enemyInfo.getPlayer()));
					}
				}
			}
			else{
				log.log(Level.INFO, "We are not in our base, going to our base [guardBase()]");
				runActivity(new Move(informationBase, log, this, ctf.getOurBase().getLocation()));
			}
		}
		else{
			getBackOurFlag();
		}
	}
	
	private void runActivity(Activity activity){
		log.log(Level.INFO, "runActivity start {0} [runActivity()]", activity.getName());
		
		// if new activity differs from the previous. Insances of the same Activity can differ, so it's necessary to
		// overide equals
		if(!activity.equals(currentActivity)){
			log.log(Level.INFO, "current activity have to be replaced. [runActivity()]", activity.getName());
			
			// at start, current activity is null
			if(currentActivity != null){
				currentActivity.end();
			}
			bot.setName(activity.getName());
			activity.start();
			
			currentActivity = activity;
		}
		
		log.log(Level.INFO, "now the current activity will be runned {0} [runActivity()]", currentActivity.getName());
		currentActivity.run();
		
		log.log(Level.INFO, "runActivity end [runActivity()]", activity.getName());
	}

    private void getBackOurFlag() {
		
		// bot see the flag
		if(ctf.getOurFlag().isVisible()){
			
			// flag is dropped - grab the flag!
			if(ctf.isOurFlagDropped()){
				log.log(Level.INFO, "our flag is dropped - go to drop spot [getBackOurFlag()]");
				runActivity(new Move(informationBase, log, this, ctf.getOurFlag().getLocation()));
			}
			else{
				
				 // bot carrying enemy flag - continue going home
				if(ctf.isBotCarryingEnemyFlag()){
					log.log(Level.INFO, "bot hols enemy flag - going home [getBackOurFlag()]");
					runActivity(new Move(informationBase, log, this, ctf.getOurBase().getLocation()));
				}
				// fight the thief
				else{
					log.log(Level.INFO, "our flag is hold by enemy - killing him [getBackOurFlag()]");
					runActivity(
							new FightEnemy(informationBase, log, this, players.getPlayer(ctf.getOurFlag().getHolder())));
				}
			}
		}
        
        // bot carrying enemy flag - continue going home
        if(ctf.isBotCarryingEnemyFlag()){
			log.log(Level.INFO, "bot hols enemy flag - going home [getBackOurFlag()]");
            runActivity(new Move(informationBase, log, this, ctf.getOurBase().getLocation()));
        }
        else{
            
			// bot knows where the flag was recently, go to that place
			if(ourFlagInfo.getLastKnownLocationTime() < OUR_FLAG_LOCATION_EXPIRE_TIME && 
					informationBase.getDistance(ourFlagInfo.getLastKnownLocation(), info.getLocation()) < 
						OUR_FLAG_MAX_DISTANCE_TO_LAST_KNOW_LOCATION){
				log.log(Level.INFO, "we kdoe recent location of the flag - going to that location [getBackOurFlag()]");
				runActivity(new Move(informationBase, log, this, ourFlagInfo.getLastKnownLocation()));
			}
			// go to enemy base and wait for him!
			else{
				log.log(Level.INFO, "we don't know where the flag is - going to enemy base [getBackOurFlag()]");
				runActivity(new Move(informationBase, log, this, ctf.getEnemyBase().getLocation()));
			}
        }
    }
	
	public void initFlagInfo(){
		this.ourFlagInfo = informationBase.getOurFlagInfo();
		this.enemyFlagInfo = informationBase.getEnemyFlagInfo();
	}

	@Override
	public void childActivityFinished() {
		currentActivity = null;
		this.takeOver();
	}


}
