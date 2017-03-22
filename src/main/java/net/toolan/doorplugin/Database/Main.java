package net.toolan.doorplugin.Database;

// rename this to the local plugin when copying.
import net.toolan.doorplugin.doorplugin;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.logging.Logger;

/**
 * Created by jonathan on 18/03/2017.
 */
public class Main {
    private doorplugin _plugin;
    Main(doorplugin plugin) {
        _plugin = plugin;
    }
    FileConfiguration getConfig() { return _plugin.getConfig(); }
    Logger getLogger() { return _plugin.getLogger(); }
    File getDataFolder() { return _plugin.getDataFolder(); }
}
