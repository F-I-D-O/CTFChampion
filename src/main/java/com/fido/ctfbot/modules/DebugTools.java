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
package com.fido.ctfbot.modules;

import com.fido.ctfbot.CTFChampion;
import com.fido.ctfbot.informations.InformationBase;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMeshModule;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.drawing.UT2004Draw;
import java.awt.Color;
import java.util.logging.Level;

/**
 *
 * @author david_000
 */
public class DebugTools extends CTFChampionModule {
    
    private static final double PLAYER_INTENTION_START_CUBE_SIZE = 40;
    
    
    private final UT2004Draw draw;
    
    private final NavMeshModule nmModule; 
    

    public DebugTools(CTFChampion bot, LogCategory log, InformationBase informationBase, UT2004Draw draw) {
        super(bot, log, informationBase);
        this.draw = draw;
        nmModule = bot.getNavMeshModule();
    }
    
    /**
     * Draw intention. Have to be called after navigate
     */
    public void drawIntention(){
        draw.clearAll();
        draw.drawCube(Color.yellow, informationBase.getInfo().getLocation(), PLAYER_INTENTION_START_CUBE_SIZE);
        draw.drawLine(informationBase.getInfo().getLocation(), bot.getMainNavigation().getCurrentTarget().getLocation());
        return;
    }
    
    public void drawIntention(Location target){
//        draw.clearAll();
        draw.drawCube(Color.yellow, informationBase.getInfo().getLocation(), PLAYER_INTENTION_START_CUBE_SIZE);
        draw.drawLine(informationBase.getInfo().getLocation(), target);
        return;
    }
	
	public void clear(){
		draw.clearAll();
	}
    
    public boolean drawNavMesh() { 
    		log.log(Level.INFO, "Drawing NavMesh...");
    		nmModule.getNavMeshDraw().clearAll();
    		nmModule.getNavMeshDraw().draw(true, false);
    		log.log(Level.INFO, "Okey, drawing commands issued, now we have to wait a bit till it gets drawn completely...");
    		
//    		waitForMesh = navMeshModule.getNavMesh().getPolys().size() / 35;
//    		waitingForMesh = -info.getTimeDelta();
//    	
//		
//		if (waitForMesh > 0) {
//    		waitForMesh -= info.getTimeDelta();
//    		waitingForMesh += info.getTimeDelta();
//    		if (waitingForMesh > 2) {
//    			waitingForMesh = 0;
//    			say(((int)Math.round(waitForMesh)) + "s...");
//    		}
//    		if (waitForMesh > 0) {
//    			return false;
//    		}    		
//    	}
		
		return true;
	}
    
    public boolean drawOffMeshLinks() { 		
		
			if (nmModule.getNavMesh().getOffMeshPoints().isEmpty()) {
				log.log(Level.INFO, "Ha! There are no off-mesh points / links within this map!");
				return true;
			}
			
			log.log(Level.INFO, "Drawing OffMesh Links...");
    		nmModule.getNavMeshDraw().draw(false, true);
    		log.log(Level.INFO, "Okey, drawing commands issued, now we have to wait a bit till it gets drawn completely...");    		
//    		waitForOffMeshLinks = navMeshModule.getNavMesh().getOffMeshPoints().size() / 10;
//    		waitingForOffMeshLinks = -info.getTimeDelta();

		
//		if (waitForOffMeshLinks > 0) {
//			waitForOffMeshLinks -= info.getTimeDelta();
//			waitingForOffMeshLinks += info.getTimeDelta();
//    		if (waitingForOffMeshLinks > 2) {
//    			waitingForOffMeshLinks = 0;
//    			say(((int)Math.round(waitForOffMeshLinks)) + "s...");
//    		}
//    		if (waitForOffMeshLinks > 0) {
//    			return false;
//    		}    		
//    	}
		
		return true;
	}
}
