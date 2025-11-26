package es.uab.tqs.f1_2D.vista;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.*;
import javax.swing.*;

public class OffTrackOverlay extends JPanel 
{
    //Icono de la imagen a mostrar en el overlay
    private ImageIcon imageIcon;
    //Clip de audio para el sonido 
    private Clip clip;
    //Indica si el overlay ess visible o no
    private boolean visible;

    //Constructor
    public OffTrackOverlay(String imageResourcePath, String soundResourcePath) 
    {
        //Inicializar el panel de forma que no sea visible y sea transparente
        setOpaque(false);
        this.visible = false;

        //Cargar la imagen y el sonido
        this.imageIcon = loadImage(imageResourcePath);
        this.clip = loadSound(soundResourcePath);
    }

    //Carga una imagen desde la ruta especificada
    public ImageIcon loadImage(String path)
    {
        URL imageUrl = null;
        try {
            imageUrl = getClass().getResource(path);
        }
        catch (NullPointerException | IllegalArgumentException e) { 
            //Si hay excepción se queda en null
        }


        //Si se encuentra la imagen se crea el icon, en caso contrario lo deja en null
        if (imageUrl != null) 
        {
            imageIcon = new ImageIcon(imageUrl);
        }
        else
        {
            imageIcon = null;
        }
        return imageIcon;
    }

    //Carga un sonido desde la ruta especificada
    public Clip loadSound(String path)
    {
        if (path == null) 
        {
            clip = null;
            return clip;
        }
        URL soundUrl = getClass().getResource(path);
        if (soundUrl != null) 
        {
            try {
                //obtiene el audio y lo intenta abrir
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundUrl);
                clip = AudioSystem.getClip();
                clip.open(audioStream);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                clip = null; //Si hay error, lo dejamos en null
            }
        }
        else
        {
            clip = null; //si no encuentra el audio, lo dejamos en null
        }

        return clip;
    }

    //Muestra el overlay, reproduce el sonido y repinta para hacerlo visible
    public void showOverlay() 
    {
        if (!visible) 
        {
            visible = true;
            playSound();
            repaint();
        }
    }

    //Oculta el overlay y repinta para no hacerlo visible
    public void hideOverlay() 
    {
        visible = false;
        repaint();
    }

    //Reproduce el sonido si existe
    public void playSound() 
    {
        if (clip != null) 
        {
            clip.setFramePosition(0);
            clip.start();
        }
    }

    //Método para pintar el componente: si es visible y hay imagen, la dibuja centrada en la parte superior
    @Override
    protected void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        if (visible && imageIcon != null) 
        {
            int imgWidth = imageIcon.getIconWidth();
            int x = (getWidth() - imgWidth) / 2;
            int y = 20; 
            g.drawImage(imageIcon.getImage(), x, y, null);
        }
    }

    //Setters y getters principalmente para test
    public void setClip(Clip clip2) { this.clip = clip2;}
    public Clip getSound() {return clip;}
    public ImageIcon getImage() {return imageIcon;}

    public boolean isVisible() { return visible;}
    public void setvisible(boolean isVisible) {this.visible = isVisible;}
    public void setIcon(ImageIcon icon) {this.imageIcon = icon;}
}
