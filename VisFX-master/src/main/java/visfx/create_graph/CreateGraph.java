package visfx.create_graph;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import analyze_project.MethodCallInformation;
import visfx.graph.VisEdge;
import visfx.graph.VisGraph;
import visfx.graph.VisNode;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class CreateGraph {
    private HashMap<BigInteger, VisNode> nodesOfGraph;
    private HashMap<BigInteger, VisEdge> edgesOfGraph;
    private int nodeCount;
    private ArrayList<String> compareResolved = new ArrayList<>();
    private ArrayList<String> compareMethod = new ArrayList<>();
    private HashMap<BigInteger, String> uniqueClassChecker;



    public VisGraph buildGraph(ArrayList<MethodCallInformation> methodCalls, ArrayList<String> projectClasses){
        VisGraph graph = new VisGraph();
        nodesOfGraph = new HashMap<>();
        edgesOfGraph = new HashMap<>();
        uniqueClassChecker = new HashMap<>();
        nodeCount = 0;
        System.out.println("Method calls: " + methodCalls.size());

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
            }else if (caller instanceof EnumDeclaration) {
                sourceNodeLabel = ((EnumDeclaration) caller).getNameAsString();
                sourceNodeKeyString = sourceNodeLabel;
            } else if(caller instanceof ConstructorDeclaration){
                sourceNodeLabel = ((ConstructorDeclaration) caller).getNameAsString() + "\n" + ((ConstructorDeclaration) caller).getNameAsString();
                sourceNodeKeyString = sourceNodeLabel;
            }
            ResolvedMethodDeclaration targetMethod = tempMethod.getResolvedMethod();
            String targetNodeLabel = targetMethod.getClassName() + "\n" + targetMethod.getName();
            String targetNodeKeyString = targetNodeLabel + getParamsAsString(targetMethod);

            BigInteger sourceNodeKey = addNewNode(sourceNodeLabel + "()", sourceNodeKeyString, false);
            BigInteger targetNodeKey;
            if(projectClasses.contains(targetMethod.getPackageName()+ "." + targetMethod.getClassName()))
                targetNodeKey = addNewNode(targetNodeLabel + "()", targetNodeKeyString, false);
            else
                targetNodeKey = addNewNode(targetNodeLabel + "()", targetNodeKeyString, true);
            String edgeKeyString = sourceNodeKeyString + " to " + targetNodeKeyString;
            BigInteger edgeKey = addNewEdge(edgeKeyString, sourceNodeKey, targetNodeKey);

            graph.addNodes(nodesOfGraph.get(sourceNodeKey), nodesOfGraph.get(targetNodeKey));
            graph.addEdges(edgesOfGraph.get(edgeKey));
        }
        System.out.println("Nodes: " + nodesOfGraph.size());
        System.out.println("Edges: " + edgesOfGraph.size());
        return graph;
    }

    private void fixUniqueClassName(){

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
        else if(method.getParentNode().get() instanceof EnumDeclaration){
            return ((EnumDeclaration) method.getParentNode().get()).getNameAsString();
        }else
            return getClassAsString(method.getParentNode().get());
    }

    private BigInteger addNewNode(String nodeLabel, String stringNodeKey, Boolean isExternalMethod){
        BigInteger tempKey = new BigInteger((stringNodeKey).getBytes());
        if(!nodesOfGraph.containsKey(tempKey)) {
            VisNode node;
            if(isExternalMethod)
                node = new VisNode(nodeCount++, nodeLabel, "LibMethod");
            else
                node = new VisNode(nodeCount++, nodeLabel, "ProjectMethod");
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