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

import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class ItemTypeInfo extends Info implements Comparable<ItemTypeInfo> {
	
	public static final int MAX_ARMOR_LOW = 50;
	public static final int MAX_ARMOR_HIGH = 150;
	public static final int MAX_ARMOR_TOTAL = 150;
	
	public static final int MAX_HEALTH_LOW = 100;
	public static final int MAX_HEALTH_HIGH = 199;
	
	/*
	* static priorities
	*/
	public static final Map<UT2004ItemType, Integer> ITEM_TYPE_STATISTIC_PRIORITIES;
	static {
        HashMap<UT2004ItemType,Integer> map = new HashMap<UT2004ItemType, Integer>();
		
		// guns
		map.put(UT2004ItemType.ASSAULT_RIFLE, 3);
		map.put(UT2004ItemType.ASSAULT_RIFLE_AMMO, 2);
		map.put(UT2004ItemType.ASSAULT_RIFLE_GRENADE, 1);
		map.put(UT2004ItemType.BIO_RIFLE, 3);
		map.put(UT2004ItemType.BIO_RIFLE_AMMO, 3);
		map.put(UT2004ItemType.FLAK_CANNON, 10);
		map.put(UT2004ItemType.FLAK_CANNON_AMMO, 9);
		map.put(UT2004ItemType.ION_PAINTER, 0);
		map.put(UT2004ItemType.ION_PAINTER_AMMO, 0);
		map.put(UT2004ItemType.LIGHTNING_GUN, 6);
		map.put(UT2004ItemType.LIGHTNING_GUN_AMMO, 5);
		map.put(UT2004ItemType.LINK_GUN, 3);
		map.put(UT2004ItemType.LINK_GUN_AMMO, 2);
		map.put(UT2004ItemType.MINIGUN, 9);
		map.put(UT2004ItemType.MINIGUN_AMMO, 7);
		map.put(UT2004ItemType.ROCKET_LAUNCHER, 11);
		map.put(UT2004ItemType.ROCKET_LAUNCHER_AMMO, 9);
		map.put(UT2004ItemType.SHIELD_GUN, 3);
		map.put(UT2004ItemType.SHIELD_GUN_AMMO, 2);
		map.put(UT2004ItemType.SHOCK_RIFLE, 4);
		map.put(UT2004ItemType.SHOCK_RIFLE_AMMO, 3);
		map.put(UT2004ItemType.SNIPER_RIFLE, 6);
		map.put(UT2004ItemType.SHOCK_RIFLE_AMMO, 5);
		
		// other
        map.put(UT2004ItemType.ADRENALINE_PACK, 0);
		map.put(UT2004ItemType.HEALTH_PACK, 8);
		map.put(UT2004ItemType.MINI_HEALTH_PACK, 6);
		map.put(UT2004ItemType.SUPER_HEALTH_PACK, 15);
		map.put(UT2004ItemType.SUPER_SHIELD_PACK, 15);
		map.put(UT2004ItemType.U_DAMAGE_PACK, 20);
		
        ITEM_TYPE_STATISTIC_PRIORITIES = Collections.unmodifiableMap(map);
    }
	
	

	
	private final Weaponry weaponry;
	
	private final AgentInfo info;
	
	private final LogCategory log;
	
	private final Game game;
	
	
	private boolean isFull;
	
	private int amountPriority;
	
	private final int staticPriority;
	
	private final UT2004ItemType itemType;
	
	private double overallPriority;

	
	
	
	public double getOverallPriority() {
		return overallPriority;
	}
	
	
	

	public ItemTypeInfo(InformationBase informationBase, UT2004ItemType itemType, int staticPriority, Weaponry weaponry,
			AgentInfo info,	LogCategory log, Game game) {
		super(informationBase);
		this.itemType = itemType;
		this.staticPriority = staticPriority;
		this.isFull = false;
		this.amountPriority = 10;
		this.weaponry = weaponry;
		this.game = game;
		this.info = info;
		this.log = log;
	}
	
	private void countAmountPriority(){
		int currentAmount = 0;
		int maxAmount = 1;
		if(itemType.getCategory() == ItemType.Category.WEAPON){
			currentAmount = weaponry.getAmmo(itemType);
			ItemType ammoType = weaponry.getPrimaryWeaponAmmoType(itemType);
			maxAmount = weaponry.getMaxAmmo(ammoType);
		}
		else if(itemType.getCategory() == ItemType.Category.AMMO){
			currentAmount = weaponry.getAmmo(itemType);
			maxAmount = weaponry.getMaxAmmo(itemType);
		}
		else if(itemType.getCategory() == ItemType.Category.ARMOR){
			if(itemType == UT2004ItemType.SHIELD_PACK){
				currentAmount = info.getLowArmor();
				maxAmount = game.getMaxArmor();
			}
			else {	
				currentAmount = info.getHighArmor();
				maxAmount = game.getMaxHighArmor();
			}
		}
		else if(itemType.getCategory() == ItemType.Category.HEALTH){
			currentAmount =info.getHealth();
			maxAmount = itemType == UT2004ItemType.HEALTH_PACK ? game.getFullHealth() : game.getMaxHealth();
		}
		else{
			log.log(Level.INFO, "Item with not implemented category {0} [countAmountPriority()]", 
					itemType.getCategory());
		}
		
		float amountRatio;
		
		// kvůli chybě se sniperkou
//		if(maxAmount < 1){
//			log.log(Level.WARNING, "Item with max amount less than zero. Item: {0}, Amount {1}[countAmountPriority()]", 
//					new Object[]{itemType, maxAmount});
//			currentAmount = weaponry.getAmmo(itemType);
////			System.exit(-1);
//			amountRatio = 1000;
//		}
//		else{
			amountRatio = currentAmount / maxAmount;
//		}
		isFull = amountRatio == 1;
		amountPriority = 10 - Math.round(amountRatio * 10);
	}
	
	public void countOverallPriority(){
		countAmountPriority();
		overallPriority = staticPriority + amountPriority;
	}

    @Override
    public int compareTo(ItemTypeInfo o) {
        return (int) Math.round((overallPriority - o.overallPriority) * 10);
    }
	
	
}
