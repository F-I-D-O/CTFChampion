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
public abstract class HighLevelActivity extends Activity implements ICaller {
	
	protected Activity currentChildActivity;

	public HighLevelActivity(InformationBase informationBase, LogCategory log, ICaller caller) {
		super(informationBase, log, caller);
	}
	
	protected void runChildActivity(Activity activity){
		log.log(Level.INFO, "runChildActivity start {0} [runChildActivity()]", activity.getName());
		
		// if new activity differs from the previous. Insances of the same Activity can differ, so it's necessary to
		// overide equals
		if(!activity.equals(currentChildActivity)){
			log.log(Level.INFO, "current child activity have to be replaced. [runChildActivity()]", activity.getName());
			
			// at start, current activity is null, so we have to check
			if(currentChildActivity != null){
				currentChildActivity.end();
			}
			activity.start();
			
			currentChildActivity = activity;
		}
		
		log.log(Level.INFO, "now the current child activity will be runned {0} [runChildActivity()]", 
				currentChildActivity.getName());
		currentChildActivity.run();
		
		log.log(Level.INFO, "runChildActivity end [runChildActivity()]", activity.getName());
	}

	@Override
	public void childActivityFinished() {
		currentChildActivity.end();
		currentChildActivity = null;
	}
	
	
	
}
