package findCall;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class MethodCallInformation {
    private ClassOrInterfaceDeclaration classInfo;
    private MethodCallExpr methodCall;
    private int methodCallLineNumber;
    private MethodDeclaration actualMethodCalled;

    public MethodCallInformation(
            ClassOrInterfaceDeclaration classInfo, MethodCallExpr methodCall, int methodCallLineNumber){
        this.methodCall = methodCall;
        this.classInfo = classInfo;
        this.methodCallLineNumber = methodCallLineNumber;
    }

    public MethodCallInformation(
            ClassOrInterfaceDeclaration classInfo, MethodCallExpr methodCall, int methodCallLineNumber, MethodDeclaration actualMethod){
        this.methodCall = methodCall;
        this.classInfo = classInfo;
        this.methodCallLineNumber = methodCallLineNumber;
        this.actualMethodCalled = actualMethod;
    }

    public MethodCallExpr getMethodCall() {
        return methodCall;
    }

    public ClassOrInterfaceDeclaration getClassInfo() {
        return classInfo;
    }

    public int getMethodCallLineNumber() {
        return methodCallLineNumber;
    }

    public MethodDeclaration getActualMethodCalled() {
        return actualMethodCalled;
    }
}
