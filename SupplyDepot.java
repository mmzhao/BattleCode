package launcherStratPlusSoldiers;

import battlecode.common.*;

public class SupplyDepot extends BaseBot {

	public SupplyDepot(RobotController rc) {
		super(rc);
	}

	public void execute() throws GameActionException {

		
		transferSupplies();
		
		rc.yield();
	}
	

}
