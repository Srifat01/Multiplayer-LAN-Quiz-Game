package model;
public class Question {
             
    private String sentence;
    private String[] options;
    private int AnsIndex;
    
    public Question(String sentence, String[] options, int AnsIndex)
    {
          if(options == null || options.length != 4)
            {
                throw new IllegalArgumentException("Questions must have exact 4 options, no more.\n");
            }
          if(AnsIndex < 0 || AnsIndex > 3)
            {
                throw new IllegalArgumentException("Index must be 0, 1, 2, or 3.\n");
            }
            this.sentence = sentence;   
            this.options = options;
            this.AnsIndex = AnsIndex;     
    }
    public String getS()
    {
        return sentence;
    }
    public String[] getOpt()
    {
        return options;
    }
    public int getAnsIndex()
    {
        return AnsIndex;
    }

    public String getAnswer()
    {
        return options[AnsIndex];
    }

    public boolean Iscorrect(int ansIndex)
    {
        return ansIndex == AnsIndex;
    }

    //Printing using toString
    @Override
    public String toString()
    {
        String answerCorrect = sentence + "\n";

        for(int i = 0; i < options.length; i++)
        {
            answerCorrect = answerCorrect + (char)('A'+i)+ ") "+options[i]+ "\n";
        }
        return answerCorrect;
    }
}   