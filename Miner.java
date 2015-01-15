package testing;

import battlecode.common.*;

public class Miner extends BaseBot {

	public Mover move;
	public MapLocation targetLoc;
	public MiningController mc;
	public State state;

	public Miner(RobotController rc) {
		super(rc);
		move = new Mover(rc);
		targetLoc = null;
		mc = new MiningController(rc);
		state = State.MINING;
	}

	public void execute() throws GameActionException {
		RobotInfo[] enemies = getEnemiesInAttackingRange();

        if (enemies.length > 0) {
            //attack!
            if (rc.isWeaponReady()) {
                attackLeastHealthEnemy(enemies);
            }
        }
    	if (rc.isCoreReady()) {
//			if(Clock.getRoundNum() < 500 && rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) < 10){//
//    			tryMove(rc.senseHQLocation().directionTo(rc.getLocation()));
//    		}
    	
    		if(rc.senseOre(rc.getLocation()) >= 4){
	    		if(rc.canMine()){
	    			rc.mine();
	    		}
	    	}
    		else{
				int fate = rand.nextInt(1000);
				if (fate < 600) {
					rc.mine();
				} 
				else if (fate < 900) {
					tryMove(directions[rand.nextInt(8)]);
				} 
				else {
					tryMove(rc.getLocation().directionTo(rc.senseEnemyHQLocation()));
				}
    		}
			
    	}
    	
    	transferSupplies();
    	
    	rc.yield();
    }

}
