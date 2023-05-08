package de.tum.in.test.integration.testuser.subject.structural.astTestFiles.conditionals.no;

import java.util.Random;
import java.util.stream.IntStream;

public class ClassWithNoKindsOfConditionals {

    public void forLoop() {
        for (int i = 0; i < (new Random().nextInt(3)); i++) {
            System.out.println("Hello World");
        }
    }

    public void forEachLoop() {
        for (int integer : new int[] { (new Random().nextInt(3)), (new Random().nextInt(3)), (new Random().nextInt(3))}) {
            System.out.println("Hello World");
        }
    }

    public void whileLoop() {
        int i = 0;
        while (i < (new Random().nextInt(3))) {
            System.out.println("Hello World");
            i++;
        }
    }

    public void doWhileLoop() {
        int i = 0;
        do {
            System.out.println("Hello World");
            i++;
        } while (i < (new Random().nextInt(3)));
    }

    public void forEachStream() {
        IntStream.range(0, (new Random().nextInt(3))).mapToObj((int i) -> "Hello World").forEach(System.out::println);
    }

    public void assertStatement() {
        assert (new Random().nextInt(3)) == 0;
    }

    public void throwStatement() throws Exception {
        throw new Exception("This is a checked exception.");
    }

    public void catchStatement() {
        try {
            throwStatement();
        } catch (Exception e) {
        }
    }

    void localClassContainingFunction() {
        class localClass {
        }
    }

    /*void localRecordContainingFunction() {
        record localRecord(String id) {

        }
    }*/

}
