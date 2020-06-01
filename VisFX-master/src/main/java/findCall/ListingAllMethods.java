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
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class ListingAllMethods {

    private static final String PACKAGE_PATH
            = "C:\\Users\\gldng\\OneDrive\\Belgeler\\GitHub\\graduation-project\\VisFX-master\\src\\main\\java";

    private ArrayList<MethodCallInformation> allMethodCallsInProject = new ArrayList<>();
    private ArrayList<CompilationUnit> parsedClasses = new ArrayList<>();
    private CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
    private ArrayList<String> jarFiles = new ArrayList<>();
    private static int numberOfResolveErrors = 0;

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.forLanguageTag("en"));
        ListingAllMethods exClass = new ListingAllMethods();
        exClass.findJarFilesInDirectory("C:\\Program Files\\Java");
        exClass.findJarFilesInDirectory("C:\\Users\\gldng\\.m2\\repository");
        String directoryPath = "C:\\Users\\gldng\\OneDrive\\Belgeler\\GitHub\\graduation-project\\VisFX-master\\src\\main\\java";
        //exClass.findMethodCalls(directoryPath); dont forget meee
        System.out.println("Unsolved methods:" + numberOfResolveErrors);
    }

    public ArrayList<MethodCallInformation> findMethodCalls(){

        MethodCallFinder methodCallFinder = new MethodCallFinder();

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        setTypeSolver();

        for (CompilationUnit parsedClass : parsedClasses) {
            methodCallFinder.visit(parsedClass, null);
        }
        return allMethodCallsInProject;
    }

    public void findJarFilesInDirectory(String path) {
        File directory = new File(path);
        for (File file: Objects.requireNonNull(directory.listFiles())) {
            if(file.getName().endsWith(".jar"))
                jarFiles.add(file.getPath());
            else if(file.isDirectory()) {
                findJarFilesInDirectory(file.getPath());
            }
        }
    }

    private void setTypeSolver(){

        /*
        URL classUrl;
        classUrl = new URL("file:///C:/Users/gldng/IdeaProjects/DatabaseBrowser/out/production/DatabaseBrowser/");
        URL[] classUrls = { classUrl };
        URLClassLoader ucl = new URLClassLoader(classUrls);
        Class c = ucl.loadClass("sample.DatabaseController");
         */

        try {
            for (String path : jarFiles) {
                combinedTypeSolver.add(new JarTypeSolver(path));
            }
        } catch (Exception io){
            System.out.println("Oops");
        }
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(PACKAGE_PATH));
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
                ResolvedMethodDeclaration resolvedMethod = methodCallExpr.resolve();
                allMethodCallsInProject.add(
                        new MethodCallInformation(methodCallExpr, resolvedMethod, caller, methodCallExpr.getRange().get().begin.line));
            }catch (UnsupportedOperationException ex){
                System.out.println("Unsupported: " + methodCallExpr.toString());
                numberOfResolveErrors++;
            }catch (UnsolvedSymbolException ex) {
                System.out.println("Unsolved: " + methodCallExpr.toString());
                numberOfResolveErrors++;
            }catch (RuntimeException ex) {
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

    public ArrayList<MethodCallInformation> getAllMethodCallsInProject() {
        return allMethodCallsInProject;
    }

    public ArrayList<CompilationUnit> getParsedClasses() {
        return parsedClasses;
    }
}
