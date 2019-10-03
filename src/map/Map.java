package map;

import robot.Robot;
import robot.RobotConstants;

import javax.swing.*;



import java.awt.*;
import java.util.ArrayList;

/**
 * Represents the entire map grid for the arena.
 *
 * Edited the grid,bot variables, added new methods called resetMap,setStart,checkStart and changed paintcomponent
 */

public class Map extends JPanel {
    //private final Cell[][] grid; //as currently final,can't reset the map
    //private final Robot bot;
	   private Cell[][] grid;
	   private  Robot bot;
	   private boolean start =false; //added to allow painting of free cell
    /** 
     * Initialises a Map object with a grid of Cell objects.
     */
    public Map(Robot bot) {
        this.bot = bot;

        grid = new Cell[MapConstants.MAP_ROWS][MapConstants.MAP_COLS];
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                grid[row][col] = new Cell(row, col);

                // Set the virtual walls of the arena
                if (row == 0 || col == 0 || row == MapConstants.MAP_ROWS - 1 || col == MapConstants.MAP_COLS - 1) {
                    grid[row][col].setVirtualWall(true);
                }
            }
        }
    }

    public void resetMap() {
    		setStart(false);	
    	   grid = new Cell[MapConstants.MAP_ROWS][MapConstants.MAP_COLS];
           for (int row = 0; row < grid.length; row++) {
               for (int col = 0; col < grid[0].length; col++) {
                   grid[row][col] = new Cell(row, col);

                   // Set the virtual walls of the arena
                   if (row == 0 || col == 0 || row == MapConstants.MAP_ROWS - 1 || col == MapConstants.MAP_COLS - 1) {
                       grid[row][col].setVirtualWall(true);
                   }
               }
           }
 
    }
    /**
     * Returns true if the row and column values are valid.
     */
    public boolean checkValidCoordinates(int row, int col) {
        return row >= 0 && col >= 0 && row < MapConstants.MAP_ROWS && col < MapConstants.MAP_COLS;
    }

    /**
     * Returns true if the row and column values are in the start zone.
     */
    private boolean inStartZone(int row, int col) {
        return row >= 0 && row <= 2 && col >= 0 && col <= 2;
    }

    /**
     * Returns true if the row and column values are in the goal zone.
     */
    private boolean inGoalZone(int row, int col) {
        return (row <= MapConstants.GOAL_ROW + 1 && row >= MapConstants.GOAL_ROW - 1 && col <= MapConstants.GOAL_COL + 1 && col >= MapConstants.GOAL_COL - 1);
    }

    /**
     * Returns a particular cell in the grid.
     */
    public Cell getCell(int row, int col) {
        return grid[row][col];
    }

    /**
     * Returns true if a cell is an obstacle.
     */
    public boolean isObstacleCell(int row, int col) {
        return grid[row][col].getIsObstacle();
    }

    /**
     * Returns true if a cell is a virtual wall.
     */
    public boolean isVirtualWallCell(int row, int col) {
        return grid[row][col].getIsVirtualWall();
    }

    /**
     * Sets all cells in the grid to an explored state.
     */
    public void setAllExplored() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                grid[row][col].setIsExplored(true);
            }
        }
    }

    /**
     * Sets all cells in the grid to an unexplored state except for the START & GOAL zone.
     */
    public void setAllUnexplored() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (inStartZone(row, col) || inGoalZone(row, col)) {
                    grid[row][col].setIsExplored(true);
                } else {
                    grid[row][col].setIsExplored(false);
                }
            }
        }
    }

    /**
     * Sets a cell as an obstacle and the surrounding cells as virtual walls or
     * resets the cell and surrounding virtual walls.
     */
    public void setObstacleCell(int row, int col, boolean obstacle) {
        if (obstacle && (inStartZone(row, col) || inGoalZone(row, col)))
            return;

        System.out.println(String.format("There is an obstacle cell at Row %d Col %d", row+1, col+1));

        grid[row][col].setIsObstacle(obstacle);

        //System.out.println(String.format("Check row 1 col 4 virtual wall %B", grid[1][4].getIsVirtualWall()));

        if (row >= 1) {
            if (!obstacleAround(row - 1, col) || obstacle)
                grid[row - 1][col].setVirtualWall(obstacle); // bottom cell

            if (col < MapConstants.MAP_COLS - 1) {
                if (!obstacleAround(row - 1, col + 1) || obstacle)
                    grid[row - 1][col + 1].setVirtualWall(obstacle); // bottom-right cell
            }

            if (col >= 1) {
                if (!obstacleAround(row - 1, col - 1) || obstacle)
                    grid[row - 1][col - 1].setVirtualWall(obstacle); // bottom-left cell
            }
        }

        if (row < MapConstants.MAP_ROWS - 1) {
            if (!obstacleAround(row + 1, col) || obstacle)
                grid[row + 1][col].setVirtualWall(obstacle); // top cell

            if (col < MapConstants.MAP_COLS - 1) {
                if (!obstacleAround(row + 1, col + 1) || obstacle)
                    grid[row + 1][col + 1].setVirtualWall(obstacle); // top-right cell
            }

            if (col >= 1) {
                if (!obstacleAround(row + 1, col - 1) || obstacle)
                    grid[row + 1][col - 1].setVirtualWall(obstacle); // top-left cell
            }
        }

        if (col >= 1) {
            if (!obstacleAround(row, col - 1) || obstacle)
                grid[row][col - 1].setVirtualWall(obstacle); // left cell
        }

        if (col < MapConstants.MAP_COLS - 1) {
            if (!obstacleAround(row, col + 1) || obstacle)
                grid[row][col + 1].setVirtualWall(obstacle); // right cell
        }
        //System.out.println(String.format("Check row 1 col 4 virtual wall %B", grid[1][4].getIsVirtualWall()));
    }

    /**
     * Sets start - added
     */
    public void setStart(boolean start) {
    	this.start = start;
    }

    /**
     * check if started  - added
     */
    public boolean checkStart() {
    	return start;
    }
    public boolean obstacleAround(int row, int col) {
        boolean bottomCell = false, bottomRightCell = false, bottomLeftCell = false, topCell = false,
                topRightCell = false, topLeftCell = false, leftCell = false, rightCell = false;
        if (row >= 1) {
            bottomCell = grid[row - 1][col].getIsObstacle(); // bottom cell

            if (col < MapConstants.MAP_COLS - 1) {
                bottomRightCell = grid[row - 1][col + 1].getIsObstacle(); // bottom-right cell
            }

            if (col >= 1) {
                bottomLeftCell = grid[row - 1][col - 1].getIsObstacle(); // bottom-left cell
            }
        }

        if (row < MapConstants.MAP_ROWS - 1) {
            topCell = grid[row + 1][col].getIsObstacle(); // top cell

            if (col < MapConstants.MAP_COLS - 1) {
                topRightCell = grid[row + 1][col + 1].getIsObstacle(); // top-right cell
            }

            if (col >= 1) {
                topLeftCell = grid[row + 1][col - 1].getIsObstacle(); // top-left cell
            }
        }

        if (col >= 1) {
            leftCell = grid[row][col - 1].getIsObstacle(); // left cell
        }

        if (col < MapConstants.MAP_COLS - 1) {
            rightCell = grid[row][col + 1].getIsObstacle(); // right cell
        }
        return bottomCell || bottomRightCell || bottomLeftCell || topCell || topRightCell || topLeftCell || leftCell
                || rightCell;
    }

    /**
     * Returns true if the given cell is out of bounds or an obstacle.
     */
    public boolean getIsObstacleOrWall(int row, int col) {
        return !checkValidCoordinates(row, col) || getCell(row, col).getIsObstacle();
    }
    
    
    /**
     * Returns cell that is unexplored
     * 
     * */
    public Cell getUnexploredCell() {
    	Cell unexploredCell = null;
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
              if(getCell(row, col).getIsObstacle()==true) {
              
            	  unexploredCell = new Cell(row,col);
            	  return unexploredCell;
              }
              }
            }
            
        return unexploredCell;
        
    }

    
    /**
     * Returns a list of cells that is unexplored
     * 
     * */
    public ArrayList<Cell> getUnexploredCells() {
    	ArrayList<Cell> unexploredCells = new ArrayList<Cell>();
    	Cell unexploredCell = null;
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
              if(getCell(row, col).getIsExplored()==false) {
              
            	  unexploredCell = new Cell(row,col);
            	  unexploredCells.add(unexploredCell);
              }
              }
            }
            
        return unexploredCells;
        
    }

    /**
     * Overrides JComponent's paintComponent() method. It creates a two-dimensional array of _DisplayCell objects
     * to store the current map state. Then, it paints square cells for the grid with the appropriate colors as
     * well as the robot on-screen.
     */

    public void paintComponent(Graphics g) {
    	super.paintComponent(g); //added
        // Create a two-dimensional array of _DisplayCell objects for rendering.
        _DisplayCell[][] _mapCells = new _DisplayCell[MapConstants.MAP_ROWS][MapConstants.MAP_COLS];
        for (int mapRow = 0; mapRow < MapConstants.MAP_ROWS; mapRow++) {
            for (int mapCol = 0; mapCol < MapConstants.MAP_COLS; mapCol++) {
                _mapCells[mapRow][mapCol] = new _DisplayCell(mapCol * GraphicsConstants.CELL_SIZE, mapRow * GraphicsConstants.CELL_SIZE, GraphicsConstants.CELL_SIZE);
            }
        }

        // Paint the cells with the appropriate colors.
        //changed abit 
        for (int mapRow = 0; mapRow < MapConstants.MAP_ROWS; mapRow++) {
            for (int mapCol = 0; mapCol < MapConstants.MAP_COLS; mapCol++) {
                Color cellColor;

                if (inStartZone(mapRow, mapCol))
                    cellColor = GraphicsConstants.C_START;
                else if (inGoalZone(mapRow, mapCol))
                    cellColor = GraphicsConstants.C_GOAL;
                else {
                    if (!grid[mapRow][mapCol].getIsExplored())
                    	 cellColor = GraphicsConstants.C_UNEXPLORED;
                    else 
                    	if(grid[mapRow][mapCol].getIsObstacle())
                        cellColor = GraphicsConstants.C_OBSTACLE;
                    	else  if (checkStart()==true) 
                        cellColor = GraphicsConstants.C_EXPLORED;
                    	else
                    		cellColor = GraphicsConstants.C_FREE;

                }

                g.setColor(cellColor);
                g.fillRect(_mapCells[mapRow][mapCol].cellX + GraphicsConstants.MAP_X_OFFSET, _mapCells[mapRow][mapCol].cellY, _mapCells[mapRow][mapCol].cellSize, _mapCells[mapRow][mapCol].cellSize);

            }
        }

        // Paint the robot on-screen.
        g.setColor(GraphicsConstants.C_ROBOT);
        int r = bot.getRobotPosRow();
        int c = bot.getRobotPosCol();
        g.fillOval((c - 1) * GraphicsConstants.CELL_SIZE + GraphicsConstants.ROBOT_X_OFFSET + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - (r * GraphicsConstants.CELL_SIZE + GraphicsConstants.ROBOT_Y_OFFSET), GraphicsConstants.ROBOT_W, GraphicsConstants.ROBOT_H);

        // Paint the robot's direction indicator on-screen.
        g.setColor(GraphicsConstants.C_ROBOT_DIR);		
        RobotConstants.DIRECTION d = bot.getRobotCurDir();
        switch (d) {
            case NORTH:
                g.fillOval(c * GraphicsConstants.CELL_SIZE + 15 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE - 15, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            case EAST:
                g.fillOval(c * GraphicsConstants.CELL_SIZE + 40 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 10, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            case SOUTH:
                g.fillOval(c * GraphicsConstants.CELL_SIZE + 15 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 35, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            case WEST:
                g.fillOval(c * GraphicsConstants.CELL_SIZE - 20 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 10, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
        }
    }

    private class _DisplayCell {
        public final int cellX;
        public final int cellY;
        public final int cellSize;

        public _DisplayCell(int borderX, int borderY, int borderSize) {
            this.cellX = borderX + GraphicsConstants.CELL_LINE_WEIGHT;
            this.cellY = GraphicsConstants.MAP_H - (borderY - GraphicsConstants.CELL_LINE_WEIGHT);
            this.cellSize = borderSize - (GraphicsConstants.CELL_LINE_WEIGHT * 2);
        }
    }
    
    //added to try out goToPoint
    /**
     * Return the nearest unexplored cell from a location
   
     */
    public Cell nearestUnexplored(Cell loc) {
        double dist = 1000, tempDist;
        Cell nearest = null, tempCell;

        for (int row = 0; row < MapConstants.MAP_ROWS; row++) {
            for (int col = 0; col < MapConstants.MAP_COLS; col++) {
                tempCell = grid[row][col];
                tempDist = costH(tempCell,loc.getRow(),loc.getCol());
                if ((!tempCell.getIsExplored()) && (tempDist < dist) && clearForRobot(row,col)) {
                    nearest = tempCell;
                    dist = tempDist;
                }
            }
        }
        return nearest;
    }
 
    
    //added to try out goToPoint
    /**
     * Return the nearest explored cell from a location
   
     */
    public Cell nearestExplored(Cell loc) {
        double dist = 1000, tempDist;
        Cell nearest = null, tempCell;

        for (int row = 0; row < MapConstants.MAP_ROWS; row++) {
            for (int col = 0; col < MapConstants.MAP_COLS; col++) {
                tempCell = grid[row][col];
                tempDist = costH(tempCell,loc.getRow(),loc.getCol());
                if ((tempCell.getIsExplored()) && (tempDist < dist) && !tempCell.getIsVirtualWall()&& !tempCell.getIsObstacle()) { //added to check if it is virtual wall
                    nearest = tempCell;
                    dist = tempDist;
                }
            }
        }
        return nearest;
    }
    
    /**
     * Return the nearest explored but not move through cell given the nearest unexplored cell
     * @param loc nearest unexplored point location
     * @param botLoc location of the robot
     * @return nearest explored Cell, null if there isnt one
     */
    /* no needed
    public Cell nearestExplored(Cell loc, Cell botLoc) {
        Cell cell, nearest = null;
        double distance =  1000;


        for (int row = 0; row < MapConstants.MAP_ROWS; row++) {
            for (int col = 0; col < MapConstants.MAP_COLS; col++) {
            	System.out.println("System trying");
                cell = new Cell(row,col);

                if (checkValidCoordinates(row, col) && clearForRobot(row, col) && cell.getIsExplored()) {
                    System.out.println("Cell Row: " +row +" Col: "+col );
                    if (distance > costH(botLoc,cell.getRow(),cell.getCol()) ) {       // actually no need to check for botLoc
                        nearest = cell;
                        distance = costH(botLoc,cell.getRow(),cell.getCol());
           

                    }
                }
            }
        }
        System.out.println(nearest);
        return nearest;
    }
    */

    /**
     * Check whether a particular grid is clear for robot to move through
     * @param row
     * @param col
     * @return true if the cell, its left and its right are valid and non-obstacle cell
     */
    public boolean clearForRobot(int row, int col) {
     	Cell checkCell=null;
     	int counter =0;
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
            	checkCell= new Cell(r,c);
                if (!checkValidCoordinates(r,c) || checkCell.getIsObstacle()) {
                	counter++;
                	if(counter==3) {
                    return false;
                	}
                }
            }
        }
        return true;
    }
    private double costH(Cell b, int goalRow, int goalCol) {
        // Heuristic: The no. of moves will be equal to the difference in the row and column values.
        double movementCost = (Math.abs(goalCol - b.getCol()) + Math.abs(goalRow - b.getRow())) * RobotConstants.MOVE_COST;

        if (movementCost == 0) return 0;

        // Heuristic: If b is not in the same row or column, one turn will be needed.
        double turnCost = 0;
        if (goalCol - b.getCol() != 0 || goalRow - b.getRow() != 0) {
            turnCost = RobotConstants.TURN_COST;
        }

        return movementCost + turnCost;
    }
}
