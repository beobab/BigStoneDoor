package net.toolan.doorplugin;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jonathan on 24/03/2017
 * DoorSize tests.
 */
public class DoorSizeTest {

    @Test
    public void maxDepthIsOne() throws Exception {

        DoorSize dEmpty = new DoorSize(0,0,0);
        DoorSize dCube = new DoorSize(5,5,5);
        DoorSize dSmallX = new DoorSize(1,5,5);

        assertEquals("Z should be 1 on 5x5x5 cube", 1, dCube.Z);
        assertEquals("Z should be 0 on 0x0x0 cube", 0, dEmpty.Z);
        assertEquals("Z should be 5 on 1x5x5 cube", 5, dSmallX.Z);
    }

}