package pw.biome.biomedodgeball;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
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

        saveDefaultConfig();

        biomeChatHook();
    }

    @Override
    public void onDisable() {

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

                if (dodgeballPlayer.getCurrentTeam() != null) {
                    int score = dodgeballPlayer.getHits();
                    player.setPlayerListName(playerCache.getRank().getPrefix() + player.getDisplayName() + ChatColor.GOLD + " | " + score); //todo something in name to distinguish those who are playing
                } else {
                    player.setPlayerListName(playerCache.getRank().getPrefix() + player.getDisplayName());
                }
            }
        });
    }
}
