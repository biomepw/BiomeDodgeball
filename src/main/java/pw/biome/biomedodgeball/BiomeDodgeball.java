package pw.biome.biomedodgeball;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import pw.biome.biomedodgeball.commands.DodgeballCommands;
import pw.biome.biomedodgeball.listeners.DodgeballListener;
import pw.biome.biomedodgeball.objects.DodgeballPlayer;
import pw.biome.biomedodgeball.objects.GameManager;

public final class BiomeDodgeball extends JavaPlugin {

    @Getter
    private static BiomeDodgeball instance;

    @Getter
    private GameManager gameManager;

    @Override
    public void onEnable() {
        instance = this;
        this.gameManager = new GameManager();

        getServer().getPluginManager().registerEvents(new DodgeballListener(), this);

        PaperCommandManager manager = new PaperCommandManager(instance);
        manager.getCommandContexts().registerIssuerOnlyContext(DodgeballPlayer.class, DodgeballPlayer::getContext);
        manager.registerDependency(GameManager.class, gameManager);
        manager.registerCommand(new DodgeballCommands());

        // Add config defaults
        World world = getServer().getWorld("world");
        getConfig().addDefault("red.spawn_location", world.getSpawnLocation());
        getConfig().addDefault("blue.spawn_location", world.getSpawnLocation());
        getConfig().addDefault("lobby.spawn_location", world.getSpawnLocation());
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }
}
