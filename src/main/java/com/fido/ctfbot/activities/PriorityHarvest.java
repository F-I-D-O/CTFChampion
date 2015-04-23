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

import com.fido.ctfbot.Goal;
import com.fido.ctfbot.RequestType;
import com.fido.ctfbot.informations.InformationBase;
import com.fido.ctfbot.modules.ActivityPlanner;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class PriorityHarvest extends Harvest{

	public PriorityHarvest(InformationBase informationBase, LogCategory log, ICaller caller, Location searchingAreaCenter, double maxDistance, double minDistance) {
		super(informationBase, log, caller, searchingAreaCenter, maxDistance, minDistance);
	}

	@Override
	public void run() {
		if(currentChildActivity == null){
			callculateHarvestingPriority();
			if(harvestingPriorities.isEmpty()){
				log.log(Level.INFO, "Nothing to harvest [Harvest.run()]");
				if(bot.getGoal() == Goal.GUARD_OUR_FLAG){
					((ActivityPlanner) caller).tryToHarvestWhileGuardingCoolDown.use();
					caller.childActivityFinished();
				}
				else if(informationBase.amIArmed()){
					bot.request(RequestType.END_HARVEST);
					caller.childActivityFinished();
				}
			}
			else{
				chosenItem = harvestingPriorities.get(0);
                
				log.log(Level.INFO, "Item for harvest chosen: {0}, location: {1}[Harvest.run()]", 
						new String[]{chosenItem.getItem().toString(), chosenItem.getItem().getLocation().toString()});
				runChildActivity(new QuickMove(informationBase, log, this, chosenItem.getItem().getLocation()));
			}
		}
		else{
			log.log(Level.INFO, "Item for harvesting already chosen: {0} [Harvest.run()]", chosenItem.getItem());
			currentChildActivity.run();
		}
	}
	
	
	
}
