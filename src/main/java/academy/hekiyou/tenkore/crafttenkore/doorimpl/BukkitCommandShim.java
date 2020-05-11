package academy.hekiyou.tenkore.crafttenkore.doorimpl;

import academy.hekiyou.tenkore.crafttenkore.doorimpl.tabcompletion.Completer;
import academy.hekiyou.tenkore.crafttenkore.doorimpl.tabcompletion.Completers;
import academy.hekiyou.door.FrontDoor;
import academy.hekiyou.door.annotations.RegisterCommand;
import academy.hekiyou.door.exception.BadCastException;
import academy.hekiyou.door.model.Command;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Represents a shim for Bukkit's Command class, as we don't utilize Bukkit's command system fully
 */
public class BukkitCommandShim extends org.bukkit.command.Command {
    
    private Command internalCommand;
    
    /**
     * Constructs a dummy {@link Command} shim that serves no purpose other than to call Faucet.process().
     * Do note that we still pass valid values back up to Bukkit.
     *
     * @param internal     The internal door {@link academy.hekiyou.door.model.Command} that we are based off of
     * @param name         The name of the command
     * @param description  The description of the command
     * @param usageMessage The usage message (Use {@link String#join(CharSequence, CharSequence...)} with
     *                     {@link RegisterCommand#usage()} to generate)
     * @param aliases      A list of aliases for this command
     */
    BukkitCommandShim(@NotNull academy.hekiyou.door.model.Command internal,
                      @NotNull String name, @NotNull String description,
                      @NotNull String usageMessage, @NotNull List<String> aliases){
        super(name, description, usageMessage, aliases);
        super.setPermission(internal.getMetadata().permission());
        this.internalCommand = internal;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String label, @NotNull String[] args){
        try {
            return FrontDoor.process(getName(), new BukkitInvoker(commandSender), args);
        } catch(Exception exc) {
            // go all the way down to figure out the real cause
            Exception cause = exc;
            while(cause.getCause() != null)
                cause = (Exception) cause.getCause();
            
            // bad cast? handle it differently
            if(cause instanceof BadCastException && ((BadCastException) cause).getExpected() == Player.class){
                commandSender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            } else {
                // just rethrow since it wasn't a bad cast
                throw exc;
            }
            return false;
        }
    }
    
    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args){
        Bukkit.getLogger().log(Level.WARNING, "reee");
        if(!sender.hasPermission(internalCommand.getMetadata().permission())){
            Bukkit.getLogger().log(Level.WARNING, "reee1");
            return Collections.emptyList();
        }
    
        Parameter[] parameters = internalCommand.getParameters();
        if(args.length > parameters.length){
            Bukkit.getLogger().log(Level.WARNING, "reee2");
            return Collections.emptyList();
        }
        
        Parameter currArgType = parameters[args.length - 1];
        Bukkit.getLogger().log(Level.WARNING, currArgType.getType().getCanonicalName());
        Completer completer = Completers.completerFor(currArgType.getType());
        if(completer != null){
            Bukkit.getLogger().log(Level.WARNING, "reee3");
            return completer.provide(args[args.length - 1]);
        } else {
            Bukkit.getLogger().log(Level.WARNING, "reee4");
            return Collections.emptyList();
        }
    }
    
}
