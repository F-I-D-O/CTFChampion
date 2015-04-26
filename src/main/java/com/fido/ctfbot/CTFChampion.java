package com.fido.ctfbot;


import com.fido.ctfbot.actions.SimpleAction;
import com.fido.ctfbot.informations.InfoType;
import com.fido.ctfbot.modules.NavigationUtils;
import com.fido.ctfbot.informations.InformationBase;
import com.fido.ctfbot.informations.ItemInfo;
import com.fido.ctfbot.informations.flags.FlagInfo;
import com.fido.ctfbot.informations.players.EnemyInfo;
import com.fido.ctfbot.informations.players.FriendInfo;
import com.fido.ctfbot.modules.StrategyPlanner;
import com.fido.ctfbot.modules.ComunicationModule;
import com.fido.ctfbot.modules.ActivityPlanner;
import com.fido.ctfbot.modules.ActionPlanner;
import com.fido.ctfbot.messages.CommandMessage;
import com.fido.ctfbot.messages.LocationMessage;
import com.fido.ctfbot.messages.PickupMessage;
import com.fido.ctfbot.messages.RequestMessage;
import com.fido.ctfbot.modules.DebugTools;
import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectClassEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.event.WorldObjectUpdatedEvent;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base3d.worldview.object.event.WorldObjectAppearedEvent;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ItemPickedUp;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerJoinsGame;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import cz.cuni.amis.utils.Cooldown;
import cz.cuni.amis.utils.exception.PogamutException;
import java.util.logging.Level;

/**
 * Pogamut "Hello world!" example showing few extra things such as introspection and various bot-initializing methods.
 * <p>
 * We advise you to use simple: DM-TrainingDay map with this example.
 * 
 * <p><p>
 * First, try to run the bot and kill it. If you are a NetBeans user, the bot can be either run by right-clicking the project and choosing Run
 * or by directly running this file. If you are an Eclipse user, run directly this file.
 * 
 * <p><p>
 * Then uncomment the line inside {@link EmptyBot#botKilled(BotKilled)} and run the bot again kill it to see the difference.
 * 
 * <p><p>
 * Then uncomment all lines within {@link EmptyBot#logic()}, run the bot and approach it ... then try to escape from it.
 * 
 * <p><p>
 * Hard to escape? Try the same on map DM-1on1-Albatross
 * 
 * <p><p>
 * If you're using Pogamut NetBeans plugin, try to run the bot, click on its node within Services/UT2004 tab and open properties window (Ctrl+Shift+7).
 * You will see that all fields flagged with {@link JProp} are exported to this window. You can also change their values at runtime.
 * 
 * <p><p>
 * Check out the MAIN method at the end of this file, it contains a hint how to run multiple agents of the same kind at once.
 *
 * @author Jakub Gemrot aka Jimmy
 * @author Michal Bida aka Knight
 * @author Rudolf Kadlec aka ik
 */
@AgentScoped
public class CTFChampion extends UT2004BotTCController {
	
	/****************************************
	* STATIC
	****************************************/
	
	private static final double LOCATION_MESSAGE_SEND_INTERVAL = 5;
	
	
	
	
	
	/****************************************
	* INSTANCE
	****************************************/
		
	public Cooldown testHeatup = new Cooldown(10000);
	
	private InformationBase informationBase;
	
	private Goal goal = Goal.GUARD_OUR_FLAG;
	
	private boolean leader = false;
	
	private IUT2004Navigation mainNavigation;
	
	private String startName;
	
	private double lastLocationMessageSendTime = 0;
	
	private boolean firsLogic = true;
	
	private boolean allBotsInGame = false;
	
	private boolean allPlayersConnectedToInformationBase = false;
	
    private long   lastLogicTime        = -1;
	
    private long   logicIterationNumber = 0;    
	
	private boolean bioRifleCharged = false;
	
	private boolean callMapInfoObtained = false;
	
	
	/*
	 * Bot modules
	 */
	
	private ActionPlanner actionPlanner;
	
	private ActivityPlanner activityPlanner;
	
	private StrategyPlanner strategyPlanner;
	
	private ComunicationModule comunicationModule;
	
	private NavigationUtils navigationUtils;
    
    private DebugTools debugTools;
	
	
	
	
	
	/*
	* GETTERS AND SETTERS
	*/

	public Goal getGoal() {
		return goal;
	}

	public void setGoal(Goal goal) {
		this.goal = goal;
	}

	public boolean isLeader() {
		return leader;
	}

	public NavigationUtils getNavigationUtils() {
		return navigationUtils;
	}

	public IUT2004Navigation getMainNavigation() {
		return mainNavigation;
	}

    public DebugTools getDebugTools() {
        return debugTools;
    }

	public long getLogicIterationNumber() {
		return logicIterationNumber;
	}

	public ComunicationModule getComunicationModule() {
		return comunicationModule;
	}

	public boolean isBioRifleCharged() {
		return bioRifleCharged;
	}

	public void setBioRifleCharged(boolean bioRifleCharged) {
		this.bioRifleCharged = bioRifleCharged;
	}
	
    
	
	
	
	
	/*
	 * EVENT LISTENERS
	 */
	
	@EventListener(eventClass = CommandMessage.class)
    public void onCommandAquired(CommandMessage commandMessage){
       log.log(Level.INFO, "Command aquired: {0}", commandMessage.getLogInfo()); 
	   
	   // change goal if this bot is the target
	   if(info.getId().equals(commandMessage.getTargetPlayerId())){
		   log.log(Level.INFO, "Changing goal to: {0}", commandMessage.getGoal()); 
		   goal = commandMessage.getGoal();
		   setName("goal just aquired");
	   }
	   else{
		   FriendInfo commandedFriend = informationBase.getFriends().get(commandMessage.getTargetPlayerId());
		   if(commandedFriend != null){
			   commandedFriend.setGoal(commandMessage.getGoal());
		   }
		   else{
			   log.log(Level.SEVERE, "Player with id: {0} aquired command but is not initialized", 
					   commandMessage.getTargetPlayerId());
		   }
	   }
    }
	
	@EventListener(eventClass = RequestMessage.class)
    public void onRequestAquired(RequestMessage requestMessage){
       log.log(Level.INFO, "Request aquired: {0}", requestMessage.getLogInfo()); 
	   
	   // process request - only for leader
	   if(isLeader()){
		   log.log(Level.INFO, "Going to process request: {0}", requestMessage.getRequestType()); 
		   strategyPlanner.queueRequest(requestMessage);
	   }
	}
	
	@EventListener(eventClass = LocationMessage.class)
    public void onLocationAcquired(LocationMessage locationMessage){
//       log.log(Level.INFO, "Location acquired: type: {0}, unrealId: {1} loaction: {2}", 
//			new String[]{locationMessage.getInfoType().toString(), locationMessage.getUnrealId().toString(),
//				locationMessage.getLocation().toString()}); 
	   
		switch(locationMessage.getInfoType()){
			case FRIEND:
				if(!allBotsInGame){
					informationBase.tryAddBlankFriend(locationMessage.getUnrealId());
				}
				informationBase.updateFriendLocation(locationMessage.getUnrealId(), locationMessage.getLocation());
				break;
			case ENEMY:
				informationBase.updateEnemyLocation(locationMessage.getUnrealId(), locationMessage.getLocation());
				break;
				
			case OUR_FLAG:
			case ENEMY_FLAG:
				informationBase.updateFlagLocation(locationMessage.getInfoType(), locationMessage.getLocation());
				break;
		}
	   
    }
    
    @EventListener(eventClass = PickupMessage.class)
    public void onPickupMessageAcquired(PickupMessage pickupMessage){
//        log.log(Level.INFO, pickupMessage.getLogInfo()); 
        ItemInfo itemInfo = informationBase.getItemInfo(pickupMessage.getId());
        if(itemInfo != null){
            itemInfo.restartRespawnTime();
        }
        else{
            log.log(Level.WARNING, "Item without ItemInfo picked up: {0} by your friend", pickupMessage.getId()); 
        }  
    }
    
	
	@EventListener(eventClass = ItemPickedUp.class)
	private void OnItemPickedUp(ItemPickedUp event){
		ItemInfo itemInfo = informationBase.getItemInfo(event.getId());
        
		if(itemInfo != null){
			itemInfo.restartRespawnTime();
            comunicationModule.sendPickup(event.getId(), event.getType(), event.getLocation());
		}
		else{
//			log.log(Level.WARNING, "Item without ItemInfo picked up: {0}", event.getId()); 
		}
		
		event.getType();
	}
	
	@ObjectClassEventListener(eventClass = WorldObjectAppearedEvent.class, objectClass = Item.class)
	private void OnItemAppeared(WorldObjectAppearedEvent<Item> event){
		Item item = event.getObject();
		informationBase.getRecentSpotedItems().addItem(item);
//		log.log(Level.INFO, "Item {0} appeard", item.getType()); 
	}
	
	@EventListener(eventClass = PlayerJoinsGame.class)
	private void OnPlayerJoinsGame(PlayerJoinsGame event){
		informationBase.addPlayer(event.getId());
		log.log(Level.INFO, "Player {0} joined game", event.getName()); 
	}
	
	@EventListener(eventClass = PlayerKilled.class)
	private void OnPlayerKilled(PlayerKilled event){
		if(isLeader()){
			FriendInfo friendInfo = informationBase.getFriends().get(event.getId());
			if(friendInfo != null){
				log.log(Level.INFO, "Team member {0} killed", friendInfo.getName()); 
				friendInfo.setReadyForAttack(false);
			}
		}
	}
	
	@EventListener(eventClass = WorldObjectUpdatedEvent.class)
	private void OnPlayerWorldObjectUpdated(WorldObjectUpdatedEvent event){
		if(event.getObject() instanceof Player){
			if(allBotsInGame && !allPlayersConnectedToInformationBase){
				informationBase.tryConnectPlayer((Player) event.getObject());
				
				if(informationBase.getNumberOfNotConnectedPlayers() == 0){
					this.allPlayersConnectedToInformationBase = true;
				}
//				log.log(Level.INFO, "Player {0} update event aquired", event.getName()); 
			}
			
		}
	}
	
	@ObjectClassEventListener(eventClass = WorldObjectUpdatedEvent.class, objectClass = Player.class)
    protected void onPlayerUpdated(WorldObjectUpdatedEvent<Player> event) {
		Player player = event.getObject();

		// First player objects are received in HandShake - at that time we don't have Self message yet or players location!!
		if (player.getLocation() == null || info.getLocation() == null) {
			return;
		}

		// we only have to update enemy, because friends reports about themaselfes
		EnemyInfo enemyInfo = informationBase.getEnemies().get(player.getId());

		if(enemyInfo != null && enemyInfo.getSendPositionMessageCooldown().isCool()){
			enemyInfo.setLastKnownLocation(player.getLocation());
			enemyInfo.setLastKnownLocationTime(info.getTime());
			comunicationModule.sendEnemyMessage(player);
			enemyInfo.getSendPositionMessageCooldown().use();	
		}
    }
	
	@ObjectClassEventListener(eventClass = WorldObjectUpdatedEvent.class, 
			objectClass = cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.FlagInfo.class)
    protected void onFlagInfoUpdated(
			WorldObjectUpdatedEvent<cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.FlagInfo> event) {
        cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.FlagInfo flag = event.getObject();
		
        // First flagInfo objects are received in HandShake - at that time we don't have Self message yet or flagInfo location!!
        if (flag.getLocation() == null || info.getLocation() == null) {
            return;
        }
		
		FlagInfo flagInfo;
		InfoType infoType;
		boolean flagHome;
		if(informationBase.getOurFlagInfo().getFlag().getId().equals(flag.getId())){
			flagInfo =  informationBase.getOurFlagInfo();
			infoType = InfoType.OUR_FLAG;
			flagHome = ctf.isOurFlagHome();
		}
		else{
			flagInfo = informationBase.getEnemyFlagInfo();
			infoType = InfoType.ENEMY_FLAG;
			flagHome = ctf.isEnemyFlagHome();
		}
		
		if(flagInfo.getSendPositionMessageCooldown().isCool() && !flagHome){
			flagInfo.setLastKnownLocation(flag.getLocation());
			flagInfo.setLastKnownLocationTime(info.getTime());
			comunicationModule.sendFlagMessage(flag, infoType);
			flagInfo.getSendPositionMessageCooldown().use();
		}
    }
	
	@EventListener(eventClass=BotDamaged.class)
    public void botDamaged(BotDamaged event) {
    	// Uncomment this line to gain information about damage the bot receives
    	//sayGlobal("GOT DAMAGE: " + event.getDamage() + ", HEALTH: " + info.getHealth());
    	// Notice that "HEALTH" does not fully match the damage received, because "HEALTH" is updated periodically
    	// but BotDamaged message comes as event, therefore the "HEALTH" number lags behind a bit (250ms at max)
    }
	
	
	
	
	
	 /**
     * Called each time the bot dies. Good for reseting all bot's state
     * dependent variables.
     *
     * @param event
     */
    @Override
    public void botKilled(BotKilled event) {
        if(!isLeader()){
			actionPlanner.addSimpleAction(SimpleAction.REQUEST_GOAL_RESEND);
		}
		setBioRifleCharged(false);
		informationBase.getSelfInfo().setReadyForAttack(false);
    }
    
    /**
     * Initialize all necessary variables here, before the bot actually receives 
     * anything from the environment.
     */
    @Override
    public void prepareBot(UT2004Bot bot) {
		System.out.println("prepareBot start"); 
		mainNavigation = nmNav;
		
	
		
//		initNavigationSetting();
		
		ititWeaponPreferences();
		
		initializeModules();		
		
//		debugTools.clear();
	
        // By uncommenting following line, you can make the bot to do the file logging of all its components
        //bot.getLogger().addDefaultFileHandler(new File("EmptyBot.log"));
		System.out.println("prepareBot end"); 
    }
    
    /**
     * Here we can modify initializing command for our bot, e.g., sets its name or skin.
     *
     * @return instance of {@link Initialize}
     */
    @Override
    public Initialize getInitializeCommand() {   
		return new Initialize().setDesiredSkill(getParams().getSkillLevel()).setTeam(getParams().getTeam());
//    	return new Initialize().setName("DeathMatch Champion");
    	// By commenting out the line above and uncommenting line below, you will change the skin of your bot.
    	//return new Initialize().setName("SkinBot").setSkin("Dominator");        
    }

    /**
     * Handshake with GameBots2004 is over - bot has information about the map
     * in its world view. Many agent modules are usable since this method is
     * called.
     *
     * @param gameInfo informaton about the game type
     * @param config information about configuration
     * @param init information about configuration
     */
    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
		log.info("botInitialized start"); 

		
        informationBase.initItemTypeInfo();
		informationBase.initItemsInfo();
		
		log.info("botInitialized end"); 
    }

    /**
     * The bot is initilized in the environment - a physical representation of
     * the bot is present in the game.
     *
     * @param gameInfo informaton about the game type
     * @param config information about configuration
     * @param init information about configuration
     * @param self information about the agent
     */
    @Override
    public void botFirstSpawn(GameInfo gameInfo, ConfigChange config, InitedMessage init, Self self) {
		log.info("botFirstSpawn start"); 
        // Display a welcome message in the game engine
        // right in the time when the bot appears in the environment, i.e., his body has just been spawned 
        // into the UT2004 for the first time.    	
//        body.getCommunication().sendGlobalTextMessage("Hello world! I am alive!");

        // alternatively, you may use getAct() method for issuing arbitrary {@link CommandMessage} for the bot's body
        // inside UT2004
//        act.act(new SendMessage().setGlobal(true).setText("And I can speak! Hurray!"));
		
		// nastaví prvního bota na vedoucího
		if(players.getFriends().isEmpty()){
			log.log(Level.INFO, "bot set as leader [botFirstSpawn()]");
			leader = true;
			strategyPlanner = new StrategyPlanner(this, log, comunicationModule, informationBase);
		}
		
		informationBase.addPlayersAlreadyInGame();
		informationBase.addSelf();
		
		startName = info.getName();
		
		navigationUtils.initHalfMapSize();
		
		log.info("botFirstSpawn end"); 
    }
    
    /**
     * This method is called only once, right before actual logic() method is
     * called for the first time.
     * 
     * Similar to {@link EmptyBot#botFirstSpawn(cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo, cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange, cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage, cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self)}.
     */
    @Override
    public void beforeFirstLogic() {
        if(isLeader()){
//            debugTools.drawNavMesh();
//            debugTools.drawOffMeshLinks();
        }
    }
    
    private void sayGlobal(String msg) {
    	// Simple way to send msg into the UT2004 chat
    	body.getCommunication().sendGlobalTextMessage(msg);
    	// And user log as well
    	log.info(msg);
    }
    
    int num;
    
    /**
     * Main method that controls the bot - makes decisions what to do next. It
     * is called iteratively by Pogamut engine every time a synchronous batch
     * from the environment is received. This is usually 4 times per second - it
     * is affected by visionTime variable, that can be adjusted in ini file
     * inside UT2004/System/GameBots2004.ini
     *
     * @throws cz.cuni.amis.pogamut.base.exceptions.PogamutException
     */
    @Override
    public void logic() throws PogamutException {
    	log.log(Level.INFO, "---LOGIC: {0}---", ++logicIterationNumber);  
    	    	  
        // Log logic periods
        long currTime = System.currentTimeMillis();
        if (lastLogicTime > 0){
			log.log(Level.INFO, "Logic invoked after: {0} ms", currTime - lastLogicTime);
		}
		lastLogicTime = currTime;
		
		// stuff that should be done only once, and can|t be done since logic
		if(firsLogic){
			doFirstLogicInit();
			firsLogic = false;
		}
		
		sendLocationMessage(); 
		
		// checks that no action is taken until all bots are in game
		if(allBotsInGame || checkAllBotsInGame()){
            
            // decrease items respawn times
            informationBase.decreaseRespawnTimes();

            // strategy planning
			if(leader){
				strategyPlanner.makeStrategy();
			}

            // actions and activities after that
			actionPlanner.takeOver();
		}
        else{
            log.log(Level.INFO, "There are not enough players in game. Number of bots in game: {0}.", 
                    informationBase.getNumberOfBotsInGame());
        }
    }

//	private void dealWithEnemis() {
//		log.log(Level.INFO, "Deal with enemies start [dealWithEnemis()]");
//		
//		Player enemy = players.getNearestVisiblePlayer();
//
//        if(visibility.isInitialized()){
//            log.info("Visibility module ready");
//            if(players.canSeePlayers()){
//                VisibilityLocation loc = DistanceUtils.getNearest(
//                        visibility.getCoverPointsFrom(players.getNearestVisiblePlayer()),
//                        info.getLocation(),
//                        new DistanceUtils.IGetDistance<ILocated>() {
//                            
//                            @Override
//                            public double getDistance(ILocated object, ILocated target) {
//                                return fwMap.getDistance(navPoints.getNearestNavPoint(object), navPoints.getNearestNavPoint(target));
//                            }
//                        });
//                if(!navigation.isNavigating()){
//                    navigation.navigate(loc);
//                }
//            }
//        }
//        
//        
//		Item item;
//		if(weaponsPriority != 0 && (item = getTargetItem()) != null){
//			log.log(Level.INFO, "Harvesting in combat [dealWithEnemis()]");
////			move.strafeTo(item, enemy);
////			enemyInfo.setLastKnownLocation(enemy.getLocation());
//		}
//		else{
////			navigation.navigate(players.getNearestVisiblePlayer());
//			
//		}
//		log.log(Level.INFO, "Deal with enemies end [dealWithEnemis()]");
//	}





	private void ititWeaponPreferences() {
		initGeneralWeaponPreferences();
		initRangeWeaponPreferences();
	}

	private void initGeneralWeaponPreferences() {
		weaponPrefs.addGeneralPref(UT2004ItemType.MINIGUN, false);
        // Second priority is Link gun with secondary mode
        weaponPrefs.addGeneralPref(UT2004ItemType.LINK_GUN, false);
        // Third is LIGHTING_GUN but now we use primary firing mode (bUsePrimary is set to true)
        weaponPrefs.addGeneralPref(UT2004ItemType.LIGHTNING_GUN, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.SHOCK_RIFLE, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.ROCKET_LAUNCHER, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.ASSAULT_RIFLE, true);        
        weaponPrefs.addGeneralPref(UT2004ItemType.FLAK_CANNON, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.BIO_RIFLE, true);
	}

	private void initRangeWeaponPreferences() {
		
		// melee - under 80cm
        weaponPrefs.newPrefsRange(80).add(UT2004ItemType.SHIELD_GUN, true);
        // Only one weapon is added to this close combat range and it is SHIELD GUN		
		
		// short range - under 6 meters
		weaponPrefs.newPrefsRange(600)
				.add(UT2004ItemType.FLAK_CANNON, true)
				.add(UT2004ItemType.SHOCK_RIFLE, true)
				.add(UT2004ItemType.MINIGUN, true)
                .add(UT2004ItemType.LINK_GUN, false)
				.add(UT2004ItemType.BIO_RIFLE, true)
				.add(UT2004ItemType.ASSAULT_RIFLE, true);
			
        // short range - under 10 meters
        weaponPrefs.newPrefsRange(1000)
				.add(UT2004ItemType.FLAK_CANNON, true)
				.add(UT2004ItemType.ROCKET_LAUNCHER, true)
				.add(UT2004ItemType.SHOCK_RIFLE, true)
				.add(UT2004ItemType.MINIGUN, true)
                .add(UT2004ItemType.LINK_GUN, false)
				.add(UT2004ItemType.ASSAULT_RIFLE, true);   
				
        // Under 40 meters  - long range guns
        weaponPrefs.newPrefsRange(4000)
				.add(UT2004ItemType.LIGHTNING_GUN, true)
				.add(UT2004ItemType.ROCKET_LAUNCHER, true)
				.add(UT2004ItemType.SHOCK_RIFLE, true)
				.add(UT2004ItemType.MINIGUN, false);
				
        // sniper guns - infinity range
        weaponPrefs.newPrefsRange(100000)
				.add(UT2004ItemType.LIGHTNING_GUN, true)
				.add(UT2004ItemType.SHOCK_RIFLE, true);
	}

	/**
	 * Initialize bot modules
	 */
	private void initializeModules() {

		informationBase = new InformationBase(this, log, players, ctf, info, fwMap, navigation, shoot,
			weaponPrefs, weaponry, items, navPoints, game);
        
        debugTools = new DebugTools(this, log, informationBase, draw);
		
		navigationUtils = new NavigationUtils(this, log, informationBase, fwMap, navPoints, 
				(UT2004Navigation) navigation, nmNav);
		
		comunicationModule = new ComunicationModule(this, log, tcClient, informationBase);
		
		activityPlanner = new ActivityPlanner(this, log, move, ctf, info, navigation, informationBase);
		
		actionPlanner = new ActionPlanner(activityPlanner, this, log, informationBase);
		
		activityPlanner.init(actionPlanner);
	}
	
	public void setName(String state){
		String name = isLeader() ? String.format("%s (leader)", startName) : startName;
		config.setName(String.format("%s - %s, %s", name, getGoal(), state));
	}

	private void sendLocationMessage() {
		if(info.getTime() - lastLocationMessageSendTime >  LOCATION_MESSAGE_SEND_INTERVAL){
			comunicationModule.sendMyLocationMessage();
			lastLocationMessageSendTime = info.getTime();
		}
	}

	private void doFirstLogicInit() {
		informationBase.initFlags();
		activityPlanner.initFlagInfo();
	}

	private boolean checkAllBotsInGame() {
		informationBase.processMissingPlayers();
		// there we expecttwo teams and human player as observer.
		if(informationBase.getFriends().size()  >= InformationBase.TEAM_SIZE){
			allBotsInGame = true;
			return true;
		}
		return false;
	}

	public void request(RequestType requestType) {
		if(isLeader()){
			strategyPlanner.processRequest(requestType);
		}
		else{
			comunicationModule.sendRequest(requestType);
		}
	}

	private void initNavigationSetting() {
		if(isLeader()){
			navMeshModule.setReloadNavMesh(true);
			callMapInfoObtained = true;
		}
	}

	@Override
	public void mapInfoObtained() {
		if(callMapInfoObtained){
			if (navBuilder.isMapName("DM-1on1-Albatross")) {
				navBuilder.removeEdge("JumpSpot8", "PathNode88");
				navBuilder.removeEdgesBetween("CTF-Citadel.PathNode99", "CTF-Citadel.JumpSpot27");
			}
			if (navBuilder.isMapName("CTF-BP2-Concentrate")) {
				navBuilder.removeEdge("PathNode39", "JumpSpot3");
				navBuilder.removeEdge("PathNode75", "JumpSpot2");

				navBuilder.removeEdge("PathNode74", "JumpSpot2");
				navBuilder.removeEdge("PathNode81", "JumpSpot2");
				navBuilder.removeEdge("PathNode2", "PathNode76");
				navBuilder.removeEdge("InventorySpot1", "AIMarker6");
				navBuilder.removeEdge("InventorySpot55", "PathNode44");
				navBuilder.removeEdge("PathNode0", "JumpSpot3");
				navBuilder.removeEdgesBetween("PathNode44", "JumpSpot3");
				navBuilder.removeEdge("InventorySpot9", "PathNode43");

				navBuilder.removeEdge("PathNode0", "PathNode39");
				navBuilder.removeEdge("PathNode0", "JumpSpot0");
				navBuilder.removeEdge("PathNode0", "xBlueFlagBase0");

				navBuilder.removeEdge("PathNode44", "PathNode39");
				navBuilder.removeEdge("PathNode44", "JumpSpot0");
				navBuilder.removeEdge("PathNode44", "xBlueFlagBase0");

				navBuilder.removeEdge("PathNode74", "PathNode75");
				navBuilder.removeEdge("PathNode74", "JumpSpot1");
				navBuilder.removeEdge("PathNode74", "xRedFlagBase1");

				navBuilder.removeEdge("PathNode81", "PathNode75");
				navBuilder.removeEdge("PathNode81", "JumpSpot1");
				navBuilder.removeEdge("PathNode81", "xRedFlagBase1");

				navBuilder.removeEdge("PathNode68", "JumpSpot12");
				navBuilder.removeEdge("PathNode69", "JumpSpot10");
				navBuilder.removeEdge("PathNode76", "JumpSpot11");
				navBuilder.removeEdge("PathNode44", "JumpSpot11");

				navBuilder.removeEdge("InventorySpot2", "AIMarker6");
				navBuilder.removeEdge("JumpSpot3", "xBlueFlagBase0");
			}
		}
	}
	
	public CTFChampionBotParams getParams() {
        return (CTFChampionBotParams) bot.getParams();
    }
}
