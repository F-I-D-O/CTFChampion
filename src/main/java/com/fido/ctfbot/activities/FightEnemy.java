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

import com.fido.ctfbot.informations.InformationBase;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Players;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.bot.command.ImprovedShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class FightEnemy extends Activity {

	private final Players players;
	
	private final IUT2004Navigation navigation;
	
	private final ImprovedShooting shoot;
	
	private final WeaponPrefs weaponPrefs;
    
    
    private Player chosenEmemy;

	
	public Player getChosenEmemy() {
		return chosenEmemy;
	}
	
	
	
	
	
	/**
     * Constructor with no enemy. Nearest enemy will be chosen
     * @param informationBase
     * @param log 
	 * @param caller 
     */
	public FightEnemy(InformationBase informationBase, LogCategory log, ICaller caller) {
		super(informationBase, log, caller);
		players = informationBase.getPlayers();
		navigation = bot.getMainNavigation();
		shoot = informationBase.getShoot();
		weaponPrefs = informationBase.getWeaponPrefs();
        chosenEmemy = players.getNearestVisibleEnemy();
	}
    
    public FightEnemy(InformationBase informationBase, LogCategory log, ICaller caller,
			Player enemy) {
		super(informationBase, log, caller);
		players = informationBase.getPlayers();
		navigation = informationBase.getNavigation();
		shoot = informationBase.getShoot();
		weaponPrefs = informationBase.getWeaponPrefs();
        chosenEmemy = enemy;
	}

	@Override
	public void run() {
		
        if(chosenEmemy != null){
            navigation.setFocus(chosenEmemy);
            navigation.navigate(chosenEmemy);
            shoot.shoot(weaponPrefs, chosenEmemy);
        }
        else{
            log.log(Level.WARNING, "Chosen enemy null [FightEnemy.start()]");
        }
	}


	@Override
	protected void close() {
		shoot.stopShooting();
	}

	@Override
	protected boolean activityParametrsEquals(Object activity) {
		return this.chosenEmemy.getId().equals(((FightEnemy) activity).chosenEmemy.getId()); 
	}
	
	
	
}
