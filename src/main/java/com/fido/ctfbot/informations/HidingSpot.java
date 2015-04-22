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

import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;

/**
 *
 * @author Fido
 */
public class HidingSpot extends Info {

	private final NavPoint position;
	
	private boolean used;

	
	
	
	
	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	public NavPoint getPosition() {
		return position;
	}
	
	
	
	
	
	public HidingSpot(InformationBase informationBase, NavPoint position) {
		this(informationBase, position, true);
	}
	
	public HidingSpot(InformationBase informationBase, NavPoint position, boolean used) {
		super(informationBase);
		this.position = position;
		this.used = used;
	}
	
}
