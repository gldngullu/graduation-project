package example_classes;

import java.util.ArrayList;

public class ExampleClass {

    private static int i = Math.abs(25);

    public static void main(String[] args) {
        ExampleClass e = new ExampleClass();
        ChildExampleClass ch = new ChildExampleClass();
        e = ch;
        System.out.println(e.toString());
        ArrayList<String> list = new ArrayList();
        ridiculousFunction(list);
        System.out.println("Ridiculous sum result:" + squareCalculation(i) + 5);

    }

    public static int ridiculousFunction(ArrayList<String> list){
        for (int j = 0; j < 3; j++) {
            squareCalculation(i);
        }
        return 5;
    }

    public static int squareCalculation(int i){
        int a = 8;
        System.out.println(doSomeMath());
        System.out.println(doSomeMath(a));
        return i*i;
    }

    public static int doSomeMath(){
        squareCalculation(6);
        return 5 + 3 ;
    }

    public static int doSomeMath(int i){
        return i + 3 ;
    }

    public String toString() {
        return "This is toString method of example class";
    }
}
