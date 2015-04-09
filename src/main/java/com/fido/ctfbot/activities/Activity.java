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

/**
 *
 * @author Fido
 */
public abstract class Activity {
	
	protected final InformationBase informationBase;
	
	protected final LogCategory log;

	
	
	public Activity(InformationBase informationBase, LogCategory log) {
		this.informationBase = informationBase;
		this.log = log;
	}
	
	
	
	public String getName(){
		return this.getClass().getSimpleName();
	}

	public abstract void start();
	
	
}
