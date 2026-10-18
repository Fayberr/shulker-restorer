package dev.fayber.shulkerrestorer;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ShulkerRestorer implements ModInitializer {
    public static final String MOD_ID = "shulker-restorer";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Shulker Restorer: 1.20.5 Shulker Bullet redirects, hit order, collision margin, and teleport targets enabled.");
    }
}
