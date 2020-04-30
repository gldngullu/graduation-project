package visfx.examples;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import findCall.ListingAllMethods;
import findCall.MethodCallInformation;
import javafx.application.Application;
import javafx.stage.Stage;
import javassist.compiler.ast.MethodDecl;
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
        VisGraph graph = new VisGraph();

        for (int i = 0; i < methodCalls.size(); i++) {
            MethodCallInformation tempMethod = methodCalls.get(i);
            Node caller = getCallerOfMethodCall(tempMethod);
            String nodeLabel = "";
            if(caller instanceof MethodDeclaration)
                nodeLabel = ((MethodDeclaration) caller).getNameAsString();
            else if(caller instanceof ClassOrInterfaceDeclaration)
                nodeLabel = ((ClassOrInterfaceDeclaration) caller).getNameAsString();
            VisNode from = new VisNode(2 * i, nodeLabel);
            VisNode to = new VisNode( (2 * i + 1) , tempMethod.getMethodCall().getName().asString());
            VisEdge edge = new VisEdge(from, to, "to", "");
            graph.addNodes(from,to);
            graph.addEdges(edge);
        }

        return graph;
    }

    private Node getCallerOfMethodCall(MethodCallInformation methodCall){
        Node caller =  methodCall.getMethodCall().getParentNode().get();
        while(!(caller instanceof MethodDeclaration)){
            if(caller instanceof ClassOrInterfaceDeclaration)
                return caller;
            caller = caller.getParentNode().get();
        }
        return caller;
    }

}
