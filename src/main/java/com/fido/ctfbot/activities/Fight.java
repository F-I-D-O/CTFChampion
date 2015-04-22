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

import com.fido.ctfbot.Direction;
import com.fido.ctfbot.informations.InformationBase;
import com.fido.ctfbot.informations.players.EnemyInfo;
import com.fido.ctfbot.modules.NavigationUtils;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.base3d.worldview.object.Velocity;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Players;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.bot.command.AdvancedLocomotion;
import cz.cuni.amis.pogamut.ut2004.bot.command.ImprovedShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.utils.Cooldown;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class Fight extends Activity {

	private final Players players;
	
	private final IUT2004Navigation navigation;
	
	private final ImprovedShooting shoot;
	
	private final WeaponPrefs weaponPrefs;
	
	private final AgentInfo info;
	
	private final AdvancedLocomotion move;
    
    
	private final NavigationUtils navigationUtils;
	
    private final Player chosenEmemy;
	
	private final EnemyInfo chosenEmemyInfo;
	
	private final Cooldown expertMoveCooldown = new Cooldown(500);
	
	private ArrayList<Integer> expertMoveTriedTypes;

	
	public Player getChosenEmemy() {
		return chosenEmemy;
	}
	
	
	
	
	
	/**
     * Constructor with no enemy. Nearest enemy will be chosen
     * @param informationBase
     * @param log 
	 * @param caller 
     */
	public Fight(InformationBase informationBase, LogCategory log, ICaller caller) {
		this(informationBase, log, caller, informationBase.getPlayers().getNearestVisibleEnemy());
	}
    
    public Fight(InformationBase informationBase, LogCategory log, ICaller caller,
			Player enemy) {
		super(informationBase, log, caller);
		players = informationBase.getPlayers();
		navigation = informationBase.getNavigation();
		shoot = informationBase.getShoot();
		weaponPrefs = informationBase.getWeaponPrefs();
        chosenEmemy = enemy;
		chosenEmemyInfo  = informationBase.getEnemies().get(chosenEmemy.getId());
		info = bot.getInfo();
		navigationUtils = bot.getNavigationUtils();
		move = bot.getMove();
	}

	@Override
	public void run() {
		
        if(chosenEmemy != null){
			if(chosenEmemy.isVisible()){
				navigation.setFocus(chosenEmemy);
				navigation.navigate(chosenEmemy);
				shoot.shoot(weaponPrefs, chosenEmemy);
				log.log(Level.INFO, "Chosen enemy is visible, shooting with: {0} [FightEnemy.run()]", shoot.getLastShooting());
				doExpertMove(chosenEmemy);
			}
			else{
				log.log(Level.INFO, "Chosen enemy is not visible. [FightEnemy.run()]");
				shoot.stopShooting();
				Location bestKnownLocation = chosenEmemyInfo.getBestLocation();
				if(bestKnownLocation != null){
					navigation.navigate(bestKnownLocation);
				}
				else{
					caller.childActivityFinished();
				}
			}
        }
        else{
            log.log(Level.WARNING, "Chosen enemy null [FightEnemy.run()]");
        }
	}


	@Override
	protected void close() {
		shoot.stopShooting();
	}

	@Override
	protected boolean activityParametrsEquals(Object activity) {
		return this.chosenEmemy.getId().equals(((Fight) activity).chosenEmemy.getId()); 
	}
	
	private void doExpertMove(Player player) {
		if(expertMoveCooldown.isCool()){
			expertMoveCooldown.use();
			expertMoveTriedTypes = new ArrayList<Integer>();
			int type = 0;
			for (int i = 0; i < 3; i++) {
				while(true){
					type = (int) Math.round(Math.random() * 2);
					if(!expertMoveTriedTypes.contains(type)){
						expertMoveTriedTypes.add(type);
						break;
					}
				}
				
				Direction direction = null;
				switch(type){
					case 0:
						direction = Direction.BACK;
						break;
					case 1:
						direction = Direction.RIGHT;
						break;
					case 2:
						direction = Direction.LEFT;
						break;
				}
				if(direction != null){
					Location jumpLocation = navigationUtils.getDodgeFallLocation(Direction.BACK);
//					debugTools.drawIntention(jumpLocation);
					if(bot.getNavMeshModule().getNavMesh().getPolygonId(jumpLocation) > 0){
						navigationUtils.dodgeInDirection(direction);
						return;
					}
				}
			}
			move.jump();
		}
	}
	
}
