package findCall;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class MethodCallInformation {
    private MethodCallExpr methodCall;
    private int methodCallLineNumber;
    private MethodDeclaration actualMethodCalled;

    public MethodCallInformation(
            MethodCallExpr methodCall, int methodCallLineNumber){
        this.methodCall = methodCall;
        this.methodCallLineNumber = methodCallLineNumber;
    }

    public MethodCallInformation(
            MethodCallExpr methodCall, int methodCallLineNumber, MethodDeclaration actualMethod){
        this.methodCall = methodCall;
        this.methodCallLineNumber = methodCallLineNumber;
        this.actualMethodCalled = actualMethod;
    }

    public MethodCallExpr getMethodCall() {
        return methodCall;
    }

    public int getMethodCallLineNumber() {
        return methodCallLineNumber;
    }

    public MethodDeclaration getActualMethodCalled() {
        return actualMethodCalled;
    }
}
