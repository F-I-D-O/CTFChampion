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

import com.fido.ctfbot.informations.players.FriendInfo;
import com.fido.ctfbot.CTFChampion;
import com.fido.ctfbot.Goal;
import com.fido.ctfbot.ItemDistanceComparator;
import com.fido.ctfbot.informations.flags.EnemyFlagInfo;
import com.fido.ctfbot.informations.flags.OurFlagInfo;
import com.fido.ctfbot.informations.players.EnemyInfo;
import com.fido.ctfbot.informations.players.PlayerInfo;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.CTF;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Players;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.floydwarshall.FloydWarshallMap;
import cz.cuni.amis.pogamut.ut2004.bot.command.ImprovedShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Fido
 */
public final class InformationBase {
	
	private static final double MAX_DISTANCE = 999999999;
	
	public static final int TEAM_SIZE = 3;
	
	public static final double BASE_SIZE = 1500;
	
	/*
	* Pogamut modules
	*/
	
	private final CTFChampion bot;
	
	private final LogCategory log;
	
	private final Players players;
	
	private final CTF ctf;
	
	private final AgentInfo info;
	
	private final FloydWarshallMap fwMap;
	
	private final IUT2004Navigation navigation;
	
	private final ImprovedShooting shoot;
	
	private final WeaponPrefs weaponPrefs;
	
	private final Weaponry weaponry;
	
	private final Items items;
	
	private final NavPoints navPoints;
	
	private final Game game;
	
	/*
	* Other properties
	*/
	
	/* submodules */
	
	
	
	
	/* collections */
	
	private final HashMap<UnrealId, FriendInfo> friends;
	
	private final HashMap<UnrealId, EnemyInfo> enemies;
	
	private final HashMap<UnrealId, PlayerInfo> allPlayersInfo;
	
	private final ArrayList<UnrealId> unidentifiedPlayersIds;
	
	private final HashMap<UT2004ItemType, ItemTypeInfo> itemTypesInfo;
	
	private final HashMap<UnrealId, ItemInfo> itemsInfo;
	
	private final RecentSpotedItems recentSpotedItems;
	
	private final ItemDistanceComparator itemDistanceComparator;
	
	
	/* other */
	
	private OurFlagInfo ourFlagInfo;
	
	private EnemyFlagInfo enemyFlagInfo;
	
	private int numberOfNotConnectedPlayers = 0;
	
	private double timeOfLastNothingToHarvest = 0;
	
	
	
		

	public CTFChampion getBot() {
		return bot;
	}

	public Players getPlayers() {
		return players;
	}

	public CTF getCtf() {
		return ctf;
	}

	public IUT2004Navigation getNavigation() {
		return navigation;
	}

	public ImprovedShooting getShoot() {
		return shoot;
	}

	public WeaponPrefs getWeaponPrefs() {
		return weaponPrefs;
	}

	public OurFlagInfo getOurFlagInfo() {
		return ourFlagInfo;
	}

	public EnemyFlagInfo getEnemyFlagInfo() {
		return enemyFlagInfo;
	}

	public AgentInfo getInfo() {
		return info;
	}
	
	public HashMap<UnrealId, FriendInfo> getFriends() {
		return friends;
	}

	public HashMap<UnrealId, EnemyInfo> getEnemies() {
		return enemies;
	}

	public HashMap<UnrealId, PlayerInfo> getAllPlayersInfo() {
		return allPlayersInfo;
	}

	public int getNumberOfNotConnectedPlayers() {
		return numberOfNotConnectedPlayers;
	}

	public Items getItems() {
		return items;
	}

	public RecentSpotedItems getRecentSpotedItems() {
		return recentSpotedItems;
	}

	public FloydWarshallMap getFwMap() {
		return fwMap;
	}

	public ItemDistanceComparator getItemDistanceComparator() {
		return itemDistanceComparator;
	}

	public HashMap<UT2004ItemType, ItemTypeInfo> getItemTypesInfo() {
		return itemTypesInfo;
	}

	public HashMap<UnrealId, ItemInfo> getItemsInfo() {
		return itemsInfo;
	}

	public Weaponry getWeaponry() {
		return weaponry;
	}

	public Game getGame() {
		return game;
	}

	public double getTimeOfLastNothingToHarvest() {
		return timeOfLastNothingToHarvest;
	}

	public void setTimeOfLastNothingToHarvest(double timeOfLastNothingToHarvest) {
		this.timeOfLastNothingToHarvest = timeOfLastNothingToHarvest;
	}

	public NavPoints getNavPoints() {
		return navPoints;
	}
	
	
	
	
	
	
	
	
	
	public InformationBase(CTFChampion bot, LogCategory log, Players players, CTF ctf, AgentInfo info, 
			FloydWarshallMap fwMap, IUT2004Navigation navigation, ImprovedShooting shoot, WeaponPrefs weaponPrefs, 
			Weaponry weaponry, Items items, NavPoints navPoints, Game game) {
		this.log = log;
		this.bot = bot;
		this.players = players;
		this.ctf = ctf;
		this.info = info;
		this.fwMap = fwMap;
		this.navigation = navigation;
		this.shoot = shoot;
		this.weaponPrefs = weaponPrefs;
		this.weaponry = weaponry;
		this.items = items;
		this.navPoints = navPoints;
		this.game = game;
		
        ItemInfo.setInfo(items);
        
		friends = new HashMap<UnrealId, FriendInfo>();
		enemies = new HashMap<UnrealId, EnemyInfo>();
		allPlayersInfo = new HashMap<UnrealId, PlayerInfo>();
		unidentifiedPlayersIds = new ArrayList<UnrealId>();
		itemTypesInfo = new HashMap<UT2004ItemType, ItemTypeInfo>();
		initItemTypeInfo();
		itemsInfo = new HashMap<UnrealId, ItemInfo>();
		recentSpotedItems = new RecentSpotedItems(this);
		itemDistanceComparator = new ItemDistanceComparator(this);
	}

	
	
	
	public void addPlayer(UnrealId id) {
		Player player = players.getPlayer(id);
		
		// player instance hasn't been created in players yet
		if(player == null){
			unidentifiedPlayersIds.add(id);
		}
		else{
			if(players.getFriends().get(id) != null){
				FriendInfo friendInfo = new FriendInfo(player, players);
				friends.put(id, friendInfo);
				allPlayersInfo.put(id, friendInfo);
			}
			else if(players.getEnemies().get(id) != null){
				EnemyInfo enemyInfo = new EnemyInfo(player, players);
				enemies.put(player.getId(), enemyInfo);
				allPlayersInfo.put(player.getId(), enemyInfo);
			}
		}
	}
	
	private void addBlankFriend(UnrealId id){
		FriendInfo friendInfo = new FriendInfo(id, players);
		friends.put(id, friendInfo);
		allPlayersInfo.put(id, friendInfo);
		numberOfNotConnectedPlayers++;
	}
	
	private void addBlankEnemy(UnrealId id){
		EnemyInfo enemyInfo = new EnemyInfo(id, players);
		enemies.put(id, enemyInfo);
		allPlayersInfo.put(id, enemyInfo);
		numberOfNotConnectedPlayers++;
	}
	
	public void addSelf() {
		FriendInfo friendInfo = new FriendInfo(info, players);
		friends.put(info.getId(), friendInfo);
		allPlayersInfo.put(info.getId(), friendInfo);
	}
	
	public void addPlayersAlreadyInGame(){
		for(Player friend : players.getFriends().values()){
			FriendInfo friendInfo = new FriendInfo(friend, players);
			friends.put(friend.getId(), friendInfo);
			allPlayersInfo.put(friend.getId(), friendInfo);
		}
		
		for(Player enemy : players.getEnemies().values()){
			EnemyInfo enemyInfo = new EnemyInfo(enemy, players);
			enemies.put(enemy.getId(), enemyInfo);
			allPlayersInfo.put(enemy.getId(), enemyInfo);
		}
	}
	
	public UnrealId getNearestFriendTo(Location nearestTo, HashMap<UnrealId, FriendInfo> friends){
		double minDistance = getDistance(info.getLocation(), nearestTo);
		UnrealId minDistanceFriendId = info.getId();
		for(FriendInfo friend : friends.values()){
			log.log(Level.INFO, "player {0} checked for distance: [getNearestFriendTo()]", friend.getName());
			double distance = getDistance(friend.getBestLocation(), nearestTo);
			if(distance < minDistance){
				minDistance = distance;
				minDistanceFriendId = friend.getId();
			}
		}
		return minDistanceFriendId;
	}
	
	public UnrealId getNearestFriendTo(Location nearestTo){ 
		return getNearestFriendTo(nearestTo, friends);
	}
	
	public double getDistance(ILocated from, ILocated to){
//		if(mainPathPlanner instanceof FloydWarshallMap){		
			return fwMap.getDistance(info.getNearestNavPoint(from), info.getNearestNavPoint(to));
//		}
		
	}

	public void initFlags() {
		ourFlagInfo = new OurFlagInfo(this, ctf.getOurFlag(), ctf.getOurBase().getLocation(), info.getTime());
		enemyFlagInfo = new EnemyFlagInfo(this, ctf.getEnemyFlag(), ctf.getEnemyBase().getLocation(), info.getTime());
	}

	public void updateFriendLocation(UnrealId unrealId, Location location) {
		FriendInfo friendInfo = friends.get(unrealId);
		if(friendInfo == null){
			log.log(Level.INFO, 
					"Friend {0} location cannot be updated - friendInfo is not initialized yet. [updateFriendLocation()]", 
					unrealId);
		}
		else{
			friendInfo.setLastKnownLocation(location);	
			friendInfo.setLastKnownLocationTime(info.getTime());
			log.log(Level.INFO, "Friend {0} location updated. [updateFriendLocation()]", unrealId);
		}
	}
	
	public synchronized void tryAddBlankFriend(UnrealId unrealId){
		if(!friends.containsKey(unrealId)){
			addBlankFriend(unrealId);
			unidentifiedPlayersIds.remove(unrealId);
			log.log(Level.INFO, "Friend {0} initialized: [tryAddBlankFriend()]", unrealId);
		}
		else{
			log.log(Level.INFO, "Friend {0} is already initialized: [tryAddBlankFriend()]", unrealId);
		}
	}

	public synchronized void processMissingPlayers() {
		
		// the graeter than operator has to be here because of human player!
		if(friends.size() >= TEAM_SIZE){
			UnrealId playerId;
			for (final Iterator iterator = unidentifiedPlayersIds.iterator(); iterator.hasNext();) {
				playerId = (UnrealId) iterator.next();
				addBlankEnemy(playerId);
				iterator.remove();
			}
		}
	}

	public void tryConnectPlayer(Player player) {
		PlayerInfo playerInfo = allPlayersInfo.get(player.getId());
		
		// first test: if player info was initialized, second test: if player is connected 
		if(playerInfo != null && playerInfo.getPlayer() == null){
			playerInfo.connectPlayer(player);
			numberOfNotConnectedPlayers--;
		}
	}

	public void initItemTypeInfo() {
		for (Map.Entry<UT2004ItemType, Integer> staticPriority : 
				ItemTypeInfo.ITEM_TYPE_STATISTIC_PRIORITIES.entrySet()) {
			if(items.getAllItems(staticPriority.getKey()).size() > 0){
				itemTypesInfo.put(staticPriority.getKey(), 
					new ItemTypeInfo(this, staticPriority.getKey(), staticPriority.getValue(), weaponry, info, log, game));
			}
		}
	}

	public void initItemsInfo() {
		for (Item item : items.getAllItems().values()) {
			ItemInfo itemStat = new ItemInfo(this, item, fwMap, info, navigation, log);
			itemsInfo.put(item.getId(), itemStat);
//			log.log(Level.INFO, "item statistic initialized for item{0}", item.getId()); 
		}
	}

	public ItemInfo getItemInfo(Item item) {
		return itemsInfo.get(item.getId());
	}
    
    public ItemInfo getItemInfo(UnrealId itemId) {
        Item item = items.getItem(itemId);
        if(item == null){
            log.log(Level.INFO, "Item {0} not initialized in world - maybe startup item: [getItemInfo()]", itemId);
            return null;
        }
        else{
            return getItemInfo(items.getItem(itemId));
        }
	}
	
	public NavPoint getItemNavPoint(Object itemObject) {
        Item item = (Item) itemObject;
		return itemsInfo.get(item.getId()) == null ? 
				bot.getNavigationUtils().getNearestNavpoint(item.getLocation()) : item.getNavPoint();
	}

	public boolean AmIInOurBase() {
		return isInOurBase(info.getLocation());
	}
	
	public EnemyInfo getEnemyInOurBase() {
		for (EnemyInfo enemyInfo : enemies.values()) {
			if(isInOurBase(enemyInfo.getBestLocation())){
				return enemyInfo;
			}
		}
		return null;
	}
	
	public boolean isInOurBase(Location location){
		return bot.getNavigationUtils().getDistance(location, ctf.getOurBase().getLocation()) < BASE_SIZE;
	}
	
	public boolean isReachable(NavPoint navPoint) {
		return bot.getNavigationUtils().pathExist(bot.getNavigationUtils().getNearestNavpoint(info.getLocation()), navPoint);
	}
    
    public void decreaseRespawnTimes() {
		for (ItemInfo itemInfo : itemsInfo.values()) {
			itemInfo.decreaseRespawnTime();
		}
	}

	public void updateEnemyLocation(UnrealId unrealId, Location location) {
		EnemyInfo enemyInfo = enemies.get(unrealId);
		if(enemyInfo == null){
			log.log(Level.INFO, 
					"Enemy {0} location cannot be updated - enemyInfo is not initialized yet. [updateEnemyLocation()]", 
					unrealId);
		}
		else{
			enemyInfo.setLastKnownLocation(location);
			enemyInfo.setLastKnownLocationTime(info.getTime());
			log.log(Level.INFO, "Enemy {0} location updated. [updateEnemyLocation()]", unrealId);
		}
	}

	public FriendInfo getFrindByGoal(Goal goal) {
		for (Map.Entry<UnrealId, FriendInfo> entrySet : friends.entrySet()) {
			FriendInfo friendInfo = entrySet.getValue();
			if(friendInfo.getGoal() == goal){
				return friendInfo;
			}
		}
		return null;
	}

	public boolean amIArmed() {
		if(
				// enough weapons test
				weaponry.getLoadedRangedWeapons().size() < 3){
			return false;
		}
		return true;
	}
	
}
