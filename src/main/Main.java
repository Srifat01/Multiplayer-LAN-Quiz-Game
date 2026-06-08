package main;
import model.Player;
import model.Question;

public class Main {
        public static void main(String[] args)
        {
            System.out.println("First Draft-->");

            System.out.println("Test Player CLass: ");
            Player ob1 = new Player("Eagle", 1);
            Player ob2 = new Player("OWL", 2);

            System.out.println(ob1);
            System.out.println(ob2);

            ob1.addScore(2);
            ob1.addScore(4);
            ob2.addScore(44);

            System.out.println("Resultant Score: ");
            System.out.println(ob1);
            System.out.println(ob2);

            ob1.addScore(-2);
            System.out.println("Negative value is ignored"+ ob1);

            ob1.resetScore();
            System.out.println("After reset the score: "+ob1);

            System.out.println(" Test Questions ");
            String[] opt1 = {"3000", "33", "8000", "12"};
            Question q1 = new Question("What is the default port for HTTP?", opt1, 0);
            System.out.print(q1);
             
            String[] opt2 = {"Compiler", "JVM", "Interpreter", "MINGW"};
            Question q2 = new Question("What executes the java byte code?", opt2, 1);
            System.out.println(q2);

            System.out.println("Correct-> ");
            System.out.println("First Q answer index: "+q1.getAnsInd());
            System.out.println("correct? q1(0) "+q1.correct(0));
            System.out.println("correct? q1(2) "+q2.correct(2));
            
            System.out.println("correct answer for second q: "+q2.getAnsIndex());
            System.out.println("correct answer for q2: "+q2.correct(1));

        System.out.println("Exception Handling");
        try
        {
            String[] wrongOpt = {"only", "three", "options"};
            Question WQue = new Question("Wrong Q", wrongOpt, 0);

        }
        catch (IllegalArgumentException e)
        {
            System.out.println("Caught an error wrong option"+ e.getMessage());
        }
        try
        {
            String[] corrOpt = {"A", "B", "C", "D"};
            Question wrongIndex = new Question("Wrong index", corrOpt, 5);

        }
        catch (IllegalArgumentException e)
        {
            System.out.println("Caught an exception: "+e.getMessage());
        }
        System.out.println("Test passed");
        }
}
