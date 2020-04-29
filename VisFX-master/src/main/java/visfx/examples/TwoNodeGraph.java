package visfx.examples;

import visfx.api.VisFx;
import visfx.graph.VisEdge;
import visfx.graph.VisGraph;
import visfx.graph.VisNode;
import javafx.application.Application;
import javafx.stage.Stage;

public class TwoNodeGraph extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        VisFx.graphNetwork(getDummyGraph(),primaryStage);
    }

    private VisGraph getDummyGraph(){
        VisGraph graph = new VisGraph();
        VisNode node1 = new VisNode(1,"a");
        VisNode node2 = new VisNode(2,"b");
        VisEdge edge = new VisEdge(node1,node2,"to","part_of");
        System.out.println(edge.getArrows());
        graph.addNodes(node1,node2);
        graph.addEdges(edge);
        return graph;
    }
}
