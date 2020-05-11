package academy.hekiyou.tenkore.crafttenkore.doorimpl.tabcompletion;

import academy.hekiyou.tenkore.crafttenkore.CraftTenkore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class CompleterDefaults {
    
    public static @NotNull List<String> onlinePlayerCompleter(String partial){
        return findMatches(partial, Bukkit.getOnlinePlayers(), Player::getName);
    }
    
    // don't actually search offline players due to the lag it can incur
    public static @NotNull List<String> offlinePlayerCompleter(String partial){
        return onlinePlayerCompleter(partial);
    }
    
    public static @NotNull List<String> worldCompleter(String partial){
        return findMatches(partial, Bukkit.getWorlds(), World::getName);
    }
    
    public static @NotNull List<String> materialCompleter(String partial){
        return findMatches(partial, MATERIAL_LIST, mat -> mat.getKey().getKey());
    }
    
    public static @NotNull List<String> pluginCompleter(String partial){
        return findMatches(partial, Arrays.asList(Bukkit.getPluginManager().getPlugins()), Plugin::getName);
    }
    
    public static @NotNull List<String> commandCompleter(String partial){
        return findMatches(partial, CraftTenkore.instance().getCommandRegister().getRegistered().keySet(), Function.identity());
    }
    
    private static final List<Material> MATERIAL_LIST = Arrays.asList(Material.values());
    
    private static <T> List<String> findMatches(String partial, Collection<T> values, Function<T, String> mapper){
        partial = partial.toLowerCase();
        List<String> list = new ArrayList<>();
        for(T elem : values){
            String elemKey = mapper.apply(elem);
            if(elemKey.toLowerCase().startsWith(partial)){
                list.add(elemKey);
            }
        }
        return list;
    }
    
}
