package net.toolan.doorplugin;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jonathan on 26/03/2017.
 */
public class DoorInterpreterTest {
    @Test
    public void interpretMaterial() throws Exception {
        String material = "STONE";
        MaterialData actual = DoorInterpreter.InterpretMaterial(material);
        assertEquals("Material matches stone", Material.STONE, actual.getItemType());

        String material2 = "STONE,3";
        MaterialData actual2 = DoorInterpreter.InterpretMaterial(material2);
        assertEquals("Material matches stone", Material.STONE, actual2.getItemType());
        assertEquals("Data matches 3", 3, actual2.getData());
    }
}