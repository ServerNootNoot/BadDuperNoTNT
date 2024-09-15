package lol.nightshade.baddupernotnt;

import org.bukkit.plugin.java.JavaPlugin;

public class BadDuperNoTNT extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        ChestProtectionUtil.initialize(this);
        getServer().getPluginManager().registerEvents(new ChestProtectionListener(this), this);

        getLogger().info("BadDuperNoTNT has been enabled!");
    }

    @Override
    public void onDisable() {
        ChestProtectionUtil.clearOpenContainers();
        getLogger().info("BadDuperNoTNT has been disabled!");
    }
}