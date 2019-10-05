package robot;

import map.Map;
import map.MapConstants;
import robot.RobotConstants.DIRECTION;
//import robot.RobotConstants.DiagonalDIRECTION;
import robot.RobotConstants.MOVEMENT;
import utils.CommMgr;
import utils.MapDescriptor;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

// @formatter:off

/**
 * Represents the robot moving in the arena.
 * <p>
 * The robot is represented by a 3 x 3 cell space as below:
 * <p>
 * ^   ^   ^
 * SR  SR  SR
 * < SR
 * < LR [X] [X] [X] SR>
 * [X] [X] [X]
 * [X] [X] [X] SR>
 * <p>
 * SR = Short Range Sensor, LR = Long Range Sensor
 * 
 * 
 *updated one 
 * 
 *  
 *  
 *       ^  ^    ^  
 *      SR  SR   SR
 * 	 	[X] [X] [X] 
 * < LR [X] [X] [X] LR >
 * 		[X] [X] [X] 
 * 
 * 
 *suggesting 
 **       ^  ^    ^  
 *      SR  SR   SR
 * 	 	[X] [X] [X] LR >
 * < LR [X] [X] [X] 
 * 		[X] [X] [X] SR>
 * <p>
 * SR = Short Range Sensor, LR = Long Range Sensor
 *
 *
 *Right Bottom may explore some cells but some cells remain unexplored
 * @author Suyash Lakhotia
 */
// @formatter:on

public class Robot {
	private int posRow; // row position based on center cell
	private int posCol; // col position based on center cell
	private DIRECTION robotDir;
	private int speed;
	private final Sensor SRMiddleLeftN;		// center row leftside, facing North
	private final Sensor SRFrontCenterN;	// front row center, facing North
	private final Sensor SRMiddleRightN; 	// center row rightside, facing North
	private final Sensor SRFrontRightE;		// front row rightside, facing East
	private final Sensor SRFrontLeftW;		// front row leftside, facing West
	private final Sensor LRMiddleCenterW;		// middle row center, facing West
	private int counter=0;
	
	private boolean touchedGoal;
	public boolean realBot;
	
	public Robot(int row, int col, boolean realBot) {
		posRow = row;
		posCol = col;
        robotDir = RobotConstants.START_DIR;
        speed = RobotConstants.SPEED;

        this.realBot = realBot;
        
        SRMiddleLeftN = new Sensor(1,3, this.posRow + 1, this.posCol - 1, this.robotDir,"SRMLN");
        SRFrontCenterN = new Sensor(1,3, this.posRow + 1, this.posCol, this.robotDir, "SRFCN");
        SRMiddleRightN = new Sensor(1,3, this.posRow + 1, this.posCol + 1, this.robotDir, "SRMRN");
        SRFrontRightE = new Sensor(1,3, this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT), "SRFRE");
        SRFrontLeftW = new Sensor(1,3, this.posRow + 1, this.posCol - 1, findNewDirection(MOVEMENT.LEFT), "SRFLW");
        LRMiddleCenterW = new Sensor(3,7, this.posRow, this.posCol - 1, findNewDirection(MOVEMENT.LEFT), "LRL");
	}
	
	public void setRobotPos(int row, int col) {
        posRow = row;
        posCol = col;
    }

    public int getRobotPosRow() {
        return posRow;
    }

    public int getRobotPosCol() {
        return posCol;
    }

    public void setRobotDir(DIRECTION dir) {
        robotDir = dir;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public DIRECTION getRobotCurDir() {
        return robotDir;
    }

    public boolean getRealBot() {
        return realBot;
    }

    private void updateTouchedGoal() { //always update after everymove to see if it is at goal zone
    	//means after it touch goal, touchedGoal will always be true
        if (this.getRobotPosRow() == MapConstants.GOAL_ROW && this.getRobotPosCol() == MapConstants.GOAL_COL)
            this.touchedGoal = true;
    }

    public boolean getTouchedGoal() {
        return this.touchedGoal;
    }
    
    /**
     * Takes in a MOVEMENT and moves the robot accordingly by changing its position and direction. Sends the movement
     * if this.realBot is set.
     */
    public void move(MOVEMENT m, boolean sendMoveToAndroid) {
        if (!realBot) {
        	
            // Emulate real movement by pausing execution.
            try {
                TimeUnit.MILLISECONDS.sleep(speed);
            } catch (InterruptedException e) {
                System.out.println("Something went wrong in Robot.move()!");
            }
        }
       // System.out.println("Moving");
        switch (m) {
            case FORWARD:
                switch (robotDir) {
                    case NORTH:
                        posRow++;
                        break;
                    case EAST:
                        posCol++;
                        break;
                    case SOUTH:
                        posRow--;
                        break;
                    case WEST:
                        posCol--;
                        break;
                    case NORTHEAST:
                    	posRow++;
                    	posCol++;
                    	break;
                    case NORTHWEST:
                    	posRow++;
                    	posCol--;
                    	break;
                    case SOUTHEAST:
                    	posRow--;
                    	posCol++;
                    	break;
                    case SOUTHWEST:
                    	posRow--;
                    	posCol--;
                    	break;
                    	
                }
                break;
            case BACKWARD:
                switch (robotDir) {
                    case NORTH:
                        posRow--;
                        break;
                    case EAST:
                        posCol--;
                        break;
                    case SOUTH:
                        posRow++;
                        break;
                    case WEST:
                        posCol++;
                        break;
                    case NORTHEAST:
                    	posRow--;
                    	posCol--;
                    	break;
                    case NORTHWEST:
                    	posRow--;
                    	posCol++;
                    	break;
                    case SOUTHEAST:
                    	posRow++;
                    	posCol--;
                    	break;
                    case SOUTHWEST:
                    	posRow++;
                    	posCol++;
                    	break;
                    	
                }
                break;
            case RIGHT:
            case LEFT:
                robotDir = findNewDirection(m);
                break;
            case CALIBRATE:
            
            case ALIGN_FRONT:
            case ALIGN_RIGHT:
            case LEFT_NO_SENSE:
            case RIGHT_NO_SENSE:
            case SENSE:
                break;
            default:
                System.out.println("Error in Robot.move()!");
                break;
        }

        if (realBot) sendMovement(m, sendMoveToAndroid);
        else {
        	System.out.println("sengind movement for cali");
        	System.out.println("Move: " + MOVEMENT.print(m));
        }
        
        
        System.out.println("-------------------------------------");
        updateTouchedGoal();
    }

    /**
     * Overloaded method that calls this.move(MOVEMENT m, boolean sendMoveToAndroid = true).
     */
    public void move(MOVEMENT m) {
        this.move(m, true);
    }

    /**
     * Sends a number instead of 'F' for multiple continuous forward movements.
     */
    public void moveForwardMultiple(int count) {
        if (count == 1) {
            move(MOVEMENT.FORWARD);
        } else {
            String[] stepIns = new String[]{"d","2","3","4","5"};

            CommMgr comm = CommMgr.getCommMgr();
            // comm.sendMsg((MOVEMENT.FORWARD + "").repeat(count), CommMgr.INSTRUCTIONS);
            comm.sendMsg("AR",CommMgr.MOVEMENT,stepIns[count-1]);
            comm.sendMsg("AN", CommMgr.MOVEMENT,stepIns[count-1]);

            switch (robotDir) {
                case NORTH:
                    posRow += count;
                    break;
                case EAST:
                    posCol += count;
                    break;
                case SOUTH:
                    posRow -= count;
                    break;
                case WEST:
                    posCol -= count;
                    break;
                case NORTHEAST:
                	posRow+=count;
                	posCol+=count;
                	break;
                case NORTHWEST:
                	posRow+=count;
                	posCol-=count;
                	break;
                case SOUTHEAST:
                	posRow-=count;
                	posCol+=count;
                	break;
                case SOUTHWEST:
                	posRow-=count;
                	posCol-=count;
                	break;

                    
            }

            // comm.sendMsg(this.getRobotPosRow() + "," + this.getRobotPosCol() + "," + DIRECTION.print(this.getRobotCurDir()), CommMgr.BOT_POS);
        }
    }
    
    

    /**
     * Uses the CommMgr to send the next movement to the robot.
     */
    private void sendMovement(MOVEMENT m, boolean sendMoveToAndroid) {
        CommMgr comm = CommMgr.getCommMgr();
        
        comm.sendMsg("AR",CommMgr.INSTRUCTIONS,(MOVEMENT.print(m)));
//        if (m != MOVEMENT.CALIBRATE && sendMoveToAndroid) {
//            comm.sendMsg(this.getRobotPosRow() + "," + this.getRobotPosCol() + "," + DIRECTION.print(this.getRobotCurDir()), CommMgr.BOT_POS);
//        }
    }

	/**
	 * Set the sensors' position and direction values according to the robot's current position and direction
	 */
	public void setSensors() {
		switch (robotDir) {
		case NORTH:
			SRMiddleLeftN.setSensor(this.posRow + 1, this.posCol - 1, this.robotDir);
			SRFrontCenterN.setSensor(this.posRow + 1, this.posCol, this.robotDir);
			SRMiddleRightN.setSensor(this.posRow + 1, this.posCol + 1, this.robotDir);
			SRFrontRightE.setSensor(this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
			SRFrontLeftW.setSensor(this.posRow + 1, this.posCol - 1, findNewDirection(MOVEMENT.LEFT));
			LRMiddleCenterW.setSensor(this.posRow, this.posCol - 1, findNewDirection(MOVEMENT.LEFT));
			break;
		case EAST:
			SRMiddleLeftN.setSensor(this.posRow + 1, this.posCol + 1, this.robotDir);
			SRFrontCenterN.setSensor(this.posRow, this.posCol + 1, this.robotDir);
			SRMiddleRightN.setSensor(this.posRow - 1, this.posCol + 1, this.robotDir);
			SRFrontRightE.setSensor(this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
			SRFrontLeftW.setSensor(this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.LEFT));
			LRMiddleCenterW.setSensor(this.posRow + 1, this.posCol, findNewDirection(MOVEMENT.LEFT));
			break;
		case SOUTH:
			SRMiddleLeftN.setSensor(this.posRow - 1, this.posCol + 1, this.robotDir);
			SRFrontCenterN.setSensor(this.posRow - 1, this.posCol, this.robotDir);
			SRMiddleRightN.setSensor(this.posRow - 1, this.posCol - 1, this.robotDir);
			SRFrontRightE.setSensor(this.posRow - 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
			SRFrontLeftW.setSensor(this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.LEFT));
			LRMiddleCenterW.setSensor(this.posRow, this.posCol + 1, findNewDirection(MOVEMENT.LEFT));
			break;
		case WEST:
			SRMiddleLeftN.setSensor(this.posRow - 1, this.posCol - 1, this.robotDir);
			SRFrontCenterN.setSensor(this.posRow, this.posCol - 1, this.robotDir);
			SRMiddleRightN.setSensor(this.posRow + 1, this.posCol - 1, this.robotDir);
			SRFrontRightE.setSensor(this.posRow + 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
			SRFrontLeftW.setSensor(this.posRow - 1, this.posCol - 1, findNewDirection(MOVEMENT.LEFT));
			LRMiddleCenterW.setSensor(this.posRow - 1, this.posCol, findNewDirection(MOVEMENT.LEFT));
			break;
		}
	}
	
    /**
     * Uses the current direction of the robot and the given movement to find the new direction of the robot.
     */
    private DIRECTION findNewDirection(MOVEMENT m) {
        if (m == MOVEMENT.RIGHT) {
            return DIRECTION.getNext(robotDir);
        } else {
            return DIRECTION.getPrevious(robotDir);
        }
    }
    
    /**
     * Calls the .sense() method of all the attached sensors and stores the received values in an integer array.
     */
    public int[] sense(Map explorationMap, Map realMap, Consumer<Boolean> shouldCheckLeftSensor) {
        int[] result = new int[7];
        String[] androidStrings;

        if (!realBot) {
        	result[0] = LRMiddleCenterW.sense(explorationMap, realMap);
        	result[1] = SRMiddleLeftN.sense(explorationMap, realMap);
        	result[2] = SRFrontLeftW.sense(explorationMap, realMap);
        	result[3] = SRMiddleRightN.sense(explorationMap, realMap);
        	result[4] = SRFrontRightE.sense(explorationMap, realMap);
        	result[5] = SRFrontCenterN.sense(explorationMap, realMap);
        	androidStrings = MapDescriptor.generateMapDescriptor(explorationMap);
        	System.out.println("Sending: " + androidStrings[0] + "," + androidStrings[1]);
        	/*
        	
        	result[0] = SRMiddleLeftN.sense(explorationMap, realMap);
        	result[1] = SRFrontCenterN.sense(explorationMap, realMap);
        	result[2] = SRMiddleRightN.sense(explorationMap, realMap);
        	result[3] = SRFrontRightE.sense(explorationMap, realMap);
        	result[4] = SRFrontLeftW.sense(explorationMap, realMap);
        	result[5] = LRMiddleCenterW.sense(explorationMap, realMap);
        	*/
        	System.out.println("after map des");
        }
        else {
        	
        	String[] msgArr;
        	CommMgr comm = CommMgr.getCommMgr();
        	System.out.println("Before dowhile");
        	
            do {
	            String realMsg = comm.recvMsg();
//	            if (realMsg == null) return result;
	            
	            msgArr = realMsg.split(",");
	            System.out.println(msgArr[6] + " : " + counter);
	
	           /* if (msgArr[0].equals(CommMgr.SENSOR_DATA)) {*/
	            
	           // special case
	            
//	           if(Integer.parseInt(msgArr[6]) > counter)
//	        	   counter++;
        	   
                try {
                    result[0] = Integer.parseInt(msgArr[0]);
                } catch (Exception e) {
                    result[0] = 0;
                    System.out.println("Result[0] set to 0");
                }
                try {
                    result[1] = Integer.parseInt(msgArr[1]);
                } catch (Exception e) {
                    result[1] = 0;
                    System.out.println("Result[1] set to 0");
                }
                try {
                    result[2] = Integer.parseInt(msgArr[2]);
                } catch (Exception e) {
                    result[2] = 0;
                    System.out.println("Result[2] set to 0");
                }
                try {
                    result[3] = Integer.parseInt(msgArr[3]);
                } catch (Exception e) {
                    result[3] = 0;
                    System.out.println("Result[3] set to 0");
                }
                try {
                    result[4] = Integer.parseInt(msgArr[4]);
                } catch (Exception e) {
                    result[4] = 0;
                    System.out.println("Result[4] set to 0");
                }
                try {
                    result[5] = Integer.parseInt(msgArr[5]);
                } catch (Exception e) {
                    result[5] = 0;
                    System.out.println("Result[5] set to 0");
	                }
                androidStrings = MapDescriptor.generateMapDescriptor(explorationMap);
	          /*  }*/
//                System.out.println("test for Map"+MapDescriptor.generateMapForAndroid(explorationMap));
	        	System.out.println("before sending msg to AN");
	        	comm.sendMsg("AN",null,androidStrings[0] + "," + androidStrings[1]);
	        	
	        	//comm.sendMsg("AN", "test", "AAA");
	        	System.out.println("after sending msg to AN");
	        	//send map to android
            } while (Integer.parseInt(msgArr[6]) != counter);
            
            LRMiddleCenterW.senseReal(explorationMap,result[0]);
        	SRMiddleLeftN.senseReal(explorationMap,result[1]);
        	SRFrontLeftW.senseReal(explorationMap,result[2]);
        	SRMiddleRightN.senseReal(explorationMap,result[4]);
        	SRFrontRightE.senseReal(explorationMap,result[3]);
        	SRFrontCenterN.senseReal(explorationMap,result[5]);
            counter++;
        }
        return result;
    }
    
    public int[] sense(Map explorationMap, Map realMap) {
        return sense(explorationMap, realMap, null);
    }

    public void setBotToFake() {
        realBot = false;
    }
	
}

