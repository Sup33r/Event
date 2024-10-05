package live.supeer.event.managers;

import live.supeer.event.GameMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class MapManager {
    private final Path mapsDirectory = Paths.get("maps"); // Directory where maps are stored

    // Load a map into the server
    public World loadMap(GameMap gameMap) {
        String mapName = gameMap.getWorldName();
        Path sourcePath = mapsDirectory.resolve(mapName);
        Path targetPath = Paths.get(Bukkit.getWorldContainer().getAbsolutePath(), mapName);

        World world = Bukkit.getWorld(mapName);
        if (world != null) {
            Bukkit.unloadWorld(world, false);
        }
        deleteDirectory(targetPath.toFile());

        // Copy map files
        try {
            copyDirectory(sourcePath, targetPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Delete uid.dat file to avoid duplicate world issues
        File uidFile = targetPath.resolve("uid.dat").toFile();
        if (uidFile.exists()) {
            uidFile.delete();
        }

        // Create and load the world
        WorldCreator worldCreator = new WorldCreator(mapName);
        return Bukkit.createWorld(worldCreator);
    }

    // Copy directory utility
    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(s -> {
            try {
                Path d = target.resolve(source.relativize(s));
                Files.copy(s, d, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // Delete directory utility
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                Arrays.stream(files).forEach(file -> {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                });
            }
            directory.delete();
        }
    }
}