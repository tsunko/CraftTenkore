package academy.hekiyou.tenkore.crafttenkore.doorimpl;

import academy.hekiyou.door.exception.BadInterpretationException;
import academy.hekiyou.door.interp.Interpreters;
import academy.hekiyou.door.model.Command;
import academy.hekiyou.tenkore.crafttenkore.CraftTenkore;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.UUID;

public class BukkitInterpreters {
    
    private static final String PLAYER_NOT_FOUND_MESSAGE = "No player was found with the name \"%s\".";
    private static final String WORLD_NOT_FOUND_MESSAGE = "No world was found with the name \"%s\".";
    private static final String PLUGIN_NOT_FOUND_MESSAGE = "No plugin was found with the name \"%s\".";
    private static final String OFFLINE_PLAYER_NOT_FOUND_MESSAGE = "No offline player has joined with the UUID \"%s\".";
    private static final String MATERIAL_NOT_FOUND_MESSAGE = "No such material with the name \"%s\".";
    private static final String GAMEMODE_NOT_FOUND_MESSAGE = "No such game mode by the name \"%s\".";
    private static final String COMMAND_NOT_FOUND_MESSAGE = "No command was found with the name \"%s\".";
    
    /**
     * Registers all the basic Bukkit interpreters
     */
    public static void register(){
        Interpreters.register(Player.class, BukkitInterpreters::toPlayer);
        Interpreters.register(HumanEntity.class, BukkitInterpreters::toExactPlayer);
        Interpreters.register(World.class, BukkitInterpreters::toWorld);
        Interpreters.register(Plugin.class, BukkitInterpreters::toPlugin);
        Interpreters.register(OfflinePlayer.class, BukkitInterpreters::toOfflinePlayer);
        Interpreters.register(Material.class, BukkitInterpreters::toMaterial);
        Interpreters.register(GameMode.class, BukkitInterpreters::toGameMode);
        Interpreters.register(CommandSender.class, BukkitInterpreters::toCommandSender);
        Interpreters.register(Command.class, BukkitInterpreters::toCommand);
    }
    
    /**
     * Interprets a {@link String} as the name (or partial name) of a {@link CommandSender}
     * @param strCommandSender The string representing a {@link CommandSender} name
     * @return The Bukkit {@link CommandSender} object
     * @throws BadInterpretationException If the input could not be interpreted as a {@link CommandSender} object
     */
    @NotNull
    public static CommandSender toCommandSender(@NotNull String strCommandSender){
        if(strCommandSender.equals("!"))
            return Bukkit.getConsoleSender();
        Player player = Bukkit.getPlayer(strCommandSender);
        if(player == null)
            throw new BadInterpretationException(String.format(PLAYER_NOT_FOUND_MESSAGE, strCommandSender));
        return player;
    }
    
    /**
     * Interprets a {@link String} as the name (or partial name) of a {@link Player}
     * @param strPlayer The string representing a {@link Player} name
     * @return The Bukkit {@link Player} object
     * @throws BadInterpretationException If the input could not be interpreted as a {@link Player} object
     */
    @NotNull
    public static Player toPlayer(@NotNull String strPlayer){
        Player player = Bukkit.getPlayer(strPlayer);
        if(player == null)
            throw new BadInterpretationException(String.format(PLAYER_NOT_FOUND_MESSAGE, strPlayer));
        return player;
    }

    /**
     * Interprets a {@link String} as the exact name of a {@link Player}
     * @param strPlayer The string representing a {@link Player} name
     * @return A {@link HumanEntity} object that is the {@link Player} (It should be safe to cast).
     * @throws BadInterpretationException If the input could not be interpreted as a {@link Player} object
     */
    @NotNull
    public static HumanEntity toExactPlayer(@NotNull String strPlayer){
        Player player = Bukkit.getPlayerExact(strPlayer);
        if(player == null)
            throw new BadInterpretationException(String.format(PLAYER_NOT_FOUND_MESSAGE, strPlayer));
        return player;
    }

    /**
     * Interprets a {@link String} as a {@link World}
     * @param strWorld The string representing a {@link World}'s name
     * @return The Bukkit {@link World} object
     * @throws BadInterpretationException If the input could not be interpreted as a {@link World} object
     */
    @NotNull
    public static World toWorld(@NotNull String strWorld){
        World world = Bukkit.getWorld(strWorld);
        if(world == null)
            throw new BadInterpretationException(String.format(WORLD_NOT_FOUND_MESSAGE, strWorld));
        return world;
    }
    
    /**
     * Interprets a {@link String} as a {@link Plugin}
     * @param strPlugin The string representing a {@link Plugin}'s name
     * @return The instance of the {@link Plugin}. Note: this does not guarantee a {@link org.bukkit.plugin.java.JavaPlugin}.
     * @throws BadInterpretationException If the input could not be interpreted as a {@link Plugin} object
     */
    @NotNull
    public static Plugin toPlugin(@NotNull String strPlugin){
        Plugin plugin = Bukkit.getPluginManager().getPlugin(strPlugin);
        if(plugin == null)
            throw new BadInterpretationException(String.format(PLUGIN_NOT_FOUND_MESSAGE, strPlugin));
        return plugin;
    }

    /**
     * Interprets a {@link String} as a UUID of an offline player
     * @param uuid The string representing the UUID of an offline player
     * @return An offline player with the UUID
     * @throws BadInterpretationException If the input could not be interpreted as an offline player's UUID or if the
     *                                    offline player has never played before.
     */
    @NotNull
    public static OfflinePlayer toOfflinePlayer(@NotNull String uuid){
        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
        if(!player.hasPlayedBefore())
            throw new BadInterpretationException(String.format(OFFLINE_PLAYER_NOT_FOUND_MESSAGE, uuid));
        return player;
    }
    
    /**
     * Interprets a {@link String} as a {@link Material}
     * @param material The string representing the {@link Material}
     * @return A {@link Material}, might be legacy
     * @throws BadInterpretationException If the input could not be interpreted as a {@link Material}, legacy or not
     */
    @NotNull
    public static Material toMaterial(@NotNull String material){
        Material mat = Material.matchMaterial(material, false);
        if(mat == null)
            mat = Material.matchMaterial(material, true);
        if(mat == null)
            throw new BadInterpretationException(String.format(MATERIAL_NOT_FOUND_MESSAGE, material));
        return mat;
    }
    
    /**
     * Interprets a {@link String} as a {@link Command}
     * @param command The string representing a {@link Command} name
     * @return A {@link Command}
     * @throws BadInterpretationException If the input could not be interpreted as a {@link Command} and/or no {@link Command}
     *                                    was found.
     */
    @NotNull
    public static Command toCommand(@NotNull String command){
        Command cmd = CraftTenkore.instance().getCommandRegister().getCommand(command);
        if(cmd == null)
            throw new BadInterpretationException(String.format(COMMAND_NOT_FOUND_MESSAGE, command));
        return cmd;
    }
    
    /**
     * Interprets a {@link String} as a {@link GameMode}
     * @param gamemode The string representing the {@link GameMode} (or it's associated numerical value)
     * @return A {@link GameMode} enum
     * @throws BadInterpretationException If the input could not be interpreted as a {@link GameMode}
     */
    @NotNull
    public static GameMode toGameMode(@NotNull String gamemode){
        GameMode mode;
        switch(gamemode.toLowerCase(Locale.ENGLISH)){
            case "survival":
            case "0":
                mode = GameMode.SURVIVAL;
                break;
            case "creative":
            case "1":
                mode = GameMode.CREATIVE;
                break;
            case "adventure":
            case "2":
                mode = GameMode.ADVENTURE;
                break;
            case "spectator":
            case "3":
                mode = GameMode.SPECTATOR;
                break;
            default:
                // last resort: try to use valueOf; we might not know of it yet
                try {
                    mode = GameMode.valueOf(gamemode);
                    break;
                } catch (IllegalArgumentException exc){
                    throw new BadInterpretationException(String.format(GAMEMODE_NOT_FOUND_MESSAGE, gamemode));
                }
        }
        return mode;
    }

}
