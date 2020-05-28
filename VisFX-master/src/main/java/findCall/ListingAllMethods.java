package findCall;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class ListingAllMethods {

    private static final String PACKAGE_PATH
            = "C:\\Users\\gldng\\OneDrive\\Belgeler\\GitHub\\graduation-project\\VisFX-master\\src\\main\\java";

    private ArrayList<MethodCallInformation> allMethodCallsInProject = new ArrayList<>();
    private ArrayList<String> deelekrg = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.forLanguageTag("en"));
        ListingAllMethods exClass = new ListingAllMethods();
        exClass.findMethodCalls();
    }

    public void findMethodCalls() throws Exception{
        String directoryPath = "C:\\Users\\gldng\\OneDrive\\Belgeler\\GitHub\\graduation-project\\VisFX-master\\src\\main\\java\\example_classes";
        ArrayList<File> classFilesInDirectory = fileFinder(directoryPath);

        MethodCallFinder methodCallFinder = new MethodCallFinder();

        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(PACKAGE_PATH));

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

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
            Node caller = getCallerOfMethodCall(methodCallExpr);
            allMethodCallsInProject.add(
                    new MethodCallInformation(methodCallExpr, methodCallExpr.resolve(), caller, methodCallExpr.getRange().get().begin.line));

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
