package map;

/**
 * Represents each cell in the map grid.
 *
 * @author Suyash Lakhotia
 */

public class Cell {
    //private final int row;
    //private final int col;
	
	 private int row;
    private int col;
    private boolean isObstacle;
    private boolean isVirtualWall;
    private boolean isExplored;
    private boolean isAccurate = false;
    private int obstacleChanges = 0;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    
    //added

    public void setRow(int row) {
         this.row =row;
    }

    public void setCol( int col) {
        this.col = col;
    }
    
    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    public void setIsObstacle(boolean val) {
        if (val != this.isObstacle) obstacleChanges++;
        if (isPhantomWall()) {
            this.isObstacle = false;
        } else {
            this.isObstacle = val;
        }
    }

    public boolean getIsObstacle() {
        return this.isObstacle;
    }

    public void setVirtualWall(boolean val) {
        if (val) {
            this.isVirtualWall = true;
        } else {
            if (row != 0 && row != MapConstants.MAP_ROWS - 1 && col != 0 && col != MapConstants.MAP_COLS - 1) {
                this.isVirtualWall = false;
            }
        }
    }

    public boolean getIsVirtualWall() {
        return this.isVirtualWall;
    }

    public void setIsExplored(boolean val) {
        this.isExplored = val;
    }

    public boolean getIsExplored() {
        return this.isExplored;
    }

    public boolean isAccurate() {
        return isAccurate;
    }

    public void setAccurate(boolean accurate) {
        isAccurate = accurate;
    }

    public boolean isPhantomWall() {
        if (obstacleChanges >= 2) {
            System.out.println(String.format("Detected an Phantom wall at Row %d Col %d ,Num of Changes: ", row, col, obstacleChanges));
        }
        return obstacleChanges >= 2;
    }
}
