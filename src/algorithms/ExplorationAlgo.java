package algorithms;

import map.Cell;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import utils.CommMgr;
import ycfastestpath.YcFastestPath;

import java.io.PrintWriter;
import java.util.ArrayList;

import static utils.MapDescriptor.generateMapDescriptor;

/**
 * Exploration algorithm for the robot.
 *
 * @author Priyanshu Singh
 * @author Suyash Lakhotia
 */

public class ExplorationAlgo {
    private final Map exploredMap;
    private final Map realMap;
    private final Robot bot;
    private final int coverageLimit;
    private final int timeLimit;
    private int exploredArea;
    private long startTime;
    private long endTime;
    private int lastFrontCalibrate;
    private boolean finish;
    private static boolean calibrationMode=false;
    //private boolean robotFrontCali= false;
    private static boolean robotFrontWall= false;
    private static boolean robotRightWall=false;
    private static boolean robotLeftWall = false;
   // private boolean robotLeftCali= false;
   // private boolean robotRightCali= false;
    private int calibrateCounter=0;
   
   // private String angleSide;

    public boolean shouldCheckLeftSensor = false;

    public ExplorationAlgo(Map exploredMap, Map realMap, Robot bot, int coverageLimit, int timeLimit) {
        this.exploredMap = exploredMap;
        this.realMap = realMap;
        this.bot = bot;
        this.coverageLimit = coverageLimit;
        this.timeLimit = timeLimit;

    }

    /**
     * Method called to start the exploration.
     */
    public void runExploration() {
        if (bot.getRealBot()) {
            System.out.println("Starting calibration...");

        }

        System.out.println("Starting exploration...");
        System.out.println(bot.getRobotPosRow()+ "col"+ bot.getRobotPosCol());

        startTime = System.currentTimeMillis();
        endTime = startTime + (timeLimit * 1000);

        
//        senseAndRepaint(); //take note of what the robot has seen

        exploredArea = calculateAreaExplored();
        System.out.println("Explored Area: " + exploredArea);

        explorationLoop(bot.getRobotPosRow(), bot.getRobotPosCol());
    }

    /**
     * Loops through robot movements until one (or more) of the following conditions is met:
     * 1. Robot is back at (r, c)
     * 2. areaExplored > coverageLimit
     * 3. System.currentTimeMillis() > endTime
     */
    private void explorationLoop(int r, int c) {
        do {
        	
        	nextMove();
        	//calibrate90();
        	//System.out.println("calibrating");
            
            exploredArea = calculateAreaExplored();
            double areaExplored = (double) exploredArea;
            
            System.out.println(String.format("Current Position : Row %d Col %d", bot.getRobotPosRow()+1, bot.getRobotPosCol() +1));
          //  System.out.println("Out of 300 cells, " + areaExplored +" cells have been explored" );
            System.out.println("Arena explored: "+String.format("%.1f", areaExplored/300 *100) +" % Completed");
            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");

            
            if (bot.getRobotPosRow() == r && bot.getRobotPosCol() == c) {
                if (areaExplored >= 100) {
                    break;
                }
            }
         
       
            
        } while (exploredArea <= coverageLimit && System.currentTimeMillis() <= endTime);

        //exit do while after hitting certain coverageLimit and endTime
        
        if(bot.getRobotPosRow() == RobotConstants.START_ROW &&bot.getRobotPosCol() == RobotConstants.START_COL){
        	///checking for any remaining unexplored cells
            try {
                do {
            	
    			 if(goToUnexplored()!=true)
    				break;
    		       
                } while (exploredArea <= coverageLimit && System.currentTimeMillis() <= endTime); // need to check if there is still explorable cells as there are areas that cant be check
                
                System.out.println("Completed!!");
                finish = true;
                goHome();
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }
        
        finish = true;
        goHome();
    }

    /**
     * Determines the next move for the robot and move accordingly.
     * Right Wall Hugging algorithm
     */
    private void nextMove() {
        // Need to counter phantom wall loop movement
        if (checkForPhantomWall()) {
            System.out.println("--Phantom loop wall found--");
            moveOutOfPhantomWall();
        } else if (checkRight()) {
            System.out.println("Moving to the right");
            moveBot(MOVEMENT.RIGHT);
            if (checkForward()) moveBot(MOVEMENT.FORWARD);
        } 
        else if (checkForward()) {
            System.out.println("Moving forward");
            moveBot(MOVEMENT.FORWARD);
            System.out.println("confirm movement");
        } 
        else if (checkLeft()) {
            System.out.println("Moving to the left");
            moveBot(MOVEMENT.LEFT);
            if (checkForward()) moveBot(MOVEMENT.FORWARD);
        } 
        else {
            System.out.println("--Checking Area--");
            moveBot(MOVEMENT.RIGHT);
            moveBot(MOVEMENT.RIGHT);
        }
    }


    /**
     * Returns true if the robot is in phantom wall loop situation
     */
    private boolean checkForPhantomWall() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
//        Test Phantom Wall Code
//        if (botRow == 4 && botCol == 8) {
//            exploredMap.getCell(2, 10).setIsObstacle(false);
//            Cell cell = exploredMap.getCell(2, 10);
//            System.out.println(String.format("%b %b %b", cell.getIsObstacle(), cell.getIsExplored(), cell.getIsVirtualWall()));
//            System.out.println(isExploredNotObstacle(botRow - 2, botCol + 2));
//        }
        //
        // +2,-2        +2,-1           +2,0            +2,+1
        // +1,-2        +2,-1           +2,0            +2,+1
        // +0,-2        +2,-1           +2,0            +2,+1
        // -1,-2        +2,-1           +2,0            +2,+1
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return eastFree() && southFree() && isExploredAndNotObstacle(botRow - 2, botCol + 2);
            case EAST:
                return southFree() && westFree() && isExploredAndNotObstacle(botRow - 2, botCol - 2);
            case SOUTH:
                return westFree() && northFree() && isExploredAndNotObstacle(botRow + 2, botCol - 2);
            case WEST:
                return northFree() && eastFree() && isExploredAndNotObstacle(botRow + 2, botCol + 2);
        }
        return false;
    }

    /**
     * Returns true if the right side of the robot is free to move.
     */
    private boolean checkRight() {
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return eastFree();
            case EAST:
                return southFree();
            case SOUTH:
                return westFree();
            case WEST:
                return northFree();
        }
        return false;
    }

    /**
     * Returns true if the robot is free to move forward.
     */
    private boolean checkForward() {
    //	System.out.println("Bot Current Direction: "+ bot.getRobotCurDir());
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return northFree();
            case EAST:
                return eastFree();
            case SOUTH:
                return southFree();
            case WEST:
                return westFree();
        }
        return false;
    }

    /**
     * * Returns true if the left side of the robot is free to move.
     */
    private boolean checkLeft() {
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return westFree();
            case EAST:
                return northFree();
            case SOUTH:
                return eastFree();
            case WEST:
                return southFree();
        }
        return false;
    }

    /**
     * Returns true if the robot can move to the north cell.
     */
    private boolean northFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredAndNotObstacle(botRow + 1, botCol - 1) && isExploredAndFree(botRow + 1, botCol) && isExploredAndNotObstacle(botRow + 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the east cell.
     */
    private boolean eastFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredAndNotObstacle(botRow - 1, botCol + 1) && isExploredAndFree(botRow, botCol + 1) && isExploredAndNotObstacle(botRow + 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the south cell.
     */
    private boolean southFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredAndNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow - 1, botCol) && isExploredAndNotObstacle(botRow - 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the west cell.
     */
    private boolean westFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredAndNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow, botCol - 1) && isExploredAndNotObstacle(botRow + 1, botCol - 1));
    }

    /**
     * Returns the robot to START after exploration and robot points the east upon reaching
     */
    //only runs goHome() is 
    private void goHome() {
        if (!bot.getTouchedGoal() && coverageLimit == 300 && timeLimit == 3600) {
            FastestPathAlgo goToGoal = new FastestPathAlgo(exploredMap, bot, realMap);
            goToGoal.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL);
        }
        
        System.out.println("Going home now! ");
        exploredMap.repaint();
        if(bot.getRobotPosRow()!=RobotConstants.START_ROW && bot.getRobotPosCol()!=RobotConstants.START_COL) {
            YcFastestPath returnToStart = new YcFastestPath(exploredMap, bot, realMap);
            //FastestPathAlgo returnToStart = new FastestPathAlgo(exploredMap, bot, realMap);
           // returnToStart.runFastestPath(RobotConstants.START_ROW, RobotConstants.START_COL);
            returnToStart.runfastestPath(bot.getRobotPosRow(),bot.getRobotPosCol(),RobotConstants.START_ROW,RobotConstants.START_COL,RobotConstants.START_ROW,RobotConstants.START_COL);

            }
        YcFastestPath returnToStart = new YcFastestPath(exploredMap, bot, realMap);
        //FastestPathAlgo returnToStart = new FastestPathAlgo(exploredMap, bot, realMap);
       // returnToStart.runFastestPath(RobotConstants.START_ROW, RobotConstants.START_COL);
        returnToStart.runfastestPath(bot.getRobotPosRow(),bot.getRobotPosCol(),RobotConstants.START_ROW,RobotConstants.START_COL,RobotConstants.START_ROW,RobotConstants.START_COL);

        System.out.println("Exploration complete!");
        exploredArea = calculateAreaExplored();
        System.out.printf("Completed: %.2f%%", (exploredArea / 300.0) * 100.0);
        System.out.println("");
        System.out.println("Out of 300 cells, "+exploredArea + " cells has been explored");
        System.out.println((System.currentTimeMillis() - startTime) / 1000 + " Seconds");

        try {
            System.out.println("----------------");
            System.out.println("[MAP_DESCRIPTOR]");
            String[] map = generateMapDescriptor(exploredMap);
            System.out.println("----------------");
            PrintWriter out = new PrintWriter("exploration.txt");
            out.print(map[0] + " " + map[1]);
          // CommMgr.getCommMgr().sendMsg("AN",map[0], "PART_1"); //added comment to remove null expection
            //CommMgr.getCommMgr().sendMsg("AN",map[1], "PART_2");
            CommMgr.getCommMgr().sendMsg("AN", "P1",map[0] + ","+map[1]); 
            out.close();
        } catch (Exception e) {
            System.out.println("CANNOT SAVE TO FILE");
        }


        bot.realBot = true;
        turnBotDirection(DIRECTION.EAST);
        bot.realBot = false;
    }

    /**
     * Returns true for cells that are explored and not obstacles.
     */
    private boolean isExploredAndNotObstacle(int r, int c) {
        if (exploredMap.checkValidCoordinates(r, c)) { 
            Cell tmp = exploredMap.getCell(r, c);
            return (tmp.getIsExplored() && !tmp.getIsObstacle());
        }
        return false;
    }
    
    private boolean isExploredAndObstacle(int r, int c) {
        if (exploredMap.checkValidCoordinates(r, c)) { 
            Cell tmp = exploredMap.getCell(r, c);
            return (tmp.getIsExplored() && tmp.getIsObstacle());
        }
        return false;
    }
    
    
    //checks if the robot's right is explored and is virtual wall(must be in 3 consecutive grids and valid coor)
    private boolean isExploredAndWall(int r, int c) {
        if (exploredMap.checkValidCoordinates(r, c)) { 
            Cell tmp = exploredMap.getCell(r, c);
            return (tmp.getIsExplored() && tmp.getIsVirtualWall());
        }
        return false;
    }
    


    /**
     * Returns true for cells that are explored, not virtual walls and not obstacles.
     */
    private boolean isExploredAndFree(int r, int c) {
        if (exploredMap.checkValidCoordinates(r, c)) {
            Cell b = exploredMap.getCell(r, c);
            return (b.getIsExplored() && !b.getIsVirtualWall() && !b.getIsObstacle());
        }
        return false;
    }

    /**
     * Returns the number of cells explored in the grid.
     */
    private int calculateAreaExplored() {
        int result = 0;
        for (int r = 0; r < MapConstants.MAP_ROWS; r++) {
            for (int c = 0; c < MapConstants.MAP_COLS; c++) {
                if (exploredMap.getCell(r, c).getIsExplored()) {
                    result++;
                }
            }
        }
        return result;
    }

    private void moveOutOfPhantomWall() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        /*
            Illustration
            ________________
            |       |       |
            |       |       |
            |   1   |   2   |
            |  <--  |  -->  |
            |       |       |
            |_______|_______|

   			If robot is in part 1, it will try to go the right wall
            If robot is in part 2, it will try to go the left wall
         
        */
        int counter = 0;
        if (botCol <= MapConstants.MAP_COLS / 2) {
            // Go to left wall
            turnBotDirection(DIRECTION.WEST);
            for (int i = botCol; i >= 0; i -= 1) {
                System.out.println(String.format("Current Robot position Row: %d Col: %d i %d Counter: %d", botRow, botCol, i, counter));
                if (!checkColObstacleInRow(botRow, i)) break;
                counter++;
            }
            // Minus 2 to allow the robot just 1 block away from the obstacle
            counter -= 2;
            if (counter <= 0) {
                turnBotDirection(DIRECTION.SOUTH);
            }
        } else {
            // Go to right wall
            turnBotDirection(DIRECTION.EAST);
            for (int i = botCol; i < MapConstants.MAP_COLS; i += 1) {
            	 System.out.println(String.format("Current Robot position Row: %d Col: %d i %d Counter: %d", botRow, botCol, i, counter));
                if (!checkColObstacleInRow(botRow, i)) break;
                counter++;
            }
            // Minus 2 to allow the robot just 1 block away from the obstacle
            counter -= 2;
            if (counter <= 0) {
                turnBotDirection(DIRECTION.NORTH);
            }
        }
        System.out.println(String.format("Counter %d", counter));
        for (int i = 0; i < counter; i++) {
            moveBot(MOVEMENT.FORWARD);
        }
    }

    public boolean checkColObstacleInRow(int botRow, int column) {
        System.out.println(String.format("Row %d Col %d isExploredAndNotObstacle %B %B %B", botRow, column, isExploredAndNotObstacle(botRow - 1, column), isExploredAndNotObstacle(botRow, column), isExploredAndNotObstacle(botRow + 1, column)));
        System.out.println(String.format("Row %d Col %d isObstacle %B %B %B", botRow, column, exploredMap.getCell(botRow - 1, column).getIsObstacle(), exploredMap.getCell(botRow, column).getIsObstacle(), exploredMap.getCell(botRow + 1, column).getIsObstacle()));
        return isExploredAndNotObstacle(botRow - 1, column) && isExploredAndNotObstacle(botRow, column) && isExploredAndNotObstacle(botRow + 1, column);
    }

    /**
     * The bot moves, repaints the map and calls senseAndRepaint().
     */
    private void moveBot(MOVEMENT m) {
        
        System.out.println("moveBot function");

        
        /*if(calibrationMode == false) {
        	senseAndRepaint();
        }*/
        senseAndRepaint();
        System.out.println("After sense and repaint");
    	System.out.println("Startin Cali");
    	//calibration();
    	//calibrationMode=false;
    	
      
       // calibration();
    	//System.out.println("calibration done");
    	
        bot.move(m);
        //calibrateCounter++;
        //System.out.println("calicounter: " + calibrateCounter);
      
    }

    /**
     * Sets the bot's sensors, processes the sensor data and repaints the map.
     */
    private void senseAndRepaint() {
        bot.setSensors();
        //request data
        if (bot.getRealBot()) {
            CommMgr.getCommMgr().sendMsg("AR","", CommMgr.SENSOR_DATA); // pass p
        }
        
        bot.sense(exploredMap, realMap, bool -> {
            this.shouldCheckLeftSensor = bool;
            System.out.println("[WARNING] Should check the left sensor");
        });
        //calibrate90();
        
        exploredMap.repaint();
    }

    /**
     * Checks if the robot can calibrate at its current position given a direction.
     */
    /*private boolean canCalibrateOnTheSpot(DIRECTION botDir) {
        int row = bot.getRobotPosRow();
        int col = bot.getRobotPosCol();

        switch (botDir) {
            case NORTH:
                return exploredMap.getIsObstacleOrWall(row + 2, col - 1) && exploredMap.getIsObstacleOrWall(row + 2, col) && exploredMap.getIsObstacleOrWall(row + 2, col + 1);
            case EAST:
                return exploredMap.getIsObstacleOrWall(row + 1, col + 2) && exploredMap.getIsObstacleOrWall(row, col + 2) && exploredMap.getIsObstacleOrWall(row - 1, col + 2);
            case SOUTH:
                return exploredMap.getIsObstacleOrWall(row - 2, col - 1) && exploredMap.getIsObstacleOrWall(row - 2, col) && exploredMap.getIsObstacleOrWall(row - 2, col + 1);
            case WEST:
                return exploredMap.getIsObstacleOrWall(row + 1, col - 2) && exploredMap.getIsObstacleOrWall(row, col - 2) && exploredMap.getIsObstacleOrWall(row - 1, col - 2);
        }

        return false;
    }*/

    /**
     * Returns a possible direction for robot calibration or null, otherwise.
     */
   /* private DIRECTION getCalibrationDirection(DIRECTION dirToCali) {
        DIRECTION origDir = bot.getRobotCurDir();
        DIRECTION dirToCheck= dirToCali;

        dirToCheck = DIRECTION.getNext(origDir);                    // right turn
        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;

        dirToCheck = DIRECTION.getPrevious(origDir);                // left turn
        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;

        dirToCheck = DIRECTION.getPrevious(dirToCheck);             // u turn
        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;

        return null;
    }*/
    
   

    /**
     * Robot turns in the needed direction and sends the CALIBRATE movement. Once calibrated, the bot is turned back
     * to its original direction.
     */
    private void calibrateBot(DIRECTION targetDir) {
        DIRECTION origDir = bot.getRobotCurDir();
        System.out.println("original dir"+ bot.getRobotCurDir());

        turnBotDirection(targetDir);
        System.out.println("new dir"+ bot.getRobotCurDir());
        if (robotFrontWall==true && robotRightWall==true) {
        	moveBot(MOVEMENT.CALIBRATE);
        	turnBotDirection(origDir);
        	moveBot(MOVEMENT.CALIBRATE);
        	return;
        	
        }
        if(robotRightWall==true) {
        	moveBot(MOVEMENT.CALIBRATE);
        	turnBotDirection(origDir);
        	return;
        }
        if( robotFrontWall==true) {
        	
        	moveBot(MOVEMENT.CALIBRATE);
        	
        	turnBotDirection(origDir);
        	return;
        }
        
        
    }

    /**
     * The robot turns to the required direction.
     */
    private void turnBotDirection(DIRECTION targetDir) {
        System.out.println(bot.getRobotCurDir().toString() + "-----> to ---->" + targetDir.toString());

        int numOfTurn = Math.abs(bot.getRobotCurDir().ordinal() - targetDir.ordinal());
        System.out.println("nuOfTurn: "+ numOfTurn);
        if (numOfTurn > 2) numOfTurn = numOfTurn % 2;

        if (numOfTurn == 1) {
            if (DIRECTION.getNext(bot.getRobotCurDir()) == targetDir) {
                moveBot(MOVEMENT.RIGHT);
            } else {
            	System.out.println("going back to original dir");
                moveBot(MOVEMENT.LEFT);
            }
        } else if (numOfTurn == 2) {
            moveBot(MOVEMENT.RIGHT);
            moveBot(MOVEMENT.RIGHT);
        }
        calibrateCounter -= numOfTurn;
    }
    
    
    public boolean goToUnexplored() throws InterruptedException {
    	System.out.println("is this mtd called?");

        // Pause for half a second
//        if(sim) {
//            TimeUnit.MILLISECONDS.sleep(500);
//        }
    	exploredArea = calculateAreaExplored();

    	if(exploredArea < coverageLimit) { //check if it is 100% completed, if it is return back to starting point
    	Cell currentCell = new Cell(bot.getRobotPosRow(),bot.getRobotPosCol());
    	
    	System.out.println("Current Location Row : "+bot.getRobotPosRow()+ " Col: "+bot.getRobotPosCol());
        Cell nearestUnexp = exploredMap.nearestUnexplored(currentCell);
        if (nearestUnexp == null) {  //checked from nearestExp to nearestUnexp
            System.out.println("No nearest unexplored found.");
            return false;
        }
        Cell nearestExp = exploredMap.nearestExplored(nearestUnexp);
        
     	System.out.println("Nearest Unexplored Row : "+nearestUnexp.getRow()+ " Col: "+nearestUnexp.getCol());

     	System.out.println("Nearest Explored Row : "+nearestExp.getRow()+ " Col: "+nearestExp.getCol());
       
        	 System.out.println("Go to nearest explored, Row : "+nearestExp.getRow() + " Col: "+nearestExp.getCol());
//        	 FastestPathAlgo goToPoint = new FastestPathAlgo(exploredMap, bot, realMap);
//             goToPoint.runFastestPath(nearestExp.getRow(),nearestExp.getCol());
        	 YcFastestPath goToPoint = new YcFastestPath(exploredMap, bot, realMap);
        	 // TODO
             goToPoint.runfastestPath(bot.getRobotPosRow(),bot.getRobotPosCol(),nearestExp.getRow(),nearestExp.getCol(),nearestExp.getRow(),nearestExp.getCol());
            return true;
        
    }
		return false;
    }
    
    //returns true if robot front has 3 grids of wall or obstacle
    private void detectFrontWall(DIRECTION robotDir) {
      	int curRow= bot.getRobotPosRow();
    	int curCol= bot.getRobotPosCol();
    	DIRECTION curDir=robotDir;
    	 
		robotFrontWall=false;
		
    	if(curDir== DIRECTION.WEST) {
			if(isExploredAndWall(curRow-1, curCol-1)&&
				isExploredAndWall(curRow, curCol-1) &&
				isExploredAndWall(curRow+1,curCol-1) &&
				isExploredAndWall(curRow-2, curCol-1) &&
				isExploredAndWall(curRow+2, curCol-1)
			)	{
				
				robotFrontWall=true;
			
			
			}
    	}
    	else if(curDir== DIRECTION.SOUTH) {
				if(isExploredAndWall(curRow-1, curCol-1)&&
					isExploredAndWall(curRow-1, curCol) &&
					isExploredAndWall(curRow-1,curCol+1) &&
					isExploredAndWall(curRow-1, curCol-2) &&
					isExploredAndWall(curRow-1, curCol+2)
				)	{
					robotFrontWall=true;
				}
			}
			
    	else if(curDir== DIRECTION.EAST) {
	    		//System.out.println("robotposition for cali"+ (bot.getRobotPosRow()+1) + "botCol "+ (bot.getRobotPosCol()+1));
				if(isExploredAndWall(curRow+1, curCol+1)&&
					isExploredAndWall(curRow, curCol+1) &&
					isExploredAndWall(curRow-1,curCol+1) &&
					isExploredAndWall(curRow-2, curCol+1) &&
					isExploredAndWall(curRow+2, curCol+1)
				)	{
					System.out.println("no obstacle in front of robot");
					robotFrontWall=true;
					
				}
			}
			
		else if(curDir== DIRECTION.NORTH) {
			if(isExploredAndWall(curRow+1, curCol-1)&&
				isExploredAndWall(curRow+1, curCol) &&
				isExploredAndWall(curRow+1,curCol+1) &&
				isExploredAndWall(curRow+1, curCol-2) &&
				isExploredAndWall(curRow+1, curCol+2)
			)	{
				robotFrontWall=true;
			}
		}
    		
    }
    
   
    // checks if robot right is a wall or obstacle (must be 3 grids)
    private void rightWall(DIRECTION robotDir) {
    	int curRow= bot.getRobotPosRow();
    	int curCol= bot.getRobotPosCol();
    	DIRECTION curDir= robotDir;
    	
    	robotRightWall=false;
    	
    	
    	if(curDir== DIRECTION.WEST) {
    		if(isExploredAndWall(curRow+2, curCol)&&
					isExploredAndWall(curRow+2, curCol-1) &&
					isExploredAndWall(curRow+2,curCol+1) && 
					isExploredAndWall(curRow+2, curCol-2) &&
					isExploredAndWall(curRow+2, curCol+2)
			){
    			robotRightWall=true;
			
			}
    	}
    	if(curDir== DIRECTION.NORTH) {
    		if(isExploredAndWall(curRow-1, curCol+2)&&
					isExploredAndWall(curRow+1, curCol+2) &&
					isExploredAndWall(curRow,curCol+2) &&
					isExploredAndWall(curRow-2, curCol+2) &&
					isExploredAndWall(curRow+2, curCol+2)
    		){
    			robotRightWall=true;
			
			}
    	}
    	if(curDir== DIRECTION.SOUTH) {
    		if(isExploredAndWall(curRow-1, curCol-1)&&
					isExploredAndWall(curRow+1, curCol-1) &&
					isExploredAndWall(curRow,curCol-1) && 
					isExploredAndWall(curRow-2, curCol-1) &&
					isExploredAndWall(curRow+2, curCol-1)
    				){
    			robotRightWall=true;
			
			}
    	}
    	if(curDir== DIRECTION.EAST) {
    		if(isExploredAndWall(curRow-1, curCol)&&
					isExploredAndWall(curRow-1, curCol-1) &&
					isExploredAndWall(curRow-1,curCol+1) &&
    				isExploredAndWall(curRow-1, curCol-2) &&
    				isExploredAndWall(curRow-1, curCol+2)
    		){
    			robotRightWall=true;
			
			}
    	}
    	
    	
    	
    }
    
    private void leftWall(DIRECTION robotDir) {
    	int curRow= bot.getRobotPosRow();
    	int curCol= bot.getRobotPosCol();
    	DIRECTION curDir= robotDir;
    	robotLeftWall=false;
    	if(curDir== DIRECTION.WEST) {
    		if(isExploredAndWall(curRow-1, curCol)&&
					isExploredAndWall(curRow-1, curCol-1) &&
					isExploredAndWall(curRow-1,curCol+1) &&
    				isExploredAndWall(curRow-1, curCol-2) &&
    				isExploredAndWall(curRow-1, curCol+2)
    		){
    			robotLeftWall=true;
			
			}
    	}
    	if(curDir== DIRECTION.NORTH) {
    		if(isExploredAndWall(curRow-1, curCol-1)&&
					isExploredAndWall(curRow+1, curCol-1) &&
					isExploredAndWall(curRow,curCol-1) &&
    				isExploredAndWall(curRow-2, curCol-1) &&
    				isExploredAndWall(curRow+2, curCol-1)
    		){
    			robotLeftWall=true;
			
			}
    	}
    	if(curDir== DIRECTION.SOUTH) {
    		if(isExploredAndWall(curRow-1, curCol+1)&&
					isExploredAndWall(curRow+1, curCol+1) &&
					isExploredAndWall(curRow,curCol+1) && 
    				isExploredAndWall(curRow-2, curCol+1) &&
    				isExploredAndWall(curRow+2, curCol+1)
    		){
    			robotLeftWall=true;
			
			}
    	}
    	if(curDir== DIRECTION.EAST) {
    		if(isExploredAndWall(curRow+1, curCol)&&
					isExploredAndWall(curRow+1, curCol-1) &&
					isExploredAndWall(curRow+1,curCol+1) &&
    				isExploredAndWall(curRow+1, curCol-2) &&
    				isExploredAndWall(curRow+1, curCol+2)
    		){
    			robotLeftWall=true;
			
			}
    	}
    	
    }
    	
    private void calibration() {
    	
    	if(calibrateCounter < 8) {
    		
    		return;
    	}
    	
    	System.out.println("inside cali loop");
    	System.out.println("bot direction before executing cali: "+bot.getRobotCurDir());
    	detectFrontWall(bot.getRobotCurDir());
    	System.out.println("boolean for front wall is: " +robotFrontWall);
    	rightWall(bot.getRobotCurDir());
    	System.out.println("boolean for right wall is: " +robotRightWall);
    	leftWall(bot.getRobotCurDir());
    	System.out.println("boolean for left wall is: " +robotLeftWall);
    	
    	if (robotFrontWall== true) {
    		if(robotRightWall==true) {
    			System.out.println("got front wall and right wall");
    			calibrationMode=true;
        		rightCalibrate();
        		frontCalibrate();
        		
        		robotFrontWall= false;
        		robotRightWall=false;
        		calibrateCounter = 0;
        		
        		return;
    			
    		}
    		else if (robotLeftWall==true) {
    			System.out.println("got front wall and left wall");
    			calibrationMode=true;
        		leftCalibrate();
        		frontCalibrate();
        		robotLeftWall=false;
        		robotFrontWall=false;
        		//calibrationMode=false;
        		calibrateCounter = 0;
        		return;
        		
        		
    		}
    		else {
    			System.out.println("only got frontwall");
    			calibrationMode=true;
        		frontCalibrate();
        		robotFrontWall=false;
        		//calibrationMode=false;
        		calibrateCounter = 0;
        		return;
    		}
    	}
    	
    	
    	else if (robotRightWall== true) {
    		System.out.println("only got rightwall");
    		calibrationMode=true;
    		rightCalibrate();
    		robotRightWall= false;
    		//calibrationMode=false;
    		calibrateCounter = 0;
    		
    		return;
    	}
    	else if (robotLeftWall == true) {
    		calibrationMode=true;
    		leftCalibrate();
    		robotLeftWall = false;
    		//calibrationMode=false;
    		calibrateCounter = 0;
    		return;
    	}
    	else {
    		robotRightWall= false;
    		robotFrontWall=false;
    		calibrationMode=false;
    		
    		return;
    	}
    	
    }
			
    private void leftCalibrate() {
    	DIRECTION nextDir= DIRECTION.getPrevious(bot.getRobotCurDir());
    	calibrateBot(nextDir);
    	//CommMgr.getCommMgr().sendMsg("AR","cali", "L"); 
    	
    }
	private void frontCalibrate() {
		CommMgr.getCommMgr().sendMsg("AR","cali", "C"); 
		System.out.println("cali robot based on front wall");
	}
    private void rightCalibrate() {
    	DIRECTION nextDir= DIRECTION.getNext(bot.getRobotCurDir());
    	System.out.println("cali robot based on right wall");
    	//CommMgr.getCommMgr().sendMsg("AR","cali", "R");
    	System.out.println("next dir "+ nextDir);
    	calibrateBot(nextDir);
		//CommMgr.getCommMgr().sendMsg("AR","cali", "R"); //send to arduino that we need cali on right side 
    }

   
    // Jesslyn's Calibration Code 
    private static boolean frontWallCalibrate = false;
    private static boolean leftWallCalibrate = false;
    private static boolean rightWallCalibrate = false;
    
    private void checkCalibrationType(DIRECTION robotDir) {
    	int curRow= bot.getRobotPosRow();
    	int curCol= bot.getRobotPosCol();
    	
    	
    }
    private void leftWall(DIRECTION robotDir) {
    	int curRow= bot.getRobotPosRow();
    	int curCol= bot.getRobotPosCol();
    	DIRECTION curDir= robotDir;
    	robotLeftWall=false;
    	if(curDir== DIRECTION.WEST) {
    		if(isExploredAndWall(curRow-1, curCol)&&
					isExploredAndWall(curRow-1, curCol-1) &&
					isExploredAndWall(curRow-1,curCol+1) &&
    				isExploredAndWall(curRow-1, curCol-2) &&
    				isExploredAndWall(curRow-1, curCol+2)
    		){
    			robotLeftWall=true;
			
			}
    	}
    	if(curDir== DIRECTION.NORTH) {
    		if(isExploredAndWall(curRow-1, curCol-1)&&
					isExploredAndWall(curRow+1, curCol-1) &&
					isExploredAndWall(curRow,curCol-1) &&
    				isExploredAndWall(curRow-2, curCol-1) &&
    				isExploredAndWall(curRow+2, curCol-1)
    		){
    			robotLeftWall=true;
			
			}
    	}
    	if(curDir== DIRECTION.SOUTH) {
    		if(isExploredAndWall(curRow-1, curCol+1)&&
					isExploredAndWall(curRow+1, curCol+1) &&
					isExploredAndWall(curRow,curCol+1) && 
    				isExploredAndWall(curRow-2, curCol+1) &&
    				isExploredAndWall(curRow+2, curCol+1)
    		){
    			robotLeftWall=true;
			
			}
    	}
    	if(curDir== DIRECTION.EAST) {
    		if(isExploredAndWall(curRow+1, curCol)&&
					isExploredAndWall(curRow+1, curCol-1) &&
					isExploredAndWall(curRow+1,curCol+1) &&
    				isExploredAndWall(curRow+1, curCol-2) &&
    				isExploredAndWall(curRow+1, curCol+2)
    		){
    			robotLeftWall=true;
			
			}
    	}
    	
    }
    
    
   
    
}
