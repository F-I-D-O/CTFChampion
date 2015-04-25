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
package com.fido.ctfbot;

import com.fido.ctfbot.actions.SimpleAction;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import java.util.ArrayList;

/**
 *
 * @author Fido
 */
public class SimpleActionsList {
	
	private final ArrayList<SimpleAction> simpleActions;

	
	
	
	
	public SimpleActionsList() {
		this.simpleActions = new ArrayList<SimpleAction>();
	}
	
	
	
	
	public synchronized void addSimpleAction(SimpleAction simpleAction){
		simpleActions.add(simpleAction);
	}
	
	public synchronized ArrayList<SimpleAction> getCopySimpleActions(){
		return new ArrayList<SimpleAction>(simpleActions);
	}
	
	public synchronized void remove(SimpleAction simpleAction){
		simpleActions.remove(simpleAction);
	}
}
