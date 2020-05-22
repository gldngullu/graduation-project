package findCall;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

public class MethodCallInformation {
    private MethodCallExpr methodCall;
    private ResolvedMethodDeclaration resolvedMethod;
    private Node parentNode;
    private int methodCallLineNumber;

    public MethodCallInformation(
            MethodCallExpr methodCall, ResolvedMethodDeclaration resolvedMethod, Node parentNode, int methodCallLineNumber){
        this.methodCall = methodCall;
        this.methodCallLineNumber = methodCallLineNumber;
        this.parentNode = parentNode;
        this.resolvedMethod = resolvedMethod;
    }

    public MethodCallExpr getMethodCall() {
        return methodCall;
    }

    public int getMethodCallLineNumber() {
        return methodCallLineNumber;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public ResolvedMethodDeclaration getResolvedMethod() {
        return resolvedMethod;
    }
}
