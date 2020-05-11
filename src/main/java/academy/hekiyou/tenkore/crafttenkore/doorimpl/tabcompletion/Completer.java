package academy.hekiyou.tenkore.crafttenkore.doorimpl.tabcompletion;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a functional interface that can provide possible tab completions for
 * {@link org.bukkit.command.Command#tabComplete(CommandSender, String, String[])}
 */
@FunctionalInterface
public interface Completer {
    
    /**
     * Provides a {@link List<String>} of possible tab completions for the type.
     *
     * @param base A possibly-empty part of an argument to work off of
     *
     * @return A {@link List<String>} containing possible tab completions.
     */
    @NotNull List<String> provide(String base);
    
}
