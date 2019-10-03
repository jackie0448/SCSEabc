package robot;

/**
 * Constants used in this package.
 *
 * @author Suyash Lakhotia
 */

public class RobotConstants {
    public static final int GOAL_ROW = 18;                          // row no. of goal cell
    public static final int GOAL_COL = 13;                          // col no. of goal cell
    public static final int START_ROW = 1;                          // row no. of start cell
    public static final int START_COL = 1;                          // col no. of start cell
    public static final int MOVE_COST = 5;                         // cost of FORWARD, BACKWARD movement
    public static final int TURN_COST = 10;                         // cost of RIGHT, LEFT movement
    public static final int DIAGONAL_COST=5;
    public static final int SPEED = 100;                            // delay between movements (ms)
    public static final DIRECTION START_DIR = DIRECTION.EAST;      // start direction
    //public static final int SENSOR_SHORT_RANGE_L = 1;               // range of short range sensor (cells)
    //public static final int SENSOR_SHORT_RANGE_H = 2;               // range of short range sensor (cells)
    //public static final int SENSOR_LONG_RANGE_L = 1;                // range of long range sensor (cells)
    //public static final int SENSOR_LONG_RANGE_H = 5;                // range of long range sensor (cells)

    
    public static final int SENSOR_SHORT_RANGE_L = 1;               // range of short range sensor (cells)
    public static final int SENSOR_SHORT_RANGE_H = 2;               // range of short range sensor (cells)
    public static final int SENSOR_LONG_RANGE_L = 1;                // range of long range sensor (cells)
    public static final int SENSOR_LONG_RANGE_H = 5;                // range of long range sensor (cells)

    public static final int INFINITE_COST = 9999;

    public enum DIRECTION {
        NORTH, EAST, SOUTH, WEST,NORTHEAST,SOUTHEAST, SOUTHWEST,NORTHWEST;

        public static DIRECTION getNext(DIRECTION curDirection) {
            return values()[(curDirection.ordinal() + 1) % (values().length-4)];
        }

        public static DIRECTION getPrevious(DIRECTION curDirection) {
            return values()[(curDirection.ordinal() + values().length - 1) % (values().length-4)];
        }

        /*public static char prnt(DIRECTION d) {
            switch (d) {
                case NORTH:
                    return 'N';
                case EAST:
                    return 'E';
                case SOUTH:
                    return 'S';
                case WEST:
                    return 'W';
                default:
                    return 'X';
            }
        }*/
    }
   

    public enum MOVEMENT {
        FORWARD, BACKWARD,DIAGONALEFT,DIAGONALRIGHT, RIGHT, LEFT,CALIBRATE, ERROR, ALIGN_FRONT, ALIGN_RIGHT, SENSE, RIGHT_NO_SENSE, LEFT_NO_SENSE, INITIAL_CALIBRATION;

        public static String print(MOVEMENT m) {
            switch (m) {
                case FORWARD:
                    return "1";
                case BACKWARD:  //move 180 right
                    return "s";
                case RIGHT:		// move 90 right
                    return "d";
                case LEFT:		//move 90 left
                    return "a";
                case DIAGONALEFT: //move 45 left
                	return "q";
                case DIAGONALRIGHT: //move 45 right
                	return "e";
                	
                /*case CALIBRATERIGHT: 
                    return "R";	
                case CALIBRATELEFT:
                	return"L";*/
                case CALIBRATE:
                	return "c";
                	/*
                case RIGHT_NO_SENSE:
                    return 'K';
                case LEFT_NO_SENSE:
                    return 'J';
                case CALIBRATE:
                    return 'S';
                case ALIGN_FRONT:
                    return 'E';
                case ALIGN_RIGHT:
                    return 'Q';
                case SENSE:
                    return 'Z';
                case INITIAL_CALIBRATION:
                    return 'S';
                    */
                case ERROR:
                default:
                    return "E";
                    
            }
        }
    }

    public enum FAST_MOVEMENT {
        LEFT, RIGHT, HUG, FF1, FF2, FF3, FF4, FF5, FF6, FF7, FF8, FF9, FF10;

        public static char print(FAST_MOVEMENT m) {
            switch (m) {
            	
                case LEFT:
                    return 'J';
                case RIGHT:
                    return 'K';
                case HUG:
                    return 'L';
                case FF1:
                    return 'R';
                case FF2:
                    return 'T';
                case FF3:
                    return 'Y';
                case FF4:
                    return 'U';
                case FF5:
                    return 'I';
                case FF6:
                    return 'O';
                case FF7:
                    return 'P';
                case FF8:
                    return 'F';
                case FF9:
                    return 'G';
                case FF10:
                    return 'H';
                default:
                    return 'E'; // TODO: Shouldn't reach this case
            }
        }
    }
}
