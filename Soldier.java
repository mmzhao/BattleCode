package launcherStrat;

import battlecode.common.*;

//NEEDS DECISION MAKING ON WHEN TO SKIRMISH
public class Soldier extends BaseBot{

	private int towerToProtect;
	private boolean attackTarget;
	public MapLocation targetLoc;
	
    public Soldier(RobotController rc) throws GameActionException {
        super(rc);
        towerToProtect = -1;
        attackTarget = false;
        targetLoc = null;
//        if(rc.getID() % 2 == 0){
//        	offsets[1] *= -1;
//			offsets[2] *= -1;
//			offsets[3] *= -1;
//        }
    }

    public void execute() throws GameActionException {
    	if (Clock.getRoundNum() <= 500) { //protect miners mode
    		earlyGame();
    	}
    	
    	else {
    		restofGame();
    	}
    	
    	transferSupplies();
    	
    	rc.yield();
    }
    
    public void earlyGame() throws GameActionException {
    	MapLocation cur = rc.getLocation();
    	if (rc.readBroadcast(10000) == 1) { //miners found enemies early 
    		MapLocation target = new MapLocation(rc.readBroadcast(10001), rc.readBroadcast(10002));
    		RobotInfo ri;
    		if (rc.canSenseLocation(target)) {
    			ri = rc.senseRobotAtLocation(target);
    		}
    		else {
    			ri = null;
    		}
    		if (ri == null || ri.team == myTeam) { //location is old, update it
    			RobotInfo enemies[] = rc.senseNearbyRobots(35, myTeam.opponent());
    			if (enemies.length == 0) { //no enemies nearby
    				rc.broadcast(10000, 0);
    			}
    			else { // go to enemy
    				if (rc.isCoreReady()) 
    					tryMove(cur.directionTo(target));
    			}
    		}
    		else {
    			if (rc.canAttackLocation(target) && rc.isWeaponReady()) { //we can attack
    				rc.attackLocation(target);
    			}
    			else if (!rc.canAttackLocation(target)) { //too far away
    				if (rc.isCoreReady())
    					tryMove(cur.directionTo(target));
    			}
    		}
    	}
    	else { //go to enemy side
    		RobotInfo[] enemies = rc.senseNearbyRobots(8, theirTeam);
    		if (enemies.length != 0) {
    			if (rc.isWeaponReady()) {
    				super.attackForValue(enemies);
    			}
    		}
    		if (targetLoc == null) {
    			targetLoc = rc.senseEnemyHQLocation();
    		}
    		if (rc.isCoreReady())
    			tryMove(cur.directionTo(targetLoc));
    	}
    }
    
    public void restofGame() throws GameActionException{
    	RobotInfo[] enemies = getEnemiesInAttackingRange(RobotType.SOLDIER);
    	if (enemies.length > 0) { //attack!
    		if (rc.isWeaponReady()) {
    			attackForValue(enemies);
    		}
    	}
    	
    	if(rc.isCoreReady()){
        	micro();
        }
        
        if (rc.isCoreReady()) {
    		if(rc.readBroadcast(2000) == 1){
    			micro();
    			if(rc.isCoreReady()){
	    			attackTarget = true;
	    			int rallyX = rc.readBroadcast(2001);
		            int rallyY = rc.readBroadcast(2002);
		            targetLoc = new MapLocation(rallyX, rallyY);
		            tryMove(rc.getLocation().directionTo(targetLoc));
    			}
    		}
    		else{
    			micro();
    			if(rc.isCoreReady()){
	    			attackTarget = false;
		            int rallyX = rc.readBroadcast(1001);
		            int rallyY = rc.readBroadcast(1002);
		            targetLoc = new MapLocation(rallyX, rallyY);
	//	            rc.setIndicatorString(0, rallyX + " " + rallyY);
	//	            rc.setIndicatorString(0, rallyX + " " + rallyY);
		            tryMove(rc.getLocation().directionTo(targetLoc));
    			}
    		}
        }
    }
    
    public void micro() throws GameActionException{
    	RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 24, theirTeam);
    	int length = enemies.length;
    	if(length == 0) return;
    	boolean noAttackersInRange = true;
    	for(int i = length - 1; --i>=0;){
    		RobotType iType = enemies[i].type;
    		if(iType == RobotType.TOWER || iType == RobotType.HQ) continue;
    		if(iType.attackRadiusSquared >= enemies[i].location.distanceSquaredTo(rc.getLocation())){
    			noAttackersInRange = false;
    			break;
    		}
    	}
    	if(noAttackersInRange){ 
    		return;
    	}
    	int totalX = 0;
    	int totalY = 0;
    	for(int i = length; --i>0;){
    		totalX += enemies[i].location.x;
    		totalY += enemies[i].location.y;
    	}
    	MapLocation enemyCenter = new MapLocation(totalX/enemies.length, totalY/enemies.length);
    	tryMove(enemyCenter.directionTo(rc.getLocation()));
    	
    }
    
    public void attackForValue(RobotInfo[] targets) throws GameActionException {
    	if (rc.readBroadcast(15000) == 1) { //already have a target
    		
    	}
    	int maxValue = -1, minDist = 1000;
    	int dist, value;
    	boolean sensedMissile = false;
    	MapLocation target = null;
    	RobotInfo r = null;
    	for (RobotInfo ri : targets) {
    		if (ri.type == RobotType.MISSILE) {
    			sensedMissile = true;
    			dist = ri.location.distanceSquaredTo(rc.getLocation());
    			if (dist < minDist) {
    				minDist = dist;
    				r = ri;
    				target = ri.location;
    			}
    		}
    		else if (!sensedMissile) {
    			switch(ri.type) {
    			case TOWER: case HQ:
    				value = 0;
    				break;
    			case BEAVER:
    				value = 2;
    				break;
    			case MINER:
    				value = 3;
    				break;
    			case SOLDIER:
    				value = 4;
    				break;
    			case BASHER:
    				value = 5;
    				break;
    			case DRONE:
    				value = 6;
    				break;
    			case TANK:
    				value = 7;
    				break;
    			case LAUNCHER:
    				value = 8;
    				break;
    			case COMMANDER:
    				value = 9;
    				break;
    			case COMPUTER:
    				value = 1;
    				break;
    			default:
    				value = 1;
    				break;
    			}
    			if(ri.health < RobotType.SOLDIER.attackPower)
    				value += 10;      
    			
    			if (value > maxValue) {
    				maxValue = value;
    				target = ri.location;
    			}
    		}
    	}
    	
    	if (sensedMissile) { //prioritize handling missiles
    		handleMissile(r, target, minDist);
    	}
    	
    	else {
    		rc.attackLocation(target);
    	}
    	
    }

    public void handleMissile(RobotInfo r, MapLocation loc, int dist) throws GameActionException {
    	if ((r.health == 1 && rc.canAttackLocation(loc)) || dist < 2 ) { 
    		if (rc.isWeaponReady()) //if we can kill safely, attack; or if its already next to us
    			rc.attackLocation(loc);
    		else {
    			if (rc.isCoreReady()) //can't attack, try to retreat
    				tryMove(loc.directionTo(rc.getLocation()));
    		}
    		return;
    	}
    	
    	
    	RobotInfo[] allies = rc.senseNearbyRobots(24, myTeam);
    	if (allies.length == 0) { //safe to kite back
    		if (rc.isCoreReady()) {
    			tryMove(loc.directionTo(rc.getLocation()));
    		}
    		else {
    			rc.attackLocation(loc);
    		}
    	}
    	
    	else {
    		for (RobotInfo ri : allies) {
    			if (ri.type == RobotType.LAUNCHER && ri.location.distanceSquaredTo(loc) < 2) { //missile is next to launcher
    				if (rc.canAttackLocation(loc) && rc.isWeaponReady()) {
    					rc.attackLocation(loc);
    				}
    				else if (rc.isCoreReady()) //retreat
    					tryMove(loc.directionTo(rc.getLocation()));
    			}
    			
    		}
    	}
    	
    }
    
/*    public void execute() throws GameActionException {
    	rc.setIndicatorString(1, towerToProtect + "");
        RobotInfo[] enemies = getEnemiesInAttackingRange(rc.getType());
        
        
        
        //micro
        if(rc.isCoreReady()){
//        	micro();
        }
        
        if (enemies.length > 0) {
            //attack!
            if (rc.isWeaponReady()) {
                attackForValue(enemies);
            }
        }
        
        if (rc.isCoreReady()) {
//			micro();
			if(rc.isCoreReady()){
    			attackTarget = false;
	            int rallyX = rc.readBroadcast(1001);
	            int rallyY = rc.readBroadcast(1002);
	            targetLoc = new MapLocation(rallyX, rallyY);
//	            rc.setIndicatorString(0, rallyX + " " + rallyY);
//	            rc.setIndicatorString(0, rallyX + " " + rallyY);
	            tryMove(rc.getLocation().directionTo(targetLoc));
			}
        }
        
        transferSupplies();
        
        rc.yield();
    }
*/
    
    public void tryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < offsets.length &&
				(!rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8]) || 
						!isSafe(rc.getLocation().add(directions[(dirint+offsets[offsetIndex]+8)%8])))) {
			offsetIndex++;
		}
		if (offsetIndex < offsets.length) {
			previous = rc.getLocation();
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
		}
		else{
			offsets[1] *= -1;
			offsets[2] *= -1;
			offsets[3] *= -1;
		}
	}
    
	
    public boolean isSafe(MapLocation ml) throws GameActionException{
    	MapLocation[] enemyTowers = rc.senseEnemyTowerLocations();
    	for(MapLocation m: enemyTowers){
    		if(targetLoc != null && targetLoc.x == m.x && targetLoc.y == m.y){
    			continue;
    		}
    		if(ml.distanceSquaredTo(m) <= RobotType.TOWER.attackRadiusSquared){
    			return false;
    		}
    	}
    	int HQRadiusAdd = 0;
    	if(enemyTowers.length >= 2){
    		HQRadiusAdd = 11;
    	}
    	if(ml.distanceSquaredTo(theirHQ) <= RobotType.HQ.attackRadiusSquared + HQRadiusAdd){
    		if(targetLoc == null || targetLoc.x != theirHQ.x || targetLoc.y != theirHQ.y){
        		return false;
    		}
    	}
    	if(attackTarget) return true;
    	if(rc.readBroadcast(4000) == 1 || rc.readBroadcast(3000) == 1) return true;
    	RobotInfo[] enemies = rc.senseNearbyRobots(rc.getLocation(), 24, theirTeam);
    	for(RobotInfo enemy:enemies){
    		if(ml.distanceSquaredTo(enemy.location) <= enemy.type.attackRadiusSquared){
    			return false;
    		}
    	}
    	return true;
    }
    
}

//TOWER PROTECTION CODE
//towerToProtect = -1;
//int closestDist = Integer.MAX_VALUE;
//for(int i = 0; i < rc.readBroadcast(20); i++){
//	int numProtect = rc.readBroadcast(23 + 10 * i);
//	if(numProtect < 2){
//		towerToProtect = i;
//		break;
//	}
//	if(numProtect < 3){
//		MapLocation ml = new MapLocation(rc.readBroadcast(21 + 10 * i), rc.readBroadcast(22 + 10 * i));
//		int dist = rc.getLocation().distanceSquaredTo(ml);
//		if(dist < closestDist){
//			closestDist = dist;
//			towerToProtect = i;
//		}
//	}
//}
//if(towerToProtect != -1){
//	rc.broadcast(23 + 10 * towerToProtect, rc.readBroadcast(23 + 10 * towerToProtect) + 1);
//}
