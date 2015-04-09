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
import java.util.logging.Level;

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

	public abstract void run();
	
	
	public final void start(){
		 log.log(Level.INFO, "Activity {0} started. [start()]", this.getClass().getSimpleName());
	}
	
	public final void end(){
		close();
		log.log(Level.INFO, "Activity {0} ended. [end()]", this.getClass().getSimpleName());
	}

	@Override
	public boolean equals(Object obj) {
		
		// null test
		if(obj == null){
			return false;
		}
		
		// early success
		if(super.equals(obj)){
			return true;
		}
		
		// same class test
		if(this.getClass().equals(obj.getClass())){
			return false;
		};
		
		// activity state test
		return activityParametrsEquals(obj);
	}

	protected abstract void close();

	protected abstract boolean activityParametrsEquals(Object obj);

	
}
