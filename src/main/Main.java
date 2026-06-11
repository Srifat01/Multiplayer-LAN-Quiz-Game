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

            System.out.println("Test Questions ");
            String[] opt1 = {"3000", "33", "80", "12"};
            Question q1 = new Question("What is the default port for HTTP?", opt1, 2);
            System.out.print(q1);
             
            String[] opt2 = {"Compiler", "JVM", "Interpreter", "MINGW"};
            Question q2 = new Question("What executes the java byte code?", opt2, 1);
            System.out.println(q2);

            System.out.println("Correct Answers:- ");
            System.out.println("First Q answer index: "+q1.getAnsIndex());
            System.out.println("is q2 correct? q1(0) "+q1.Iscorrect(0));
            System.out.println("is q1 correct? q1(2) "+q2.Iscorrect(2));
            
            System.out.println("correct answer for second question: "+q2.getAnswer());
            System.out.println("correct answer for first question: "+q1.getAnswer());
        }
}
