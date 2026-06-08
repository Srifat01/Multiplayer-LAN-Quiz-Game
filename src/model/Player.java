package model;
public class Player
{
      private String name;
      private int id, score;
      
      
      public Player(String name, int id)
      {
            this.name = name;
            this.id = id;
            this.score = 0;

      }
      public String getName()
      {
            return name;
      }
      public int getID()
      {
            return id;
      }
      public int getScore()
      {
            return score;
      }

      public void setName(String name)
      {
            this.name = name;
      }
      public void setScore(int score)
      {
            this.score = score;
      }
      public void setID(int id)
      {
            this.id = id;
      }
      //Add points
      public void addScore(int points)
      {
             if(points > 0)
             {
                  this.score = this.score + points;
             }
      }
      public void resetScore()
      {
            this.score = 0;
      }
@Override
public String toString()
{
        return ("Player ID: "+ id+ ", Name: "+name+", Score: "+score);
}
}