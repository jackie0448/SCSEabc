package ycfastestpath;

import map.Cell;
import map.Map;
import map.MapConstants;
import robot.Robot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;



import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;

public class YcFastestPath {
	private Map exploredMap;
    private Map realMap;
    private Robot robot;
    private double[][] gCosts;  
    private int curDir=3;
    private Stack<Cell> fastPath;
    private boolean explorationMode;
    
    //private Cell curCell = exploredMap.getCell(robot.getRobotPosRow(), robot.getRobotPosCol());
    private HashMap<Cell, Cell> parents= new HashMap<>();
    
    private ArrayList<Cell>visited= new ArrayList<Cell>();
    private ArrayList<Cell> toVisitQueue= new ArrayList<Cell>();
    

    public YcFastestPath(Map exploredMap, Robot robot) {
        this.realMap = null;
        this.exploredMap= exploredMap;
        this.robot= robot;
       // this.explorationMode=true;
        this.gCosts = new double[MapConstants.MAP_ROWS][MapConstants.MAP_COLS];
        
    }
    
    public YcFastestPath(Map exploredMap,Robot robot, Map realMap) {
    	  this.realMap = realMap;
          this.explorationMode = true;
          this.exploredMap= exploredMap;
          this.robot = robot;
          this.gCosts = new double[MapConstants.MAP_ROWS][MapConstants.MAP_COLS];
    }
    
    public String runfastestPath(int startRow, int startCol, int wayPointRow,int wayPointCol,int goalRow,int goalCol) {  
    	convertEnumtoIntDir(robot);
    	System.out.println("current direction is : "+curDir);
    	ASTARSearch(startRow,startCol,wayPointRow,wayPointCol,curDir);
    	int indexToRemove= visited.size()-1;
    	visited.remove(indexToRemove);
    	toVisitQueue.clear();
    	convertEnumtoIntDir(robot);
        ASTARSearch(wayPointRow,wayPointCol,goalRow,goalCol,curDir );
    	
    	fastPath= getPath(goalRow, goalCol);
    	
		printFastPath(fastPath);
		return MoveRobot(fastPath, goalRow, goalCol);	
		
    }
    
    
    public void ASTARSearch(int startRow, int startCol, int goalRow, int goalCol, int curDir) {	
    	if(startRow!=goalRow || startCol!=goalCol) {
    		gCosts[startRow][startCol] = 0;
    		initGCost();
//        	System.out.println("calculating fastest path::");
        	Cell curCell = exploredMap.getCell(startRow,startCol);
        	//System.out.println(curCell.getRow() +"is"+ curCell.getCol());
        	toVisitQueue.add(curCell);
        	
        	while(!toVisitQueue.isEmpty()) {
        		curCell= minCost(goalRow,goalCol);
        		//System.out.println("hi");
        		//System.out.println(curCell.getRow() + curCell.getCol());
        		if (parents.containsKey(curCell)) {//curCell, neighbour
                    curDir = getNextDir(parents.get(curCell),curCell);//ppossible error
                    
                }
        		visited.add(curCell); //add current cell to visited
        		toVisitQueue.remove(curCell); //remove current cell from toVisitQueue
        		if(visited.contains(exploredMap.getCell(goalRow, goalCol))) {
        			System.out.println("Goal cell has been reached. Fastest path found");
        			
        			return;
        			///might need to change here for waypoint
        		}
        		else {
        			 ArrayList<Cell> neighbours; //maybe error
        			 neighbours= getNeighbours(curCell);
        			 for(Cell c : neighbours) {
        				
        				 if(visited.contains(c)) {
        					 continue;
        				 }
        				 else if(!(toVisitQueue.contains(c))) {
        					 parents.put(c,curCell);
        					 gCosts[c.getRow()][c.getCol()]=gCosts[curCell.getRow()][curCell.getCol()]+getGCost(curCell,c,curDir);
        					 toVisitQueue.add(c);
        				 }
        				 else { //if neighbour is in Queue
        					 double curNeighbourGCost= gCosts[c.getRow()][c.getCol()];
        					 double gCostToCompare= gCosts[curCell.getRow()][curCell.getCol()]+ getGCost(curCell,c,curDir);
        					 if(gCostToCompare<curNeighbourGCost) {
        						 gCosts[c.getRow()][c.getCol()]= gCostToCompare;
        						 parents.put(c, curCell);
        					 }
        				 }
        			 }
        		}
        		
        	} //while loop ends here
        	
    	}
    	else {
    		System.out.println("path not found");
    	}
    	
    }
    private void initGCost() {
    	this.gCosts = new double[MapConstants.MAP_ROWS][MapConstants.MAP_COLS];
        for (int i = 0; i < MapConstants.MAP_ROWS; i++) {
            for (int j = 0; j < MapConstants.MAP_COLS; j++) {
                Cell cell = exploredMap.getCell(i, j);
                if (!canVisit(cell)) {
                    gCosts[i][j] = RobotConstants.INFINITE_COST;
                } else {
                    gCosts[i][j] = 0;
                }
            }
        }
    }
    private boolean canVisit(Cell cell) {
        return cell.getIsExplored() && !cell.getIsObstacle() && !cell.getIsVirtualWall();
    }
    
    private Cell minCost(int goalRow, int goalCol) {
    	double minCost= RobotConstants.INFINITE_COST;
    	int size= toVisitQueue.size();
    	Cell minCostCell= null; 
    	for (int i=0;i<size;i++) {
    		int row= toVisitQueue.get(i).getRow();
    		int col= toVisitQueue.get(i).getCol();
    		double gCost= gCosts[row][col];
    		double hCost= heuristicCost(toVisitQueue.get(i),goalRow,goalCol);
    		double fCost= gCost+hCost;
    		if(fCost<minCost) {
    			minCost=fCost;
    			minCostCell=toVisitQueue.get(i);
    		}
    	}
    	return minCostCell;
    }
    /*
     //heuristic for mahhantan dist
    private double heuristicCost(Cell refCell, int goalRow, int goalCol) {
    	//mahhantan dist
    	double movementCost= ((Math.abs(goalRow-refCell.getRow())+ Math.abs(goalCol-refCell.getCol()))* RobotConstants.MOVE_COST);
    	double turnCost= 0;
    	if (goalRow - refCell.getRow() != 0|| goalCol - refCell.getCol() != 0 ) {
            turnCost = RobotConstants.TURN_COST;
        }
    	return movementCost + turnCost;
    }
    */
    
   //heuristic for diagonal distance
    private double heuristicCost(Cell refCell, int goalRow, int goalCol) {
    	//straight heuristic cost
    	double strMoveCost= ((Math.abs(goalRow-refCell.getRow())+ Math.abs(goalCol-refCell.getCol()))* RobotConstants.MOVE_COST);
    	double rowDiff= Math.abs((refCell.getRow()- goalRow));
    	double colDiff= Math.abs((refCell.getCol()-goalCol));
    	double diagonalMoveCost= Math.min(rowDiff, colDiff);
    	double finalHurCost= (diagonalMoveCost*RobotConstants.DIAGONAL_COST)+ (RobotConstants.MOVE_COST*(strMoveCost-(2*diagonalMoveCost)));
    	return finalHurCost;
    }
    
    
    private int getNextDir(Cell curCell, Cell neighbour) { //neighbour is next move
    	int curCellRow= curCell.getRow();
    	int curCellCol= curCell.getCol();
    	int neighbourRow= curCell.getRow();
    	int neighbourCol= curCell.getCol();
    	int nextDir;
    	/*
    	 * heuristic for 4 directions
    	if(neighbourRow-curCellRow==1) {
    		nextDir=2; // move up
    	}
    	if(neighbourRow-curCellRow==-1) {
    		nextDir= 0; //move down
    	}
    	if(neighbourCol-curCellCol==1) {
    		nextDir= 3; //move right
    	}
    	*/
    	
    	// make changes here
    	if(neighbourRow-curCellRow==1 && neighbourCol-curCellCol==1) {
    		nextDir=5; //move diagonally up right
    	}
    	if(neighbourRow-curCellRow==-1 && neighbourCol-curCellCol==1) {
    		nextDir=7;//move diagonally down right
    	}
    	if(neighbourRow-curCellRow==1 && neighbourCol-curCellCol==-1) {
    		nextDir= 3;//move diagonally up left
    	}
    	if(neighbourRow-curCellRow==-1 && neighbourCol-curCellCol==-1) {
    		nextDir=1;  //move diagonally down left
    	}
    	if(neighbourRow-curCellRow==1) {
    		nextDir=4; // move up
    	}
    	if(neighbourRow-curCellRow==-1) {
    		nextDir= 0; //move down
    	}
    	if(neighbourCol-curCellCol==1) {
    		nextDir= 6; //move right
    	}
    	else {
    		nextDir=2; //move left
    	}
    	return nextDir;
    }
    
    private DIRECTION getTargetDir(int robotRow, int robotCol, DIRECTION robotDir, Cell target) {
        /*
         * This is for 4 directions only
    	if (robotCol - target.getCol() > 0) {
            return DIRECTION.WEST;
        }
        else if (target.getCol() - robotCol > 0) {
            return DIRECTION.EAST;
        }
        else {
            if (robotRow - target.getRow() > 0) {
                return DIRECTION.SOUTH;
            } else if (target.getRow() - robotRow > 0)
            {
                return DIRECTION.NORTH;
            }
            else {
                return robotDir;
            }
        }
        */
    	if ((target.getRow() - robotRow > 0 && (target.getCol() - robotCol > 0))){
        	return DIRECTION.NORTHEAST;
        }
        else if ((target.getRow() - robotRow > 0) && (robotCol - target.getCol() > 0)) {
        	return DIRECTION.NORTHWEST;
        }
        else if ((robotRow - target.getRow() > 0 ) && (robotCol - target.getCol() > 0)){
        	return DIRECTION.SOUTHWEST;
        }
        else if ((robotRow - target.getRow() > 0 ) && (target.getCol() - robotCol > 0)) {
        	return DIRECTION.SOUTHEAST;
        }
        else if (robotCol - target.getCol() > 0) {
            return DIRECTION.WEST;
        }
        else if (target.getCol() - robotCol > 0) {
            return DIRECTION.EAST;
        }
        else if(robotRow - target.getRow() > 0 ) {
        	 return DIRECTION.SOUTH;
        }
        else if (target.getRow() - robotRow > 0) {
        	return DIRECTION.NORTH;
        }
        
        else {
        	return robotDir;
        }
    }
    private double getGCost(Cell curCell, Cell neighbour, int curDir) {
    	int nextDir= getNextDir(curCell, neighbour);
    	int turnCost;
    	int dirDiff= Math.abs(curDir-nextDir);
    	
    	if(dirDiff<=4) {
    		turnCost= dirDiff * RobotConstants.TURN_COST;
    	}
    	else {
    		int tempDirCost=0;
    		int smallestDir= Math.min(curDir, nextDir);
    		if (dirDiff ==5){
    			tempDirCost= dirDiff %2;
    		}
    		if ( dirDiff==6) {
    			tempDirCost= dirDiff %4;
    		}
    		if ( dirDiff==7) {
    			tempDirCost= dirDiff %6;
    		}
    		turnCost= (tempDirCost + smallestDir)* RobotConstants.TURN_COST;
    	}
    	return turnCost;
    	
    
    	/*
    	 * turn cost for only 4 directions
    	 if(Math.abs(nextDir-curDir)==2) {
    		turnCost= 2* RobotConstants.TURN_COST;
    	}
    	
    	else if(Math.abs(nextDir-curDir)==1 || Math.abs(nextDir-curDir)==3) {
    		turnCost=RobotConstants.TURN_COST;
    		
    	else {
    		turnCost=0;
    	}
    	}*/
    	
    }
    
    private ArrayList<Cell> getNeighbours(Cell curCell){
    	ArrayList<Cell> neighbours = new ArrayList<Cell>();
    	int curRow= curCell.getRow();
    	int curCol= curCell.getCol();
    	//Cell cell= new Cell(exploredMap.getCell(row, col);
    	if (exploredMap.checkValidCoordinates(curRow+1, curCol)) {
    		Cell neighbourCell1= exploredMap.getCell(curRow+1, curCol);
    		if(canVisit(neighbourCell1)) {
    			neighbours.add(neighbourCell1);
    		}
    	}
    	if (exploredMap.checkValidCoordinates(curRow-1, curCol)) {
    		Cell neighbourCell2= exploredMap.getCell(curRow-1, curCol);
    		if(canVisit(neighbourCell2)) {
    			neighbours.add(neighbourCell2);
    		}
    	}
    	if (exploredMap.checkValidCoordinates(curRow, curCol+1)) {
    		Cell neighbourCell3= exploredMap.getCell(curRow, curCol+1);
    		if(canVisit(neighbourCell3)) {
    			neighbours.add(neighbourCell3);
    		}
    	}
    	if (exploredMap.checkValidCoordinates(curRow, curCol-1)) {
    		Cell neighbourCell4= exploredMap.getCell(curRow+1, curCol-1);
    		if(canVisit(neighbourCell4)) {
    			neighbours.add(neighbourCell4);
    		}
    	}
    	//make changes here, this change adds neighbouring cells from various diagonal directions
    	if (exploredMap.checkValidCoordinates(curRow+1, curCol+1)) {
    		Cell neighbourCell4= exploredMap.getCell(curRow+1, curCol+1);
    		if(canVisit(neighbourCell4)) {
    			neighbours.add(neighbourCell4);
    		}
    	}
    	if (exploredMap.checkValidCoordinates(curRow-1, curCol+1)) {
    		Cell neighbourCell5= exploredMap.getCell(curRow-1, curCol+1);
    		if(canVisit(neighbourCell5)) {
    			neighbours.add(neighbourCell5);
    		}
    	}
    	if (exploredMap.checkValidCoordinates(curRow-1, curCol-1)) {
    		Cell neighbourCell6= exploredMap.getCell(curRow-1, curCol);
    		if(canVisit(neighbourCell6)) {
    			neighbours.add(neighbourCell6);
    		}
    	}
    	if (exploredMap.checkValidCoordinates(curRow+1, curCol-1)) {
    		Cell neighbourCell7= exploredMap.getCell(curRow-1, curCol);
    		if(canVisit(neighbourCell7)) {
    			neighbours.add(neighbourCell7);
    		}
    	}
    	return neighbours;
    }
    public void convertEnumtoIntDir(Robot robot) {
    	/*
    	 * this part of the code is for 4 directions only
    	if(robot.getRobotCurDir()== RobotConstants.DIRECTION.SOUTH) {
    		curDir=0;
    	}
    	else if(robot.getRobotCurDir()== RobotConstants.DIRECTION.WEST) {
    		curDir=1;
    	}
    	else if(robot.getRobotCurDir()== RobotConstants.DIRECTION.NORTH) {
    		curDir=2;
    	}
    	else if(robot.getRobotCurDir()== RobotConstants.DIRECTION.WEST) {
    		curDir=3;
    	}
    	*/
    	if(robot.getRobotCurDir()== RobotConstants.DIRECTION.SOUTH) {
    		curDir=0;
    	}
    	else if(robot.getRobotCurDir()== RobotConstants.DIRECTION.WEST) {
    		curDir=2;
    	}
    	else if(robot.getRobotCurDir()== RobotConstants.DIRECTION.NORTH) {
    		curDir=4;
    	}
    	else if(robot.getRobotCurDir()== RobotConstants.DIRECTION.WEST) {
    		curDir=6;
    	}
    	else if(robot.getRobotCurDir()== RobotConstants.DIRECTION.NORTHEAST) {
    		curDir=5;
    	}
    	else if(robot.getRobotCurDir()== RobotConstants.DIRECTION.NORTHWEST) {
    		curDir=3;
    	}
    	else if(robot.getRobotCurDir()== RobotConstants.DIRECTION.SOUTHEAST) {
    		curDir=7;
    	}
    	else if(robot.getRobotCurDir()== RobotConstants.DIRECTION.SOUTHWEST) {
    		curDir=1;
    	}
    	
    }
    
    //gives a stack that contains all the correct cells for fastest path
    private Stack<Cell> getPath(int goalRow, int goalCol) {
        Stack<Cell> actualPath = new Stack<>();
        Cell tempCell = exploredMap.getCell(goalRow, goalCol);

        while (true) {
            actualPath.push(tempCell);
            tempCell = parents.get(tempCell);
            if (tempCell == null) {
                break;
            }
        }

        return actualPath;
    }
    private void printFastPath(Stack<Cell> path) { //this mtd just print out which cell the robot must travel from
        //System.out.println("\nLooped " + loopCount + " times.");
        System.out.println("The number of steps is: " + (path.size() - 1) + "\n");

        Stack<Cell> pathForPrint = (Stack<Cell>) path.clone();
        Cell temp;
        System.out.println("Path:");
        while (!pathForPrint.isEmpty()) {
            temp = pathForPrint.pop();
            if (!pathForPrint.isEmpty()) System.out.print("(" + temp.getRow() + ", " + temp.getCol() + ") --> ");
            else System.out.print("(" + temp.getRow() + ", " + temp.getCol() + ")");
        }

        System.out.println("\n");
    }
    
    
    private String MoveRobot(Stack<Cell> path, int goalRow, int goalCol) {
        StringBuilder outputString = new StringBuilder();
        
        Cell temp = path.pop();
        DIRECTION targetDir;

        ArrayList<MOVEMENT> movements = new ArrayList<>();

        Robot tempBot = new Robot(1, 1, false);
        tempBot.setRobotDir(robot.getRobotCurDir());
       	tempBot.setRobotPos(robot.getRobotPosRow(), robot.getRobotPosCol());
        tempBot.setSpeed(0);
        while ((tempBot.getRobotPosRow() != goalRow) || (tempBot.getRobotPosCol() != goalCol)) {
            if (tempBot.getRobotPosRow() == temp.getRow() && tempBot.getRobotPosCol() == temp.getCol()) {
                temp = path.pop();
            }

            targetDir = getTargetDir(tempBot.getRobotPosRow(), tempBot.getRobotPosCol(), tempBot.getRobotCurDir(), temp);

            MOVEMENT m;
            System.out.println("Current: " + tempBot.getRobotCurDir() + ", Target: " + targetDir);
            if (tempBot.getRobotCurDir() != targetDir) {
                m = getTargetMove(tempBot.getRobotCurDir(), targetDir);//only give left or right
                //maybe need edit here
            } else {
            	m = MOVEMENT.FORWARD;
            }       

            System.out.println("Movement " + MOVEMENT.print(m) + " from (" + tempBot.getRobotPosRow() + ", " + tempBot.getRobotPosCol() + ") to (" + temp.getRow() + ", " + temp.getCol() + ")");
            tempBot.move(m);
            movements.add(m); //give an arraylist of movement eg Forward,digaonalLeft, diagonalright ect
            //outputString.append(MOVEMENT.print(m));
        }

        if (!robot.getRealBot() || explorationMode) { //maybe need edit this IF part here
            for (MOVEMENT x : movements) {
                if (x == MOVEMENT.FORWARD) {
                    if (!canMoveForward()) {
                        System.out.println("Early termination of fastest path execution.");
                        return "T";
                    }
                }

                robot.move(x);
                this.exploredMap.repaint();

                // During exploration, use sensor data to update exploredMap.
                if (explorationMode) {
                    robot.setSensors();
                    robot.sense(this.exploredMap, this.realMap);
                    this.exploredMap.repaint();
                }
            }
        } else {
            int fCount = 0;
            for (MOVEMENT x : movements) {
                if (x == MOVEMENT.FORWARD) {
                    fCount++;
                    if (fCount == 5) {
                        robot.moveForwardMultiple(fCount);
                        outputString.append(Integer.toString(fCount));
                        fCount = 0;
                        exploredMap.repaint();
                    }
                }
                else if (x == MOVEMENT.RIGHT || x == MOVEMENT.LEFT || x==MOVEMENT.DIAGONALEFT || x==MOVEMENT.DIAGONALRIGHT) {
                    if (fCount > 0) {
                        robot.moveForwardMultiple(fCount);
                        outputString.append(Integer.toString(fCount));
                        outputString.append(MOVEMENT.print(x));
                        fCount = 0;
                        
                        exploredMap.repaint();
                    }

                    robot.move(x);
                    outputString.append(MOVEMENT.print(x));
                    exploredMap.repaint();
                }
            }

            if (fCount > 0) { //for the last part of the direction
                robot.moveForwardMultiple(fCount);
                exploredMap.repaint();
            }
        }

        System.out.println("\nMovements: " + outputString.toString()); //not weok
        return outputString.toString();
    }
    
    private boolean canMoveForward() {
        int row = robot.getRobotPosRow();
        int col = robot.getRobotPosCol();

        switch (robot.getRobotCurDir()) {
            case NORTH:
                if (!exploredMap.isObstacleCell(row + 2, col - 1) && !exploredMap.isObstacleCell(row + 2, col) && !exploredMap.isObstacleCell(row + 2, col + 1)) {
                    return true;
                }
                break;
            case EAST:
                if (!exploredMap.isObstacleCell(row + 1, col + 2) && !exploredMap.isObstacleCell(row, col + 2) && !exploredMap.isObstacleCell(row - 1, col + 2)) {
                    return true;
                }
                break;
            case SOUTH:
                if (!exploredMap.isObstacleCell(row - 2, col - 1) && !exploredMap.isObstacleCell(row - 2, col) && !exploredMap.isObstacleCell(row - 2, col + 1)) {
                    return true;
                }
                break;
            case WEST:
                if (!exploredMap.isObstacleCell(row + 1, col - 2) && !exploredMap.isObstacleCell(row, col - 2) && !exploredMap.isObstacleCell(row - 1, col - 2)) {
                    return true;
                }
                break;
            case NORTHEAST:
            	if (!exploredMap.isObstacleCell(row, col+2) && !exploredMap.isObstacleCell(row+1, col+2) && !exploredMap.isObstacleCell(row+ 1, col +2)&& 
            			!exploredMap.isObstacleCell(row+ 2, col +1) && !exploredMap.isObstacleCell(row+ 2, col)) {
            		return true;
            	}
            	break;
            case NORTHWEST:
            	if (!exploredMap.isObstacleCell(row, col-2) && !exploredMap.isObstacleCell(row+1, col-2) && !exploredMap.isObstacleCell(row+ 2, col -2)&& 
            			!exploredMap.isObstacleCell(row+ 2, col) && !exploredMap.isObstacleCell(row+ 2, col-2)){
            		return true;
            	}
            case SOUTHEAST:
            	if (!exploredMap.isObstacleCell(row, col+2) && !exploredMap.isObstacleCell(row-1, col+2) && !exploredMap.isObstacleCell(row- 2, col +2)&& 
            			!exploredMap.isObstacleCell(row- 2, col) && !exploredMap.isObstacleCell(row- 2, col+1)){
            		return true;
            	}
            case SOUTHWEST:
            	if (!exploredMap.isObstacleCell(row-2, col) && !exploredMap.isObstacleCell(row-2, col-1) && !exploredMap.isObstacleCell(row- 2, col -2)&& 
            			!exploredMap.isObstacleCell(row, col-2) && !exploredMap.isObstacleCell(row-1, col-2)){
            		return true;
            	}
        }

        return false;
    }

    
    /**
     * Returns the movement to execute to get from one direction to another.
     */
    private MOVEMENT getTargetMove(DIRECTION a, DIRECTION b) {
        switch (a) {
            case NORTH:
                switch (b) {
                    case NORTH:
                        return MOVEMENT.ERROR;
                    case SOUTH:
                        return MOVEMENT.LEFT;
                    case WEST:
                        return MOVEMENT.LEFT;
                    case EAST:
                        return MOVEMENT.RIGHT;
                    case NORTHEAST: 
                    	return MOVEMENT.DIAGONALRIGHT;
                    case NORTHWEST:
                    	return MOVEMENT.DIAGONALEFT;
                    case SOUTHWEST:
                    	return MOVEMENT.DIAGONALEFT;
                    case SOUTHEAST:
                    	return MOVEMENT.DIAGONALRIGHT;
                }
                break;
            case SOUTH:
                switch (b) {
                    case NORTH:
                        return MOVEMENT.LEFT;
                    case SOUTH:
                        return MOVEMENT.ERROR;
                    case WEST:
                        return MOVEMENT.RIGHT;
                    case EAST:
                        return MOVEMENT.LEFT;
                    case SOUTHEAST:
                    	return MOVEMENT.DIAGONALEFT;
                    case SOUTHWEST:
                    	return MOVEMENT.DIAGONALRIGHT;
                    case NORTHEAST:
                    	return MOVEMENT.DIAGONALEFT;
                    case NORTHWEST:
                    	return MOVEMENT.DIAGONALRIGHT;
                }
                break;
            case WEST:
                switch (b) {
                    case NORTH:
                        return MOVEMENT.RIGHT;
                    case SOUTH:
                        return MOVEMENT.LEFT;
                    case WEST:
                        return MOVEMENT.ERROR;
                    case EAST:
                        return MOVEMENT.LEFT;
                    case SOUTHWEST:
                    	return MOVEMENT.DIAGONALEFT;
                    case NORTHWEST: 
                    	return MOVEMENT.DIAGONALRIGHT;
                    case  NORTHEAST:
                    	return MOVEMENT.DIAGONALRIGHT;
                    case SOUTHEAST:
                    	return MOVEMENT.DIAGONALEFT;
                }
                break;
            case EAST:
                switch (b) {
                    case NORTH:
                        return MOVEMENT.LEFT;
                    case SOUTH:
                        return MOVEMENT.RIGHT;
                    case WEST:
                        return MOVEMENT.LEFT;
                    case EAST:
                        return MOVEMENT.ERROR;
                    case NORTHEAST: 
                    	return MOVEMENT.DIAGONALEFT;
                    case SOUTHEAST:
                    	return MOVEMENT.DIAGONALRIGHT;
                    case NORTHWEST:
                    	return MOVEMENT.DIAGONALEFT;
                    case SOUTHWEST:
                    	return MOVEMENT.DIAGONALRIGHT;
                }
            case NORTHEAST:
            	switch(b) {
            	case NORTH:
                    return MOVEMENT.DIAGONALEFT;
                case SOUTH:
                    return MOVEMENT.DIAGONALRIGHT;
                case WEST:
                    return MOVEMENT.DIAGONALEFT;
                case EAST:
                    return MOVEMENT.DIAGONALRIGHT;
                case NORTHEAST: 
                	return MOVEMENT.ERROR;
                case SOUTHEAST:
                	return MOVEMENT.DIAGONALRIGHT;
                case NORTHWEST:
                	return MOVEMENT.DIAGONALEFT;
                case SOUTHWEST:
                	return MOVEMENT.DIAGONALEFT;
            	}
            case NORTHWEST:
            	switch(b) {
            	case NORTH:
                    return MOVEMENT.DIAGONALRIGHT;
                case SOUTH:
                    return MOVEMENT.DIAGONALEFT;
                case WEST:
                    return MOVEMENT.DIAGONALEFT;
                case EAST:
                    return MOVEMENT.DIAGONALRIGHT;
                case NORTHEAST: 
                	return MOVEMENT.DIAGONALRIGHT;
                case SOUTHEAST:
                	return MOVEMENT.DIAGONALEFT;
                case NORTHWEST:
                	return MOVEMENT.ERROR;
                case SOUTHWEST:
                	return MOVEMENT.DIAGONALEFT;
            	}
            case SOUTHWEST:
            	switch(b) {
            	case NORTH:
                    return MOVEMENT.DIAGONALRIGHT;
                case SOUTH:
                    return MOVEMENT.DIAGONALEFT;
                case WEST:
                    return MOVEMENT.DIAGONALRIGHT;
                case EAST:
                    return MOVEMENT.DIAGONALEFT;
                case NORTHEAST: 
                	return MOVEMENT.DIAGONALRIGHT;
                case SOUTHEAST:
                	return MOVEMENT.DIAGONALEFT;
                case NORTHWEST:
                	return MOVEMENT.DIAGONALRIGHT;
                case SOUTHWEST:
                	return MOVEMENT.ERROR;
            	}
            case SOUTHEAST:
            	switch(b) {
            	case NORTH:
                    return MOVEMENT.DIAGONALEFT;
                case SOUTH:
                    return MOVEMENT.DIAGONALRIGHT;
                case WEST:
                    return MOVEMENT.DIAGONALRIGHT;
                case EAST:
                    return MOVEMENT.DIAGONALEFT;
                case NORTHEAST: 
                	return MOVEMENT.DIAGONALEFT;
                case SOUTHEAST:
                	return MOVEMENT.ERROR;
                case NORTHWEST:
                	return MOVEMENT.DIAGONALRIGHT;
                case SOUTHWEST:
                	return MOVEMENT.DIAGONALRIGHT;
            	}
        }
        return MOVEMENT.ERROR;
    }

}