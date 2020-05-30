package example_classes;

public class ChildExampleClass extends ExampleClass {

    public static void main(String[] args) {
        ChildExampleClass childEx = new ChildExampleClass();
        System.out.println(childEx.toString());
        ExampleClass exampleClass = new ExampleClass();
        System.out.println(exampleClass.toString());
    }

    @Override
    public String toString() {
        return "This is child example class";
    }
}
