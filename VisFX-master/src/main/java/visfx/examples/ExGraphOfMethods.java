package visfx.examples;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import findCall.ListingAllMethods;
import findCall.MethodCallInformation;
import javafx.application.Application;
import javafx.stage.Stage;
import javassist.compiler.ast.MethodDecl;
import javassist.compiler.ast.Visitor;
import javassist.expr.MethodCall;
import visfx.api.VisFx;
import visfx.graph.VisEdge;
import visfx.graph.VisGraph;
import visfx.graph.VisNode;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class ExGraphOfMethods extends Application {
    private HashMap<BigInteger, VisNode> nodesOfGraph = new HashMap<>();
    private HashMap<BigInteger, VisEdge> edgesOfGraph = new HashMap<>();
    private int nodeCount = 0;

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
            String sourceNodeLabel = "";
            if(caller instanceof MethodDeclaration)
                sourceNodeLabel =  getClass(caller).getNameAsString() + "\n" + ((MethodDeclaration) caller).getNameAsString();
            else if(caller instanceof ClassOrInterfaceDeclaration)
                sourceNodeLabel = ((ClassOrInterfaceDeclaration) caller).getNameAsString();

            String targetNodeLabel;
            if(tempMethod.getActualMethodCalled() != null){
                targetNodeLabel = getClass(tempMethod.getActualMethodCalled()).getNameAsString() + "\n" + tempMethod.getMethodCall().getName().asString();
            } else {
                targetNodeLabel = tempMethod.getMethodCall().getName().asString();
            }

            BigInteger sourceNodeKey = addNewNode(sourceNodeLabel);
            BigInteger targetNodeKey = addNewNode(targetNodeLabel + "()");

            String edgeKeyString = sourceNodeLabel + " to " + targetNodeLabel;
            BigInteger edgeKey = addNewEdge(edgeKeyString, sourceNodeKey, targetNodeKey);

            graph.addNodes(nodesOfGraph.get(sourceNodeKey), nodesOfGraph.get(targetNodeKey));
            graph.addEdges(edgesOfGraph.get(edgeKey));
        }

        return graph;
    }

    private BigInteger addNewNode(String nodeLabel){
        BigInteger tempKey = new BigInteger(nodeLabel.getBytes());
        if(!nodesOfGraph.containsKey(tempKey)) {
            VisNode node = new VisNode(nodeCount++, nodeLabel);
            nodesOfGraph.put(tempKey, node);
        }
        return tempKey;
    }

    private ClassOrInterfaceDeclaration getClass(Node node){
        Node classOfNode =  node.getParentNode().get();
        while(!(classOfNode instanceof ClassOrInterfaceDeclaration)){
            classOfNode = classOfNode.getParentNode().get();
        }
        return (ClassOrInterfaceDeclaration)classOfNode;
    }

    private BigInteger addNewEdge(String keyString, BigInteger sourceKey, BigInteger targetKey){
        BigInteger tempKey = new BigInteger(keyString.getBytes());
        if(!edgesOfGraph.containsKey(tempKey)) {
            VisEdge edge = new VisEdge(nodesOfGraph.get(sourceKey), nodesOfGraph.get(targetKey), "to", "");
            edgesOfGraph.put(tempKey, edge);
        }
        return tempKey;
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
