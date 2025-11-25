package es.uab.tqs.f1_2D.vista;

import es.uab.tqs.f1_2D.controlador.AIController;
import es.uab.tqs.f1_2D.controlador.CarController;
import es.uab.tqs.f1_2D.controlador.MapController;
import es.uab.tqs.f1_2D.model.AICar;
import es.uab.tqs.f1_2D.model.Car;
import es.uab.tqs.f1_2D.model.Map;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.*;

public class CarDisplay extends JPanel 
{
    //Connexió amb les altres clases
    private CarController controller;
    private Map trackMap;
    private MapController trackMapController;
    private LapUI lapUI;
    private OffTrackOverlay offTrackOverlay;
    private OffTrackOverlay invalidLapOverlay;
    private AIController aiController;

    //Variables a controlar durant els updates i inicialització del joc
    private BufferedImage largerMap; 
    private BufferedImage collisionMap;
    private Set<Integer> keysPressed = new HashSet<>();
    Rectangle finish = new Rectangle(280, 1200, 320, 40);
    List<Rectangle> checkpoints = new ArrayList<>();

    //Variables de control d'estil
    private boolean inMainMenu = true;
    private MainMenu mainMenu;
    private GameMode currentGameMode = GameMode.QUALY;
    private Dimension screenSize;

    //Diferents modes de Joc
    public enum GameMode 
    {
        QUALY,
        RACE
    }

    // Constructor
    public CarDisplay(CarController controller, BufferedImage map, BufferedImage collisionMap1) 
    {
        try 
        {
            this.screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        }
        catch(Exception e)
        {
            this.screenSize = new Dimension(1920, 1080);
        }
        //Inicialitzar controlador
        this.controller = controller;
        
        // Si no hay mapas, usar valores mínimos seguros i inicialitzar-los al display
        if (map == null) 
        {
            map = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        }
        if (collisionMap1 == null) 
        {
            collisionMap1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        }
        //Inicialitzar mapes
        this.largerMap = map;
        this.collisionMap = collisionMap1;

        // Configurar el mapa de la pista
        checkpoints.add(new Rectangle(4548, 900, 40, 270)); 
        checkpoints.add(new Rectangle(5484, 100, 40, 230));
        checkpoints.add(new Rectangle(1330, 1216, 350, 50));
        checkpoints.add(new Rectangle(400, 2344, 350, 50));

        //Inicialitzar mapa amb els checkpoints i finish line
        trackMap = new Map(map.getWidth(), map.getHeight(), finish, checkpoints);
        //Inicialitzar mapController
        trackMapController = new MapController(trackMap);
        //Inicialitzar el cronòmetre
        trackMapController.setTimeProvider(System::currentTimeMillis);

        // Configurar interfaces de usuario
        lapUI = new LapUI(trackMap);

        //Creació d'un nou overlay per offtrack position
        offTrackOverlay = new OffTrackOverlay("/img/lapdeleted.png", "/sound/Popup.wav");

        //Desactiva el layout automàtic per poder manipular-lo amb setbounds
        setLayout(null);
        offTrackOverlay.setBounds(0, 0, 1024, 860);

        //Creació d'un nou overlay per invalidlap position
        invalidLapOverlay = new OffTrackOverlay("/img/invalidlap.png", "/sound/boxbox.wav");

        //Definició posició i tamany d'invalid lap overlay
        invalidLapOverlay.setBounds(0, 0, 1024, 860);

        //Definició posició i tamany del UI del cronòmetre i sectors
        lapUI.setBounds((int)screenSize.getWidth() - 360, (int)screenSize.getHeight() - 200, 340, 150);

        //Incloure els tres components al contenedor principal i possar-los a sobre amb ZOrder
        add(lapUI);
        add(offTrackOverlay);
        setComponentZOrder(offTrackOverlay, 0);
        add(invalidLapOverlay);
        setComponentZOrder(invalidLapOverlay, 0);

        //Inicialització del Main Menu 
        setupMainMenu();

        //Indicar que pot rebre events del teclat
        setFocusable(true);

        // Cargar el sprite del coche i dels IACars
        loadCarSprite(controller.getCar(), "/img/ferrari_car.png");
        /* 
        Timer initializationTimer = new Timer(100, e -> {
            loadAICarSprites();
        });
        //Indicar que nomès es realitzi un cop el timer i l'executem
        initializationTimer.setRepeats(false);
        initializationTimer.start();
        */
        loadAICarSprites();
        // Configurar listeners de teclat
        addKeyListener(new KeyAdapter() 
        {
            //Quan l'event agafa una nova tecla
            @Override
            public void keyPressed(KeyEvent e) 
            {
                //Només agafar tecles si no es troba en el MainMenu
                if(!inMainMenu) 
                {
                    //Si la tecla es l'Esc, anar al main menu
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) 
                    {
                        showMainMenu();
                    }
                    //En cas contrari afegir-la a la llista per tratar-la posteriorment
                    else if(!trackMap.isCountdownActive())
                    {
                        keysPressed.add(e.getKeyCode());
                    }
                }
            }

            //Quan l'event troba que la tecla es deixa de prèmer
            @Override
            public void keyReleased(KeyEvent e) 
            {
                //Només tratar l'acció quan es troba en joc
                if (!inMainMenu) 
                {
                    if(!trackMap.isCountdownActive())
                    {
                        keysPressed.remove(e.getKeyCode());
                    }
                }
            }
        });

        // Ajustar overlays al redimensionar
        addComponentListener(new ComponentAdapter() 
        {
            @Override
            public void componentResized(ComponentEvent e) 
            {
                offTrackOverlay.setBounds(0, 0, getWidth(), getHeight());
                invalidLapOverlay.setBounds(0, 0, getWidth(), getHeight());
                mainMenu.setBounds(0, 0, getWidth(), getHeight());
            }
        });

        // Timer per actualitzar el joc i repintar
        new Timer(16, e -> { 
            //Si no es troba al MainMenu processar les tecles i fer l'update corresponent
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
        //Configurar la mdia i el tamany del mainMenu i afegirlo al component visual a Z 0
        mainMenu.setBounds(0, 0, getWidth(), getHeight());
        add(mainMenu);
        setComponentZOrder(mainMenu, 0);
        //Mostrar main menu
        showMainMenu();
    }

    // Mostrar el menú principal
    public void showMainMenu() 
    {
        inMainMenu = true;
        mainMenu.setVisible(true);
        keysPressed.clear();       
    }

    // Ocultar el menú principal y empezar el juego
    public void startGame(GameMode mode) 
    {
        //Resetejar cada cop que es vol jugar
        currentGameMode = mode;
        resetGame();
        inMainMenu = false;
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
            //Inicialitzar el controlador pels cotxes IA
            this.aiController = new AIController(trackMap, controller.getCar());   
            trackMapController.setAIController(aiController);  
            loadAICarSprites();     
        }
        else
        {
            // Resetear la posición del coche
            Car car = controller.getCar();
            car.setX(880);
            car.setY(1780);
            car.setAngle(267);
            car.setVelocity(0);
            trackMapController.startQualyMode();
            this.aiController = null;
        }
        
        //Demana al component actual que pugui rebre accions des del teclat
        requestFocusInWindow();
    }

    private void resetGame()
    {
        //Resetejar totes les clases i carregar de nou els cotxes IA i repintar
        trackMapController.reset();
        trackMap.reset();
        if(aiController != null) aiController.reset();
        offTrackOverlay.hideOverlay();
        invalidLapOverlay.hideOverlay();
        if(currentGameMode == GameMode.RACE) loadAICarSprites();
        repaint();
    }
    
    // Actualiza el estado de la carrera y los overlays
    public void updateRace() 
    {
        //Si es troba al main menu no hi ha res a actualitzar
        if (inMainMenu) return;

        //Si no es troba dins la pantalla de countdown i la carrera no ha finalitzat
        if (!trackMap.isCountdownActive() && trackMap.getState() != Map.State.RACE_FINISHED)
        {
            Car car = controller.getCar();

            // Aplicar slipstream al jugador
            if(currentGameMode == GameMode.RACE)
            {
                boolean playerInSlipstream = checkPlayerSlipstream();
                car.applySlipstream(playerInSlipstream, trackMap.getCurrentLap());
            }

            // Actualizar posición del coche en el mapa
            boolean offTrack = car.trackLimits(collisionMap);
            trackMapController.updatePosition(car.getX(), car.getY(), offTrack);

            //Actualitzar l'estat de tots els cotxes rivals
            if(currentGameMode == GameMode.RACE)
            {
                aiController.updateAllAI(collisionMap);
            }
        }

        //Si la carrera ha finalitzat, veure els resultats
        if (currentGameMode == GameMode.RACE && isRaceFinished()) 
        {
            showRaceResults();
            return;
        }

        //Si el jugador es troba offtrack, ensenyar l'overlay corresponent
        if (trackMap.getState() == Map.State.OFF_TRACK) 
        {
            // Mostrar overlay de fora de pista
            offTrackOverlay.showOverlay();
            invalidLapOverlay.hideOverlay();
        } 
        //Si el jugador es troba invalid lap, ensenyar l'overlay corresponent
        else if (trackMap.getState() == Map.State.INVALID_LAP)
        {
            // Mostrar overlay de vuelta invàlida
            invalidLapOverlay.showOverlay();
            offTrackOverlay.hideOverlay();
        }
        //Si no es cap de les anteriors situacions, ocultar els overlays
        else 
        {
            // Ocultar ambdos overlays
            offTrackOverlay.hideOverlay();
            invalidLapOverlay.hideOverlay();
        }
    }

    //Verificar si la carrera s'ha acabat
    public boolean isRaceFinished() 
    {
        // Verificar si el jugador ha completado la carrera
        if(trackMap.isRaceComplete()) 
        {
            return true;
        }
        
        if(aiController != null)
        {
            // Verificar si algún AI ha completado la carrera
            for (AICar aiCar : aiController.getAICars()) 
            {
                if (aiCar.getCurrentLap() > trackMap.getTotalLaps()) 
                {
                    return true;
                }
            }
        }
        else
        {
            System.out.println("fghwioeb");
        }
        //Si ningú compleix els requisits, la carrera segueix
        return false;
    }

    //Comprobar si el cotxe del jugador es troba just darrere d'un altre per donar un impuls
    public boolean checkPlayerSlipstream() 
    {
        Car playerCar = controller.getCar();
        
        //Comprobar la distancia a cada cotxe rival
        for (AICar aiCar : aiController.getAICars()) 
        {
            double distance = Math.sqrt(
                Math.pow(aiCar.getX() - playerCar.getX(), 2) + 
                Math.pow(aiCar.getY() - playerCar.getY(), 2)
            );
            
            //Si es troba a una distància de menys de 100 píxels, calcular l'angle a que es troba
            if (distance < 100) 
            { 
                
                double dx = aiCar.getX() - playerCar.getX();
                double dy = aiCar.getY() - playerCar.getY();
                double angleToAICar = Math.toDegrees(Math.atan2(dy, dx));
                double angleDiff = Math.abs(normalizeAngle(angleToAICar - playerCar.getAngle()));
                
                //Si l'angle és menor als 30 graus, considerem que es troba darrere
                if (angleDiff < 30) 
                { 
                    return true;
                }
            }
        }

        //Si cap situació es compleix, no aplicar slipstram
        return false;
    }
    
    //Funció d'utilitat per normalitzar un angle entre 0 i 360
    public double normalizeAngle(double angle) 
    {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    //Si algú ha finalitzat la carrera, ensenyar els resultats
    public void showRaceResults() 
    {
        // Obtener la clasificación final
        List<AICar> positions = aiController.getPositions();
        int playerPosition = getPlayerPosition();
        
        //Preparar els títols
        StringBuilder results = new StringBuilder();
        results.append("¡Carrera completada!\n\n");
        results.append("Clasificación final:\n\n");
        
        // Mostrar les 10 primeres posicions
        for (int i = 0; i < Math.min(positions.size(), 10); i++) 
        {
            //Agafar el cotxe a tractar
            AICar aiCar = positions.get(i);
            //Canviar el temps de milisegons a minuts/segons/milisegons
            String timeStr = formatTime(aiCar.getBestLapTime());
            //Calcular el número de voltes del cotxe a tractar
            int laps = aiCar.getCurrentLap();
            
            //Si la posició és la de jugador, afegir el seu resultat
            if (i + 1 == playerPosition) 
            {
                results.append("→ ").append(playerPosition).append(". PLAYER - ")
                .append(formatTime(trackMap.getBestLapTime())).append(" - L").append(trackMap.getCurrentLap())
                .append("\n");
            }
            //Si la posició del cotxe que tractem és igual o 
            //major en la llista a la posició del jugador, afegir una posició
            if(i + 1 >= playerPosition)
            {
                results.append((i + 2)).append(". ").append(aiCar.getTeam()).append(" - ")
                .append(timeStr).append(" - L").append(laps).append("\n");
            }
            //En cas contrari, agafar l'ordre de la llista
            else
            {
                results.append((i + 1)).append(". ").append(aiCar.getTeam()).append(" - ")
                .append(timeStr).append(" - L").append(laps).append("\n");
            }
        }
        
        // Si el jugador no está en las primeras 10 posiciones, añadirlo al final
        if (playerPosition > 10) 
        {
            results.append("...\n");
            results.append(playerPosition).append(". PLAYER - ")
            .append(formatTime(trackMap.getBestLapTime())).append(" - L")
            .append(trackMap.getCurrentLap()).append("\n");
        }
        
        //Ensenyar els resultats complets
        JOptionPane.showMessageDialog(this, results.toString(), 
            "Resultados de Carrera", JOptionPane.INFORMATION_MESSAGE);
        
        // Volver al menú principal
        showMainMenu();
    }

    //Pasar de milisegons al format MM:SS.mmm (minuts, segons, milisegons)
    public String formatTime(long time) 
    {
        long minutes = (time / 60000) % 60;
        long seconds = (time / 1000) % 60;
        long milliseconds = (time % 1000);
        return String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);
    }

    //En carrera, funció que s'encarrega de pintar les posicions on toca
    public void drawPositions(Graphics g) 
    {
        //Agafar la llista de posicions en carrera
        List<AICar> positions = aiController.getPositions();

        //Si no existeix, no avançem 
        if (positions == null || positions.isEmpty()) return;
        
        //Definició del layout semitransparent
        g.setColor(new Color(0, 0, 0, 180)); 
        g.fillRect(10, 120, 300, 550);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        
        int startX = 20;
        int startY = 140;
        int lineHeight = 25;
        
        //Títol
        g.drawString("CLASSIFICATION", startX, startY);
        
        //Línea separadora
        g.drawLine(startX, startY + 5, startX + 200, startY + 5);
        
        //Jugador
        g.setColor(Color.YELLOW);
        int playerPosition = getPlayerPosition();
        String playerTime = formatTime(trackMap.getLapTime());
        g.drawString(playerPosition + ". PLAYER - " + playerTime + " - L" + 
            trackMap.getCurrentLap() + "/3", startX, startY + lineHeight);
        
        //AI Cars
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        
        //Iterar sobre la resta de cotxes
        for (int i = 0; i < positions.size(); i++) 
        {
            //Agafar de la llista el cotxe a iterar
            AICar aiCar = positions.get(i);
            if (aiCar == null) continue;
            
            //Agafar les seves dades
            String team = aiCar.getTeam();
            long lapTime = aiCar.getLapTime();
            String timeStr = formatTime(lapTime);
            int laps = aiCar.getCurrentLap();
            
            int displayPos = i+1;
            //Si la posició del cotxe que tractem és igual o major en la llista a la posició del jugador, 
            //afegir una posició
            if(displayPos >= playerPosition) displayPos++;
            String displayText = displayPos + ". " + team + " - " + timeStr + " - L" + laps;
            //Dibuixar la posició del cotxe que estem iterarnt
            g.drawString(displayText, startX, startY + (i + 2) * lineHeight);
        }
    }
    
    //Funció complementaria per determinar la posició del jugador
    public int getPlayerPosition() 
    {
        //Almacenar les posicions actuals a una llista per iterar-la
        List<AICar> positions = aiController.getPositions();
        if (positions == null) return 1;
        
        //Consultar situació del jugador
        int playerLap = trackMap.getCurrentLap();
        int playerCheckpoints = trackMap.getPassedCheckpoints().size();
        double playerDistance = getDistanceToNextCheckpoint(controller.getCar());
        
        //Mínima posició
        int position = 1;
        
        //Iterar sobre els diferents cotxes
        for (AICar aiCar : positions) 
        {
            if (aiCar == null) continue;
            
            //Consultar situació del cotxe iterat
            int aiLap = aiCar.getCurrentLap();
            int aiCheckpoints = aiCar.getPassedCheckpoints().size();
            double aiDistance = getDistanceToNextCheckpoint(aiCar);
            
            //Si el cotxe té més voltes que el jugador, el jugador va per darrere d'aquest cotxe
            if (aiLap > playerLap) 
            {
                position++;
            } 
            //Si el cotxe té les mateixes voltes que el jugador, però té més checkpoints, 
            //el jugador va per darrere d'aquest cotxe
            else if (aiLap == playerLap && aiCheckpoints > playerCheckpoints) 
            {
                position++;
            }
            //Si el cotxe té les mateixes voltes i checkpoints que el jugador, 
            //però està més aprop del següent checkpoint, el jugador va per darrere d'aquest cotxe
            else if (aiLap == playerLap && aiCheckpoints == playerCheckpoints && aiDistance < playerDistance) 
            {
                position++;
            }
        }
        
        return position;
    }

    //Funció complementaria per calcular la distància al següent checkpoint
    public double getDistanceToNextCheckpoint(Car car) 
    {
        //Lista de checkpoints i finishLine
        List<Rectangle> allCheckpoints = Arrays.asList (
            new Rectangle(4548, 940, 40, 230),  
            new Rectangle(5484, 100, 20, 230),   
            new Rectangle(1330, 1216, 350, 50), 
            new Rectangle(400, 2344,350, 50),  
            new Rectangle(280, 1200, 320, 40)
        );
        
        int nextCheckpointIndex;

        //Depenent de la instància que sigui Car, utlitzar la funció corresponent
        if (car instanceof AICar) 
        {
            nextCheckpointIndex = ((AICar) car).getNextCheckpointIndex();
        } 
        else 
        {
            nextCheckpointIndex = trackMap.getNextCheckpointIndex();
        }
        
        //Si tots els checkpoints han sigut passats, utilitzar la finish line
        if (nextCheckpointIndex >= allCheckpoints.size()) 
        {
            nextCheckpointIndex = allCheckpoints.size() - 1;
        }
        
        //Calcular la distància des del centre del cotxe fins el centre del següent checkpoint
        Rectangle nextCheckpoint = allCheckpoints.get(nextCheckpointIndex);
        
        double carCenterX = car.getX() + 40;
        double carCenterY = car.getY() + 40;
        double checkpointCenterX = nextCheckpoint.getX() + nextCheckpoint.getWidth() / 2.0;
        double checkpointCenterY = nextCheckpoint.getY() + nextCheckpoint.getHeight() / 2.0;
        
        return Math.sqrt(Math.pow(checkpointCenterX - carCenterX, 2) + 
                        Math.pow(checkpointCenterY - carCenterY, 2));
    }


    // PaintComponent per dibuixar el mapa i el cotxe
    @Override
    protected void paintComponent(Graphics g) 
    {
        //Cridar a la classe base per asegurar funcionament 
        super.paintComponent(g);

        //Si es troba en el menú principal, dibuixar-lo
        if (inMainMenu) 
        {
            // Dibujar fondo del menú principal
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }

        // Mostrar compte enrerre si està activa
        if (trackMap.isCountdownActive()) 
        {
            drawCountdown(g);
            return;
        }

        Car car = controller.getCar();
        Graphics2D g2d = (Graphics2D) g;

        // Calcular la posició de la càmara centrada al cotxe
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        double cameraX = car.getX() - panelWidth / 2.0;
        double cameraY = car.getY() - panelHeight / 2.0;

        cameraX = Math.max(0, Math.min(cameraX, largerMap.getWidth() - panelWidth));
        cameraY = Math.max(0, Math.min(cameraY, largerMap.getHeight() - panelHeight));

       // Dibuixar la porció visible del mapa
        g2d.drawImage(largerMap,
                0, 0, panelWidth, panelHeight,
                (int) cameraX, (int) cameraY,
                (int) cameraX + panelWidth, (int) cameraY + panelHeight,
                null);

        //Si no es troba ni en el menú ni en el countdown, i és mode carrera, dibuixar posicions
        if (!inMainMenu && !trackMap.isCountdownActive())
        {
            if(trackMap.isRaceMode()) drawPositions(g);
        }

        //Calcular on dibuixar el cotxe del jugador en funció de la càmera
        double drawX = car.getX() - cameraX;
        double drawY = car.getY() - cameraY;

        // Dibuixar el cotxe amb rotació
        if (car.getSprite() != null) 
        {
            //Transformar la imatge/sprite del cotxe per tal de rotar-la en funció de l'angle
            AffineTransform transform = new AffineTransform();
            transform.translate(drawX, drawY);
            transform.rotate(Math.toRadians(car.getAngle()),
                    car.getSprite().getWidth() / 2.0,
                    car.getSprite().getHeight() / 2.0);
            //Dibuixar el cotxe
            g2d.drawImage(car.getSprite(), transform, null);

            /* DEBUGGING PURPOSES
            // Dibujar hitbox del coche
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2)); 
            Rectangle rect = new Rectangle(0, 0, car.getSprite().getWidth(), car.getSprite().getHeight());
            Shape hitbox = transform.createTransformedShape(rect);
            g2d.draw(hitbox);
            */

        } 
        else 
        {
            // Dibuixar un punt vermell si el cotxe no té sprite
            g2d.setColor(Color.RED);
            g2d.fillRect((int) drawX, (int) drawY, 20, 10);
        }

        /*
        // DEBUGGING PURPOSES
        // Dibujar checkpoints
        g2d.setColor(new Color(0, 255, 0, 100)); 
        g2d.fillRect(
            (int)(finish.x - cameraX),
            (int)(finish.y - cameraY),
            finish.width,
            finish.height
        );

        g2d.setColor(new Color(255, 0, 0, 100)); 
        checkpoints.add(new Rectangle(1600, 650, 100, 250));
        checkpoints.add(new Rectangle(4548, 900, 40, 270));
        checkpoints.add(new Rectangle(5484, 100, 40, 230));
        checkpoints.add(new Rectangle(1330, 1216, 350, 50));
        checkpoints.add(new Rectangle(400, 2344, 350, 50));
        for (Rectangle cp : checkpoints) 
        {
            g2d.fillRect(
                (int)(cp.x - cameraX),
                (int)(cp.y - cameraY),
                cp.width,
                cp.height
            );
        }
        */
    
        if(trackMap.isRaceMode())
        {
            //Dibujar  tots els cotxes IA
            Graphics2D g2d2 = (Graphics2D) g;
            for (AICar aiCar : aiController.getAICars()) 
            {
                //Calcular posició del cotxe
                double drawX2 = aiCar.getX() - cameraX;
                double drawY2 = aiCar.getY() - cameraY;
                if (aiCar.getSprite() != null) 
                {
                    //Si té sprite, calcular la rotació
                    AffineTransform transform = new AffineTransform();
                    transform.translate(drawX2, drawY2);
                    transform.rotate(Math.toRadians(aiCar.getAngle()),
                            aiCar.getSprite().getWidth() / 2.0,
                            aiCar.getSprite().getHeight() / 2.0);
                    //Dibuixar el cotxe
                    g2d2.drawImage(aiCar.getSprite(), transform, null);
                }
            }

            /*
            //DEBUGGING PURPOSES
            //RACING LINE DRAWING
            List<Point> racingLine = aiController.getRacingLine();
    
            if (racingLine.isEmpty()) 
            {
                return;
            }

            // Configurar el color y el trazo para la línea
            g2d.setColor(new Color(255, 0, 0, 150)); 
            g2d.setStroke(new BasicStroke(3)); 

            // Dibujar la línea que conecta los waypoints
            Point prev = racingLine.get(0);
            for (int i = 1; i < racingLine.size(); i++) 
            {
                Point current = racingLine.get(i);
                int x1 = (int) (prev.x - cameraX);
                int y1 = (int) (prev.y - cameraY);
                int x2 = (int) (current.x - cameraX);
                int y2 = (int) (current.y - cameraY);
                g2d.drawLine(x1, y1, x2, y2);
                prev = current;
            }

            // Dibujar los waypoints como círculos
            g2d.setColor(new Color(0, 255, 0, 200)); 
            for (Point point : racingLine) 
            {
                int x = (int) (point.x - cameraX);
                int y = (int) (point.y - cameraY);
                g2d.fillOval(x - 5, y - 5, 10, 10); 
            }
            */
        }

    }

    //Funció per dibuixar la countdown abans de la carrera
    public void drawCountdown(Graphics g) 
    {
        Graphics2D g2d = (Graphics2D) g;
        
        // Nomès dibuixar el mapa si estem en mode carrera
        if (currentGameMode == GameMode.RACE) 
        {
            Car car = controller.getCar();
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            
            // Calcular la posició de la càmara centrada en el cotxe 
            double cameraX = car.getX() - panelWidth / 2.0;
            double cameraY = car.getY() - panelHeight / 2.0;

            cameraX = Math.max(0, Math.min(cameraX, largerMap.getWidth() - panelWidth));
            cameraY = Math.max(0, Math.min(cameraY, largerMap.getHeight() - panelHeight));

            // Dibujar la porció visible del mapa
            g2d.drawImage(largerMap,
                    0, 0, panelWidth, panelHeight,
                    (int) cameraX, (int) cameraY,
                    (int) cameraX + panelWidth, (int) cameraY + panelHeight,
                    null);
        } 
        else 
        {
            //Per QUALY, fons negre
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        
        //Dibuixar overlay semitransparent
        g.setColor(new Color(0, 0, 0, 150)); 
        g.fillRect(0, 0, getWidth(), getHeight());
        
        //calcular el número que s'ha d'imprimir per pantalla al countdown
        int countdown = trackMap.getRemainingCountdown();
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 120));
        
        String countdownText = countdown > 0 ? String.valueOf(countdown) : "GO!";
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(countdownText)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

        //Dibuixar a cada instant el que queda de countdown
        g.drawString(countdownText, x, y);
        
        // Mostrar informació de la carrera
        if (currentGameMode == GameMode.RACE) 
        {
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            String info = "Carrera - " + trackMap.getTotalLaps() + " vueltas";
            FontMetrics infoFm = g.getFontMetrics();
            int infoX = (getWidth() - infoFm.stringWidth(info)) / 2;
            int infoY = y + 80;
            g.drawString(info, infoX, infoY);
        }
    }

    //Funció per carregar els Sprites dels cotxes rivals
    public void loadAICarSprites() 
    {
        if(aiController == null) return;
        //Possibles noms dels cotxes rivals
        String[] teamNames = 
        {
            "mercedes", "redbull", "mclaren",
            "alpine", "racingbulls", "astonmartin", "williams", 
            "sauber", "haas"
        };

        //Direccions a les imatges de cada sprite
        String[] teamColors = 
        {
            "/img/mercedes_car.png", "/img/redbull_car.png",
            "/img/mclaren_car.png", "/img/alpine_car.png", "/img/racingbulls_car.png",
            "/img/astonmartin_car.png", "/img/williams_car.png", "/img/sauber_car.png",
            "/img/haas_car.png"
        };

        //Per cada cotxe, assignar-li el seu Sprite corresponent al nom de l'equip
        for (AICar aiCar : aiController.getAICars()) 
        {
            String team = aiCar.getTeam().toLowerCase().replace(" ", "");
            int teamIndex = -1;
            
            //Buscar l'equip dins de l'array
            for (int i = 0; i < teamNames.length; i++) 
            {
                if (teamNames[i].equals(team)) 
                {
                    teamIndex = i;
                    break;
                }
            }
            
            // Si no es troba, utilitzar index per defecte
            if (teamIndex == -1) 
            {
                teamIndex = 0;
            }
            
            //Carregar la direcció en la funció principal de load
            loadCarSprite(aiCar, teamColors[teamIndex]);
        }
    }

    // Carga i assigna el sprite del cotxe al controlador
    public void loadCarSprite(Car car, String path) 
    {
        BufferedImage carImage;

        try {
            if (path == null) 
            {
                throw new IllegalArgumentException("Ruta del sprite no puede ser null");
            }

            //Intentar llegir la imatge passada per parametre 
            carImage = ImageIO.read(getClass().getResource(path));

            // Redimensionar la imatge del cotxe
            int newWidth = 80;
            int newHeight = 80;
            Image scaledImage = carImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

            BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            //Crear el gràfic per poder pintar-lo
            Graphics2D g2d = resized.createGraphics();
            g2d.drawImage(scaledImage, 0, 0, null);
            //Alliberar recursos per poder inicialitzar el següent
            g2d.dispose();

            //Asignar l'sprite al cotxe corresponent
            car.setSprite(resized);

        } catch (IOException | IllegalArgumentException e) {
            //Si hi ha algún problema en carregar algún sprite, possar una imatge default
            BufferedImage defaultImage = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
            car.setSprite(defaultImage);
        }
    }

    // Carga una imatge pel track i s'escala per cubrir tota l'àrea
    public static BufferedImage loadImage(String path, double scaleFactor) 
    {
        BufferedImage original;
        try
        {
            if (path == null) throw new IllegalArgumentException("Path no puede ser null");
            //Intentar llegir la imatge del track
            original = ImageIO.read(CarDisplay.class.getResource(path));
        }
        catch (IOException | IllegalArgumentException e)
        {
            //Si no es pot llegir, possar una default
            original = new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_RGB);
        }

        // Escalar la imatge
        int bigWidth = (int)(original.getWidth() * scaleFactor);
        int bigHeight = (int)(original.getHeight() * scaleFactor);

        BufferedImage scaled = new BufferedImage(bigWidth, bigHeight, BufferedImage.TYPE_INT_RGB);
        //Crear el gràfic per poder dibuixar-lo
        Graphics2D g = scaled.createGraphics();
        g.drawImage(original, 0, 0, bigWidth, bigHeight, null);
        //Alliberar recursos
        g.dispose();
        return scaled;
    }

    //Crea la finestra del joc amb el CarDisplay
    public static JFrame createGame(CarController controller, BufferedImage largeMap, BufferedImage collisionMap) 
    {
        //Definir un JFrame per afegir tots els components anteriors
        JFrame frame = new JFrame("F1 2D");
        CarDisplay display = new CarDisplay(controller, largeMap, collisionMap);
        frame.add(display);
        //S'obre en pestanya gran
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        //Quan es tanca, finalitza l'execució
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return frame;
    }

    // Getter/Setter para test
    public Set<Integer> getKeysPressed() { return keysPressed; }
    public Map getTrackMap() { return trackMap; }
    public OffTrackOverlay getOffTrackOverlay() { return offTrackOverlay; }
    public OffTrackOverlay getInvalidLapOverlay() { return invalidLapOverlay; }
    public boolean isInMainMenu() { return inMainMenu; }
    public GameMode getCurrentGameMode() { return currentGameMode; }
    public void setIsMainMenu(boolean active) {this.inMainMenu = active;} 
    public MainMenu getMainMenu() {return mainMenu;}
    

    // Classe interna necessaria pel menú principal
    class MainMenu extends JPanel 
    {
        //Variables pels botons
        private int buttonWidth = 200;
        private int buttonHeight = 60;

        //Botons
        JButton qualyButton = new JButton("QUALY");
        JButton raceButton = new JButton("RACE");

        //Inicialitzar el menú principal
        public MainMenu() 
        {
            //Definir layout a null per poder controlar a gust
            setLayout(null);
            
            // Calcular posició centrada pels botons
            int panelWidth = (int)screenSize.getWidth();
            int panelHeight = (int)screenSize.getHeight(); 
            
            int centerX = panelWidth / 2 - buttonWidth / 2;
            int buttonsY = panelHeight / 2 + 50; 
            
            //Crear botó de Qualy i assignar startGame de Qualy al prèmer
            qualyButton.setBounds(centerX - buttonWidth - 20, buttonsY, buttonWidth, buttonHeight);
            qualyButton.addActionListener(d -> startGame(GameMode.QUALY));
            add(qualyButton);
            
            //Crear botó de Race i assignar startGame de Race al prèmer
            raceButton.setBounds(centerX + buttonWidth + 20, buttonsY, buttonWidth, buttonHeight);
            raceButton.addActionListener(f -> startGame(GameMode.RACE));
            add(raceButton);
        }
        
        //Funció per poder pintar el main menu
        @Override
        protected void paintComponent(Graphics g)
        {
            //Cridar a la funció base per comporbar el funcionament
            super.paintComponent(g);
        
            g.setColor(new Color(30, 30, 60));
            g.fillRect(0, 0, getWidth(), getHeight());
            
            // Títol per defecte
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String title = "F1 2D";
            int titleWidth = g.getFontMetrics().stringWidth(title);
            g.drawString(title, (getWidth() - titleWidth) / 2, getHeight() / 3);
        }

        public JButton getRaceButton() {return raceButton;}
        public JButton getQualyButton() {return qualyButton;}
    }
    
    //Main per executar el joc
    public static void main(String[] args) 
    {
        double scaleFactor = 4.0;
        BufferedImage largeMap = null;
        BufferedImage collisionMap = null;

        largeMap = loadImage("/track/monaco.jpg", scaleFactor);
        collisionMap = loadImage("/track/monacoCollision.jpg", scaleFactor);

        // Crear el cotxe i el controlador
        Car car = new Car(880, 1780, 267, 0, 30, -5, 0.2, 0.5, largeMap.getHeight() - 150, largeMap.getWidth() - 200);
        CarController controller = new CarController(car);

        // Crear i mostrar la finestra del joc
        JFrame frame = createGame(controller, largeMap, collisionMap);
        frame.setVisible(true);
    }

}
