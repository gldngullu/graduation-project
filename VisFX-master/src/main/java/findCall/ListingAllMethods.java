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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.jar.JarFile;

public class ListingAllMethods {

    private static final String PACKAGE_PATH
            = "C:\\Users\\gldng\\OneDrive\\Belgeler\\GitHub\\graduation-project\\VisFX-master\\src\\main\\java";

    private ArrayList<MethodCallInformation> allMethodCallsInProject = new ArrayList<>();
    private ArrayList<CompilationUnit> allClassesInProject = new ArrayList<>();
    private CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
    private ArrayList<String> jarFiles = new ArrayList<>();
    private static int numberOfResolveErrors = 0;

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.forLanguageTag("en"));
        ListingAllMethods exClass = new ListingAllMethods();
        exClass.findJarFiles("C:\\Program Files\\Java");
        exClass.findJarFiles("C:\\Users\\gldng\\.m2\\repository");
        String directoryPath = "C:\\Users\\gldng\\OneDrive\\Belgeler\\GitHub\\graduation-project\\VisFX-master\\src\\main\\java";
        exClass.findMethodCalls(directoryPath);
        System.out.println("Unsolved methods:" + numberOfResolveErrors);
    }

    public ArrayList<MethodCallInformation> findMethodCalls(String directoryPath) throws Exception{
        ArrayList<File> classFilesInDirectory = javaClassFinder(directoryPath);

        MethodCallFinder methodCallFinder = new MethodCallFinder();

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        setTypeSolver();

        for (File classFile: classFilesInDirectory) {
            CompilationUnit compilationUnit = StaticJavaParser.parse(classFile);
            allClassesInProject.add(compilationUnit);
            methodCallFinder.visit(compilationUnit, null);
        }

        return allMethodCallsInProject;
    }

    private void findJarFiles(String path) {
        File directory = new File(path);
        for (File file: Objects.requireNonNull(directory.listFiles())) {
            if(file.getName().endsWith(".jar"))
                jarFiles.add(file.getPath());
            else if(file.isDirectory()) {
                findJarFiles(file.getPath());
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

    private ArrayList<File> javaClassFinder(String filePath) throws Exception{

        ArrayList<File> classFilesInDirectory = new ArrayList<>();
        File directory = new File(filePath);

        File[] allFilesInDirectory = directory.listFiles();
        for (File file: allFilesInDirectory) {
            if(file.getName().endsWith(".java"))
                classFilesInDirectory.add(file);
            else if(file.isDirectory()) {
                classFilesInDirectory.addAll(javaClassFinder(file.getPath()));
            }
        }
        return classFilesInDirectory;
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

    public ArrayList<CompilationUnit> getAllClassesInProject() {
        return allClassesInProject;
    }
}
