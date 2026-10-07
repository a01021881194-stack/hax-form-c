package dev.cheatclient;

import java.util.EnumMap;
import java.util.Map;

public final class Config {
    public static boolean storageEsp = true;
    public static boolean freecam = false;
    public static boolean susFinder = true;
    public static boolean susDeepslate = true;
    public static boolean susAmethyst = true;
    /** Min fully grown amethyst clusters in a chunk to flag it. */
    public static int amethystThreshold = 1;
    public static double freecamSpeed = 1.0;

    public static final Map<StorageType, Boolean> storage = new EnumMap<>(StorageType.class);
    static {
        for (StorageType t : StorageType.values()) storage.put(t, true);
    }

    private Config() {}
}
