package findCall;

import java.util.ArrayList;

public class ExampleClass {

    private static int i = Math.abs(25);

    public static void main(String[] args) {
        squareCalculation(i);
        int a = 8;
        System.out.println("Ridiculous sum result:" + squareCalculation(i) + 5);
    }

    public static int ridiculousFunction(ArrayList<String> list){
        return 5;
    }

    public static int squareCalculation(int i){
        return i*i;
    }
}
