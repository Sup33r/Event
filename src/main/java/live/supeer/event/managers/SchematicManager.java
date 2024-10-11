package live.supeer.event.managers;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.regions.Region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class SchematicManager {
    private final Path schematicsDirectory = Path.of("schematics");

    public Map<File, String> listSchematicsForGame(String gameType) {
        Map<File, String> schematics = new HashMap<>();
        File gameTypeFolder = schematicsDirectory.resolve(gameType).toFile();
        if (gameTypeFolder.exists() && gameTypeFolder.isDirectory()) {
            for (File difficultyFolder : Objects.requireNonNull(gameTypeFolder.listFiles(File::isDirectory))) {
                File[] difficultySchematics = listSchematics(gameType, difficultyFolder.getName());
                for (File schematic : difficultySchematics) {
                    schematics.put(schematic, difficultyFolder.getName());
                }
            }
        }
        return schematics;
    }

    public File[] listSchematics(String gameType, String difficulty) {
        return Optional.ofNullable(schematicsDirectory.resolve(gameType).resolve(difficulty).toFile().listFiles((dir, name) -> name.endsWith(".schem")))
                .orElse(new File[0]);
    }

    public Clipboard loadSchematic(File schematicFile) {
        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            Bukkit.getLogger().warning("Unknown schematic format: " + schematicFile.getName());
            return null;
        }
        try (FileInputStream fis = new FileInputStream(schematicFile);
             ClipboardReader reader = format.getReader(fis)) {
            return reader.read();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void pasteSchematic(World world, Clipboard clipboard, Location location) {
        try (EditSession editSession = com.sk89q.worldedit.WorldEdit.getInstance().newEditSession(new BukkitWorld(world))) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                    .ignoreAirBlocks(false)
                    .build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }

    public List<BlockVector3> findLightBlocks(Clipboard clipboard, int targetLevel) {
        List<BlockVector3> lightBlocks = new ArrayList<>();
        Region region = clipboard.getRegion();

        for (BlockVector3 position : region) {
            BlockState blockState = clipboard.getBlock(position);

            // Check if the block is a light block
            if (blockState.getBlockType().id().equals("minecraft:light")) {

                // Extract NBT data from the block state
                CompoundTag nbtData = blockState.getNbtData();

                // Check if the NBT contains the level information
                if (nbtData != null && nbtData.containsKey("level")) {
                    int level = nbtData.getInt("level"); // Retrieve the level

                    // If the level matches the target level, add the block's position to the list
                    if (level == targetLevel) {
                        lightBlocks.add(position.subtract(clipboard.getOrigin()));
                    }
                }
            }
        }

        return lightBlocks;
    }


}