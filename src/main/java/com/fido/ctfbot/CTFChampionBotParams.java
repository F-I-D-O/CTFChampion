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
package com.fido.ctfbot;

import cz.cuni.amis.pogamut.ut2004.bot.params.UT2004BotParameters;

/**
 *
 * @author Fido
 */
public class CTFChampionBotParams extends UT2004BotParameters{

    private String botSkin;
	
	private int skillLevel = 4;
	
	
	
	public String getBotSkin() {
		return botSkin;
	}
	
	public CTFChampionBotParams setBotSkin(String botSkin) {
		this.botSkin = botSkin;
		return this;
	}

	public int getSkillLevel() {
		return skillLevel;
	}
	
	
	
	public CTFChampionBotParams setSkillLevel(int skillLevel) {
		this.skillLevel = skillLevel;
		return this;
	}
	
}
