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
package com.fido.ctfbot.messages;

import com.fido.ctfbot.RequestType;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.utils.token.IToken;
import cz.cuni.amis.utils.token.Tokens;

/**
 *
 * @author Fido
 */
public class RequestMessage extends Message{
	
	public static final IToken MESSAGE_TYPE = Tokens.get("Request");
	
	
	
	
	private final RequestType requestType;
	
	private final UnrealId playerId;
	
	
	

	public RequestType getRequestType() {
		return requestType;
	}

	public UnrealId getPlayerId() {
		return playerId;
	}	

	public RequestMessage(RequestType requestType, UnrealId playerId) {
		super(String.format("Request: %s of player %s", requestType, playerId), MESSAGE_TYPE);
		this.requestType = requestType;
		this.playerId = playerId;
	}
	
	

	
	
	
}
