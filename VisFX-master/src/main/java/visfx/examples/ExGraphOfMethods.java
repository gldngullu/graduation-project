package visfx.examples;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import findCall.ListingAllMethods;
import findCall.MethodCallInformation;
import javafx.application.Application;
import javafx.stage.Stage;
import javassist.expr.MethodCall;
import visfx.api.VisFx;
import visfx.graph.VisEdge;
import visfx.graph.VisGraph;
import visfx.graph.VisNode;

import java.util.ArrayList;

public class ExGraphOfMethods extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        VisFx.graphNetwork(getDummyGraph(),primaryStage);
    }

    private VisGraph getDummyGraph() throws Exception{
        ListingAllMethods listingAllMethods = new ListingAllMethods();
        listingAllMethods.findMethodCalls();
        ArrayList<MethodCallInformation> methodCalls = listingAllMethods.getAllMethodCallsInProject();
        ArrayList<MethodDeclaration> methods = listingAllMethods.getAllMethodsInProject();

        ArrayList<VisNode> nodes = new ArrayList<>();
        ArrayList<VisEdge> edges = new ArrayList<>();

        VisGraph graph = new VisGraph();

        for (int i = 0; i < methodCalls.size(); i++) {
            String methodClass =
        }

        VisNode node1 = new VisNode(1,"a");
        VisNode node2 = new VisNode(2,"b");
        VisEdge edge = new VisEdge(node1,node2,"to","part_of");
        System.out.println(edge.getArrows());
        graph.addNodes(node1,node2);
        graph.addEdges(edge);
        return graph;
    }

    private ClassOrInterfaceDeclaration getClassOfMethod(){
        
    }
}
