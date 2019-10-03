package cjfastestpath;

public class Node {

    public int cost; //g(h)
    public int total; //g+h
    public int[] coor = new int[2]; //[row,col]
    public Node parent; //preceding node coor

    public Node(int cost, int total, int[] coor, Node parent){
        this.cost=cost;
        this.total=total;
        this.coor=coor;
        this.parent=parent;
    }
}
