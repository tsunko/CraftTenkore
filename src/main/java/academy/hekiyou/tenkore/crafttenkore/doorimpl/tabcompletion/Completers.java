package academy.hekiyou.tenkore.crafttenkore.doorimpl.tabcompletion;

import academy.hekiyou.door.model.Command;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to maintain the registry and retrieval of tab completers.
 * <p>
 * Applications can call {@link Completers#register(Class, Completer)} to register new {@link Completer}s.
 */
public class Completers {
    
    private static final Map<Class<?>, Completer> REGISTERED = new HashMap<>();
    
    static {
        registerDefaults();
    }
    
    public static <T> @Nullable Completer completerFor(Class<T> klass){
        return REGISTERED.get(klass);
    }
    
    /**
     * Registers an {@link Completer} to be associated with the specified {@link Class}, which later can be
     * retrieved and used.
     *
     * @param klass     the {@link Class} to map as a key
     * @param completer the {@link Completer} to map as a value
     * @param <T>       the type of {@code klass}
     *
     * @throws IllegalArgumentException if {@code klass} is already mapped to an {@link Completer}
     */
    public static <T> void register(@NotNull Class<T> klass, @NotNull Completer completer){
        if(REGISTERED.containsKey(klass))
            throw new IllegalArgumentException(String.format("%s is already registered to %s!",
                    klass.getName(), String.valueOf(REGISTERED.get(klass))));
        
        REGISTERED.put(klass, completer);
    }
    
    /**
     * Registers the default interpreters, which cover {@link String} and any primitive data type.
     */
    private static void registerDefaults(){
        register(Player.class, CompleterDefaults::onlinePlayerCompleter);
        register(HumanEntity.class, CompleterDefaults::onlinePlayerCompleter);
        register(OfflinePlayer.class, CompleterDefaults::offlinePlayerCompleter);
        register(CommandSender.class, CompleterDefaults::onlinePlayerCompleter);
        register(Material.class, CompleterDefaults::materialCompleter);
        register(Plugin.class, CompleterDefaults::pluginCompleter);
        register(World.class, CompleterDefaults::worldCompleter);
        register(Command.class, CompleterDefaults::commandCompleter);
    }
    
    
}
