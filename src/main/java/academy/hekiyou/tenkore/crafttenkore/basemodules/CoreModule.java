package academy.hekiyou.tenkore.crafttenkore.basemodules;

import academy.hekiyou.door.annotations.Module;
import academy.hekiyou.door.annotations.RegisterCommand;
import academy.hekiyou.door.model.Command;
import academy.hekiyou.door.model.Invoker;
import academy.hekiyou.tenkore.crafttenkore.CraftTenkore;
import academy.hekiyou.tenkore.crafttenkore.doorimpl.BukkitRegister;
import academy.hekiyou.tenkore.crafttenkore.loader.WhiteboxException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;

@Module
public class CoreModule {

    @RegisterCommand(
            permission = "crafttenkore.coremodule.man",
            description = "It's like man pages but for Minecraft!"
    )
    public void man(Invoker invoker, Command command){
        RegisterCommand metadata = command.getMetadata();
        String argsManStr;
        String aliasManStr;
        
        invoker.sendMessage(ChatColor.GREEN + "Manual page for " + command.getName() + ":");
        sendDualColored(invoker, "Permission Node: ", metadata.permission());
        
        if(command.getUsage().length > 0){
            argsManStr = String.join(" ", command.getUsage());
        } else {
            argsManStr = "No arguments.";
        }
        sendDualColored(invoker, "Arguments: ", argsManStr);
        
        if(metadata.alias().length > 0){
            aliasManStr = String.join(", ", metadata.alias());
        } else {
            aliasManStr = "No aliases.";
        }
        sendDualColored(invoker, "Aliases: ", aliasManStr);
        
        sendDualColored(invoker, "Overrides: ", String.valueOf(metadata.override()));
        sendDualColored(invoker, "Permission Node: ", metadata.permission());
        sendDualColored(invoker, "Plugin Class: ", command.getOwningClass()
                                                                    .replace("academy.hekiyou.tenkore", "a.h.t"));
        sendDualColored(invoker, "Description: ", metadata.description());
    }
    
    @RegisterCommand(
            permission = "crafttenkore.coremodule.unloadplugin",
            description = "Unloads a given plugin, Bukkit or not."
     )
    public void unloadPlugin(Invoker invoker, String target){
        CraftTenkore tenkore = CraftTenkore.instance();
        Plugin maybePlugin = Bukkit.getPluginManager().getPlugin(target);
        if(maybePlugin instanceof JavaPlugin){
            // let bukkit handle however it handles disabling plugins

            // for the most part:
            // clears out its classloader
            // cancels any pending scheduled tasks
            // unregisters any services that were registered
            // unregisters all of its event listeners
            // unregisters incoming/outgoing plugin channels
            // removes any world tickets

            // what we need to do:
            // remove commands
            Bukkit.getServer().getPluginManager().disablePlugin(maybePlugin);

            // TODO: move this somewhere else; it doesn't belong here
            Map<String, org.bukkit.command.Command> commandMap =
                    ((BukkitRegister)tenkore.getCommandRegister()).getInternalMap();
            for(Map.Entry<String, org.bukkit.command.Command> entry : commandMap.entrySet()){
                if(entry.getKey().startsWith(maybePlugin.getName() + ":")){
                    commandMap.remove(entry.getKey());
                }
            }

            invoker.sendMessage(ChatColor.YELLOW + "Double check plugins list to see if plugin unloaded.");
        } else if(tenkore.getLoader().unloadPlugin(target) != null){
            invoker.sendMessage(ChatColor.GREEN + "Unloaded plugin successfully.");
        }
    }
    
    @RegisterCommand(
            permission = "crafttenkore.coremodule.loadplugin",
            description = "Calls Bukkit to load a given plugin."
    )
    public void loadPlugin(Invoker invoker, String fileName){
        CraftTenkore tenkore = CraftTenkore.instance();

        try {
            if(fileName.endsWith("." + CraftTenkore.WHITEBOX_EXTENSION)){
                tenkore.getLoader().loadPlugin(Paths.get(tenkore.getDataFolder().getParentFile().getAbsolutePath(), fileName));
            } else {
                if(!fileName.endsWith(".jar"))
                    invoker.sendMessage(ChatColor.YELLOW + "Not a jar or wb file... Trying anyways.");
                PluginManager manager = Bukkit.getPluginManager();
                Plugin plugin = manager.loadPlugin(new File(tenkore.getDataFolder().getParentFile(), fileName));
                if(plugin == null){
                    invoker.sendMessage(ChatColor.RED + "Failed to load plugin. Check console.");
                    return;
                }
                manager.enablePlugin(plugin);
            }
        } catch (InvalidPluginException | InvalidDescriptionException | WhiteboxException exc){
            CraftTenkore.logger().log(Level.WARNING, "Failed to load " + fileName, exc);
            invoker.sendMessage(ChatColor.RED + "Failed to load requested plugin - check console.");
        }
    }
    
    /**
     * Helper function to prevent crappy looking code (but it still looks crappy)
     */
    private void sendDualColored(Invoker invoker, String a, String b){
        invoker.sendMessage("%s%s%s%s", ChatColor.GRAY, a, ChatColor.YELLOW, b);
    }

}
