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
package com.fido.ctfbot.informations;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;

/**
 *
 * @author Fido
 */
public abstract class LocationInfo extends Info {
	
	protected Location lastKnownLocation;
    
    protected double lastKnownLocationTime;
	
	
	
	
	public Location getLastKnownLocation() {
		return lastKnownLocation;
	}

	public double getLastKnownLocationTime() {
		return lastKnownLocationTime;
	}

	public void setLastKnownLocation(Location lastKnownLocation) {
		this.lastKnownLocation = lastKnownLocation;
	}

	public void setLastKnownLocationTime(double lastKnownLocationTime) {
		this.lastKnownLocationTime = lastKnownLocationTime;
	}
	
	

	
	
	
	public LocationInfo(InformationBase informationBase, Location lastKnownPosition, double lastKnownPositionTime) {
		super(informationBase);
		this.lastKnownLocation = lastKnownPosition;
		this.lastKnownLocationTime = lastKnownPositionTime;
	}
	
	
	public Location getBestLocation(){
		Location actualLocation = getActualLocation();
		if(actualLocation == null){
			if(lastKnownLocationTimeExpired()){
				return null;
			}
			else{
				return lastKnownLocation;
			}
		}
		else {
			return actualLocation;
		}
	}

	protected abstract Location getActualLocation();

	protected abstract boolean lastKnownLocationTimeExpired();
	
	
}
