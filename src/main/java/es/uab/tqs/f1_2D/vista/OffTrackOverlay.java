package es.uab.tqs.f1_2D.vista;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class OffTrackOverlay extends JPanel {
    private ImageIcon imageIcon;
    private Clip clip;
    private boolean visible;

    public OffTrackOverlay(String imageResourcePath, String soundResourcePath) {
        setOpaque(false);
        this.visible = false;

        URL imageUrl = getClass().getResource(imageResourcePath);
        if (imageUrl != null) {
            imageIcon = new ImageIcon(imageUrl);
        }

        URL soundUrl = getClass().getResource(soundResourcePath);
        if (soundUrl != null) {
            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundUrl)) {
                clip = AudioSystem.getClip();
                clip.open(audioStream);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                System.err.println("Error cargando sonido off-track: " + e.getMessage());
            }
        }
    }

    public void showOverlay() {
        if (!visible) {
            visible = true;
            playSound();
            repaint();
        }
    }

    public void hideOverlay() {
        visible = false;
        repaint();
    }

    private void playSound() {
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (visible && imageIcon != null) {
            int imgWidth = imageIcon.getIconWidth();
            int x = (getWidth() - imgWidth) / 2;
            int y = 20; 
            g.drawImage(imageIcon.getImage(), x, y, null);
        }
    }

    public void setClip(Clip clip2) { this.clip = clip2;}

    public boolean isVisible() { return visible;}
}
