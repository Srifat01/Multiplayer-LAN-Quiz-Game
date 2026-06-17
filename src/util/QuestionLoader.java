package util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import model.Question;

public class QuestionLoader
{
    public static ArrayList<Question> loadQuestions(String filePath) throws IOException
    {
        ArrayList<Question> questions = new ArrayList<>();

        try (BufferedReader buffreader = new BufferedReader(new FileReader(filePath)))
        {
            String line;
            int lineNo = 0;
            while((line = buffreader.readLine()) != null)
            {
                lineNo++;
                line = line.trim();
                if(line.isEmpty() || line.startsWith("#"))
                {
                    continue;
                } 
            try
            {
                Question q = parseLine(line);
                questions.add(q);
            } 
            catch(IllegalArgumentException e)
            {
                System.out.println("Line Skipped "+lineNo+ "(invalid format)" + e.getMessage());
            }
            }
        }
    return questions;
    }
    private static Question parseLine(String line)
    {
        String [] sline = line.split("\\|");
        if(sline.length != 6)
        {
            throw new IllegalArgumentException("Each line must have 6 bodies(question|A|B|C|D|AnswerIndex)"+ sline.length);     
        }
        String text = sline[0].trim();
        String [] options = new String[4];
        for(int i = 0; i < 4; i++)
        {
            options[i] = sline[i+1].trim();
        }
        int AnswerIndex;
        try
        {
            AnswerIndex = Integer.parseInt(sline[5].trim());
        }
        catch(NumberFormatException e)
        {
            throw new IllegalArgumentException("AnsIndex must be a number "+sline[5]);
        }
        return new Question(text, options, AnswerIndex);
    }
}