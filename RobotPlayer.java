package launcherStratPlusSoldiers;

import battlecode.common.*;

import java.util.*;

public class RobotPlayer {
	public static void run(RobotController rc) throws GameActionException {
        BaseBot myself;
        
        switch(rc.getType()) {
        	case HQ: 
        		myself = new HQ(rc);
        		break;
        	case BEAVER:
        		myself = new Beaver(rc);
        		break;
        	case MINER:
        		myself = new Miner(rc);
        		break;
        	case MINERFACTORY:
        		myself = new MinerFactory(rc);
        		break;
        	case HELIPAD:
        		myself = new Helipad(rc);
        		break;		
        	case DRONE:
                myself = new Drone(rc);
                break;
        	case BARRACKS:
        		myself = new Barracks(rc);
        		break;
        	case SOLDIER:
        		myself = new Soldier(rc);
        		break;
        	case TANKFACTORY:
        		myself = new TankFactory(rc);
        		break;
        	case TANK:
        		myself = new Tank(rc);
        		break;
        	case TECHNOLOGYINSTITUTE:
        		myself = new TechnologyInstitute(rc);
        		break;
        	case TRAININGFIELD:
        		myself = new TrainingField(rc);
        		break;
        	case COMMANDER:
        		myself = new Commander(rc);
        		break;
        	case TOWER:
        		myself = new Tower(rc);
        		break;
        	case AEROSPACELAB:
        		myself = new AerospaceLab(rc);
        		break;
        	case LAUNCHER:
        		myself = new Launcher(rc);
        		break;
        	case MISSILE:
        		myself = new Missile(rc);
        		break;
        	case SUPPLYDEPOT:
        		myself = new SupplyDepot(rc);
        		break;
        	default:
        		myself = new BaseBot(rc);
        		break;
        }
        while (true) {
            try {
                myself.go();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}
}