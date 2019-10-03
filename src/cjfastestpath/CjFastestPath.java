package cjfastestpath;

import utils.CommMgr;

import java.util.ArrayList;
import java.util.List;

public class CjFastestPath {

    PriorityQueue queue = new PriorityQueue();
    public int[][] exploredMap;

    int dir=2;

    public CjFastestPath(String mapState, int[][] exploredMap){
        this.exploredMap = exploredMap;

        // print explored map
        for (int i = 0; i < exploredMap.length; i++) {
            for (int j = 0; j < exploredMap[0].length; j++) {
                System.out.print(exploredMap[i][j] + " ");
            }
            System.out.println();
        }
    }

    public void wayPoint(int[] start, int[] wayPoint, int[] goal){
        List<Integer> path1 = fastestPath(start, wayPoint);
        queue.clear();
        List<Integer> path2 = fastestPath(wayPoint,goal);
        path2.remove(0);
        List<Integer> finalPath = new ArrayList<Integer>();
        finalPath.addAll(path1);
        finalPath.addAll(path2);

        //movement(calDirStep(finalPath));
        List<List<Integer>> sample = calDirStep(finalPath);
        for (List<Integer> i:sample) System.out.println(i.get(0)+" "+i.get(1));
        movement(sample);
    }

    public List<Integer> fastestPath(int[]startP, int[]goalP){ //pass in coor of start and goal
        Node prevNode;
        Node curNode = new Node(0,0, startP,null);
        exploredMap[startP[0]][startP[1]] = 1; //set as visited
        List<List<Integer>> nextMove;

        do {
                nextMove = getNextMove(curNode.coor,dir);

            try{
                if (nextMove.isEmpty()) curNode = queue.get();

                else {
                    //calculate g and h of next moves and put into the priority queue
                    for (List<Integer> i : nextMove) {
                        int row = i.get(0);
                        int col = i.get(1);
                        int[] coor = {row,col};
                        int cost = curNode.cost + dist(curNode.coor,coor);
                        int total = cost + dist(coor, goalP);
                        Node newNode = new Node(cost, total, coor, curNode);
                        queue.put(newNode);
                        exploredMap[row][col] = 1; //mark this box as alr in queue/visited
                    }
                    prevNode = curNode;
                    curNode = queue.get();
                    exploredMap[curNode.coor[0]][curNode.coor[1]] = 1;
                    //calDirStep(prevNode,curNode);
                    calDir(prevNode.parent,curNode);

                }
            }
            catch (Exception e){
                List<Integer> error = trace(curNode);
                for (Integer i:error) System.out.println(i);
            }
        }while (!((curNode.coor[0]==goalP[0]) && (curNode.coor[1]==goalP[1])));

        //using step list, cal turns
       //movement(calDirStep(curNode));
        return trace(curNode);
    }

    //movement to send. COMMUNICATION IS HERE!!!
    private void movement(List<List<Integer>> stepList){
        String[] stepIns = new String[]{"R","T","Y","U","I","O","P","F","G","H"};

        System.out.println("Step List size: " + stepList.size());
        StringBuilder movementList = new StringBuilder();
        for (int i =0;i<stepList.size()-1;i++){
            int numSteps = stepList.get(i).get(1);
            if (numSteps != 0) {
                if (numSteps > 10) {
                    movementList.append("H");
                }
                movementList.append(stepIns[(stepList.get(i).get(1) % 10) - 1]);
            }

            //then turn to get ready for the next steps
            if(stepList.get(i+1).get(0)-stepList.get(i).get(0)==1) {
                movementList.append("D");
            }
            else if (stepList.get(i+1).get(0)-stepList.get(i).get(0)==-1) {
                movementList.append("A");
            }
            else System.out.println("Error!");
        }

        CommMgr comm = CommMgr.getCommMgr();
        comm.sendMsg(movementList.toString(), CommMgr.INSTRUCTIONS);
    }

    private void turnLeft(){
        //send msg
    }
    private void turnRight(){
        //send msg
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
            nextMoves = new int[][]{{row-1,col},{row,col-1},{row+1,col},{row,col+1}};
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
    private List<List<Integer>> calDirStep(List<Integer> path){
        //List<Integer> path = trace(current);
        for (Integer x:path) System.out.println(x);

        int dir = 2;
        int steps = 0;
        List<List<Integer>> stepList = new ArrayList<List<Integer>>();
        for (int i =0;i<path.size()-1;i++){
            int nextDir = calDir(path.get(i),path.get(i+1));
            if (nextDir==dir) steps++;
            else{
                List<Integer> stepsInDIr = new ArrayList<Integer>();
                stepsInDIr.add(dir);
                stepsInDIr.add(steps);
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