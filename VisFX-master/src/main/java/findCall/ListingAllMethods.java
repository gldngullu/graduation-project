package findCall;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class ListingAllMethods {

    private static String packagePath;
    private ArrayList<MethodCallInformation> allMethodCallsInProject = new ArrayList<>();
    private ArrayList<CompilationUnit> parsedClasses = new ArrayList<>();
    private JavaParserFacade javaParserFacade;
    private ArrayList<String> jarFiles = new ArrayList<>();
    private static int numberOfResolveErrors = 0;

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.forLanguageTag("en"));
        packagePath = "C:\\Users\\gldng\\IdeaProjects\\button\\src";
        ListingAllMethods exClass = new ListingAllMethods();
        exClass.findJavaFiles(packagePath);
        exClass.findJarFilesInDirectory("C:\\Program Files\\Java");
        //exClass.findJarFilesInDirectory("C:\\Users\\gldng\\.m2\\repository");
        exClass.findMethodCalls();
        System.out.println("Unsolved methods:" + numberOfResolveErrors);
    }

    public ArrayList<MethodCallInformation> findMethodCalls(){

        MethodCallFinder methodCallFinder = new MethodCallFinder();

        setTypeSolver();

        for (CompilationUnit parsedClass : parsedClasses) {
            methodCallFinder.visit(parsedClass, null);
        }
        return allMethodCallsInProject;
    }

    public void findJarFilesInDirectory(String path) {
        File directory = new File(path);
        File[] directoryFiles = directory.listFiles();
        if(directoryFiles.length == 0)
            return;
        for (File file : directory.listFiles()) {
            if (file.getName().endsWith(".jar"))
                jarFiles.add(file.getPath());
            else if (file.isDirectory()) {
                findJarFilesInDirectory(file.getPath());
            }
        }
    }

    private void setTypeSolver(){
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        try {
            for (String path : jarFiles) {
                combinedTypeSolver.add(new JarTypeSolver(path));
            }
        } catch (Exception io){
            System.out.println("Oops");
        }
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(packagePath));
        javaParserFacade = JavaParserFacade.get(combinedTypeSolver);
    }

    public void findJavaFiles(String filePath){
        File directory = new File(filePath);
        try {
            File[] allFilesInDirectory = directory.listFiles();
            for (File file : allFilesInDirectory) {
                if (file.getName().endsWith(".java")){
                    CompilationUnit compilationUnit = StaticJavaParser.parse(file);
                    parsedClasses.add(compilationUnit);
                }
                else if (file.isDirectory()) {
                    findJavaFiles(file.getPath());
                }
            }
        }catch (Exception ex){
            System.out.println("Error finding source code files");
        }
    }

    private class MethodCallFinder extends VoidVisitorAdapter<Void> {

        @Override
        public void visit(MethodCallExpr methodCallExpr, Void arg) {
            super.visit(methodCallExpr, arg);
            Node caller = getCallerOfMethodCall(methodCallExpr);
            try {
                ResolvedMethodDeclaration resolvedMethod = javaParserFacade.solve(methodCallExpr).getCorrespondingDeclaration();
                allMethodCallsInProject.add(
                        new MethodCallInformation(methodCallExpr, resolvedMethod, caller, methodCallExpr.getRange().get().begin.line));
            }catch (UnsupportedOperationException ex){
                System.out.println("Unsupported: " + methodCallExpr.toString());
                numberOfResolveErrors++;
            }catch (UnsolvedSymbolException ex) {
                System.out.println("Unsolved: " + methodCallExpr.toString());
                numberOfResolveErrors++;
            }catch (RuntimeException ex) {
                if(numberOfResolveErrors == 1)
                    ex.printStackTrace();
                System.out.println("Runtime: " + methodCallExpr.toString());
                numberOfResolveErrors++;
            }
        }
    }

    private Node getCallerOfMethodCall(MethodCallExpr methodCall){
        Node caller =  methodCall.getParentNode().get();
        while(!(caller instanceof MethodDeclaration)){
            if(caller instanceof ClassOrInterfaceDeclaration)
                return caller;
            caller = caller.getParentNode().get();
        }
        return caller;
    }

    public ArrayList<CompilationUnit> getParsedClasses() {
        return parsedClasses;
    }

    public static void setPackagePath(String packagePath) {
        ListingAllMethods.packagePath = packagePath;
    }
}
