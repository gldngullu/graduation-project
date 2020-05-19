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
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import javax.lang.model.type.ArrayType;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class ListingAllMethods {

    private static final String PACKAGE_PATH = "C:\\Users\\gldng\\OneDrive\\Belgeler\\GitHub\\graduation-project\\VisFX-master\\src\\main\\java";

    private ArrayList<MethodDeclaration> allMethodsInProject = new ArrayList<>();
    private ArrayList<MethodCallInformation> allMethodCallsInProject = new ArrayList<>();
    private static ClassOrInterfaceDeclaration currentClass;
    private  HashMap<BigInteger, MethodDeclaration> methodMapping = new HashMap<>();
    private static ArrayList<VariableDeclarator> currentClassDeclarations = new ArrayList<>();
    private static ArrayList<String> deleteMeMethods = new ArrayList<>();
    private static ArrayList<String> deleteMeMethodCalls = new ArrayList<>();


    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.forLanguageTag("en"));
        ListingAllMethods exClass = new ListingAllMethods();
        exClass.findMethodCalls();
    }

    public void findMethodCalls() throws Exception{
        String directoryPath = "C:\\Users\\gldng\\OneDrive\\Belgeler\\GitHub\\graduation-project\\VisFX-master\\src\\main\\java\\example_classes";
        ArrayList<File> classFilesInDirectory = fileFinder(directoryPath);

        /*Temporary lines
        ArrayList<File> classFilesInDirectory = new ArrayList<>();
        classFilesInDirectory.add(new File("C:\\Users\\gldng\\OneDrive\\Belgeler\\GitHub\\graduation-project\\VisFX-master\\src\\main\\java\\example_classes"));
         */

        VoidVisitor<?> methodFinder = new MethodFinder();
        MethodCallFinder methodCallFinder = new MethodCallFinder();
        VariableDeclarationFinder declarationFinder = new VariableDeclarationFinder();

        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(PACKAGE_PATH));

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

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
        }
        for (File classFile: classFilesInDirectory) {
            CompilationUnit compilationUnit = StaticJavaParser.parse(classFile);
            methodCallFinder.visit(compilationUnit, null);
        }
    }

    public ArrayList<File> fileFinder(String filePath){

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

    private class MethodCallFinder extends VoidVisitorAdapter<Void> {

        @Override
        public void visit(MethodCallExpr methodCallExpr, Void arg) {
            super.visit(methodCallExpr, arg);
            /*
            String keyString = methodCallExpr.getNameAsString() + "-" +
                                findClassOfMethodCall(methodCallExpr) +
                                findParameterOfMethodCall(methodCallExpr);
                                class bulma methodundan tireyi sildin eklemeyi unutma
             */
            String keyString = methodCallExpr.resolve().getQualifiedSignature();
            deleteMeMethodCalls.add(keyString);
            BigInteger tempKey = new BigInteger(keyString.getBytes());

            if(methodMapping.get(tempKey) != null)
                allMethodCallsInProject.add(
                        new MethodCallInformation(methodCallExpr, methodCallExpr.getRange().get().begin.line, methodMapping.get(tempKey)));
            else
                allMethodCallsInProject.add(
                        new MethodCallInformation(methodCallExpr, methodCallExpr.getRange().get().begin.line));
        }
    }

    private String findParameterOfMethodCall(MethodCallExpr methodCallExpr){
        if (methodCallExpr.getArguments().isEmpty())
            return "None";
        else
            return "Blabla";
    }

    //Bu bilgiyi tutmaya gerek var mı?
    private String findClassOfMethodCall(MethodCallExpr methodCall){
        if(methodCall.getScope().isPresent()){
            Node tempNode = methodCall.getScope().get();
            while (!tempNode.getChildNodes().isEmpty()){
                tempNode = tempNode.getChildNodes().get(0);
            }
            String scope = tempNode.toString();
            if(scope.equals("this"))
                return currentClass.getNameAsString();
            for (int i = 0; i < currentClassDeclarations.size(); i++) {
                if(currentClassDeclarations.get(i).getNameAsString().equals(scope)){
                    return ((ClassOrInterfaceType)currentClassDeclarations.get(i).getType()).getNameAsString();
                }
            }
            return "Java";
        } else
            return currentClass.getNameAsString();
    }

    private class VariableDeclarationFinder extends VoidVisitorAdapter<Void>{

        @Override
        public void visit(VariableDeclarator variableDeclarator, Void arg) {
            super.visit(variableDeclarator, arg);
            if (variableDeclarator.getType().getMetaModel().getTypeName().equals("ClassOrInterfaceType"))
                currentClassDeclarations.add(variableDeclarator);
        }
    }

    private class MethodFinder extends VoidVisitorAdapter<Void> {

        @Override
        public void visit(MethodDeclaration methodDeclaration, Void arg) {
            super.visit(methodDeclaration, arg);
            allMethodsInProject.add(methodDeclaration);
            /*
            String keyString =
                    methodDeclaration.getNameAsString() + "-" +
                            ((ClassOrInterfaceDeclaration)methodDeclaration.getParentNode().get()).getName() + "-" +
                            getMethodParameterTypesAsString(methodDeclaration);
             */
            String keyString = methodDeclaration.resolve().getQualifiedSignature();
            deleteMeMethods.add(keyString);
            BigInteger tempKey = new BigInteger(keyString.getBytes());
            methodMapping.put(tempKey, methodDeclaration);
        }
    }

    private String getMethodParameterTypesAsString(MethodDeclaration method){
        NodeList<Parameter> methodParameters = method.getParameters();
        if(methodParameters.size() == 0)
            return "None";
        StringBuilder parameterTypes = new StringBuilder();
        for (Parameter parameter: method.getParameters()){
            parameterTypes.append(parameter.getType().getElementType().toString());
            if(parameter instanceof ArrayType)
                parameterTypes.append("[]");
            parameterTypes.append("-");
        }
        return parameterTypes.toString().substring(0, parameterTypes.length()-1);
    }

    public ArrayList<MethodCallInformation> getAllMethodCallsInProject() {
        return allMethodCallsInProject;
    }

    public ArrayList<MethodDeclaration> getAllMethodsInProject() {
        return allMethodsInProject;
    }

}
