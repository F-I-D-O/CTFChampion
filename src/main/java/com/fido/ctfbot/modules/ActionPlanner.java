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
import com.fido.ctfbot.RequestType;
import com.fido.ctfbot.SimpleActionsList;
import com.fido.ctfbot.actions.SimpleAction;
import com.fido.ctfbot.informations.InformationBase;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public class ActionPlanner extends CTFChampionModule{
	
	private ActivityPlanner activityPlanner;
	
	private final SimpleActionsList simpleActionsList;

	public ActionPlanner(ActivityPlanner activityPlanner, CTFChampion bot, LogCategory log, InformationBase informationBase) {
		super(bot, log, informationBase);
		this.activityPlanner = activityPlanner;
		
		this.simpleActionsList = new SimpleActionsList();
	}
	


	
	public void init(ActivityPlanner activityPlanner){
		this.activityPlanner = activityPlanner;
	}
	
	public void takeOver(){
		processSimpleActions();
		
		activityPlanner.takeOver();
	}

	private void processSimpleActions() {
		ArrayList<SimpleAction> simpleActions = simpleActionsList.getCopySimpleActions();
		if(!simpleActions.isEmpty()){
			for(SimpleAction simpleAction : simpleActions){
				processSimpleAction(simpleAction);
				simpleActionsList.remove(simpleAction);
			}
		}
	}

	private void processSimpleAction(SimpleAction simpleAction) {
		switch(simpleAction){
			case REQUEST_GOAL_RESEND:
				bot.getComunicationModule().sendRequest(RequestType.RESEND_GOAL);
				break;
			default:
				log.log(Level.SEVERE, "Simple action not implemented! {0} [processSimpleAction()]", simpleAction);
				break;
		}
	}

	public void addSimpleAction(SimpleAction simpleAction) {
		this.simpleActionsList.addSimpleAction(simpleAction);
	}
	
	
	
	
	
}
