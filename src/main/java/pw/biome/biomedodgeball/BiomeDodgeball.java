package pw.biome.biomedodgeball;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pw.biome.biomechat.BiomeChat;
import pw.biome.biomechat.obj.PlayerCache;
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
        getCommand("dodgeball").setExecutor(new DodgeballCommands());

        // Add config defaults
        World world = getServer().getWorld("world");
        getConfig().addDefault("red.spawn_location", world.getSpawnLocation());
        getConfig().addDefault("blue.spawn_location", world.getSpawnLocation());
        getConfig().addDefault("lobby.spawn_location", world.getSpawnLocation());
        saveDefaultConfig();

        biomeChatHook();
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    private void biomeChatHook() {
        BiomeChat biomeChat = BiomeChat.getPlugin();
        biomeChat.stopScoreboardTask();

        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::updateScoreboards, 20, 20);
    }

    /**
     * Helper method to show the stats of players in the scoreboard
     */
    private void updateScoreboards() {
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            ImmutableList<Player> playerList = ImmutableList.copyOf(getServer().getOnlinePlayers());
            for (Player player : playerList) {
                PlayerCache playerCache = PlayerCache.getFromUUID(player.getUniqueId());

                if (playerCache == null) return;
                DodgeballPlayer dodgeballPlayer = DodgeballPlayer.getFromUUID(player.getUniqueId());

                player.setPlayerListHeader(ChatColor.BLUE + "Biome");

                if (dodgeballPlayer != null && dodgeballPlayer.getCurrentTeam() != null) {
                    int lives = dodgeballPlayer.getLives();
                    player.setPlayerListName(dodgeballPlayer.getCurrentTeam().getTeamColour() + player.getDisplayName() + ChatColor.GOLD + " | Lives:" + lives);
                } else {
                    player.setPlayerListName(playerCache.getRank().getPrefix() + player.getDisplayName());
                }
            }
        });
    }
}
