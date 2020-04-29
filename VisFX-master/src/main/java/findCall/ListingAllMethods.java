package findCall;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class ListingAllMethods {

    private static ArrayList<MethodDeclaration> allMethodsInProject = new ArrayList<>();
    private static ArrayList<MethodCallInformation> allMethodCallsInProject = new ArrayList<>();
    private static ClassOrInterfaceDeclaration currentClass;
    private static HashMap<BigInteger, MethodDeclaration> methodMapping = new HashMap<>();
    private static ArrayList<VariableDeclarator> currentClassDeclarations = new ArrayList<>();
    private static ArrayList<String> deleteMeLater = new ArrayList<>();
    private static ArrayList<String> deleteMeAlso = new ArrayList<>();


    public static void main(String[] args) throws Exception {

        //directoryPath is the project path!
        String directoryPath = "C:\\Users\\gldng\\IdeaProjects\\JavaParserTesting";
        ArrayList<File> classFilesInDirectory = fileFinder(directoryPath);
        VoidVisitor<?> methodFinder = new MethodFinder();
        MethodCallFinder methodCallFinder = new MethodCallFinder();
        VariableDeclarationFinder declarationFinder = new VariableDeclarationFinder();

        for (File classFile: classFilesInDirectory) {
            CompilationUnit compilationUnit = StaticJavaParser.parse(classFile);
            for (Node node : compilationUnit.getChildNodes()) {
                if(node instanceof ClassOrInterfaceDeclaration) {
                    currentClass = (ClassOrInterfaceDeclaration) node;
                    break;
                }
            }
            declarationFinder.visit(currentClass, null);
            methodFinder.visit(compilationUnit, null);
            methodCallFinder.visit(compilationUnit, null);
        }
    }

    public static ArrayList<File> fileFinder(String filePath){

        ArrayList<File> classFilesInDirectory = new ArrayList<>();
        File directory = new File(filePath);

        File[] allFilesInDirectory = directory.listFiles();
        for (File file: allFilesInDirectory) {
            if(file.getName().endsWith(".java"))
                classFilesInDirectory.add(file);
            else if(file.isDirectory()) {
                classFilesInDirectory.addAll(fileFinder(file.getPath()));
            }
        }
        return classFilesInDirectory;
    }

    private static class MethodCallFinder extends VoidVisitorAdapter<Void> {

        @Override
        public void visit(MethodCallExpr methodCallExpr, Void arg) {
            super.visit(methodCallExpr, arg);
            String keyString = methodCallExpr.getNameAsString() + "-" +
                                findClassOfMethodCall(methodCallExpr) +
                                findParameterOfMethodCall(methodCallExpr);
            BigInteger tempKey = new BigInteger(keyString.getBytes());
            deleteMeAlso.add(keyString);
            if(methodMapping.get(tempKey) != null)
                allMethodCallsInProject.add(
                        new MethodCallInformation(currentClass, methodCallExpr, methodCallExpr.getRange().get().begin.line, methodMapping.get(tempKey)));
            else
                allMethodCallsInProject.add(
                        new MethodCallInformation(currentClass, methodCallExpr, methodCallExpr.getRange().get().begin.line));
        }
    }

    private static String findParameterOfMethodCall(MethodCallExpr methodCallExpr){
        if (methodCallExpr.getArguments().isEmpty())
            return "None";
        else
            return "Blabla";
    }

    private static String findClassOfMethodCall(MethodCallExpr methodCall){
        if(methodCall.getScope().isPresent()){
            Node tempNode = methodCall.getScope().get();
            while (!tempNode.getChildNodes().isEmpty()){
                tempNode = tempNode.getChildNodes().get(0);
            }
            String scope = tempNode.toString();
            if(scope.equals("this"))
                return currentClass.getName() + "-";
            for (int i = 0; i < currentClassDeclarations.size(); i++) {
                if(currentClassDeclarations.get(i).getTypeAsString().equals(scope)){
                    return scope + "-";
                }
            }
            return "Java-";
        } else
            return currentClass.getName() + "-";
    }

    private static class VariableDeclarationFinder extends VoidVisitorAdapter<Void>{

        @Override
        public void visit(VariableDeclarator variableDeclarator, Void arg) {
            super.visit(variableDeclarator, arg);
            if (variableDeclarator.getType().getMetaModel().getTypeName().equals("ClassOrInterfaceDeclaration"))
                currentClassDeclarations.add(variableDeclarator);
        }
    }

    private static class MethodFinder extends VoidVisitorAdapter<Void> {

        @Override
        public void visit(MethodDeclaration methodDeclaration, Void arg) {
            super.visit(methodDeclaration, arg);
            allMethodsInProject.add(methodDeclaration);
            String keyString =
                    methodDeclaration.getNameAsString() + "-" +
                            ((ClassOrInterfaceDeclaration)methodDeclaration.getParentNode().get()).getName() + "-" +
                            getMethodParameterTypesAsString(methodDeclaration);
            deleteMeLater.add(keyString);
            BigInteger tempKey = new BigInteger(keyString.getBytes());
            methodMapping.put(tempKey, methodDeclaration);
        }
    }

    private static String getMethodParameterTypesAsString(MethodDeclaration method){
        NodeList<Parameter> methodParameters = method.getParameters();
        if(methodParameters.size() == 0)
            return "None";
        StringBuilder parameterTypes = new StringBuilder();
        for (Parameter parameter: method.getParameters()){
            parameterTypes.append(parameter.getType().getElementType().toString());
            if(parameter.getType().getMetaModel().getTypeName().equals("ArrayType"))
                parameterTypes.append("[]");
            parameterTypes.append("-");
        }
        return parameterTypes.toString().substring(0, parameterTypes.length()-1);
    }


}