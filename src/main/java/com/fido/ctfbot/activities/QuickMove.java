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
import com.fido.ctfbot.informations.ItemInfo;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;

/**
 *
 * @author Fido
 */
public class QuickMove extends Move{

	public QuickMove(InformationBase informationBase, LogCategory log, ICaller caller, Location target) {
		super(informationBase, log, caller, target);
	}

	@Override
	protected boolean isItemWorthTakingWhileMoving(Item item, ItemInfo itemInfo) {
		if(super.isItemWorthTakingWhileMoving(item, itemInfo)){
			if(info.getHealth() < informationBase.getGame().getFullHealth() * 0.75 && 
					item.getType().getCategory() == UT2004ItemType.Category.HEALTH){
				return true;
			}
		} 
		return false;
	}


	
	
	
}
