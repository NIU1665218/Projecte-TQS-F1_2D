package es.uab.tqs.f1_2D.vista;

import es.uab.tqs.f1_2D.model.Car;
import es.uab.tqs.f1_2D.controlador.CarController;
import es.uab.tqs.f1_2D.model.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import javax.imageio.ImageIO;

public class CarDisplay extends JPanel {
    private CarController controller;
    private BufferedImage carImage;
    private BufferedImage largerMap; 
    private BufferedImage collisionMap;
    private Set<Integer> keysPressed = new HashSet<>();

    private Map trackMap;
    private LapUI lapUI;
    private OffTrackOverlay offTrackOverlay;

    Rectangle finish = new Rectangle(360, 1200, 200, 40);
    List<Rectangle> checkpoints = new ArrayList<>();


    public CarDisplay(CarController controller, BufferedImage map, BufferedImage collisionMap) {
        this.controller = controller;
        this.largerMap = map;
        this.collisionMap = collisionMap;

        checkpoints.add(new Rectangle(1800, 700, 40, 200));
        checkpoints.add(new Rectangle(5484, 152, 20, 200));
        checkpoints.add(new Rectangle(1388, 1216, 200, 50));
        checkpoints.add(new Rectangle(432, 2344, 250, 50));
        trackMap = new Map(map.getWidth(), map.getHeight(), finish, checkpoints);
        trackMap.setTimeProvider(System::currentTimeMillis);

        lapUI = new LapUI(trackMap);
        offTrackOverlay = new OffTrackOverlay("/img/lapdeleted.png", "/sound/Popup.wav");
        setLayout(null);
        offTrackOverlay.setBounds(0, 0, 1024, 860);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        lapUI.setBounds((int)screenSize.getWidth() - 360, (int)screenSize.getHeight() - 200, 340, 150);

        add(lapUI);
        add(offTrackOverlay);
        setComponentZOrder(offTrackOverlay, 0);

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

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                offTrackOverlay.setBounds(0, 0, getWidth(), getHeight());
            }
        });


        new Timer(16, e -> { 
            controller.processInput(keysPressed, collisionMap); 
            updateRace();
            repaint();
        }).start();
    }
    
    private void updateRace() {
        Car car = controller.getCar();
        if (trackMap.getState() != Map.State.RESULT) 
        {
            boolean offTrack = car.trackLimits(collisionMap);
            trackMap.updatePosition(car.getX(), car.getY(), offTrack);
        }

        if (trackMap.getState() == Map.State.OFF_TRACK) 
        {
            offTrackOverlay.showOverlay();
        } else 
        {
            offTrackOverlay.hideOverlay();
        }
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
            /* hitbox for debugging 
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2)); 
            Rectangle rect = new Rectangle(0, 0, car.getSprite().getWidth(), car.getSprite().getHeight());
            Shape hitbox = transform.createTransformedShape(rect);
            g2d.draw(hitbox);
            */
        } else {
            g2d.setColor(Color.RED);
            g2d.fillRect((int) drawX, (int) drawY, 20, 10);
        }

        
        // Dibujar checkpoints
        g2d.setColor(new Color(0, 255, 0, 100)); 
        g2d.fillRect(
            (int)(finish.x - cameraX),
            (int)(finish.y - cameraY),
            finish.width,
            finish.height
        );

        
        g2d.setColor(new Color(255, 0, 0, 100)); 
        checkpoints.add(new Rectangle(1800, 700, 40, 200));
        checkpoints.add(new Rectangle(5484, 152, 20, 200));
        checkpoints.add(new Rectangle(1388, 1216, 250, 50));
        checkpoints.add(new Rectangle(432, 2344, 250, 50));
        for (Rectangle cp : checkpoints) {
            g2d.fillRect(
                (int)(cp.x - cameraX),
                (int)(cp.y - cameraY),
                cp.width,
                cp.height
            );
        }
        

    }

    public static void main(String[] args) {
       
        BufferedImage backgroundImage = null;
        BufferedImage largeMap = null;
        BufferedImage collisionImage = null;
        BufferedImage collisionMap = null;
        double scaleFactor = 4.0;

        try {
            backgroundImage = ImageIO.read(CarDisplay.class.getResource("/track/monaco.jpg"));
            int bigWidth = (int) (backgroundImage.getWidth() * scaleFactor);
            int bigHeight = (int) (backgroundImage.getHeight() * scaleFactor);

            largeMap = new BufferedImage(bigWidth, bigHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D gMap = largeMap.createGraphics();
            gMap.drawImage(backgroundImage, 0, 0, bigWidth, bigHeight, null);
            gMap.dispose();
        } catch (IOException e) {
            System.err.println("Error cargando el mapa: " + e.getMessage());
        }

        try {
            collisionImage = ImageIO.read(CarDisplay.class.getResource("/track/monacoCollision.jpg"));
            int bigWidth = (int) (backgroundImage.getWidth() * scaleFactor);
            int bigHeight = (int) (backgroundImage.getHeight() * scaleFactor);

            collisionMap = new BufferedImage(bigWidth, bigHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D gCollision = collisionMap.createGraphics();
            gCollision.drawImage(collisionImage, 0, 0, bigWidth, bigHeight, null);
            gCollision.dispose();
        } catch (IOException e) {
            System.err.println("Error cargando el mapa de collisiones: " + e.getMessage());
        }

        
        int mapWidth = (largeMap != null) ? largeMap.getWidth() : 2000;
        int mapHeight = (largeMap != null) ? largeMap.getHeight() : 2000;

        Car car = new Car(880, 1780, 267, 0, 15, -5, 2, 0.5, mapHeight - 150, mapWidth - 200);
        CarController controller = new CarController(car);

        
        JFrame frame = new JFrame("F1 2D");
        CarDisplay display = new CarDisplay(controller, largeMap, collisionMap);
        frame.add(display);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
