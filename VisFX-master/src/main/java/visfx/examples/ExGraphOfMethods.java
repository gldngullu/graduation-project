package visfx.examples;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import findCall.ListingAllMethods;
import findCall.MethodCallInformation;
import javafx.application.Application;
import javafx.stage.Stage;
import visfx.api.VisFx;
import visfx.graph.VisEdge;
import visfx.graph.VisGraph;
import visfx.graph.VisNode;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class ExGraphOfMethods extends Application {
    private HashMap<BigInteger, VisNode> nodesOfGraph = new HashMap<>();
    private HashMap<BigInteger, VisEdge> edgesOfGraph = new HashMap<>();
    private int nodeCount = 0;
    private ArrayList<String> watchNowDeleteLater = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Locale.setDefault(Locale.forLanguageTag("en"));
        VisFx.graphNetwork(getDummyGraph(),primaryStage);
    }

    private VisGraph getDummyGraph() throws Exception{
        ListingAllMethods listingAllMethods = new ListingAllMethods();
        listingAllMethods.findMethodCalls();
        ArrayList<MethodCallInformation> methodCalls = listingAllMethods.getAllMethodCallsInProject();
        VisGraph graph = new VisGraph();

        for (int i = 0; i < methodCalls.size(); i++) {
            MethodCallInformation tempMethod = methodCalls.get(i);
            Node caller = tempMethod.getParentNode();
            String sourceNodeLabel = "";
            String sourceNodeKeyString = "";
            if(caller instanceof MethodDeclaration){
                sourceNodeLabel =  ((MethodDeclaration)caller).resolve().getClassName() + "\n" + ((MethodDeclaration) caller).getNameAsString();
                sourceNodeKeyString = sourceNodeLabel + getParamsAsString((MethodDeclaration)caller);
            }
            else if(caller instanceof ClassOrInterfaceDeclaration) {
                sourceNodeLabel = ((ClassOrInterfaceDeclaration) caller).getNameAsString();
                sourceNodeKeyString = sourceNodeLabel;
            }
            ResolvedMethodDeclaration targetMethod = tempMethod.getResolvedMethod();
            String targetNodeLabel = targetMethod.getClassName() + "\n" + targetMethod.getName();
            String targetNodeKeyString = targetNodeLabel + getParamsAsString(targetMethod);

            BigInteger sourceNodeKey = addNewNode(sourceNodeLabel + "()", sourceNodeKeyString);
            BigInteger targetNodeKey = addNewNode(targetNodeLabel + "()", targetNodeKeyString);

            String edgeKeyString = sourceNodeKeyString + " to " + targetNodeKeyString;
            BigInteger edgeKey = addNewEdge(edgeKeyString, sourceNodeKey, targetNodeKey);

            graph.addNodes(nodesOfGraph.get(sourceNodeKey), nodesOfGraph.get(targetNodeKey));
            graph.addEdges(edgesOfGraph.get(edgeKey));
        }

        return graph;
    }

    private String getParamsAsString(ResolvedMethodDeclaration methodDeclaration){
        StringBuilder parameters = new StringBuilder();
        for (int i = 0; i < methodDeclaration.getNumberOfParams(); i++) {
            parameters.append(methodDeclaration.getParam(i).getType() + "-");
        }
        return parameters.toString();
    }

    private String getParamsAsString(MethodDeclaration methodDeclaration){
        StringBuilder parameters = new StringBuilder();
        for (int i = 0; i < methodDeclaration.getParameters().size(); i++) {
            parameters.append(methodDeclaration.getParameter(i).getType() + "-");
        }
        return parameters.toString();
    }

    // Square calculationların parametrelerine bak
    private BigInteger addNewNode(String nodeLabel, String stringNodeKey){
        BigInteger tempKey = new BigInteger((stringNodeKey).getBytes());
        if(!nodesOfGraph.containsKey(tempKey)) {
            VisNode node = new VisNode(nodeCount++, nodeLabel);
            nodesOfGraph.put(tempKey, node);
        }
        return tempKey;
    }

    private BigInteger addNewEdge(String keyString, BigInteger sourceKey, BigInteger targetKey){
        BigInteger tempKey = new BigInteger(keyString.getBytes());
        if(!edgesOfGraph.containsKey(tempKey)) {
            VisEdge edge = new VisEdge(nodesOfGraph.get(sourceKey), nodesOfGraph.get(targetKey), "to", "");
            edgesOfGraph.put(tempKey, edge);
        }
        return tempKey;
    }
}
