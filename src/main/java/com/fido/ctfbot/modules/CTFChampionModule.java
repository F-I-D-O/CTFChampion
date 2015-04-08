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

import com.fido.ctfbot.CTFChampion;
import com.fido.ctfbot.InformationBase;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;

/**
 *
 * @author Fido
 */
class CTFChampionModule {
	
	protected final LogCategory log;
	
	protected final CTFChampion bot;
	
	protected final InformationBase informationBase;
	
	public CTFChampionModule(CTFChampion bot, LogCategory log, InformationBase informationBase) {
		this.bot = bot;
		this.log = log;
		this.informationBase = informationBase;
	}
}
