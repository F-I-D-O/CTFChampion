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

import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import java.util.ArrayList;


/**
 *
 * @author Fido
 */
public class RecentSpotedItems {
	
	private final ArrayList<Item> spotedItems;
	
	private final InformationBase informationBase;
	
	

	public RecentSpotedItems(InformationBase informationBase) {
		this.informationBase = informationBase;
		spotedItems = new ArrayList<Item>();
	}
	
	public synchronized void addItem(Item item){
		spotedItems.add(item);
	}
	
	public synchronized Item getAndRemove(){
		Item item = spotedItems.get(0);
		spotedItems.remove(0);
		return item;
	}
	
	public synchronized boolean isEmpty(){
		return spotedItems.isEmpty();
	}
	
	public synchronized int getNumberOfItems(){
		return spotedItems.size();
	}

	public synchronized ArrayList<Item> getAllSorted() {
		spotedItems.sort(informationBase.getItemDistanceComparator());
		return spotedItems;
	}
	
	public synchronized void remove(Item item){
		spotedItems.remove(item);
	}
	
}
