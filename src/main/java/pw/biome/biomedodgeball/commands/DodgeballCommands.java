package pw.biome.biomedodgeball.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pw.biome.biomedodgeball.objects.DodgeballPlayer;
import pw.biome.biomedodgeball.objects.DodgeballTeam;
import pw.biome.biomedodgeball.objects.GameManager;

@CommandAlias("dodgeball|db")
@Description("Dodgeball commands")
public class DodgeballCommands extends BaseCommand {

    @Dependency
    private GameManager gameManager;

    @Subcommand("join")
    @Description("Joins a dodgeball game")
    public void onDodgeballJoin(DodgeballPlayer dodgeballPlayer, @Optional String team) {
        if (dodgeballPlayer.getCurrentTeam() == null && !gameManager.getQueuedPlayers().contains(dodgeballPlayer)) {
            if (team != null) {
                DodgeballTeam dodgeballTeam = DodgeballTeam.getFromName(team);

                if (dodgeballTeam != null) {
                    dodgeballTeam.addMember(dodgeballPlayer);
                    dodgeballPlayer.getPlayerObject().sendMessage(ChatColor.DARK_AQUA + "You have joined the team '" +
                            dodgeballTeam.getColouredName() + ChatColor.DARK_AQUA + "'!");
                }
            } else {
                gameManager.getQueuedPlayers().add(dodgeballPlayer);
                Bukkit.broadcastMessage(ChatColor.GOLD + dodgeballPlayer.getDisplayName() + ChatColor.DARK_AQUA + " has just joined the queue!");
            }
        }
        gameManager.processAutoRun();
    }

    @Subcommand("leave")
    @Description("Joins a dodgeball game")
    public void onDodgeballLeave(DodgeballPlayer dodgeballPlayer) {
        if (gameManager.getQueuedPlayers().contains(dodgeballPlayer)) {
            gameManager.getQueuedPlayers().remove(dodgeballPlayer);
            Bukkit.broadcastMessage(ChatColor.GOLD + dodgeballPlayer.getDisplayName() + ChatColor.DARK_AQUA + " has just left the queue!");
        } else {
            dodgeballPlayer.getPlayerObject().sendMessage(ChatColor.RED + "You're not in the queue!");
        }
    }

    @Subcommand("create")
    @CommandPermission("dodgeball.admin")
    @Description("Creates a dodgeball team")
    public void onDodgeballCreate(Player player, String teamName, String teamColourString) {
        if (!gameManager.isGameRunning()) {
            if (teamName != null && teamColourString != null) {
                ChatColor teamColour = ChatColor.valueOf(teamColourString);
                Location spawnLocation = player.getLocation();
                DodgeballTeam dodgeballTeam = new DodgeballTeam(teamName, spawnLocation, teamColour);
                player.sendMessage(ChatColor.DARK_AQUA + "Team '" + dodgeballTeam.getColouredName() + ChatColor.DARK_AQUA + " created");
            } else {
                player.sendMessage(ChatColor.RED + "/db create <team name> <team colour>");
            }
        }
    }

    @Subcommand("start")
    @CommandPermission("dodgeball.admin")
    @Description("Starts dodgeball game")
    public void onDodgeballStart(CommandSender sender) {
        if (!gameManager.isGameRunning()) {
            gameManager.startGame();
            sender.sendMessage(ChatColor.GREEN + "Attempting to start dodgeball game!");
        } else {
            sender.sendMessage(ChatColor.RED + "Game already running!");
        }
    }

    @Subcommand("set")
    @CommandPermission("dodgeball.admin")
    @Description("Adds location to dodgeball team")
    public void onDodgeballSet(Player player, String teamName) {
        if (teamName.equalsIgnoreCase("red")) {
            gameManager.setRedSpawnLocation(player.getLocation());
            player.sendMessage(ChatColor.DARK_AQUA + "Red team spawn location stored.");
        } else if (teamName.equalsIgnoreCase("blue")) {
            gameManager.setBlueSpawnLocation(player.getLocation());
            player.sendMessage(ChatColor.DARK_AQUA + "Blue team spawn location stored.");
        } else if (teamName.equalsIgnoreCase("lobby")) {
            gameManager.setLobbyLocation(player.getLocation());
            player.sendMessage(ChatColor.DARK_AQUA + "Lobby location stored.");
        } else if (teamName.equalsIgnoreCase("spectator")) {
            gameManager.addSpectatorLocation(player.getLocation());
            player.sendMessage(ChatColor.DARK_AQUA + "Spectator location stored.");
        }
    }

    @Subcommand("autostart")
    @CommandPermission("dodgeball.admin")
    @Description("Toggles autostart of future dodgeball games")
    public void onDodgeballAutostart(CommandSender sender) {
        gameManager.setAutoRun(!gameManager.isAutoRun());
        sender.sendMessage(ChatColor.DARK_AQUA + "Autorun now: " + gameManager.isAutoRun());
    }

    @Subcommand("stop")
    @CommandPermission("dodgeball.admin")
    @Description("Stops current dodgeball game")
    public void onDodgeballStop(CommandSender sender) {
        if (gameManager.isGameRunning()) {
            gameManager.stopGame();
            sender.sendMessage(ChatColor.GREEN + "Dodgeball game stopped!");
        } else {
            sender.sendMessage(ChatColor.RED + "No game running!");
        }
    }
}