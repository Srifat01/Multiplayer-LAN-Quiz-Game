package model;
public class Question {
             
    private String sentence;
    private String[] options;
    private int AnsIndex;
    
    public Question(String sentence, String[] options, int AnsIndex)
    {
          if(options == null || options.length != 4)
            {
                throw new IllegalArgumentException("Questions must have exact 4 options, no more.");
            }
          if(AnsIndex < 0 || AnsIndex > 3)
            {
                throw new IllegalArgumentException("Index must be 0, 1, 2, or 3.");
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
    public int getAnsInd()
    {
        return AnsIndex;
    }

    public String getAnsIndex()
    {
        return options[AnsIndex];
    }

    public boolean correct(int ansIndex)
    {
        return ansIndex == AnsIndex;
    }

    //Printing using toString
    @Override
    public String toString()
    {
           StringBuilder ob = new StringBuilder();
           ob.append(sentence).append("  ");
           char opt = 'A';
           for(int i = 0; i<options.length; i++)
           {
            ob.append(opt++).append(") ").append(options[i]);
           
           if(i == AnsIndex)
           {
            ob.append("<-:3> correct");
           }
        }
           ob.append("  ");
           return ob.toString();
    }

}
