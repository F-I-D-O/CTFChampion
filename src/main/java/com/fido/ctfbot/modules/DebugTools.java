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
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.drawing.UT2004Draw;
import java.awt.Color;

/**
 *
 * @author david_000
 */
public class DebugTools extends CTFChampionModule {
    
    private static final double PLAYER_INTENTION_START_CUBE_SIZE = 40;
    
    
    private final UT2004Draw draw;
    

    public DebugTools(CTFChampion bot, LogCategory log, InformationBase informationBase, UT2004Draw draw) {
        super(bot, log, informationBase);
        this.draw = draw;
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
    
}
