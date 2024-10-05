package live.supeer.event;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;

@Getter
public class GameMap {
    private final String worldName;
    private final Material triggerBlock;
    private final List<String> breakableBlocks;
    private final Integer elimHeight;
    private final Location centerLocation;

    public GameMap(String worldName, Material triggerBlock, List<String> breakableBlocks, Integer elimHeight, Location centerLocation) {
        this.worldName = worldName;
        this.triggerBlock = triggerBlock;
        this.breakableBlocks = breakableBlocks;
        this.elimHeight = elimHeight;
        this.centerLocation = centerLocation;
    }

}