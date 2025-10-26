package es.uab.tqs.f1_2D.vista;

import es.uab.tqs.f1_2D.model.Map;
import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class LapUI extends JPanel
{
    //Referencia al mapa del juego
    private Map map;
    //Imagen de fondo para el overlay de tiempos
    private BufferedImage overlayBackground;
    //Fuente personalizada para el texto
    private Font customFont;
    //Matriz 3x4 que almacena imágenes de sectores [3 sectores] x [4 colores diferentes]
    private BufferedImage[][] sectorImages = new BufferedImage[3][4]; 
    //Ruta relativa al archivo de fuente personalizada
    private String pathFont = "/fonts/Formula1-Bold.ttf";
    //Ruta relativa a la imagen de fondo del overlay
    private String pathOverlay = "/img/laptime.jpg";

    //Constructor
    public LapUI(Map map) 
    {
        this.map = map;
        //Carga la fuente personalizada
        loadFont(pathFont);
        //Array de sujifos de color para las imagenes de sectores
        String[] colors = {"W", "G", "O", "P"};
        //Carga de todas las imagenes de sectores en sus 4 variantes de color
        for (int sector = 0; sector < 3; sector++) {
            for (int color = 0; color < 4; color++) {
                //Construcción de la ruta dinámica para cada imagen de sector/color
                String path = String.format("/img/Sector%d/Sector%d%s.jpg", sector + 1, sector + 1, colors[color]);
                loadSectorImages(path, sector, color);
            }
        }
        //Carga la imagen de fondo en el overlay
        loadOverlay(pathOverlay);
    }

    @Override
    protected void paintComponent(Graphics g) {
        //Lamada al método padre para asegurar comportamiento correcto
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        //Dibuja la imagen de fondo escalada al tamaño real
        g2d.drawImage(overlayBackground, 0, 0, getWidth(), getHeight(), null);
        //Establecimiento de la fuente personalizada a tamaño 28 para que sea más grande el tiempo actual que el mejor
        g2d.setFont(customFont.deriveFont(28f));
    
        if (map.getState() == Map.State.OFF_TRACK || map.getState() == Map.State.INVALID_LAP)
            //Si el jugador se sale de pista o se declara vuelta invalida, el timer se pone de color rojo
            g2d.setColor(Color.RED);
        else if (map.getState() == Map.State.RESULT)
        {
            //Si el jugador ha acabado la vuelta
            if(map.getBestLapTime() < map.getLapTime())
            {
                //Si el jugador tiene una peor vuelta que su mejor, timer en amarillo
                g2d.setColor(Color.YELLOW); 
            }
            else
            {
                //Si el jugador tiene una mejor vuelta que su mejor, timer en verde
                g2d.setColor(Color.GREEN);
            }   
        }    
        else 
            //Si el jugador está en cualquier otro estado, el color del timer es blanco
            g2d.setColor(Color.WHITE);
    
        //Dibuja el timer en la posición establecida
        g2d.drawString(formatTime(map.getLapTime()), 35, 90);

        //Cambia el color a gris claro 
        g2d.setColor(Color.LIGHT_GRAY);
        //Rebaja el tamaño de la letra a 20
        g2d.setFont(customFont.deriveFont(20f));
        //Dibuja el mejor tiempo en la posición establecida
        g2d.drawString("PB: " + formatTime(map.getBestLapTime()), 190, 90);

        //Posiciones de los sectores en relación al overlay
        int baseX = 2;
        int baseY = 113;
        int gap = 115;

        //Dibuja los 3 sectores con sus respectivos colores
        for(int i=0; i<3; i++) 
        {
            //Obtiene el color del sector 
            Map.SectorColor color = map.getSectorColor(i);
            int colorIndex = switch(color) {
                case NONE -> 0;
                case GREEN -> 1;
                case ORANGE -> 2;
                case PURPLE -> 3;
            };
            //Obtiene, de la matriz, la imagen correspondiente al sector y color
            BufferedImage img = sectorImages[i][colorIndex];
            //Dibuja la imagen del sector en posición calculada
            g2d.drawImage(img, baseX + i * gap, baseY, img.getWidth(), img.getHeight(), null);
        }
        //Libera recursos
        g2d.dispose();
    }

    //Convierte milisegundos a string formateado 
    public String formatTime(long ms) {
        if (ms <= 0) return "0.000";
        return String.format("%.3f s", ms / 1000.0);
    }

    //Carga la imagen de fondo del overlay desde recursos
    public void loadOverlay(String path) 
    {
        try {
            overlayBackground = ImageIO.read(getClass().getResource(path));
        } catch (NullPointerException | IOException | IllegalArgumentException e) {
            //En caso de encontrar una excepción, crea una imagen por default
            overlayBackground = new BufferedImage(320, 150, BufferedImage.TYPE_INT_ARGB);
        }
    }

    //Carga y registra la fuente personalizada desde recursos
    public void loadFont(String path) 
    {
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, 
                    getClass().getResourceAsStream(path))
                    .deriveFont(22f);
            //Registra la fuente en el entorno gráfico para que esté disponible
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (Exception e) {
            //En caso de encontrar una excepción, carga Arial como fuente
            customFont = new Font("Arial", Font.BOLD, 22);
        }
    }

    //Carga una imagen específica de sector y la almacena en la matriz
    public void loadSectorImages(String path, int sector, int color) 
    {
       
        try {
            sectorImages[sector][color] = ImageIO.read(getClass().getResource(path));
        } catch (NullPointerException | IOException | IllegalArgumentException e) {
            //En caso de encontrar una excepción, establece null para esa imagen y no se carga
            sectorImages[sector][color] = null;
        }
    }

    // Getter para test
    public BufferedImage getSectorImage(int sector, int colorIndex){ return sectorImages[sector][colorIndex];}
    public Font getFont(){ return customFont; }
    public BufferedImage getOverlayBackground() { return overlayBackground;}
}
