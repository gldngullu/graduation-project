package analyze_project;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
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
import java.util.NoSuchElementException;

public class AnalyzeProject {

    private static String packagePath;
    private ArrayList<MethodCallInformation> allMethodCallsInProject;
    private ArrayList<CompilationUnit> parsedClasses;
    private JavaParserFacade javaParserFacade;
    private ArrayList<String> jarFiles;
    private ArrayList<String> unsolvedMethods;
    private ArrayList<String> qualifiedClassNames;

    public void initializeAnalyze() {
        parsedClasses = new ArrayList<>();
        jarFiles = new ArrayList<>();
        qualifiedClassNames = new ArrayList<>();
    }

    public ArrayList<MethodCallInformation> findMethodCalls() {

        allMethodCallsInProject = new ArrayList<>();
        unsolvedMethods = new ArrayList<>();
        MethodCallFinder methodCallFinder = new MethodCallFinder();

        setTypeSolver();

        for (CompilationUnit parsedClass : parsedClasses) {
            qualifiedClassNames.add(getClassAsString(parsedClass));
            methodCallFinder.visit(parsedClass, null);
        }
        System.out.println("Unsolved methods:" + unsolvedMethods.size());
        return allMethodCallsInProject;
    }

    public void findJarFilesInDirectory(String path) {
        File directory = new File(path);
        File[] directoryFiles = directory.listFiles();
        if (directoryFiles.length == 0)
            return;
        for (File file : directory.listFiles()) {
            if (file.getName().endsWith(".jar"))
                jarFiles.add(file.getPath());
            else if (file.isDirectory()) {
                findJarFilesInDirectory(file.getPath());
            }
        }
    }

    private void setTypeSolver() {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(packagePath));
        try {
            for (String path : jarFiles) {
                combinedTypeSolver.add(new JarTypeSolver(path));
            }
        } catch (Exception io) {
            System.out.println("Oops");
        }
        javaParserFacade = JavaParserFacade.get(combinedTypeSolver);
    }

    public void findJavaFiles(String filePath) throws Exception {
        File directory = new File(filePath);
        File[] allFilesInDirectory = directory.listFiles();
        for (File file : allFilesInDirectory) {
            if (file.getName().endsWith(".java")) {
                CompilationUnit compilationUnit = StaticJavaParser.parse(file);
                parsedClasses.add(compilationUnit);
            } else if (file.isDirectory()) {
                findJavaFiles(file.getPath());
            }
        }
    }

    private String getClassAsString(CompilationUnit compilationUnit) {
        String className = "";
        for (Node childNode : compilationUnit.getChildNodes()) {
            if (childNode instanceof PackageDeclaration)
                className += ((PackageDeclaration) childNode).getNameAsString() + ".";
            if (childNode instanceof ClassOrInterfaceDeclaration)
                className += ((ClassOrInterfaceDeclaration) childNode).getNameAsString();
        }
        return className;
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
            } catch (UnsupportedOperationException ex) {
                unsolvedMethods.add("Unsupported: " + methodCallExpr.toString());
            } catch (UnsolvedSymbolException ex) {
                unsolvedMethods.add("Unsolved: " + methodCallExpr.toString());
            } catch (RuntimeException ex) {
                unsolvedMethods.add("Runtime: " + methodCallExpr.toString());
            }
        }
    }

    private Node getCallerOfMethodCall(MethodCallExpr methodCall) {
        Node caller = methodCall.getParentNode().get();
        while (!((caller instanceof MethodDeclaration) || (caller instanceof ConstructorDeclaration))) {
            if (caller instanceof ClassOrInterfaceDeclaration || caller instanceof EnumDeclaration)
                return caller;
            try {
                caller = caller.getParentNode().get();
            } catch (NoSuchElementException ex) {
                ex.printStackTrace();
            }
        }
        return caller;
    }

    public ArrayList<CompilationUnit> getParsedClasses() {
        return parsedClasses;
    }

    public static void setPackagePath(String packagePath) {
        AnalyzeProject.packagePath = packagePath;
    }

    public ArrayList<String> getQualifiedClassNames() {
        return qualifiedClassNames;
    }
}
