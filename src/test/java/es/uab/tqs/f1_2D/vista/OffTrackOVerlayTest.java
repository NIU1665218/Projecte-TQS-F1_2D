package es.uab.tqs.f1_2D.vista;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sound.sampled.Clip;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OffTrackOverlayTest {

    private OffTrackOverlay overlay;

    @Mock
    private Clip mockClip;

    @BeforeEach
    void setUp() {
        overlay = new OffTrackOverlay("", "");  
        overlay.setClip(mockClip);
    }

    @Test
    void testShowOverlaySound() {
        overlay.showOverlay();

        assertTrue(overlay.isVisible());
        verify(mockClip, times(1)).setFramePosition(0);
        verify(mockClip, times(1)).start();
    }

    @Test
    void testShowOverlaySoundTwice() {
        overlay.showOverlay();
        overlay.showOverlay(); 

        verify(mockClip, times(1)).start();
    }

    @Test
    void testHideOverlay() {
        overlay.showOverlay();
        overlay.hideOverlay();
        assertFalse(overlay.isVisible());
    }
}
