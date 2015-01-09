package testing;

import battlecode.common.*;

public class Miner extends BaseBot {
	
	public Mover move;
	public MapLocation targetLoc;
	public MiningController mc;
	
    public Miner(RobotController rc) {
        super(rc);
        move = new Mover(rc);
        targetLoc = null;
        mc = new MiningController(rc);
    }
    
    
    
    
}
