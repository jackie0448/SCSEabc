package map;

import java.awt.*;

/**
 * Constants used in the Map class for rendering the arena in the simulator.
 *
 * Changed the colors
 */

class GraphicsConstants {
    public static final int CELL_LINE_WEIGHT = 2;

    public static final Color C_START = Color.BLUE;
    public static final Color C_GOAL = Color.CYAN;
    public static final Color C_UNEXPLORED = Color.LIGHT_GRAY;
    public static final Color C_FREE = Color.WHITE; //before running exploration
    public static final Color C_OBSTACLE = Color.BLACK;
    public static final Color C_EXPLORED= Color.PINK;

    public static final Color C_ROBOT = Color.ORANGE;
    public static final Color C_ROBOT_DIR = Color.WHITE;

    public static final int ROBOT_W = 95;
    public static final int ROBOT_H = 95;

    public static final int ROBOT_X_OFFSET = 12;
    public static final int ROBOT_Y_OFFSET = 25;

    public static final int ROBOT_DIR_W = 12;
    public static final int ROBOT_DIR_H = 12;

    public static final int CELL_SIZE = 40;

    public static final int MAP_H = 750;
    public static final int MAP_X_OFFSET = 150;
}
