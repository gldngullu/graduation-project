package visfx.examples;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import findCall.MethodCallInformation;
import visfx.graph.VisEdge;
import visfx.graph.VisGraph;
import visfx.graph.VisNode;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

//TODO: Duplicate nodes exist, check them

public class CreateGraph {
    private HashMap<BigInteger, VisNode> nodesOfGraph;
    private HashMap<BigInteger, VisEdge> edgesOfGraph;
    private int nodeCount;
    private ArrayList<String> compareResolved = new ArrayList<>();
    private ArrayList<String> compareMethod = new ArrayList<>();



    public VisGraph buildGraph(ArrayList<MethodCallInformation> methodCalls) {
        VisGraph graph = new VisGraph();
        nodesOfGraph = new HashMap<>();
        edgesOfGraph = new HashMap<>();
        nodeCount = 0;

        for (MethodCallInformation tempMethod : methodCalls) {
            Node caller = tempMethod.getParentNode();
            String sourceNodeLabel = "";
            String sourceNodeKeyString = "";
            if (caller instanceof MethodDeclaration) {
                sourceNodeLabel = getClassAsString(caller) + "\n" + ((MethodDeclaration) caller).getNameAsString();
                sourceNodeKeyString = sourceNodeLabel + getParamsAsString((MethodDeclaration) caller);
            } else if (caller instanceof ClassOrInterfaceDeclaration) {
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
            String param = methodDeclaration.getParam(i).describeType();
            if(param.contains(".")) {
                parameters.append(fixParamString(param) + "-");
                compareResolved.add(fixParamString(param));
            }else {
                parameters.append(param + "-");
                compareMethod.add(param);
            }
        }
        return parameters.toString();
    }

    private String fixParamString(String parameter){
        if(parameter.matches("[a-zA-Z|.]+[<][a-zA-Z|.]+[>]")){
            String[] strings = parameter.split("<");
            return removeDotNotation(strings[0]) + "<" + removeDotNotation(strings[1].substring(0, strings[1].length()-1));
        }else
            return removeDotNotation(parameter);
    }

    private String removeDotNotation(String dottedString){
        String[] strings = dottedString.split("\\.");
        return strings[strings.length-1];
    }

    private String getParamsAsString(MethodDeclaration methodDeclaration){
        StringBuilder parameters = new StringBuilder();
        for (int i = 0; i < methodDeclaration.getParameters().size(); i++) {
            parameters.append(methodDeclaration.getParameter(i).getType() + "-");
            compareMethod.add(methodDeclaration.getParameter(i).getType().asString());
        }
        return parameters.toString();
    }

    private String getClassAsString(Node method){
        if(method.getParentNode().get() instanceof ClassOrInterfaceDeclaration)
            return ((ClassOrInterfaceDeclaration) method.getParentNode().get()).getNameAsString();
        else
            return getClassAsString(method.getParentNode().get());
    }

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
