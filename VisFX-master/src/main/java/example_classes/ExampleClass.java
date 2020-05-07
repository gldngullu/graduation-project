package example_classes;

import java.util.ArrayList;

public class ExampleClass {

    private static int i = Math.abs(25);

    public static void main(String[] args) {
        squareCalculation(i);
        int a = 8;
        System.out.println("Ridiculous sum result:" + squareCalculation(i) + 5);
        System.out.println(doSomeMath());
        System.out.println(doSomeMath(a));
    }

    public static int ridiculousFunction(ArrayList<String> list){
        return 5;
    }

    public static int squareCalculation(int i){
        return i*i;
    }

    public static int doSomeMath(){
        return 5 + 3 ;
    }

    public static int doSomeMath(int i){
        return i + 3 ;
    }

    public String toString() {
        return "This is toString method of example class";
    }
}
