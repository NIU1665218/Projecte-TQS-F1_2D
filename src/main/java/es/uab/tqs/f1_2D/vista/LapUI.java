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
    private BufferedImage clockIcon;

    public LapUI(Map map) 
    {
        this.map = map;
        try {
            clockIcon = ImageIO.read(getClass().getResource("/img/reloj.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("No se pudo cargar el icono del reloj: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setFont(new Font("Arial", Font.BOLD, 20));

        if (map.getState() == Map.State.OFF_TRACK)
            g2d.setColor(Color.RED);
        else
            g2d.setColor(Color.BLACK);

        if (clockIcon != null)
            g2d.drawImage(clockIcon, 10, 10, 40, 40, null);

        g2d.drawString("Tiempo: " + formatTime(map.getLapTime()), 60, 35);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Mejor: " + formatTime(map.getBestLapTime()), 60, 60);
    }

    private String formatTime(long ms) {
        if (ms <= 0) return "0.000";
        return String.format("%.3f s", ms / 1000.0);
    }
}
