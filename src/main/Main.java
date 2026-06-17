package main;
import java.io.IOException;
import java.util.ArrayList;
import model.Player;
import model.Question;
import util.QuestionLoader;

public class Main {
        public static void main(String[] args)
        {
          System.out.println("Testing Question Loader ");
          System.out.println(".....Loading Questions from data/questions.txt");
          String filePath = "data/questions.txt";
          try
          {
            ArrayList<Question> questions = QuestionLoader.loadQuestions(filePath);
            System.out.println("Loaded-> "+questions.size()+ " questions.");

            int num = 1;
            for(Question q : questions)
            {
                System.out.println("Question "+num+ ":");
                System.out.println(q);
                num++;
            }
          }
          catch(IOException e)
          {
            System.out.println("Error loading file: "+e.getMessage());
          }
          System.out.println("->Loading a fake file to see if try catch works as expected");
          try
          {
            ArrayList<Question> missing = QuestionLoader.loadQuestions("data/nonexistant.txt");
            System.out.println("Loaded "+ missing.size() +"questions (unexpected!)");
          }
          catch(IOException e)
          {
            System.out.println("Caught expected error-> "+ e.getMessage());
          }
          
          System.out.println("Week 1 demo checking.....");
          Player p1 =  new Player("Eagle", 1);
          p1.addScore(17);
          System.out.println(p1);
        }
}

