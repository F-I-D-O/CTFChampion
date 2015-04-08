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

import com.fido.ctfbot.InformationBase;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Players;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.bot.command.ImprovedShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;

/**
 *
 * @author Fido
 */
public class FightEnemy extends Activity {

	private final Players players;
	
	private final IUT2004Navigation navigation;
	
	private final ImprovedShooting shoot;
	
	private final WeaponPrefs weaponPrefs;
	
	
	public FightEnemy(InformationBase informationBase, LogCategory log) {
		super(informationBase, log);
		players = informationBase.getPlayers();
		navigation = informationBase.getNavigation();
		shoot = informationBase.getShoot();
		weaponPrefs = informationBase.getWeaponPrefs();
	}

	@Override
	public void start() {
		if(players.canSeeEnemies()){
			Player chosenEmemy = players.getNearestVisibleEnemy();
			if(chosenEmemy != null){
				navigation.setFocus(chosenEmemy);
				navigation.navigate(chosenEmemy);
				shoot.shoot(weaponPrefs, chosenEmemy);
			}
		}
	}
	
}
