package academy.hekiyou.tenkore.crafttenkore.doorimpl;

import academy.hekiyou.tenkore.crafttenkore.CraftTenkore;
import academy.hekiyou.door.annotations.RegisterCommand;
import academy.hekiyou.door.model.Command;
import academy.hekiyou.door.model.Register;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BukkitRegister implements Register {
    
    // <fallback prefix + command (or just command), bukkit command object>
    // caveat with bukkit: command name must be lower case or else it can't find anything...
    private Map<String, org.bukkit.command.Command> internalBukkitMap;
    // <command name, gunvarrel command object>
    private Map<String, Command> registeredCommands;
    private CommandMap commandMap;
    
    public BukkitRegister(){
        commandMap = getInternalCommandMap(Bukkit.getPluginManager());
        internalBukkitMap = getInternalKnownCommands(commandMap);
        registeredCommands = new HashMap<>();
    }
    
    /**
     * @inheritDoc
     */
    @Override @Nullable
    public Command getCommand(@NotNull String commandName){
        return registeredCommands.get(commandName);
    }

    /**
     * @inheritDoc
     */
    @Override @NotNull
    public Map<String, String> getRegistered(){
        return registeredCommands.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getOwningClass()
                ));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void register(@NotNull Command command){
        RegisterCommand meta = command.getMetadata();
        BukkitCommandShim shim = new BukkitCommandShim(command, command.getName(), meta.description(),
                String.join(" ", front(command.getName(), meta.usage())),
                Arrays.asList(meta.alias()));
        
        for(String alias : front(command.getName(), meta.alias())){
            alias = alias.toLowerCase();
            
            String owner = command.getOwningClass();
            registerShims(owner.substring(owner.lastIndexOf('.') + 1), alias, shim, meta.override());
            if(registeredCommands.containsKey(alias)){
                CraftTenkore.logger().log(Level.WARNING,
                                          "Command alias \"" + alias + "\" " +
                                          "(owned by " + registeredCommands.get(alias).getOwningClass() + ") " +
                                          "clashes with " + command.getOwningClass());
                continue;
            }
    
            registeredCommands.put(alias, command);
            shim.register(commandMap);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void unregister(@NotNull Command command){
        for(String alias : front(command.getName(), command.getMetadata().alias())){
            alias = alias.toLowerCase();
    
            String owner = command.getOwningClass();
            for(org.bukkit.command.Command bukkitCommand : unregisterShims(owner.substring(owner.lastIndexOf('.') + 1), alias)){
                bukkitCommand.unregister(commandMap);
            }
            registeredCommands.remove(alias);
        }
    }

    /**
     * Retrieves the internal mapping. Take extreme care as this is the actual map - NOT a copy!
     * @return The raw internal mapping
     */
    public Map<String, org.bukkit.command.Command> getInternalMap(){
        return internalBukkitMap;
    }
    
    /**
     * Registers a shim to Bukkit
     * @param baseFallback The base fallback of the command
     * @param alias The name of the command to register with the shim
     * @param shim The shim to register with
     * @param override If {@code true}, then overwrite other commands.
     */
    private void registerShims(@NotNull String baseFallback, @NotNull String alias, @NotNull BukkitCommandShim shim, boolean override){
        baseFallback = baseFallback.toLowerCase();

        internalBukkitMap.put(baseFallback + ":" + alias, shim);
        if(!internalBukkitMap.containsKey(alias) || override)
            internalBukkitMap.put(alias, shim);
    }
    
    /**
     * Unregisters our shims from Bukkit
     * @param alias The alias of the command to unregister
     */
    private List<org.bukkit.command.Command> unregisterShims(@NotNull String baseFallback, @NotNull String alias){
        baseFallback = baseFallback.toLowerCase();
        
        List<org.bukkit.command.Command> removed = new ArrayList<>();
        org.bukkit.command.Command baseCommand = internalBukkitMap.remove(baseFallback + ":" + alias);
        if(baseCommand == null){
            Bukkit.getLogger().warning("??? " + baseFallback + ":" + alias + " not registered in internal map");
        }
        if(internalBukkitMap.get(alias) instanceof BukkitCommandShim){
            if(internalBukkitMap.remove(alias) == null){
                Bukkit.getLogger().warning("??? " + alias + " not registered in internal map");
            }
        }
        return removed;
    }
    
    /**
     * We need a way to get access to the internal map that Bukkit uses to register and execute commands;
     * this method provides it through reflection.
     */
    @SuppressWarnings("unchecked") // needed or else knownCommandsField.get() complains
    private @Nullable Map<String, org.bukkit.command.Command> getInternalKnownCommands(CommandMap commandMap){
        if(!(commandMap instanceof SimpleCommandMap)){
            CraftTenkore.logger().severe("Can't steal map from command map; not SimpleCommandMap!");
            return null;
        }
        
        Field knownCommandsField;
        Map<String, org.bukkit.command.Command> knownCommands = null;
        try {
            knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            knownCommands = (Map<String, org.bukkit.command.Command>)knownCommandsField.get(commandMap);
        } catch (NoSuchFieldException exc){
            CraftTenkore.logger().severe("Can't steal map from plugin manager; not SimplePluginManager!");
        } catch (IllegalAccessException exc){
            CraftTenkore.logger().severe("No access to internal fields; security manager?");
        }
        return knownCommands;
    }
    
    private @Nullable CommandMap getInternalCommandMap(PluginManager pluginManager){
        if(!(pluginManager instanceof SimplePluginManager)){
            CraftTenkore.logger().severe("Can't steal map from plugin manager; not SimplePluginManager!");
            return null;
        }
    
        Field commandMapField;
        SimpleCommandMap mapping = null;
        try {
            commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            mapping = (SimpleCommandMap)commandMapField.get(pluginManager);
        } catch (NoSuchFieldException exc){
            CraftTenkore.logger().severe("Can't steal map from plugin manager; not SimplePluginManager!");
        } catch (IllegalAccessException exc){
            CraftTenkore.logger().severe("No access to internal fields; security manager?");
        }
        return mapping;
    }
    
    /**
     * Creates a new List with the given front element being in the forefront of the new list.
     *
     * @param frontElem The front-most element that should be used
     * @param arr       The array to base the list off of
     * @param <T>       The type of elements in the list
     *
     * @return A list with frontElem as the 0th item
     */
    private <T> @NotNull List<T> front(@NotNull T frontElem, @NotNull T[] arr){
        List<T> list = new ArrayList<>(Arrays.asList(arr));
        list.add(0, frontElem);
        return list;
    }
    
}
