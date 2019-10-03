package algorithms;

import map.Cell;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import utils.CommMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

// @formatter:off
/**
 * Fastest path algorithm for the robot. Uses a version of the A* algorithm.
 *
 * g(n) = Real Cost from START to n
 * h(n) = Heuristic Cost from n to GOAL
 *
 * @author Suyash Lakhotia
 */
// @formatter:on

public class FastestPathAlgo {
    private ArrayList<Cell> toVisit;        // array of Cells to be visited
    private ArrayList<Cell> visited;        // array of visited Cells
    private HashMap<Cell, Cell> parents;    // HashMap of Child --> Parent
    private Cell current;                   // current Cell
    private Cell[] neighbors;               // array of neighbors of current Cell
    private DIRECTION curDir;               // current direction of robot
    private double[][] gCosts;              // array of real cost from START to [row][col] i.e. g(n)
    private Robot bot;
    private Map exploredMap;
    private final Map realMap;
    private int loopCount;
    private boolean explorationMode;
    
    //add to store original robot position
    private Cell robotPosition;

    public FastestPathAlgo(Map exploredMap, Robot bot) {
        this.realMap = null;
        initObject(exploredMap, bot);
    }

    public FastestPathAlgo(Map exploredMap, Robot bot, Map realMap) {
        this.realMap = realMap;
        this.explorationMode = true;
        initObject(exploredMap, bot);
    }

    /**
     * Initialise the FastestPathAlgo object.
     */
    private void initObject(Map map, Robot bot) {
        this.bot = bot;
        this.exploredMap = map;
        this.toVisit = new ArrayList<>();
        this.visited = new ArrayList<>();
        this.parents = new HashMap<>();
        this.neighbors = new Cell[4];
        this.current = map.getCell(bot.getRobotPosRow(), bot.getRobotPosCol());
        this.curDir = bot.getRobotCurDir();
        this.gCosts = new double[MapConstants.MAP_ROWS][MapConstants.MAP_COLS];

        // Initialise gCosts array
        for (int i = 0; i < MapConstants.MAP_ROWS; i++) {
            for (int j = 0; j < MapConstants.MAP_COLS; j++) {
                Cell cell = map.getCell(i, j);
                if (!canBeVisited(cell)) {
                    gCosts[i][j] = RobotConstants.INFINITE_COST;
                } else {
                    gCosts[i][j] = 0;
                }
            }
        }
        toVisit.add(current);

        // Initialise starting point
        gCosts[bot.getRobotPosRow()][bot.getRobotPosCol()] = 0;
        this.loopCount = 0;
    }

    /**
     * Returns true if the cell can be visited.
     */
    private boolean canBeVisited(Cell c) {
        return c.getIsExplored() && !c.getIsObstacle() && !c.getIsVirtualWall();
    }

    /**
     * Returns the Cell inside toVisit with the minimum g(n) + h(n).
     */
    private Cell minimumCostCell(int goalRow, int getCol) {
        int size = toVisit.size();
        double minCost = RobotConstants.INFINITE_COST;
        Cell result = null;

        for (int i = size - 1; i >= 0; i--) {
            double gCost = gCosts[(toVisit.get(i).getRow())][(toVisit.get(i).getCol())];
            double cost = gCost + costH(toVisit.get(i), goalRow, getCol);
            if (cost < minCost) {
                minCost = cost;
                result = toVisit.get(i);
            }
        }

        return result;
    }

    /**
     * Returns the heuristic cost i.e. h(n) from a given Cell to a given [goalRow, goalCol] in the maze.
     */
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

    /**
     * Returns the target direction of the bot from [botR, botC] to target Cell.
     */
    private DIRECTION getTargetDir(int botR, int botC, DIRECTION botDir, Cell target) {
        if (botC - target.getCol() > 0) {
            return DIRECTION.WEST;
        } else if (target.getCol() - botC > 0) {
            return DIRECTION.EAST;
        } else {
            if (botR - target.getRow() > 0) {
                return DIRECTION.SOUTH;
            } else if (target.getRow() - botR > 0) {
                return DIRECTION.NORTH;
            } else {
                return botDir;
            }
        }
    }

    /**
     * Get the actual turning cost from one DIRECTION to another.
     */
    private double getTurnCost(DIRECTION a, DIRECTION b) {
        int numOfTurn = Math.abs(a.ordinal() - b.ordinal());
        if (numOfTurn > 2) {
            numOfTurn = numOfTurn % 2;
        }
        return (numOfTurn * RobotConstants.TURN_COST);
    }

    /**
     * Calculate the actual cost of moving from Cell a to Cell b (assuming both are neighbors).
     */
    private double costG(Cell a, Cell b, DIRECTION aDir) {
        double moveCost = RobotConstants.MOVE_COST; // one movement to neighbor

        double turnCost;
        DIRECTION targetDir = getTargetDir(a.getRow(), a.getCol(), aDir, b);
        turnCost = getTurnCost(aDir, targetDir);

        return moveCost + turnCost;
    }

    /**
     * Find the fastest path from the robot's current position to [goalRow, goalCol].
     */
    public String runFastestPath(int goalRow, int goalCol) {
        System.out.println("Calculating fastest path from (" + current.getRow() + ", " + current.getCol() + ") to goal (" + goalRow + ", " + goalCol + ")...");


        Stack<Cell> path;
        do {
            loopCount++;
    

            // Get cell with minimum cost from toVisit and assign it to current.
            current = minimumCostCell(goalRow, goalCol);

           
            // Point the robot in the direction of current from the previous cell.
            if (parents.containsKey(current)) {
                curDir = getTargetDir(parents.get(current).getRow(), parents.get(current).getCol(), curDir, current);
            }
            

            visited.add(current);       // add current to visited
            toVisit.remove(current);    // remove current from toVisit

            
            
            if (visited.contains(exploredMap.getCell(goalRow, goalCol))) {
                System.out.println("Goal visited. Path found!");
                path = getPath(goalRow, goalCol);
                printFastestPath(path);
                return executePath(path, goalRow, goalCol);
            }

            // Setup neighbors of current cell. [Top, Bottom, Left, Right].
            if (exploredMap.checkValidCoordinates(current.getRow() + 1, current.getCol())) {
                neighbors[0] = exploredMap.getCell(current.getRow() + 1, current.getCol());
                if (!canBeVisited(neighbors[0])) {
                    neighbors[0] = null;
                }
            }
            if (exploredMap.checkValidCoordinates(current.getRow() - 1, current.getCol())) {
                neighbors[1] = exploredMap.getCell(current.getRow() - 1, current.getCol());
                if (!canBeVisited(neighbors[1])) {
                    neighbors[1] = null;
                }
            }
            if (exploredMap.checkValidCoordinates(current.getRow(), current.getCol() - 1)) {
                neighbors[2] = exploredMap.getCell(current.getRow(), current.getCol() - 1);
                if (!canBeVisited(neighbors[2])) {
                    neighbors[2] = null;
                }
            }
            if (exploredMap.checkValidCoordinates(current.getRow(), current.getCol() + 1)) {
                neighbors[3] = exploredMap.getCell(current.getRow(), current.getCol() + 1);
                if (!canBeVisited(neighbors[3])) {
                    neighbors[3] = null;
                }
            }

            // Iterate through neighbors and update the g(n) values of each.
            for (int i = 0; i < 4; i++) {
                if (neighbors[i] != null) {
               
                    if (visited.contains(neighbors[i])) {
                        continue;
                    }

                    if (!(toVisit.contains(neighbors[i]))) {
                        parents.put(neighbors[i], current);
                        gCosts[neighbors[i].getRow()][neighbors[i].getCol()] = gCosts[current.getRow()][current.getCol()] + costG(current, neighbors[i], curDir);
                        toVisit.add(neighbors[i]);
                    } else {
                        double currentGScore = gCosts[neighbors[i].getRow()][neighbors[i].getCol()];
                        double newGScore = gCosts[current.getRow()][current.getCol()] + costG(current, neighbors[i], curDir);
                        if (newGScore < currentGScore) {
                            gCosts[neighbors[i].getRow()][neighbors[i].getCol()] = newGScore;
                            parents.put(neighbors[i], current);
                        }
                    }
                }
            }
        } while (!toVisit.isEmpty());

        System.out.println("Path not found!");
        return null;
    }

    /**
     * Generates path in reverse using the parents HashMap.
     */
    private Stack<Cell> getPath(int goalRow, int goalCol) {
        Stack<Cell> actualPath = new Stack<>();
        Cell temp = exploredMap.getCell(goalRow, goalCol);

        while (true) {
            actualPath.push(temp);
            temp = parents.get(temp);
            if (temp == null) {
                break;
            }
        }

        return actualPath;
    }

    /**
     * Executes the fastest path and returns a StringBuilder object with the path steps.
     */
    private String executePath(Stack<Cell> path, int goalRow, int goalCol) {
        StringBuilder outputString = new StringBuilder();

        Cell temp = path.pop();
        DIRECTION targetDir;

        ArrayList<MOVEMENT> movements = new ArrayList<>();

        Robot tempBot = new Robot(bot.getRobotPosRow(), bot.getRobotPosCol(), false); //changed
        tempBot.setSpeed(100);
        
        //need to ensure temprobot is the same direct as the robot
        tempBot.setRobotDir(bot.getRobotCurDir());
        



        while ((tempBot.getRobotPosRow() != goalRow) || (tempBot.getRobotPosCol() != goalCol)) { 
        
            if (tempBot.getRobotPosRow() == temp.getRow() && tempBot.getRobotPosCol() == temp.getCol()) {
                temp = path.pop();
   
            }

            targetDir = getTargetDir(tempBot.getRobotPosRow(), tempBot.getRobotPosCol(), tempBot.getRobotCurDir(), temp);

       
            MOVEMENT m;

            if (tempBot.getRobotCurDir() != targetDir) {
                m = getTargetMove(tempBot.getRobotCurDir(), targetDir);
            } else {
                m = MOVEMENT.FORWARD;
            }

            System.out.println("Movement " + MOVEMENT.print(m) + " from (" + tempBot.getRobotPosRow() + ", " + tempBot.getRobotPosCol() + ") to (" + temp.getRow() + ", " + temp.getCol() + ")");

            tempBot.move(m);
            movements.add(m);
            outputString.append(MOVEMENT.print(m));
        }

        
        bot.setBotToFake();

        	if (!bot.getRealBot() || explorationMode) {
            
            for (MOVEMENT x : movements) {
            
                if (x == MOVEMENT.FORWARD) {
                    if (!canMoveForward()) {
                     	System.out.println("Error occured at Row:"+bot.getRobotPosRow()+" Col: "+bot.getRobotPosCol());
                        System.out.println("Early termination of fastest path execution.");
                        return "T";
                    }
                }


                bot.move(x);
                this.exploredMap.repaint();

                // During exploration, use sensor data to update exploredMap.
                if (explorationMode) {
                    bot.setSensors();
                    bot.sense(this.exploredMap, this.realMap);
                    this.exploredMap.repaint();
                }
            }
        } else {
            int fCount = 0;
            for (MOVEMENT x : movements) {
                if (x == MOVEMENT.FORWARD) {
                    fCount++;
                    if (fCount == 10) {
                        bot.moveForwardMultiple(fCount);
                        fCount = 0;
                        exploredMap.repaint();
                    }
                } else if (x == MOVEMENT.RIGHT || x == MOVEMENT.LEFT) {
                    if (fCount > 0) {
                        bot.moveForwardMultiple(fCount);
                        fCount = 0;
                        exploredMap.repaint();
                    }

                    bot.move(x);
                    exploredMap.repaint();
                }
            }

            if (fCount > 0) {
                bot.moveForwardMultiple(fCount);
                exploredMap.repaint();
            }
        }
        

        System.out.println("\nMovements: " + outputString.toString());
        // TODO: Use this output string to send movement instead?
        //String newMovements = computeMovements(outputString.toString()); Commented out
        // Ignore the string, comm mgr sent message in the method
        return outputString.toString();
    }

    private String computeMovements(String s) {
        if (s.length() <= 1) return s;
        int conseqW = 0;
        StringBuilder res = new StringBuilder();
        for (int i = 1; i < s.length(); i++) {
            char c1 = s.charAt(i - 1);
            char c2 = s.charAt(i);
            if (c1 == 'W') {
                conseqW++;
            }

            if (c2 != 'W') {
                res.append(compressMovement(conseqW));

                conseqW = 0;

                if (c2 == 'A') {
                    res.append('J');
                } else if (c2 == 'D') {
                    res.append('K');
                }
            }
        }

        if (s.charAt(s.length() - 1) == 'W') {
            conseqW++;
            res.append(compressMovement(conseqW));
        }

        System.out.println("New movements 1: " + res.toString());
        String newMovement2 = getNewMovement2(res.toString());
        System.out.println("New movements 2: " + newMovement2);
        try {
            CommMgr comm = CommMgr.getCommMgr();
            // comm.sendMsg(res.toString(), CommMgr.INSTRUCTIONS); - comment out
           // comm.sendMsg(newMovement2, CommMgr.INSTRUCTIONS);
        } catch (Exception e) {
        }
        return res.toString();
    }

    private enum YkDirection {
        NORTH, EAST, SOUTH, WEST
    }

    class YkRobot {
        public int x, y;
        public YkDirection direction;
        public YkRobot(int x, int y, YkDirection direction) {
            this.x = x;
            this.y = y;
            this.direction = direction;
        }

        public void rotLeft() {
            switch (direction) {
                case NORTH:
                    direction = YkDirection.WEST;
                    break;
                case EAST:
                    direction = YkDirection.NORTH;
                    break;
                case SOUTH:
                    direction = YkDirection.EAST;
                    break;
                case WEST:
                    direction = YkDirection.SOUTH;
                    break;
            }
        }

        public void rotRight() {
            switch (direction) {
                case NORTH:
                    direction = YkDirection.EAST;
                    break;
                case EAST:
                    direction = YkDirection.SOUTH;
                    break;
                case SOUTH:
                    direction = YkDirection.WEST;
                    break;
                case WEST:
                    direction = YkDirection.NORTH;
                    break;
            }
        }

        public void moveForward(int steps) {
            switch (direction) {
                case NORTH:
                    y += steps;
                    break;
                case EAST:
                    x += steps;
                    break;
                case SOUTH:
                    y -= steps;
                    break;
                case WEST:
                    x -= steps;
                    break;
            }
        }

        /*
        public boolean checkValidCoordinates(int row, int col) {
            return row >= 0 && col >= 0 && row < MapConstants.MAP_ROWS && col < MapConstants.MAP_COLS;
        }

        public boolean isObstacleCell(int row, int col) {
            return grid[row][col].getIsObstacle();
        }
         */
        private int getNewX(int x) {
            switch (direction) {
                case NORTH:
                    return x;
                case EAST:
                    return x + 2;
                case SOUTH:
                    return x;
                case WEST:
                    return x - 2;
            }
            System.out.println("ERROR");
            return -1;
        }

        private int getNewY(int y) {
            switch (direction) {
                case NORTH:
                    return y + 2;
                case EAST:
                    return y;
                case SOUTH:
                    return y - 2;
                case WEST:
                    return y;
            }
            System.out.println("ERROR");
            return -1;
        }

        private int getRightX(int x) {
            switch (direction) {
                case NORTH:
                    return x + 1;
                case EAST:
                    return x;
                case SOUTH:
                    return x - 1;
                case WEST:
                    return x;
            }
            System.out.println("ERROR");
            return -1;
        }

        private int getLeftX(int x) {
            switch (direction) {
                case NORTH:
                    return x - 1;
                case EAST:
                    return x;
                case SOUTH:
                    return x + 1;
                case WEST:
                    return x;
            }
            System.out.println("ERROR");
            return -1;
        }

        private int getRightY(int y) {
            switch (direction) {
                case NORTH:
                    return y;
                case EAST:
                    return y - 1;
                case SOUTH:
                    return y;
                case WEST:
                    return y + 1;
            }
            System.out.println("ERROR");
            return -1;
        }

        private int getLeftY(int y) {
            switch (direction) {
                case NORTH:
                    return y;
                case EAST:
                    return y + 1;
                case SOUTH:
                    return y;
                case WEST:
                    return y - 1;
            }
            System.out.println("ERROR");
            return -1;
        }

        public boolean checkFront() {
            int newX = getNewX(x);
            int newY = getNewY(y);
            int newRightX = getRightX(newX);
            int newRightY = getRightY(newY);
            int newLeftX = getLeftX(newX);
            int newLeftY = getLeftY(newY);
            if (!exploredMap.checkValidCoordinates(newY, newX) || !exploredMap.checkValidCoordinates(newRightX, newRightY)
                || !exploredMap.checkValidCoordinates(newLeftX, newLeftY) || exploredMap.isObstacleCell(newY, newX) ||
                exploredMap.isObstacleCell(newRightX, newRightY) || exploredMap.isObstacleCell(newLeftX, newLeftY)) {
                return true;
            }
            return false;
        }
    }

    private String getNewMovement2(String s) {
        YkRobot ykRobot = new YkRobot(1, 1, YkDirection.EAST);
        StringBuilder res = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == 'J') {
                ykRobot.rotLeft();
                res.append(c);
            } else if (c == 'K') {
                ykRobot.rotRight();
                res.append(c);
            } else {
                System.out.println("Num steps: " + getNumSteps(c));
                ykRobot.moveForward(getNumSteps(c));
                if (ykRobot.checkFront()) {
                    res.append("L");
                } else {
                    res.append(c);
                }
            }
            System.out.println("Yk position: " + ykRobot.x + ", " + ykRobot.y);
        }
        System.out.println("Yk position: " + ykRobot.x + ", " + ykRobot.y);
        return res.toString();
    }

    private char compressMovement(int steps) {
        switch (steps) {
            case 1:
                return 'R';
            case 2:
                return 'T';
            case 3:
                return 'Y';
            case 4:
                return 'U';
            case 5:
                return 'I';
            case 6:
                return 'O';
            case 7:
                return 'P';
            case 8:
                return 'F';
            case 9:
                return 'G';
            case 10:
                return 'H';
            case 11:
                return 'Z';
            case 12:
                return 'X';
            case 13:
                return 'C';
            case 14:
                return 'V';
            case 15:
                return 'B';
            case 16:
                return 'N';
            case 17:
                return 'M';
            case 18:
                return '<';
            case 19:
                return '>';
            case 20:
                return '?';
        }

        System.out.println("ERROR MOVEMENT!!");
        return '!';
    }

    private int getNumSteps(char c) {
        switch (c) {
            case 'R':
                return 1;
            case 'T':
                return 2;
            case 'Y':
                return 3;
            case 'U':
                return 4;
            case 'I':
                return 5;
            case 'O':
                return 6;
            case 'P':
                return 7;
            case 'F':
                return 8;
            case 'G':
                return 9;
            case 'H':
                return 10;
            case 'Z':
                return 11;
            case 'X':
                return 12;
            case 'C':
                return 13;
            case 'V':
                return 14;
            case 'B':
                return 15;
            case 'N':
                return 16;
            case 'M':
                return 17;
            case '<':
                return 18;
            case '>':
                return 19;
            case '?':
                return 20;
        }

        System.out.println("ERROR MOVEMENT!!");
        return -1;
    }

    /**
     * Returns true if the robot can move forward one cell with the current heading.
     */
    private boolean canMoveForward() {
        int row = bot.getRobotPosRow();
        int col = bot.getRobotPosCol();

        switch (bot.getRobotCurDir()) {
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
                }
        }
        return MOVEMENT.ERROR;
    }

    /**
     * Prints the fastest path from the Stack object.
     */
    private void printFastestPath(Stack<Cell> path) {
        System.out.println("\nLooped " + loopCount + " times.");
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

    /**
     * Prints all the current g(n) values for the cells.
     */
    public void printGCosts() {
        for (int i = 0; i < MapConstants.MAP_ROWS; i++) {
            for (int j = 0; j < MapConstants.MAP_COLS; j++) {
                System.out.print(gCosts[MapConstants.MAP_ROWS - 1 - i][j]);
                System.out.print(";");
            }
            System.out.println("\n");
        }
    }
}
