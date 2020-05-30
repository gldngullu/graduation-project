package findCall;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

public class ListingAllMethods {

    private static final String PACKAGE_PATH
            = "C:\\Users\\gldng\\OneDrive\\Belgeler\\GitHub\\graduation-project\\VisFX-master\\src\\main\\java";

    private ArrayList<MethodCallInformation> allMethodCallsInProject = new ArrayList<>();
    private CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
    private ArrayList<String> deelekrg = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.forLanguageTag("en"));
        ListingAllMethods exClass = new ListingAllMethods();
        String directoryPath = "C:\\Users\\gldng\\OneDrive\\Belgeler\\GitHub\\graduation-project\\VisFX-master\\src\\main\\java\\visfx";
        exClass.findMethodCalls(directoryPath);
    }

    public void findMethodCalls(String directoryPath) throws Exception{
        ArrayList<File> classFilesInDirectory = fileFinder(directoryPath);

        MethodCallFinder methodCallFinder = new MethodCallFinder();

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        setTypeSolver(new File(PACKAGE_PATH));

        for (File classFile: classFilesInDirectory) {
            CompilationUnit compilationUnit = StaticJavaParser.parse(classFile);
            methodCallFinder.visit(compilationUnit, null);
        }
    }

    private void setTypeSolver(File classFile){
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        int i = 1;
        URL[] urls = ((URLClassLoader) cl).getURLs();


        try {
            for (URL url : urls) {
                String decodedURL = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8.toString());
                if(decodedURL.endsWith(".jar")) {
                    /*
                    if(i == 43) {
                        i++;
                        continue;
                    }
                     */
                    i++;
                    combinedTypeSolver.add(new JarTypeSolver(decodedURL));
                }
            }
        } catch (Exception io){
            System.out.println("Oops");
        }


        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(PACKAGE_PATH));
    }

    private ArrayList<File> fileFinder(String filePath){

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
            Node caller = getCallerOfMethodCall(methodCallExpr);
            System.out.println(methodCallExpr.toString());
            try {
                ResolvedMethodDeclaration resolvedMethod = methodCallExpr.resolve();
                allMethodCallsInProject.add(
                        new MethodCallInformation(methodCallExpr, resolvedMethod, caller, methodCallExpr.getRange().get().begin.line));
            }catch (UnsupportedOperationException ex){
                System.out.println("oopsie dipsiiiii lalaaaaaa pooooo".toUpperCase());
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

}
