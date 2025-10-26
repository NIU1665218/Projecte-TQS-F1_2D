package es.uab.tqs.f1_2D.vista;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sound.sampled.Clip;

import static org.mockito.Mockito.*;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OffTrackOverlayTest {

    private OffTrackOverlay overlay;

    // Mock del Clip de audio para simular sonidos sin dependencias reales
    @Mock
    private Clip mockClip;

    // Graphics2D para pruebas de pintado
    private Graphics2D g2d;

    //Ruta relativa al archivo de icon para test válido
    private String imagePath = "/img/lapdeleted.png";
    //Ruta relativa al archivo de sonido para test válido
    private String soundPath = "/sound/Popup.wav";

    @BeforeEach
    void setUp() {
        overlay = new OffTrackOverlay("", "");
        //Inyecta el mock del clip para controlar el comportamiento del sonido  
        overlay.setClip(mockClip);
    }

    @Test
    void testLoadImage()
    {
        //Verifica carga exitosa de overlay existente
        assertDoesNotThrow(() -> overlay.loadImage(imagePath));
        assertNotNull(overlay.getImage());

        //Verifica comportamiento con rutas inválidas
        assertDoesNotThrow(()-> overlay.loadImage("/img/noexiste.jpg"));
        assertNull(overlay.getImage());
        assertDoesNotThrow(()-> overlay.loadImage(null));
        assertNull(overlay.getImage());
    }

    @Test
    void testLoadSound()
    {
        //Verifica carga exitosa de overlay existente
        assertDoesNotThrow(() -> overlay.loadSound(soundPath));
        assertNotNull(overlay.getSound());

        //Verifica comportamiento con rutas inválidas
        assertDoesNotThrow(()-> overlay.loadSound("/sound/noexiste.wav"));
        assertNull(overlay.getSound());
        assertDoesNotThrow(()-> overlay.loadSound(null));
        assertNull(overlay.getSound());
        assertDoesNotThrow(()-> overlay.loadSound("sound/noformato.mp4"));
        assertNull(overlay.getSound());
    }

    //Test para verificar que mostrar el overlay reproduce el sonido correctamente
    @Test
    void testShowOverlaySound() { 
        overlay.showOverlay();

        //Verifica que una vez se ejecuta, el overlay se vuelve visible y se reproduce el sonido
        assertTrue(overlay.isVisible());
        verify(mockClip, times(1)).setFramePosition(0);
        verify(mockClip, times(1)).start();
    }

    //Test para verificar que mostrar el overlay reproduce el sonido correctamente
    @Test
    void testShowOverlaySoundTwice() {
        //Muestra el overlay dos veces seguidas
        overlay.showOverlay();
        overlay.showOverlay(); 

        //Verifica que el sonido solo se reproduce una vez para evitar repeticiones seguidas  
        verify(mockClip, times(1)).start();
    }

    //Test para verificar que ocultar el overlay funciona correctamente
    @Test
    void testHideOverlay() {
        //Primero mostramos el overlay para cambiar el valor
        overlay.showOverlay();
        //Ocultamos el overlay para volver a cambiar el valor
        overlay.hideOverlay();
        //Verificamos que efectivamente se oculta el overlay
        assertFalse(overlay.isVisible());
    }

    // Test del método paintComponent bajo diferentes condiciones
    @Test
    void testPaintComponent()
    {
        g2d = (Graphics2D) new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB).getGraphics();

        //Overlay invisible y sin icono
        overlay.setvisible(false);
        overlay.setIcon(null);
        assertDoesNotThrow(() -> overlay.paintComponent(g2d));
        
        //Overlay invisible pero con imagen cargada
        overlay.loadImage(imagePath);
        assertDoesNotThrow(() -> overlay.paintComponent(g2d));

        //Overlay visible con imagen
        overlay.setvisible(true);
        assertDoesNotThrow(() -> overlay.paintComponent(g2d));  
        
        //Overlay visible pero sin imagen 
        overlay.setIcon(null);
        assertDoesNotThrow(() -> overlay.paintComponent(g2d));  
    }

    //Test para verificar el comportamiento cuando no hay sonido
    @Test
    void testNoSound()
    {
        // Configura el overlay sin Clip de sonido
        overlay.setClip(null);
        // Verifica que no se lance excepción al intentar reproducir sonido
        assertDoesNotThrow(() -> overlay.playSound());
    }
}
