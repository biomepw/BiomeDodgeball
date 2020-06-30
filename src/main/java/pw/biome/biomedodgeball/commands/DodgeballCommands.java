package pw.biome.biomedodgeball.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pw.biome.biomedodgeball.BiomeDodgeball;
import pw.biome.biomedodgeball.objects.DodgeballPlayer;
import pw.biome.biomedodgeball.objects.DodgeballTeam;
import pw.biome.biomedodgeball.objects.GameManager;

public class DodgeballCommands implements CommandExecutor {


    //todo tidy
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        GameManager gameManager = BiomeDodgeball.getInstance().getGameManager();

        if (sender instanceof Player) {
            Player player = (Player) sender;

            DodgeballPlayer dodgeballPlayer = DodgeballPlayer.getFromUUID(player.getUniqueId());

            if (dodgeballPlayer == null) {
                dodgeballPlayer = new DodgeballPlayer(player);
            }

            if (args.length > 1) {
                if (args[0].equalsIgnoreCase("join")) {
                    if (!gameManager.isGameRunning()) {
                        if (args.length == 2) {
                            DodgeballTeam dodgeballTeam = DodgeballTeam.getFromName(args[1]);

                            if (dodgeballTeam != null) {
                                dodgeballTeam.addMember(dodgeballPlayer);
                            }
                        } else {
                            gameManager.getQueuedPlayers().add(dodgeballPlayer);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("leave")) {
                    if (dodgeballPlayer.getCurrentTeam() != null) {
                        dodgeballPlayer.getCurrentTeam().removeMember(dodgeballPlayer);
                    }
                } else if (args[0].equalsIgnoreCase("create")) {
                    if (player.hasPermission("dodgeball.admin")) {
                        if (!gameManager.isGameRunning()) {
                            if (args.length == 3) {
                                String teamName = args[1];
                                ChatColor teamColour = ChatColor.valueOf(args[2]);
                                Location spawnLocation = new Location(player.getWorld(), 0, 0, 0); //todo

                                DodgeballTeam dodgeballTeam = new DodgeballTeam(teamName, spawnLocation, teamColour);

                                player.sendMessage(ChatColor.GREEN + "Team '" + teamName + " created");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Currently a game running!");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("start")) {
                    if (player.hasPermission("dodgeball.admin")) {
                        if (!gameManager.isGameRunning()) {
                            gameManager.startGame();
                            player.sendMessage(ChatColor.GREEN + "Starting game!");
                        }
                    }
                }
            }
        }
        return true;
    }
}
