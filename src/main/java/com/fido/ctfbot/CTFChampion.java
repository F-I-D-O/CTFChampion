package com.fido.ctfbot;


import com.fido.ctfbot.informations.InformationBase;
import com.fido.ctfbot.modules.StrategyPlanner;
import com.fido.ctfbot.modules.ComunicationModule;
import com.fido.ctfbot.modules.ActivityPlanner;
import com.fido.ctfbot.modules.ActionPlanner;
import com.fido.ctfbot.messages.CommandMessage;
import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathPlanner;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.visibility.model.VisibilityLocation;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
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
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import cz.cuni.amis.utils.Cooldown;
import cz.cuni.amis.utils.exception.PogamutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
	
	private static final double LOCATION_MESSAGE_SEND_INTERVAL = 5;

	
	
	
	public static LogCategory logStatic;
	
	
	

    // Following properties are exported into "Properties" tab inside NetBeans if you have Pogamut NetBeans plugin installed.
	// Note that these field must be annotated with @JProp annotations.
//	@JProp
//    public String stringProp = "Hello bot example";
	
	
	public Cooldown testHeatup = new Cooldown(10000);
	
	
	
	
	private int weaponsPriority;
	
    private HashMap<UnrealId, ItemStatistic> itemStatistics;
	
	private HashMap<UT2004ItemType, ItemTypeStatistic> itemTypeStatistics;
	
	private ArrayList<ItemStatistic> harvestingPriorities;
	
	private HashMap<UnrealId,EnemyInfo> enemies;
	
	private InformationBase informationBase;
	
	private Goal goal = Goal.GUARD_OUR_FLAG;
	
	private boolean leader = false;
	
	private IPathPlanner mainPathPlanner;
	
	private String startName;
	
	private double lastLocationMessageSendTime = 0;
	
    // Follwing fields are required only iff code inside {@link EmptyBot#logic()} is uncommented.
    private long   lastLogicTime        = -1;
    private long   logicIterationNumber = 0;    
	
	
	
	
	/*
	 * Bot modules
	 */
	
	private ActionPlanner actionPlanner;
	
	private ActivityPlanner activityPlanner;
	
	private StrategyPlanner strategyPlanner;
	
	private ComunicationModule comunicationModule;
	
	
	
	/*
	* GETTERS AND SETTERS
	*/

	public Goal getGoal() {
		return goal;
	}

	public boolean isLeader() {
		return leader;
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
	   
    }
	
	@EventListener(eventClass = ItemPickedUp.class)
	private void OnItemPickedUp(ItemPickedUp event){
		ItemStatistic itemStat = itemStatistics.get(event.getId());
		if(itemStat != null){
			itemStat.restartRespawnTime();
		}
		else{
			log.log(Level.WARNING, "item without statistic picked up: {0}", event.getId()); 
		}
		
		event.getType();
	}
	
	@EventListener(eventClass = PlayerJoinsGame.class)
	private void OnPlayerJoinsGame(PlayerJoinsGame event){
		informationBase.addPlayer(event.getId());
		
//		addEnemy(event.getId());
	}
	
	

    /**
     * Initialize all necessary variables here, before the bot actually receives 
     * anything from the environment.
     */
    @Override
    public void prepareBot(UT2004Bot bot) {
		System.out.println("prepareBot start"); 
		
		mainPathPlanner = fwMap;
		
		itemStatistics = new HashMap<UnrealId, ItemStatistic>();
		
		itemTypeStatistics = new HashMap<UT2004ItemType, ItemTypeStatistic>();
		for (Map.Entry<UT2004ItemType, Integer> staticPriority : 
				ItemTypeStatistic.ITEM_TYPE_STATISTIC_PRIORITIES.entrySet()) {
			itemTypeStatistics.put(staticPriority.getKey(), 
					new ItemTypeStatistic(staticPriority.getKey(), staticPriority.getValue(), weaponry, info, log));
		}
		
		ititWeaponPreferences();
		
		initializeModules();
		
		enemies = new HashMap<UnrealId, EnemyInfo>();
		
		
	
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
		return new Initialize();
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
		
		CTFChampion.logStatic = log;
		
		for (Item item : items.getAllItems().values()) {
			ItemStatistic itemStat = new ItemStatistic(item, fwMap, info, navigation, log);
			itemStatistics.put(item.getId(), itemStat);
//			log.log(Level.INFO, "item statistic initialized for item{0}", item.getId()); 
		}
		
		for(Player player : players.getPlayers().values()){
			if(!player.getId().equals(info.getId())){
				addEnemy(player.getId());
			}
		}
		
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
			strategyPlanner = new StrategyPlanner(this, log, comunicationModule, informationBase, mainPathPlanner);
		}
		
		informationBase.addPlayersAlreadyInGame();
		informationBase.addSelf();
		
		startName = info.getName();
		
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
		
		
    }
    
    private void sayGlobal(String msg) {
    	// Simple way to send msg into the UT2004 chat
    	body.getCommunication().sendGlobalTextMessage(msg);
    	// And user log as well
    	log.info(msg);
    }
    
    /**
     * Called each time the bot dies. Good for reseting all bot's state
     * dependent variables.
     *
     * @param event
     */
    @Override
    public void botKilled(BotKilled event) {
        // Uncomment this line to have the bot comment on its death.
        //sayGlobal("I was KILLED!");
    }
    
    @EventListener(eventClass=BotDamaged.class)
    public void botDamaged(BotDamaged event) {
    	// Uncomment this line to gain information about damage the bot receives
    	//sayGlobal("GOT DAMAGE: " + event.getDamage() + ", HEALTH: " + info.getHealth());
    	// Notice that "HEALTH" does not fully match the damage received, because "HEALTH" is updated periodically
    	// but BotDamaged message comes as event, therefore the "HEALTH" number lags behind a bit (250ms at max)
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
        
//        info.getT
		
//		decreaseRespawnTimes();
//		calculateWeaponsPriority();
//		callculateHarvestingPriority();
		
		if(leader){
			strategyPlanner.makeStrategy();
		}
		
		informationBase.initFlags();
		
		actionPlanner.takeOver();
		
		sendLocationMessage();
        
//		if(players.canSeePlayers() || fightCounter > 0){
//        if(players.canSeePlayers()){
//			dealWithEnemis();   
//        }
//        else{
//            takeNonEnemyAction();
//        }
        
    }

    

    public Item getTargetItem() {
		Iterator<ItemStatistic> iterator = harvestingPriorities.listIterator();
		
		if(iterator.hasNext()){
			return iterator.next().getItem();
		}
		else {
			return null;
		}
    }

	private void dealWithEnemis() {
		log.log(Level.INFO, "Deal with enemies start [dealWithEnemis()]");
		
		Player enemy = players.getNearestVisiblePlayer();

        if(visibility.isInitialized()){
            log.info("Visibility module ready");
            if(players.canSeePlayers()){
                VisibilityLocation loc = DistanceUtils.getNearest(
                        visibility.getCoverPointsFrom(players.getNearestVisiblePlayer()),
                        info.getLocation(),
                        new DistanceUtils.IGetDistance<ILocated>() {
                            
                            @Override
                            public double getDistance(ILocated object, ILocated target) {
                                return fwMap.getDistance(navPoints.getNearestNavPoint(object), navPoints.getNearestNavPoint(target));
                            }
                        });
                if(!navigation.isNavigating()){
                    navigation.navigate(loc);
                }
            }
        }
        
        
		Item item;
		if(weaponsPriority != 0 && (item = getTargetItem()) != null){
			log.log(Level.INFO, "Harvesting in combat [dealWithEnemis()]");
//			move.strafeTo(item, enemy);
//			enemyInfo.setLastKnownLocation(enemy.getLocation());
		}
		else{
//			navigation.navigate(players.getNearestVisiblePlayer());
			
		}
		log.log(Level.INFO, "Deal with enemies end [dealWithEnemis()]");
	}
	
	
	
	private void takeNonEnemyAction(){
//		shoot.stopShooting();
		
//		if(!navigation.isNavigating()){
			searchForUsefullObject();
//		}
	}

	public boolean searchForUsefullObject() {
		log.log(Level.INFO, "Searching of usefull objects start [searchForUsefullObject()]");
		
		Item target = getTargetItem(); 
		
		// pokud jsme nenašli vhodnou věc k sebrání
		if(target == null){
			log.log(Level.INFO, "No suitable item found [searchForUsefullObject()]");
			return false; 
		}
		log.log(Level.INFO, "item found: {0} [searchForUsefullObject()]", target);
		navigation.navigate(target);
		return true;
	}

	private void decreaseRespawnTimes() {
		for (ItemStatistic itemStatistic : itemStatistics.values()) {
			itemStatistic.decreaseRespawnTime();
		}
	}

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
		
		// First range class is defined from 0 to 80 ut units (1 ut unit ~ 1 cm)
        weaponPrefs.newPrefsRange(80).add(UT2004ItemType.SHIELD_GUN, true);
        // Only one weapon is added to this close combat range and it is SHIELD GUN		
			
        // Second range class is from 80 to 1000 ut units (its always from the previous class to the maximum
        // distance of actual class. More weapons are in this class with FLAK CANNON having the top priority.
        weaponPrefs.newPrefsRange(1000).add(UT2004ItemType.FLAK_CANNON, true).add(UT2004ItemType.MINIGUN, true)
                .add(UT2004ItemType.LINK_GUN, false).add(UT2004ItemType.ASSAULT_RIFLE, true);        
				
        // Third range class is from 1000 to 4000 ut units - that's quite far actually
        weaponPrefs.newPrefsRange(4000).add(UT2004ItemType.SHOCK_RIFLE, true).add(UT2004ItemType.MINIGUN, false);
				
        // The last range class is from 4000 to 100000 ut units. In practise 100000 is
        // the same as infinity as there is no map in UT that big
        weaponPrefs.newPrefsRange(100000).add(UT2004ItemType.LIGHTNING_GUN, true).add(UT2004ItemType.SHOCK_RIFLE, true);
	}

	private void calculateWeaponsPriority() {
		boolean primaryFiringMode = shoot.getLastShooting() == null ? true : shoot.getLastShooting().isPrimary();
		int currentAmmo = primaryFiringMode ? info.getCurrentAmmo() : info.getCurrentSecondaryAmmo();
		ItemType currentAmmoType = primaryFiringMode ? weaponry.getCurrentWeapon().getDescriptor().getPriAmmoItemType() : 
				weaponry.getCurrentWeapon().getDescriptor().getSecAmmoItemType();
		
		weaponsPriority = 0;
		
		if(currentAmmo < weaponry.getMaxAmmo(currentAmmoType)){
			weaponsPriority++;
			if(currentAmmo < getCriticalAmmoAmount()){
				weaponsPriority += 5;
			}
			if(currentAmmo == 0){
				weaponsPriority = 10;
			}
		}
		log.log(Level.INFO, "Weapons priority calculated to {0} [calculateWeaponsPriority()]", weaponsPriority);
	}

	private int getCriticalAmmoAmount() {
		return 10;
	}

	private void callculateHarvestingPriority() {
        for (ItemTypeStatistic itemTypeStatistic : itemTypeStatistics.values()) {
            itemTypeStatistic.countAmountPriority();
            itemTypeStatistic.countOverallPriority();
        }
		
		
		harvestingPriorities = new ArrayList<ItemStatistic>(itemStatistics.values());
		
		Iterator<ItemStatistic> iterator = harvestingPriorities.iterator();
		while (iterator.hasNext()) {
			ItemStatistic harvestingPriority = iterator.next(); 
		  
			// ze seznamu priorit zcela vyřadíme věci které nemůžemne sebrat, 
			if(!items.isPickable(harvestingPriority.getItem()) || 
					// které jsou nedosažitelné
					!fwMap.reachable(info.getNearestNavPoint(), 
							navigation.getNearestNavPoint(harvestingPriority.getItem())) ||
					// nebo které ještě nejsou respawnované               
					harvestingPriority.getTimeToRespawn() != 0){
				iterator.remove();
			}
			else {
				ItemTypeStatistic statisticForItemType = itemTypeStatistics.get(harvestingPriority.getItem().getType());
				harvestingPriority.countDistancePriority();
				// předměty, ke kterým nebudeme znát typ, budou mít nulovou prioritu
				
				harvestingPriority.countOverallPriority(statisticForItemType);
			}
		}
		
        Collections.sort(harvestingPriorities, Collections.reverseOrder());
	}

	private void addEnemy(UnrealId id) {
		Player enemy = players.getPlayer(id);
		enemies.put(id, new EnemyInfo(enemy));
		log.log(Level.INFO, "Enemy added: {0} [calculateWeaponsPriority()]", id);
	}

	/**
	 * Initialize bot modules
	 */
	private void initializeModules() {
		informationBase = new InformationBase(this, log, players, ctf, mainPathPlanner, info, fwMap, navigation, shoot,
			weaponPrefs);
		
		comunicationModule = new ComunicationModule(this, log, tcClient, informationBase);
		
		actionPlanner = new ActionPlanner(this);
		activityPlanner = new ActivityPlanner(this, log, move, ctf, info, navigation, informationBase);
		
		actionPlanner.init(activityPlanner);
		activityPlanner.init(actionPlanner);
		
		
	}
	
	public void setName(String state){
		String name = isLeader() ? String.format("%s (leader)", startName) : startName;
		config.setName(String.format("%s - %s, %s", name, getGoal(), state));
	}

	private void sendLocationMessage() {
		if(info.getTime() - lastLocationMessageSendTime >  LOCATION_MESSAGE_SEND_INTERVAL){
			comunicationModule.sendLocationMessage()
		}
	}
}
