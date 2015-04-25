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
package com.fido.ctfbot.informations.flags;

import com.fido.ctfbot.informations.InformationBase;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;

/**
 *
 * @author david_000
 */
public class OurFlagInfo extends FlagInfo {
	
	private static final double OUR_FLAG_LOCATION_EXPIRE_TIME = 8;
	
	public static final double OUR_FLAG_MAX_DISTANCE_TO_LAST_KNOW_LOCATION = 2000;

	public OurFlagInfo(InformationBase informationBase, cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.FlagInfo flag, Location lastKnownPosition, double lastKnownPositionTime) {
		super(informationBase, flag, lastKnownPosition, lastKnownPositionTime);
	}

	@Override
	public boolean lastKnownLocationTimeExpired() {
		if(informationBase.getCtf().isOurFlagDropped()){
			return false;
		}
		return super.lastKnownLocationTimeExpired(); //To change body of generated methods, choose Tools | Templates.
	}

	
    
}
