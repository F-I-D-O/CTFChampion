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
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class Move extends Activity {

	private Location target;
	
	private final IUT2004Navigation navigation;

	
	
	
	public Move(InformationBase informationBase, LogCategory log, Location target) {
		super(informationBase, log);
		this.navigation = informationBase.getNavigation();
        
        this.target = target;
	}

	@Override
	public void start() {
		if(target == null){
			log.log(Level.WARNING, "Cannot navigate to null target [Move.start()]");
		}
		
		// navigovate only if we don't navigating, or we navigating to different target
		if(!navigation.isNavigating() || !navigation.getCurrentTarget().getLocation().equals(target)){
			navigation.navigate(target);
		}
		
		
	}
	
}
