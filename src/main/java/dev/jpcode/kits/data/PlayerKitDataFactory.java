package dev.jpcode.kits.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.Level;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.network.ServerPlayerEntity;

import dev.jpcode.kits.KitsMod;

public final class PlayerKitDataFactory {

    private PlayerKitDataFactory() {}

    public static PlayerKitUsageData create(ServerPlayerEntity player) {
        File saveFile = getPlayerDataFile(player);

        PlayerKitUsageData playerData = new PlayerKitUsageData(player, saveFile,
            KitsMod.CONFIG.starterKitStorageLocation.getValue() == StorageLocation.Local ? new NbtStarterKitStorage() : new MySQLStarterKitStorage(player),
            KitsMod.CONFIG.kitCooldownStorageLocation.getValue() == StorageLocation.Local ? new NbtKitCooldownStorage() : new MySQLKitCooldownStorage(player));

        if (Files.exists(saveFile.toPath()) && saveFile.length() != 0) {
            try {
                NbtCompound nbtCompound = NbtIo.readCompressed(new FileInputStream(saveFile));
                playerData.fromNbt(nbtCompound);

            } catch (IOException e) {
                KitsMod.LOGGER.warn("Failed to load kits player data for {" + player.getName().getString() + "}");
                e.printStackTrace();
            }
        }
        return playerData;
    }

    private static File getPlayerDataFile(ServerPlayerEntity player) {
        Path dataDirectoryPath;
        File playerDataFile = null;
        try {
            try {
                dataDirectoryPath = Files.createDirectories(KitsMod.getUserDataDirDir());
            } catch (NullPointerException e) {
                dataDirectoryPath = Files.createDirectories(Paths.get("./world/modplayerdata/"));
                KitsMod.LOGGER.log(Level.WARN, "Session save path could not be found. Defaulting to ./world/modplayerdata");
            }
            playerDataFile = dataDirectoryPath.resolve(player.getUuidAsString() + ".nbt").toFile();
            playerDataFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return playerDataFile;
    }

}
