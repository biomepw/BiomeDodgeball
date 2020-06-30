package pw.biome.biomedodgeball.objects;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import pw.biome.biomedodgeball.BiomeDodgeball;

import java.util.ArrayList;
import java.util.List;

public class DodgeballTeam {

    @Getter
    private final List<DodgeballPlayer> teamMembers = new ArrayList<>();

    @Getter
    private final Location spawnLocation;

    @Getter
    private final String teamName;

    @Getter
    private final ChatColor teamColour;

    @Getter
    private final ItemStack[] contents;

    private static final ScoreboardManager manager = Bukkit.getScoreboardManager();

    @Getter
    private final Scoreboard scoreboard = manager.getNewScoreboard();

    private Team scoreboardTeam;

    @Getter
    private Objective stats;

    public DodgeballTeam(String teamName, Location spawnLocation, ChatColor teamColour) {
        this.teamName = teamName;
        this.spawnLocation = spawnLocation;
        this.teamColour = teamColour;
        this.contents = loadInventory();

        loadScoreboard();

        BiomeDodgeball.getInstance().getGameManager().getDodgeballTeams().add(this);
    }

    public static DodgeballTeam getFromName(String search) {
        for (DodgeballTeam dodgeballTeam : BiomeDodgeball.getInstance().getGameManager().getDodgeballTeams()) {
            if (dodgeballTeam.getTeamName().equalsIgnoreCase(search)) return dodgeballTeam;
        }
        return null;
    }

    private static ItemStack[] loadInventory() {
        ItemStack snowballs = new ItemStack(Material.SNOWBALL, 10);

        return new ItemStack[]{snowballs};
    }


    public void loadScoreboard() {
        scoreboardTeam = scoreboard.registerNewTeam(teamName);
        stats = scoreboard.registerNewObjective("Stats", "dummy", teamColour + "Stats: ");

        scoreboardTeam.setAllowFriendlyFire(false);
        scoreboardTeam.setCanSeeFriendlyInvisibles(true);
        scoreboardTeam.setDisplayName(teamColour + teamName);
        scoreboardTeam.setColor(teamColour);

        stats.setDisplaySlot(DisplaySlot.SIDEBAR);

        teamMembers.forEach(dodgeballPlayer -> scoreboardTeam.addEntry(dodgeballPlayer.getDisplayName()));

        displayScoreboard();
    }

    /**
     * Helper method to display scoreboard to all members async
     */
    public void displayScoreboard() {
        Bukkit.getScheduler().runTaskAsynchronously(BiomeDodgeball.getInstance(), () -> teamMembers.forEach(DodgeballPlayer::displayScoreboard));
    }

    public void teleportMembersToSpawn() {
        teamMembers.forEach(dodgeballPlayer -> {
            dodgeballPlayer.getPlayerObject().teleportAsync(spawnLocation);
        });
    }

    public void addMember(DodgeballPlayer dodgeballPlayer) {
        Bukkit.broadcastMessage(ChatColor.GOLD + dodgeballPlayer.getDisplayName() + " has joined " + teamColour + teamName);

        teamMembers.add(dodgeballPlayer);

        dodgeballPlayer.setCurrentTeam(this);
    }

    public void removeMember(DodgeballPlayer dodgeballPlayer) {
        Bukkit.broadcastMessage(ChatColor.GOLD + dodgeballPlayer.getDisplayName() + " has left " + teamColour + teamName);

        teamMembers.remove(dodgeballPlayer);

        dodgeballPlayer.restoreInventory();
        dodgeballPlayer.setCurrentTeam(null);
    }

    public int getCurrentlyIn() {
        int currentlyIn = 0;

        for (DodgeballPlayer teamMember : getTeamMembers()) {
            if (teamMember.isCurrentlyIn()) currentlyIn++;
        }

        return currentlyIn;
    }

    public String getColouredName() {
        return teamColour + teamName;
    }
}
