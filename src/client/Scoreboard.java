package client;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class Scoreboard extends JPanel
{
    private final JLabel titleLabel;
    private final JTextArea listArea;

    public Scoreboard()
    {
        setLayout(new BorderLayout(4, 4));
        titleLabel = new JLabel("Scoreboard");
        listArea = new JTextArea(5, 20);
        listArea.setEditable(false);
        add(titleLabel, BorderLayout.NORTH);
        add(new JScrollPane(listArea), BorderLayout.CENTER);
    }

    public void update(String scoresBody)
    {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        for(String part : scoresBody.split(","))
        {
            if (part.isEmpty()) continue;
            String[] kv = part.split("=");
            if (kv.length != 2) continue;
            try
            {
                entries.add(Map.entry(kv[0], Integer.parseInt(kv[1].trim())));
            }
            catch (NumberFormatException ignored)
            {
               
            }
        }
        entries.sort((a, b) -> b.getValue() - a.getValue()); 

        StringBuilder sb = new StringBuilder();
        int rank = 1;
        for(Map.Entry<String, Integer> e : entries)
        {
            sb.append(rank++).append(". ").append(e.getKey()).append(" — ").append(e.getValue()).append("\n");
        }
        listArea.setText(sb.toString());
    }

}