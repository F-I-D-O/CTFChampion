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

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;

/**
 *
 * @author david_000
 */
public class EnemyFlagInfo extends FlagInfo{

	public EnemyFlagInfo(cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.FlagInfo flag, Location lastKnownPosition, double lastKnownPositionTime) {
		super(flag, lastKnownPosition, lastKnownPositionTime);
	}
    
    
    
}
