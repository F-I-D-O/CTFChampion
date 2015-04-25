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
import com.fido.ctfbot.activities.Fight;
import com.fido.ctfbot.CTFChampion;
import com.fido.ctfbot.RequestType;
import com.fido.ctfbot.activities.Harvest;
import com.fido.ctfbot.activities.ICaller;
import com.fido.ctfbot.informations.InformationBase;
import com.fido.ctfbot.activities.Move;
import com.fido.ctfbot.activities.PriorityHarvest;
import com.fido.ctfbot.activities.QuickMove;
import com.fido.ctfbot.activities.TakePosition;
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
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.utils.Cooldown;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class ActivityPlanner extends CTFChampionModule implements ICaller{

	
	

	
	
	private final AdvancedLocomotion move;
	
	private final CTF ctf;
	
	private final AgentInfo info;
	
	private final IUT2004Navigation navigation;
	
	private final Players players;
    
	
	private ActionPlanner actionPlanner;
    
	private Activity currentActivity;
	
	private OurFlagInfo ourFlagInfo;
	
	private EnemyFlagInfo enemyFlagInfo;
	
	private int numberOfActivitiesEndedThisTurn = 0;
	
	public final Cooldown tryToHarvestWhileGuardingCoolDown = new Cooldown(10000);
	
	public final Cooldown attackRequestCooldown = new Cooldown(3000);
	
	
	

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
		
		numberOfActivitiesEndedThisTurn = 0;
		
		chooseBehaviouByGoal();
		
		log.log(Level.INFO, "Activity planner end [takeOver()]");
	}
	
	private void chooseBehaviouByGoal(){
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
			case HARVEST_NEAR_OUR_BASE:
                harvestNearOurBase();
                break;	
			case PREPARE_FOR_ATTACK:
                prepareForAttack();
                break;	
			default:
				log.log(Level.WARNING, "Not implemented goal [takeOver()]");	
		}
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
			armAndAttack();
		}
		else{
			UnrealId flagHolderId = enemyFlag.getHolder();
			
			// bot has the flag
			if (bot.getInfo().getId().equals(flagHolderId)) {
				log.log(Level.INFO, "bot allready have the flag - going back home [getEnemyFlag()]");
				runActivity(new QuickMove(informationBase, log, this, ctf.getOurBase().getLocation()));
			} 
			else {
				Location enemyFlagLocation = enemyFlagInfo.getBestLocation();
				
				//TODO - napsat akci pro pripad, ze nese vlajku dalsi clen tymu
				
				
				// flag is dropped somewhere
				
				// bot can't see the flag
				if (enemyFlagLocation == null) {
					armAndAttack();
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
			if(ctf.isBotCarryingEnemyFlag()){
				log.log(Level.INFO, "bot holds enemy flag - going home quickly! [getBackOurFlag()]");
				runActivity(new QuickMove(informationBase, log, this, ctf.getOurBase().getLocation()));
			}
			else{
				// am I in our base ?
				if(informationBase.AmIInOurBase()){
					EnemyInfo enemyInfo = informationBase.getEnemyInOurBase();

					// if no enemy in our base
					if(enemyInfo == null){
						// there are things to harvest - going harvest
						if(currentActivity instanceof Harvest || tryToHarvestWhileGuardingCoolDown.isCool()){
							log.log(Level.INFO, "Base clean - going to pick up some items [guardBase()]");
							runActivity(new Harvest(informationBase, log, this, ctf.getOurBase().getLocation(), 
									InformationBase.BASE_SIZE));
						}
						// nothing to harvest - take cover
						else{
							log.log(Level.INFO, "Base clean, nothing to pick up - going to find some hiding spot [guardBase()]");
							runActivity(new TakePosition(informationBase, log, this, ctf.getOurBase().getLocation(), 
									InformationBase.BASE_SIZE));
						}
					}
					// I enemy in our base!
					else{
						log.log(Level.INFO, "Enemy in our base - don't see him, but going to kill him [guardBase()]");
						runActivity(new Fight(informationBase, log, this, enemyInfo));
					}
				}
				else{
					log.log(Level.INFO, "We are not in our base, going to our base [guardBase()]");
					runActivity(new Move(informationBase, log, this, ctf.getOurBase().getLocation()));
				}
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
			
			activity.start();
			
			currentActivity = activity;
		}
		bot.setName(activity.getName());
		
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
				runActivity(new QuickMove(informationBase, log, this, ctf.getOurFlag().getLocation()));
			}
			else{
				
				 // bot carrying enemy flag - continue going home
				if(ctf.isBotCarryingEnemyFlag()){
					log.log(Level.INFO, "bot holds enemy flag - going home [getBackOurFlag()]");
//					runActivity(new QuickMove(informationBase, log, this, ctf.getOurBase().getLocation()));
					runActivity(new PriorityHarvest(informationBase, log, this, ctf.getOurBase().getLocation(), 
							bot.getNavigationUtils().getHalfMapDistance(), InformationBase.BASE_SIZE));
				}
				// fight the thief
				else{
					UnrealId enemyId = ctf.getOurFlag().getHolder();
					if(enemyId == null){
						log.log(Level.SEVERE, "Our flag state unknown - is it even posible? [getBackOurFlag()]");
					}
					else{
						log.log(Level.INFO, "our flag is hold by enemy - killing him [getBackOurFlag()]");
						EnemyInfo enemyInfo = informationBase.getEnemies().get(enemyId);
						if(enemyInfo ==  null){
							log.log(Level.SEVERE, "Enemy info not initilaized yet - is it even posible? [getBackOurFlag()]");
						}
						else{
							runActivity(new Fight(informationBase, log, this, enemyInfo));
						}
					}
				}
			}
		}
        
        // bot carrying enemy flag - continue going home
        if(ctf.isBotCarryingEnemyFlag()){
			log.log(Level.INFO, "bot holds enemy flag - going home [getBackOurFlag()]");
//            runActivity(new QuickMove(informationBase, log, this, ctf.getOurBase().getLocation()));
			runActivity(new PriorityHarvest(informationBase, log, this, ctf.getOurBase().getLocation(), 
							bot.getNavigationUtils().getHalfMapDistance(), InformationBase.BASE_SIZE));
        }
		// we see enemy flag on the ground
		else if(ctf.getEnemyFlag() != null && ctf.isEnemyFlagDropped() && ctf.getEnemyFlag().isVisible()){
			log.log(Level.INFO, "we see enemy flag - going for the flag [getBackOurFlag()]");
			runActivity(new QuickMove(informationBase, log, this, ctf.getEnemyFlag().getLocation()));
		}
        else{
            
			// bot knows where the flag was recently, go to that place
			if(!ourFlagInfo.lastKnownLocationTimeExpired() && 
					bot.getNavigationUtils().getDistance(ourFlagInfo.getLastKnownLocation(), info.getLocation()) < 
						OurFlagInfo.OUR_FLAG_MAX_DISTANCE_TO_LAST_KNOW_LOCATION){
				log.log(Level.INFO, "we know recent location of the flag - going to that location [getBackOurFlag()]");
				runActivity(new QuickMove(informationBase, log, this, ourFlagInfo.getLastKnownLocation()));
			}
			// go to enemy base and wait for him!
			else{
				// bot knows where the flag was recently, go to that place
				if(!enemyFlagInfo.lastKnownLocationTimeExpired() && 
						bot.getNavigationUtils().getDistance(enemyFlagInfo.getLastKnownLocation(), info.getLocation()) < 
							EnemyFlagInfo.ENEMY_FLAG_MAX_DISTANCE_TO_LAST_KNOW_LOCATION){
					log.log(Level.INFO, "we know recent location of enemy flag - going to that location [getBackOurFlag()]");
					runActivity(new QuickMove(informationBase, log, this, enemyFlagInfo.getLastKnownLocation()));
				}
				else{
					
					log.log(Level.INFO, "we don't know where the flag is - going to enemy base [getBackOurFlag()]");
					runActivity(new Move(informationBase, log, this, ctf.getEnemyBase().getLocation()));
				}
			}
        }
    }
	
	public void initFlagInfo(){
		this.ourFlagInfo = informationBase.getOurFlagInfo();
		this.enemyFlagInfo = informationBase.getEnemyFlagInfo();
	}

	@Override
	public void childActivityFinished() {
		currentActivity.end();
		String activityName = currentActivity.getName();
		currentActivity = null;
		numberOfActivitiesEndedThisTurn++;
		
		// neverending cycle detection, sometimes we hve to wait for a message from world
		if(numberOfActivitiesEndedThisTurn < 2){
			log.log(Level.INFO, "Activity {0} finished, another activity will be started [childActivityFinished()]", 
					activityName);
			this.chooseBehaviouByGoal();
		}
		else{
			log.log(Level.INFO, "Activity {0} finished, but {1} activities already finished this turn, we have to wait for a message from the world [childActivityFinished()]", 
					new String[]{activityName, Integer.toString(numberOfActivitiesEndedThisTurn)});
		}
	}

	private void harvestNearOurBase() {
		// is our flag home ?
		if(ctf.isOurFlagHome()){
			if(ctf.isBotCarryingEnemyFlag()){
				log.log(Level.INFO, "bot holds enemy flag - going home quickly! [getBackOurFlag()]");
				runActivity(new QuickMove(informationBase, log, this, ctf.getOurBase().getLocation()));
			}
			else{
				EnemyInfo enemyInfo = informationBase.getEnemyInOurBase();
				if(enemyInfo == null){
					log.log(Level.INFO, "Base clean - going to pick up some items [harvestNearOurBase()]");
					runActivity(new Harvest(informationBase, log, this, ctf.getOurBase().getLocation(), 
							Harvest.HARVEST_NEAR_BASE_DISTANCE_LIMIT, InformationBase.BASE_SIZE));
				}
				else{
					// enemy in our base
					log.log(Level.INFO, "Enemy in our base - don't see him, but going to kill him [harvestNearOurBase()]");
					runActivity(new Fight(informationBase, log, this, enemyInfo));
				}
			}
		}
		else{
			getBackOurFlag();
		}
	}

	private void prepareForAttack() {
		// bot see our flag
		if(ctf.getOurFlag().isVisible()){
			
			// flag is dropped - grab the flag!
			if(ctf.isOurFlagDropped()){
				log.log(Level.INFO, "our flag is dropped - go to drop spot [harvestNearEnemyBase()]");
				runActivity(new QuickMove(informationBase, log, this, ctf.getOurFlag().getLocation()));
			}
			// fight the thief
			else{
				UnrealId enemyId = ctf.getOurFlag().getHolder();
				if(enemyId == null){
					log.log(Level.SEVERE, "Our flag state unknown - is it even posible? [harvestNearEnemyBase()]");
				}
				else{
					log.log(Level.INFO, "our flag is hold by enemy - killing him [harvestNearEnemyBase()]");
					EnemyInfo enemyInfo = informationBase.getEnemies().get(enemyId);
					if(enemyInfo ==  null){
						log.log(Level.SEVERE, "Enemy info not initilaized yet - is it even posible? [harvestNearEnemyBase()]");
					}
					else{
						runActivity(new Fight(informationBase, log, this, enemyInfo));
					}
				}
			}
		}
		else{
			if((informationBase.getSelfInfo().isReadyForAttack() || informationBase.amIReadyForAttack()) 
					&& attackRequestCooldown.isCool()){
				log.log(Level.INFO, "Bot is armed - sending attack request [harvestNearEnemyBase()]");
				bot.getComunicationModule().sendRequest(RequestType.ATTACK);
				attackRequestCooldown.use();
				informationBase.getSelfInfo().setReadyForAttack(true);
			}
			log.log(Level.INFO, "Arming myself: [harvestNearEnemyBase()]");
			runActivity(new Harvest(informationBase, log, this, ctf.getOurBase().getLocation(), 
							bot.getNavigationUtils().getHalfMapDistance()));
		}
		
	}

	private void armAndAttack() {
		if(informationBase.getSelfInfo().isReadyForAttack() || informationBase.amIReadyForAttack()){
			informationBase.getSelfInfo().setReadyForAttack(true);
			log.log(Level.INFO, "We don't know the enemy flag location - going to enemy base [getEnemyFlag()]");
				runActivity(new Move(informationBase, log, this, ctf.getEnemyBase().getLocation()));
		}
		else{
			log.log(Level.INFO, "Arming myself: [harvestNearEnemyBase()]");
			runActivity(new Harvest(informationBase, log, this, ctf.getEnemyBase().getLocation(), 
							bot.getNavigationUtils().getHalfMapDistance(), InformationBase.BASE_SIZE));
		}
	}


}
