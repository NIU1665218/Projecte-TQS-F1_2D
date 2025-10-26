package es.uab.tqs.f1_2D.vista;

import es.uab.tqs.f1_2D.model.Map;
import es.uab.tqs.f1_2D.model.Map.SectorColor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

@ExtendWith(MockitoExtension.class)
class LapUITest {

    private LapUI lapUI;
    //Ruta relativa al archivo de fuente personalizada para test válido
    private String fontPath = "/fonts/Formula1-Bold.ttf";
    //Ruta relativa a la imagen de fondo del overlay para test válido
    private String pathOverlay = "/img/laptime.jpg";
    //Mock del modelo Map para simular comportamiento
    @Mock
    private Map mockMap;
    //Graphics2D para pruebas de pintado
    private Graphics2D g2d;

    /* 
      ================================================================================
        TEST SOBRE VISTA, NO ES DEMANA PERO EM SERVEIX PER VERIFICAR FUNCIONAMENT
      ================================================================================
    */

    @BeforeEach
    void setUp() 
    {
        //Instancia de LapUI con el mock del mapa
        lapUI = new LapUI(mockMap);
    }

    //Test para verificar el formateo correcto de tiempos
    @Test
    void testFormatTimeCases() {
        //Verifica que 0 milisegundos se formatee correctamente
        assertEquals("0.000", lapUI.formatTime(0));
        //Verifica que 1000 milisegundos se formatee como "1,000 s"
        assertEquals("1,000 s", lapUI.formatTime(1000));
    }

    //Test para verificar la carga de imágenes de sectores
    @Test
    void testLoadSectorImages() {
        //Verifica que todas las imágenes de sectores (3x4) se carguen correctamente
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                assertNotNull(lapUI.getSectorImage(i, j));
            }
        }

        //Verifica que no se lance excepción al intentar cargar rutas inválidas o nulas
        assertDoesNotThrow(()-> lapUI.loadSectorImages("/img/noexiste.png", 0, 0));
        assertDoesNotThrow(()-> lapUI.loadSectorImages(null, 0, 0));
    }

    //Test para verificar la carga de fuentes
    @Test
    void testLoadFont() {
        //Verifica carga exitosa de fuente existente
        assertDoesNotThrow(() -> lapUI.loadFont(fontPath));
        assertNotNull(lapUI.getFont());
        //Verifica que no falle con rutas de fuente inválidas
        assertDoesNotThrow(()-> lapUI.loadFont("/font/noexiste.tff"));
        assertDoesNotThrow(()-> lapUI.loadFont(null));
    }

    //Test para verificar la carga del overlay de fondo
    @Test
    void testLoadOverlay() {
        //Verifica carga exitosa de overlay existente
        assertDoesNotThrow(() -> lapUI.loadOverlay(pathOverlay));
        assertNotNull(lapUI.getOverlayBackground());
        //Verifica comportamiento con rutas inválidas
        assertDoesNotThrow(()-> lapUI.loadOverlay("/img/noexiste.jpg"));
        assertNotNull(lapUI.getOverlayBackground());
        assertDoesNotThrow(()-> lapUI.loadOverlay(null));
        assertNotNull(lapUI.getOverlayBackground());
    }

    // Test verificar los diferentes colores en paintComponent según estados
    @Test
    void testPaintComponentColors()
    {
        //Graphics2D real para pruebas de pintado
        g2d = (Graphics2D) new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB).getGraphics();

        //Estado OFF_TRACK debería pintar en ROJO y Sector en verde no debería dar excepción
        when(mockMap.getSectorColor(anyInt())).thenReturn(SectorColor.GREEN);
        when(mockMap.getState()).thenReturn(Map.State.OFF_TRACK);
        assertDoesNotThrow(()-> lapUI.paintComponent(g2d));

        //Estado INVALID_LAP debería pintar en ROJO y Sector en naranja no debería dar excepción
        when(mockMap.getState()).thenReturn(Map.State.INVALID_LAP);
        when(mockMap.getSectorColor(anyInt())).thenReturn(SectorColor.ORANGE);
        assertDoesNotThrow(()-> lapUI.paintComponent(g2d));

        //Estado RESULT con tiempo peor que el mejor debería pintar en AMARILLO y Sector morado no debería dar excepción
        when(mockMap.getState()).thenReturn(Map.State.RESULT);
        when(mockMap.getBestLapTime()).thenReturn(1000L);
        when(mockMap.getLapTime()).thenReturn(2000L);
        when(mockMap.getSectorColor(anyInt())).thenReturn(SectorColor.PURPLE);
        assertDoesNotThrow(()-> lapUI.paintComponent(g2d));

        //Estado RESULT con tiempo mejor que el mejor debería pintar en VERDE y no dar excepción
        when(mockMap.getBestLapTime()).thenReturn(2000L);
        when(mockMap.getLapTime()).thenReturn(1000L);
        assertDoesNotThrow(()-> lapUI.paintComponent(g2d));
    }
    
}
