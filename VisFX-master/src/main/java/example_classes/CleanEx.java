package example_classes;

public class CleanEx {

    public static void main(String[] args) {
        a();
        e();
    }

    public static void a(){
        b();
    }

    public static void b(){
        d();
    }

    public static void c(){
        d();
    }

    public static void d(){
        c();
    }

    public static void e(){
        a();
    }
}
