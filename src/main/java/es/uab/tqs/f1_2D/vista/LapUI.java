package es.uab.tqs.f1_2D.vista;

import es.uab.tqs.f1_2D.model.Map;
import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class LapUI extends JPanel
{
    private Map map;
    private BufferedImage overlayBackground;
    private Font customFont;
    private BufferedImage[][] sectorImages = new BufferedImage[3][4]; 

    public LapUI(Map map) 
    {
        this.map = map;
        setOpaque(false);
        loadFont();
        loadSectorImages();
        try {
            overlayBackground = ImageIO.read(getClass().getResource("/img/laptime.jpg"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("No se pudo cargar el background de los timers: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (overlayBackground != null) 
        {
            g2d.drawImage(overlayBackground, 0, 0, getWidth(), getHeight(), null);
        } 
        else 
        {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        g2d.setFont(customFont.deriveFont(28f));

        if (map.getState() == Map.State.OFF_TRACK || map.getState() == Map.State.INVALID_LAP)
            g2d.setColor(Color.RED);
        else if (map.getState() == Map.State.RESULT)
        {
            if(map.getBestLapTime() < map.getLapTime())
            {
                g2d.setColor(Color.YELLOW); 
            }
            else
            {
                g2d.setColor(Color.GREEN);
            }   
        }    
        else 
            g2d.setColor(Color.WHITE);
    
        g2d.drawString(formatTime(map.getLapTime()), 35, 90);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(customFont.deriveFont(20f));
        g2d.drawString("PB: " + formatTime(map.getBestLapTime()), 190, 90);

        int baseX = 2;
        int baseY = 113;
        int gap = 115;

        for(int i=0; i<3; i++) 
        {
            Map.SectorColor color = map.getSectorColor(i);
            int colorIndex = switch(color) {
                case NONE -> 0;
                case GREEN -> 1;
                case ORANGE -> 2;
                case PURPLE -> 3;
            };
            BufferedImage img = sectorImages[i][colorIndex];
            if(img != null) g2d.drawImage(img, baseX + i * gap, baseY, img.getWidth(), img.getHeight(), null);
        }

        g2d.dispose();
    }

    private String formatTime(long ms) {
        if (ms <= 0) return "0.000";
        return String.format("%.3f s", ms / 1000.0);
    }

    private void loadFont() 
    {
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, 
                    getClass().getResourceAsStream("/fonts/Formula1-Bold.ttf"))
                    .deriveFont(22f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (Exception e) {
            System.err.println("No se pudo cargar la fuente personalizada: " + e.getMessage());
            customFont = new Font("Arial", Font.BOLD, 20);
        }
    }

    private void loadSectorImages() 
    {
        String[] colors = {"W", "G", "O", "P"};
        for (int sector = 0; sector < 3; sector++) {
            for (int color = 0; color < 4; color++) {
                String path = String.format("/img/Sector%d/Sector%d%s.jpg", sector + 1, sector + 1, colors[color]);
                try {
                    sectorImages[sector][color] = ImageIO.read(getClass().getResource(path));
                } catch (IOException | IllegalArgumentException e) {
                    System.err.println("No se pudo cargar la imagen del sector: " + e.getMessage());
                }
            }
        }
    }


}
