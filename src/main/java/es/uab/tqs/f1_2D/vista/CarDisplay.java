package es.uab.tqs.f1_2D.vista;

import es.uab.tqs.f1_2D.model.Car;
import es.uab.tqs.f1_2D.controlador.CarController;
import es.uab.tqs.f1_2D.controlador.MapController;
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

public class CarDisplay extends JPanel 
{
    private CarController controller;
    private BufferedImage largerMap; 
    private BufferedImage collisionMap;
    private Set<Integer> keysPressed = new HashSet<>();

    private Map trackMap;
    private MapController trackMapController;
    private LapUI lapUI;
    private OffTrackOverlay offTrackOverlay;
    private OffTrackOverlay invalidLapOverlay;

    private boolean inMainMenu = true;
    private MainMenu mainMenu;
    private GameMode currentGameMode = GameMode.QUALY;
    private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    Rectangle finish = new Rectangle(360, 1200, 200, 40);
    List<Rectangle> checkpoints = new ArrayList<>();

    public enum GameMode {
        QUALY,
        RACE
    }

    // Constructor
    public CarDisplay(CarController controller, BufferedImage map, BufferedImage collisionMap1) {
        this.controller = controller;

        // Si no hay mapas, usar valores mínimos seguros
        if (map == null) {
            map = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        }
        if (collisionMap1 == null) 
        {
            collisionMap1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        }
        this.largerMap = map;
        this.collisionMap = collisionMap1;

        // Configurar el mapa de la pista
        checkpoints.add(new Rectangle(1800, 700, 40, 200));
        checkpoints.add(new Rectangle(5484, 152, 20, 200));
        checkpoints.add(new Rectangle(1388, 1216, 250, 50));
        checkpoints.add(new Rectangle(432, 2344, 250, 50));
        trackMap = new Map(map.getWidth(), map.getHeight(), finish, checkpoints);
        trackMapController = new MapController(trackMap);
        trackMapController.setTimeProvider(System::currentTimeMillis);

        // Configurar interfaces de usuario
        lapUI = new LapUI(trackMap);
        offTrackOverlay = new OffTrackOverlay("/img/lapdeleted.png", "/sound/Popup.wav");
        setLayout(null);
        offTrackOverlay.setBounds(0, 0, 1024, 860);
        invalidLapOverlay = new OffTrackOverlay("/img/invalidlap.png", "/sound/boxbox.wav");
        invalidLapOverlay.setBounds(0, 0, 1024, 860);

        lapUI.setBounds((int)screenSize.getWidth() - 360, (int)screenSize.getHeight() - 200, 340, 150);
        add(lapUI);
        add(offTrackOverlay);
        setComponentZOrder(offTrackOverlay, 0);
        add(invalidLapOverlay);
        setComponentZOrder(invalidLapOverlay, 0);

        setupMainMenu();

        setFocusable(true);

        // Cargar el sprite del coche
        loadCarSprite(controller, "/img/car.png");

        // Configurar listeners de teclado
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (inMainMenu) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        showMainMenu();
                    }
                } else {
                    if(!trackMap.isCountdownActive())
                    {
                        keysPressed.add(e.getKeyCode());
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!inMainMenu) {
                    if(!trackMap.isCountdownActive())
                    {
                        keysPressed.remove(e.getKeyCode());
                    }
                }
            }
        });

        // Ajustar overlays al redimensionar
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                offTrackOverlay.setBounds(0, 0, getWidth(), getHeight());
                invalidLapOverlay.setBounds(0, 0, getWidth(), getHeight());
                if (mainMenu != null) {
                    mainMenu.setBounds(0, 0, getWidth(), getHeight());
                }
            }
        });

        // Timer para actualizar el juego y repintar
        new Timer(16, e -> { 
            if(!inMainMenu)
            {
                controller.processInput(keysPressed, collisionMap); 
                updateRace();
            }
            repaint();
        }).start();
    }

    // Configurar el menú principal
    private void setupMainMenu() 
    {
        mainMenu = new MainMenu();
        mainMenu.setBounds(0, 0, getWidth(), getHeight());
        add(mainMenu);
        setComponentZOrder(mainMenu, 0);
        
        showMainMenu();
    }

    // Mostrar el menú principal
    public void showMainMenu() {
        inMainMenu = true;
        mainMenu.setVisible(true);
        setComponentZOrder(mainMenu, 0);
        keysPressed.clear();
        
    }

    // Ocultar el menú principal y empezar el juego
    public void startGame(GameMode mode) 
    {
        inMainMenu = false;
        currentGameMode = mode;
        mainMenu.setVisible(false);
        
        // Configurar el modo de juego
        if (mode == GameMode.RACE) 
        {
            // Configurar para modo carrera (número de vueltas, etc.)
            Car car = controller.getCar();
            car.setX(350);
            car.setY(1250);
            car.setAngle(280);
            car.setVelocity(0);
            trackMapController.startRaceMode();
            trackMap.setTotalLaps(3); 
            
        } else
        {
             // Resetear la posición del coche
            Car car = controller.getCar();
            car.setX(880);
            car.setY(1780);
            car.setAngle(267);
            car.setVelocity(0);
            trackMapController.startQualyMode();
        }
        
        requestFocusInWindow();
    }
    
    // Actualiza el estado de la carrera y los overlays
    public void updateRace() 
    {
        if (inMainMenu) return;


        if (!trackMap.isCountdownActive() && trackMap.getState() != Map.State.RACE_FINISHED)
        {
            Car car = controller.getCar();
            // Actualizar posición del coche en el mapa
            boolean offTrack = car.trackLimits(collisionMap);
            trackMapController.updatePosition(car.getX(), car.getY(), offTrack);
        }

        if (trackMap.getState() == Map.State.OFF_TRACK) 
        {
            // Mostrar overlay de fuera de pista
            offTrackOverlay.showOverlay();
            invalidLapOverlay.hideOverlay();
        } 
        else if (trackMap.getState() == Map.State.INVALID_LAP)
        {
            // Mostrar overlay de vuelta inválida
            invalidLapOverlay.showOverlay();
            offTrackOverlay.hideOverlay();
        }
        else if( trackMap.isRaceComplete())
        {
            showRaceResults();
        }
        else 
        {
            // Ocultar ambos overlays
            offTrackOverlay.hideOverlay();
            invalidLapOverlay.hideOverlay();
        }
    }

    private void showRaceResults()
    {
    
        JOptionPane.showMessageDialog(this, 
            "¡Carrera completada!\n" +
            "Vueltas: " + trackMap.getCurrentLap() + "/" + trackMap.getTotalLaps() + "\n" +
            "Mejor vuelta: " + formatTime(trackMap.getBestLapTime()),
            "Resultados de Carrera", 
            JOptionPane.INFORMATION_MESSAGE);
        showMainMenu();
    }

    private String formatTime(long time) 
    {
        long minutes = (time / 60000) % 60;
        long seconds = (time / 1000) % 60;
        long milliseconds = (time % 1000);
        return String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);
    }

    // PaintComponent para dibujar el mapa y el coche
    @Override
    protected void paintComponent(Graphics g) 
    {
        super.paintComponent(g);

        if (inMainMenu) {
            // Dibujar fondo del menú principal
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }

        // Mostrar cuenta atrás si está activa
        if (trackMap.isCountdownActive()) {
            drawCountdown(g);
            return;
        }

        Car car = controller.getCar();
        Graphics2D g2d = (Graphics2D) g;

        // Calcular la posición de la cámara centrada en el coche
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        double cameraX = car.getX() - panelWidth / 2.0;
        double cameraY = car.getY() - panelHeight / 2.0;

        cameraX = Math.max(0, Math.min(cameraX, largerMap.getWidth() - panelWidth));
        cameraY = Math.max(0, Math.min(cameraY, largerMap.getHeight() - panelHeight));

       // Dibujar la porción visible del mapa
        g2d.drawImage(largerMap,
                0, 0, panelWidth, panelHeight,
                (int) cameraX, (int) cameraY,
                (int) cameraX + panelWidth, (int) cameraY + panelHeight,
                null);

        
        double drawX = car.getX() - cameraX;
        double drawY = car.getY() - cameraY;

        // Dibujar el coche con rotación
        if (car.getSprite() != null) {
            AffineTransform transform = new AffineTransform();
            transform.translate(drawX, drawY);
            transform.rotate(Math.toRadians(car.getAngle()),
                    car.getSprite().getWidth() / 2.0,
                    car.getSprite().getHeight() / 2.0);
            g2d.drawImage(car.getSprite(), transform, null);

            /* DEBUGGING PURPOSES
            // Dibujar hitbox del coche
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2)); 
            Rectangle rect = new Rectangle(0, 0, car.getSprite().getWidth(), car.getSprite().getHeight());
            Shape hitbox = transform.createTransformedShape(rect);
            g2d.draw(hitbox);
            */

        } else {
            // Dibujar un rectángulo rojo si no hay sprite
            g2d.setColor(Color.RED);
            g2d.fillRect((int) drawX, (int) drawY, 20, 10);
        }

        /* DEBUGGING PURPOSES
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
        */

    }

    private void drawCountdown(Graphics g) 
    {
        Graphics2D g2d = (Graphics2D) g;
        
        // Solo dibujar el mapa si estamos en modo carrera
        if (currentGameMode == GameMode.RACE) {
            Car car = controller.getCar();
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            
            // Calcular la posición de la cámara centrada en el coche 
            double cameraX = car.getX() - panelWidth / 2.0;
            double cameraY = car.getY() - panelHeight / 2.0;

            cameraX = Math.max(0, Math.min(cameraX, largerMap.getWidth() - panelWidth));
            cameraY = Math.max(0, Math.min(cameraY, largerMap.getHeight() - panelHeight));

            // Dibujar la porción visible del mapa
            g2d.drawImage(largerMap,
                    0, 0, panelWidth, panelHeight,
                    (int) cameraX, (int) cameraY,
                    (int) cameraX + panelWidth, (int) cameraY + panelHeight,
                    null);
        } else {
            // Para QUALY, fondo negro
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        
        // Dibujar overlay semitransparente
        g.setColor(new Color(0, 0, 0, 150)); 
        g.fillRect(0, 0, getWidth(), getHeight());
        
        int countdown = trackMap.getRemainingCountdown();
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 120));
        
        String countdownText = countdown > 0 ? String.valueOf(countdown) : "GO!";
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(countdownText)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        
        g.drawString(countdownText, x, y);
        
        // Mostrar información de la carrera
        if (currentGameMode == GameMode.RACE) {
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            String info = "Carrera - " + trackMap.getTotalLaps() + " vueltas";
            FontMetrics infoFm = g.getFontMetrics();
            int infoX = (getWidth() - infoFm.stringWidth(info)) / 2;
            int infoY = y + 80;
            g.drawString(info, infoX, infoY);
        }
    }

    // Carga y asigna el sprite del coche al controlador
    public void loadCarSprite(CarController controller, String path) 
    {
        BufferedImage carImage;

        try {
            if (path == null) {
                throw new IllegalArgumentException("Ruta del sprite no puede ser null");
            }

            carImage = ImageIO.read(getClass().getResource(path));

            // Redimensionar la imagen del coche
            int newWidth = 80;
            int newHeight = 80;
            Image scaledImage = carImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

            BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resized.createGraphics();
            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();

            // Asignar el sprite redimensionado al coche
            controller.getCar().setSprite(resized);

        } catch (IOException | IllegalArgumentException e) {
            // Imagen por defecto si hay error al cargar
            BufferedImage defaultImage = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
            controller.getCar().setSprite(defaultImage);
        }
    }

    // Carga una imagen para el track y se escala para cubrir toda el área
    public static BufferedImage loadImage(String path, double scaleFactor) 
    {
        BufferedImage original;
        try
        {
            if (path == null) throw new IllegalArgumentException("Path no puede ser null");
            original = ImageIO.read(CarDisplay.class.getResource(path));
        }
        catch (IOException | IllegalArgumentException e)
        {
            original = new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_RGB);
        }

        // Escalar la imagen
        int bigWidth = (int)(original.getWidth() * scaleFactor);
        int bigHeight = (int)(original.getHeight() * scaleFactor);

        BufferedImage scaled = new BufferedImage(bigWidth, bigHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.drawImage(original, 0, 0, bigWidth, bigHeight, null);
        g.dispose();
        return scaled;
    }

    // Crea la ventana del juego con el CarDisplay
    public static JFrame createGame(CarController controller, BufferedImage largeMap, BufferedImage collisionMap) 
    {
        JFrame frame = new JFrame("F1 2D");
        CarDisplay display = new CarDisplay(controller, largeMap, collisionMap);
        frame.add(display);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return frame;
    }

    // Getter para test
    public Set<Integer> getKeysPressed() { return keysPressed; }
    public Map getTrackMap() { return trackMap; }
    public OffTrackOverlay getOffTrackOverlay() { return offTrackOverlay; }
    public OffTrackOverlay getInvalidLapOverlay() { return invalidLapOverlay; }
    public boolean isInMainMenu() { return inMainMenu; }
    public GameMode getCurrentGameMode() { return currentGameMode; }

    // Clase interna para el menú principal
    private class MainMenu extends JPanel {
        private BufferedMenuButton qualyButton;
        private BufferedMenuButton raceButton;
        private BufferedImage backgroundImage;

        public MainMenu() {
            setLayout(null);
            setOpaque(false);
            
            // Cargar imagen de fondo del menú
            try {
                backgroundImage = ImageIO.read(getClass().getResource("/img/main_menu_bg.png"));
            } catch (IOException | IllegalArgumentException e) {
                backgroundImage = null;
            }
            
            
            // Calcular posición centrada para los botones
            int panelWidth = (int)screenSize.getWidth();
            int panelHeight = (int)screenSize.getHeight(); 
            
            int buttonWidth = 200;
            int buttonHeight = 60;
            int centerX = panelWidth / 2 - buttonWidth / 2;
            int buttonsY = panelHeight / 2 + 50; 
            
            // Crear botón de Qualy
            try {
                BufferedImage qualyImg = ImageIO.read(getClass().getResource("/img/qualy_button.png"));
                qualyButton = new BufferedMenuButton(qualyImg);
                qualyButton.setBounds(centerX - buttonWidth - 20, buttonsY, buttonWidth, buttonHeight);
                qualyButton.addActionListener(e -> startGame(GameMode.QUALY));
                add(qualyButton);
            } catch (IOException | IllegalArgumentException e) {
                // Botón por defecto si no se carga la imagen
                JButton defaultQualy = new JButton("QUALY");
                defaultQualy.setBounds(centerX - buttonWidth - 20, buttonsY, buttonWidth, buttonHeight);
                defaultQualy.addActionListener(d -> startGame(GameMode.QUALY));
                add(defaultQualy);
            }
            
            // Crear botón de Race
            try {
                BufferedImage raceImg = ImageIO.read(getClass().getResource("/img/race_button.png"));
                raceButton = new BufferedMenuButton(raceImg);
                raceButton.setBounds(centerX + buttonWidth + 20, buttonsY, buttonWidth, buttonHeight);
                raceButton.addActionListener(e -> startGame(GameMode.RACE));
                add(raceButton);
            } catch (IOException | IllegalArgumentException e) {
                // Botón por defecto si no se carga la imagen
                JButton defaultRace = new JButton("RACE");
                defaultRace.setBounds(centerX + buttonWidth + 20, buttonsY, buttonWidth, buttonHeight);
                defaultRace.addActionListener(f -> startGame(GameMode.RACE));
                add(defaultRace);
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (backgroundImage != null) {
                // Centrar la imagen de fondo
                int x = (getWidth() - backgroundImage.getWidth()) / 2;
                int y = (getHeight() - backgroundImage.getHeight()) / 4; 
                g.drawImage(backgroundImage, x, y, this);
            } else {
                // Fondo por defecto
                g.setColor(new Color(30, 30, 60));
                g.fillRect(0, 0, getWidth(), getHeight());
                
                // Título por defecto
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 36));
                String title = "F1 2D";
                int titleWidth = g.getFontMetrics().stringWidth(title);
                g.drawString(title, (getWidth() - titleWidth) / 2, getHeight() / 3);
            }
        }
    }
    
    // Clase para botones con imagen
    private class BufferedMenuButton extends JButton {
        private BufferedImage image;
        
        public BufferedMenuButton(BufferedImage image) {
            this.image = image;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            if (image != null) {
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            } else {
                super.paintComponent(g);
            }
        }
    }

    // Main para ejecutar el juego
    public static void main(String[] args) 
    {
    double scaleFactor = 4.0;
    BufferedImage largeMap = null;
    BufferedImage collisionMap = null;

    largeMap = loadImage("/track/monaco.jpg", scaleFactor);
    collisionMap = loadImage("/track/monacoCollision.jpg", scaleFactor);

    // Crear el coche y el controlador
    Car car = new Car(880, 1780, 267, 0, 15, -5, 2, 0.5, largeMap.getHeight() - 150, largeMap.getWidth() - 200);
    CarController controller = new CarController(car);

    // Crear y mostrar la ventana del juego
    JFrame frame = createGame(controller, largeMap, collisionMap);
    frame.setVisible(true);
    }

}
