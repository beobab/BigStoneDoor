package net.toolan.doorplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jonathan on 24/03/2017.
 * Listens for click events, and sees if they are on a trigger for a door.
 * Must bail as soon as possible to prevent lag on clicks.
 */
public class DoorBell implements Listener {

    interface OperateDoor {
        void operation(PlayerInteractEvent e);
    }

    private Map<String, OperateDoor> _playerOperations = new HashMap<>();
    private Map<String, List<OperateDoor>> _itemOperations = new HashMap<>();

    private static String PlayerKey(Player p) {
        return (p == null ? "" : p.getName());
    }

    private static String BlockKey(Block b) {
        // X|Y|Z|world|material
        Location l = (b == null ? null : b.getLocation());
        World w = (l == null ? null : l.getWorld());
        String wn = (w == null ? "" : w.getName());
        String m = (b == null ? "" : b.getType().toString());

        if (l == null) return "0|0|0|" + wn + "|" + m;

        return Integer.toString(l.getBlockX()) + "|" +
                Integer.toString(l.getBlockY()) + "|" +
                Integer.toString(l.getBlockZ()) + "|" +
                wn + "|" + m;
    }

    // The doorbell.
    @EventHandler
    public void onButtonClick(PlayerInteractEvent e) {
        Block b = e.getClickedBlock();
        if (b == null) return;

        Action a = e.getAction();
        if (a == null) return;

        // Must switch a lever/button or step on a pressure plate.
        if (a != Action.RIGHT_CLICK_BLOCK && a != Action.PHYSICAL) return;

        Material clicker = b.getType();
        if (clicker == null) return;

        boolean isCorrectMaterial = (
// Single click items act like toggles.
                clicker == Material.STONE_BUTTON
                        || clicker == Material.WOOD_BUTTON
                        || clicker == Material.STONE_PLATE
                        || clicker == Material.WOOD_PLATE
// --  Need work to work out how pressure plates will work.
//            || clicker == Material.IRON_PLATE
//            || clicker == Material.GOLD_PLATE

// The lever is a real toggle. Should probably get it's state and open/close specifically.
                        || clicker == Material.LEVER
        );
        // Short circuit out asap if it's not interesting.
        if (!isCorrectMaterial) return;

        // See if any setup things are running for this player.
        final String playerKey = PlayerKey(e.getPlayer());
        OperateDoor operateDoor = _playerOperations.get(playerKey);
        if (operateDoor != null)
            operateDoor.operation(e);

        // This might hang on to references. It might be better to hang on to names of blocks somehow.
        final String blockKey = BlockKey(b);
        List<OperateDoor> operations = _itemOperations.get(blockKey);
        if (operations != null)
            for (OperateDoor op : operations)
                op.operation(e);
    }

    public void setDoorBell(String blockKey, BigDoor door) {
        List<OperateDoor> operateDoors = _itemOperations.get(blockKey);
        if (operateDoors == null) operateDoors = new ArrayList<>();
        operateDoors.add(x -> door.Toggle());
        _itemOperations.put(blockKey, operateDoors);
    }

    public void NextPlayerClickSetsDoorbell(Player player, BigDoor door){
        final String playerKey = PlayerKey(player);

        OperateDoor setTriggerOnDoor = (PlayerInteractEvent e) -> {
            Block b = e.getClickedBlock();
            door.AddTrigger(BlockKey(b));

            setDoorBell(BlockKey(b), door);
            e.getPlayer().sendMessage("This will now open and close door: " + door.Name);

            // remove this op.
            _playerOperations.remove(playerKey);
        };
        _playerOperations.put(playerKey, setTriggerOnDoor);
    }

}
