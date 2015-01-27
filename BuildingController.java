package launcherStratPlusSoldiers;

import battlecode.common.*;

public class BuildingController {

	public RobotController rc;
	public RobotType[] keys = {RobotType.BARRACKS, RobotType.HELIPAD, RobotType.TANKFACTORY, 
		RobotType.TECHNOLOGYINSTITUTE, RobotType.TRAININGFIELD, RobotType.AEROSPACELAB};
	public boolean[][] senseable = {{true, true, true, true, true}, {true, true, true, true, true}, {true, true, true, true, true},
			{true, true, true, true, false}, {true, true, true, false, false}};
	public RobotType structureToBeBuilt;
	
	public BuildingController(RobotController rc) {
		this.rc = rc;
		this.structureToBeBuilt = null;
	}
	
	
	public MapLocation getBuildLocation() throws GameActionException {
		MapLocation cur = rc.getLocation();
		int x = cur.x;
		int y = cur.y;
		MapLocation result = null, test;
		MapLocation[] testLocationIndices = {new MapLocation(x, y+1), new MapLocation(x+1, y+1), 
				new MapLocation(x+1, y), new MapLocation(x+1, y-1), new MapLocation(x, y-1), new MapLocation(x-1, y-1),
				new MapLocation(x-1, y), new MapLocation(x-1, y+1) }; //only check circle of radius one
				/** new MapLocation(x, y+2), new MapLocation(x+1, y+2),
				new MapLocation(x+2, y+2), new MapLocation(x+2, y+1), new MapLocation(x+2, y), new MapLocation(x+2, y-1),
				new MapLocation(x+2, y-2), new MapLocation(x+1, y-2), new MapLocation(x, y-2), new MapLocation(x-1, y-2),
				new MapLocation(x-2, y-2), new MapLocation(x-2, y-1), new MapLocation(x-2, y), new MapLocation(x-2, y+1),
				new MapLocation(x-2, y+2), new MapLocation(x-1, y+2), new MapLocation(x, y+3), new MapLocation(x+1, y+3),
				new MapLocation(x+2, y+3), new MapLocation(x+3, y+3), new MapLocation(x+3, y+2), new MapLocation(x+3, y+1),
				new MapLocation(x+3, y), new MapLocation(x+3, y-1), new MapLocation(x+3, y-2), new MapLocation(x+3, y-3), 
				new MapLocation(x+2, y-3), new MapLocation(x+1, y-3), new MapLocation(x, y-3), new MapLocation(x-1, y-3), 
				new MapLocation(x-2, y-3), new MapLocation(x-3, y-3), new MapLocation(x-3, y-2), new MapLocation(x-3, y-1), 
				new MapLocation(x-3, y), new MapLocation(x-3, y+1), new MapLocation(x-3, y+2), new MapLocation(x-3, y+3),
				new MapLocation(x-2, y+3), new MapLocation(x-1, y+3), new MapLocation(x, y+4), new MapLocation(x+1, y+4), 
				new MapLocation(x+2, y+4), new MapLocation(x+4, y+2), new MapLocation(x+4, y+1), new MapLocation(x+4, y),
				new MapLocation(x+4, y-1), new MapLocation(x+4, y-2), new MapLocation(x+2, y-4), new MapLocation(x+1, y-4), 
				new MapLocation(x, y-4), new MapLocation(x-1, y-4), new MapLocation(x-2, y-4), new MapLocation(x-4, y-2), 
				new MapLocation(x-4, y-1), new MapLocation(x-4, y), new MapLocation(x-4, y+1), new MapLocation(x-4, y+2),
				new MapLocation(x-2, y+4), new MapLocation(x-1, y+4)}; **/
		int length = testLocationIndices.length;
		structureToBeBuilt = keys[rc.readBroadcast(10)]; //change this to the right message reading method
		for (int i = 0; i<length; i++) {
			test = testLocationIndices[i];
			if (rc.senseOre(test)<=20) {
				result = test;
				break;
			}
		}
		return result;
	}
	
	
}
