package es.uab.tqs.f1_2D.vista;

import es.uab.tqs.f1_2D.model.Car;
import es.uab.tqs.f1_2D.controlador.CarController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

public class CarDisplay extends JPanel {
    private CarController controller;
    private BufferedImage carImage;
    private BufferedImage largerMap;  
    private Set<Integer> keysPressed = new HashSet<>();

    public CarDisplay(CarController controller, BufferedImage map) {
        this.controller = controller;
        this.largerMap = map;
        setFocusable(true);

        try {
            carImage = ImageIO.read(getClass().getResource("/img/car.png"));
            int newWidth = 80;
            int newHeight = 80;
            Image scaledImage = carImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resized.createGraphics();
            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();

            carImage = resized;
            controller.getCar().setSprite(carImage);

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("No se pudo cargar el sprite del coche: " + e.getMessage());
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                keysPressed.add(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keysPressed.remove(e.getKeyCode());
            }
        });

        new Timer(16, e -> { controller.processInput(keysPressed);; repaint();}).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Car car = controller.getCar();
        Graphics2D g2d = (Graphics2D) g;

       
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        double cameraX = car.getX() - panelWidth / 2.0;
        double cameraY = car.getY() - panelHeight / 2.0;

        
        cameraX = Math.max(0, Math.min(cameraX, largerMap.getWidth() - panelWidth));
        cameraY = Math.max(0, Math.min(cameraY, largerMap.getHeight() - panelHeight));

       
        g2d.drawImage(largerMap,
                0, 0, panelWidth, panelHeight,
                (int) cameraX, (int) cameraY,
                (int) cameraX + panelWidth, (int) cameraY + panelHeight,
                null);

        
        double drawX = car.getX() - cameraX;
        double drawY = car.getY() - cameraY;

        if (car.getSprite() != null) {
            AffineTransform transform = new AffineTransform();
            transform.translate(drawX, drawY);
            transform.rotate(Math.toRadians(car.getAngle()),
                    car.getSprite().getWidth() / 2.0,
                    car.getSprite().getHeight() / 2.0);
            g2d.drawImage(car.getSprite(), transform, null);
        } else {
            g2d.setColor(Color.RED);
            g2d.fillRect((int) drawX, (int) drawY, 20, 10);
        }
    }

    public static void main(String[] args) {
       
        BufferedImage backgroundImage = null;
        BufferedImage largeMap = null;
        double scaleFactor = 4.0;

        try {
            backgroundImage = ImageIO.read(CarDisplay.class.getResource("/track/bahrain.jpg"));
            int bigWidth = (int) (backgroundImage.getWidth() * scaleFactor);
            int bigHeight = (int) (backgroundImage.getHeight() * scaleFactor);

            largeMap = new BufferedImage(bigWidth, bigHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D gMap = largeMap.createGraphics();
            gMap.drawImage(backgroundImage, 0, 0, bigWidth, bigHeight, null);
            gMap.dispose();
        } catch (IOException e) {
            System.err.println("Error cargando el mapa: " + e.getMessage());
        }

        
        int mapWidth = (largeMap != null) ? largeMap.getWidth() : 2000;
        int mapHeight = (largeMap != null) ? largeMap.getHeight() : 2000;

        Car car = new Car(2050, 2670, 180, 0, 10, -5, 2, mapHeight - 150, mapWidth - 200);
        CarController controller = new CarController(car);

        
        JFrame frame = new JFrame("F1 2D");
        CarDisplay display = new CarDisplay(controller, largeMap);
        frame.add(display);
        frame.setSize(1024, 860);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
