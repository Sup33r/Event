package live.supeer.event;

import lombok.Getter;

import java.util.List;

@Getter
public class GameMap {
    private final String worldName;
    private final String triggerBlock;
    private final List<String> breakableBlocks;

    public GameMap(String worldName, String triggerBlock, List<String> breakableBlocks) {
        this.worldName = worldName;
        this.triggerBlock = triggerBlock;
        this.breakableBlocks = breakableBlocks;
    }

}