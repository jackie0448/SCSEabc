package cjfastestpath;

import java.util.ArrayList;
import java.util.List;

public class PriorityQueue {

    List<Node> queue = new ArrayList<>();

    public void put(Node node){

        int i;

        for(i=0;i<queue.size();i++){
            if (!queue.isEmpty()){
                if (node.total >= queue.get(i).total)
                    continue;
                else break;

            }else queue.add(node);
        }
        queue.add(i,node);
    }

    public Node get(){
        Node temp = queue.get(0);
        queue.remove(0);
        return temp;
    }

    public void clear(){
        queue.clear();
    }
}
