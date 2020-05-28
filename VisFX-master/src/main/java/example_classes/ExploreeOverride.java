package example_classes;

public class ExploreeOverride extends ChildExampleClass {

//ilgiiiiinç mainden blablaya bir tane ama blabladan toStringe ayrı bir nodee
    public static void main(String[] args) {
        ExampleClass ex = new ExampleClass();
        ChildExampleClass chex = new ChildExampleClass();
        ExploreeOverride exploreeOverride = new ExploreeOverride();
        exploreeOverride.blabla(ex);
        exploreeOverride.blabla(chex);
        exploreeOverride.blabla(exploreeOverride);
    }

    @Override
    public String toString() {
        return "This is example override class";
    }

    public void blabla(ExampleClass e){
        blabla(e);
        e.toString();
    }
}
