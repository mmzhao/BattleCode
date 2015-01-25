package launcherStrat;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class TechnologyInstitute extends BaseBot{
	
	public TechnologyInstitute(RobotController rc) {
		super(rc);
	}

	public void execute() throws GameActionException {
		
		
		transferSupplies();
		rc.yield();
	}

}
