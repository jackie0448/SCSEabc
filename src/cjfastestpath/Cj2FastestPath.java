package cjfastestpath;

import utils.CommMgr;

import java.util.ArrayList;
import java.util.List;

public class Cj2FastestPath {

    PriorityQueue queue = new PriorityQueue();
    public int[][] exploredMap;

    int dir = 3;

    public Cj2FastestPath(String mapState, int[][] exploredMap) {
        this.exploredMap = exploredMap;
    }

    public void wayPoint(int[] start, int[] wayPoint, int[] goal) {
        List<Integer> path1 = fastestPath(start, wayPoint);
        queue.clear();
        for(int i=0; i<20; i++){
            for(int j=0; j<15; j++){
                if(exploredMap[i][j]==3)
                    exploredMap[i][j]=0;
            }
        }
        List<Integer> path2 = fastestPath(wayPoint, goal);
        path2.remove(0);
        List<Integer> finalPath = new ArrayList<Integer>();
        finalPath.addAll(path1);
        finalPath.addAll(path2);

        //movement(calDirStep(finalPath));
        List<List<Integer>> sample = calDirStep(finalPath);
        System.out.println("Trying to solve");
        for (List<Integer> i : sample) System.out.println(i.get(0) + " " + i.get(1));
        movement(sample);
    }

    public List<Integer> fastestPath(int[] startP, int[] goalP) { //pass in coor of start and goal
        Node curNode = new Node(0, 0, startP, null);
        exploredMap[startP[0]][startP[1]] = 3; //set as visited
        List<List<Integer>> nextMove;

        do {
            nextMove = getNextMove(curNode.coor, dir);

            try {
                if (nextMove.isEmpty()) curNode = queue.get();

                else {
                    //calculate g and h of next moves and put into the priority queue
                    for (List<Integer> i : nextMove) {
                        int row = i.get(0);
                        int col = i.get(1);
                        int[] coor = {row, col};
                        int cost = curNode.cost + dist(curNode.coor, coor);
                        int total = cost + dist(coor, goalP);
                        Node newNode = new Node(cost, total, coor, curNode);
                        queue.put(newNode);
                        exploredMap[row][col] = 3; //mark this box as alr in queue/visited
                    }
                    //prevNode = curNode;
                    curNode = queue.get();
                    exploredMap[curNode.coor[0]][curNode.coor[1]] = 3;
                    //calDirStep(prevNode,curNode);
                    //calDir(curNode.parent, curNode);
                }
                calDir(curNode.parent, curNode);
            } catch (Exception e) {
                List<Integer> error = trace(curNode);
                for (Integer i : error) System.out.println(i);
            }
        } while (!((curNode.coor[0] == goalP[0]) && (curNode.coor[1] == goalP[1])));

        //using step list, cal turns
        //movement(calDirStep(curNode));
        return trace(curNode);
    }

    //movement to send. COMMUNICATION IS HERE!!!
    private void movement(List<List<Integer>> stepList) {
        String[] stepIns = new String[]{"R", "T", "Y", "U", "I", "O", "P", "F", "G", "H"};
        String instruction = "";

        if (3 - stepList.get(0).get(0) == 1) instruction += "K"; //right

        for (int i = 0; i < stepList.size()-1; i++) {
            int step = stepList.get(i).get(1);
            if (step!=0){
                if (step==20) instruction += "L";
                else if (step>10) {instruction += "H";
                instruction += stepIns[step%10-1];}
                else instruction += stepIns[step-1];
            }

            //then turn to get ready for the next steps
           // if(i<stepList.size()-1){
            int next = stepList.get(i + 1).get(0);
            int now = stepList.get(i).get(0);
            if (next-now == 1 || next-now==-3) instruction += "K"; //right
            else if (next-now == -1 || next-now==3) instruction += "J";
            else if(Math.abs(next-now)==2) instruction += "KK";
            else System.out.println("Error!");//}
        }
        instruction += "L";
        System.out.print(instruction);

        try {
            CommMgr comm = CommMgr.getCommMgr();
            comm.sendMsg(instruction, CommMgr.INSTRUCTIONS);
        } catch (Exception e) {
        }
    }


    //fastest path algo
    private int dist(int[]child, int[]cor){
        return Math.abs(child[0]-cor[0])+Math.abs(child[1]-cor[1]);
    }
    private List<List<Integer>> getNextMove(int[]cur, int dir) {
        int[][] nextMoves;
        int row = cur[0];
        int col = cur[1];
        List<List<Integer>> possibleMoves = new ArrayList<List<Integer>>();

        if(dir==1){
            nextMoves = new int[][]{{row,col-1},{row+1,col},{row,col+1},{row-1,col}};
        }
        else if (dir==0){
            nextMoves = new int[][]{{row+1,col},{row,col+1},{row-1,col},{row,col-1}};
        }
        else if(dir==3){
            nextMoves = new int[][]{{row,col+1},{row-1,col},{row,col-1},{row+1,col}};
        }
        else{//dir ==2
            nextMoves = new int[][]{{row-1,col},{row,col+1},{row+1,col},{row,col-1}};
        }

        for(int[] i:nextMoves){
            row = i[0];
            col = i[1];

            //check if next next is !(blocked&visited) or out of map
            if ((row < 19) && (row>=1) && (col<14) && (col>=1)){
                //if ((exploredMap[row][col] == 0) || (exploredMap[row][col] ==4)) {
                if (exploredMap[row][col] == 0) {
                    List<Integer> coor = new ArrayList<>(); //maybe delete/clear the previous coor
                    coor.add(row);
                    coor.add(col);
                    possibleMoves.add(coor);
                }
            }
        }
        return possibleMoves;
    }
    private List<Integer> trace(Node node) {
        List<Integer> path = new ArrayList<>();
        int boxNumber;
        Node cur = node;
        do{
            boxNumber = Math.abs(cur.coor[0]-19)+cur.coor[1]*20;
            path.add(0,boxNumber);
            cur = cur.parent;
        }while (cur!=null);
        return path;
    }

    //cal the stepList
    public List<List<Integer>> calDirStep(List<Integer> path){
        //List<Integer> path = trace(current);
        for (Integer x:path) System.out.println(x);

        int dir = 3;
        int steps = 0;
        List<List<Integer>> stepList = new ArrayList<List<Integer>>();
        for (int i =0;i<path.size()-1;i++){
            int nextDir = calDir(path.get(i),path.get(i+1));
            if (nextDir==dir) steps++;
            else{
                boolean obstacle = false;
                int y = path.get(i)/20;
                int x = Math.abs(path.get(i)%20 - 19);
                //check if turn is due to obs
                if (dir==0){
                    if ((x+2)>19 || exploredMap[x+2][y]==1 || exploredMap[x+2][y-1]==1 || exploredMap[x+2][y+1]==1) obstacle = true;
                }
                else if (dir ==1){
                    if((y-2)<0 || exploredMap[x][y-2]==1||exploredMap[x-1][y-2]==1||exploredMap[x+1][y-2]==1) obstacle=true;
                }
                else if (dir==2){
                    if ((x-2)<0 ||exploredMap[x-2][y]==1||exploredMap[x-2][y-1]==1||exploredMap[x-2][y+1]==1) obstacle=true;
                }
                else if (dir==3){
                    if((y+2)>14 || exploredMap[x][y+2]==1||exploredMap[x-1][y+2]==1||exploredMap[x+1][y+2]==1) obstacle=true;
                }
                else obstacle=false;

                List<Integer> stepsInDIr = new ArrayList<Integer>();
                stepsInDIr.add(dir);
                if (obstacle) stepsInDIr.add(20);
                else stepsInDIr.add(steps);
                stepList.add(stepsInDIr);
                dir = nextDir;
                steps=1;
            }
        }
        List<Integer> stepsInDIr = new ArrayList<Integer>();
        stepsInDIr.add(dir);
        stepsInDIr.add(steps);
        stepList.add(stepsInDIr);
        return stepList;
    }
    private int calDir(int cur, int next){
        if (next-cur==1)return 2;
        else if(next-cur==-1) return 0;
        else if (next-cur==20) return 3;
        else return 1;
    }
    private void calDir(Node previous, Node current){
        int prevX = previous.coor[0];
        int prevY = previous.coor[1];
        int curX = current.coor[0];
        int curY = current.coor[1];

        if (curX-prevX==1) dir = 0;
        else if(curX-prevX==-1) dir =2;
        else if (curY-prevY==1) dir=3;
        else dir=1;
    }
}