package academy.hekiyou.tenkore.crafttenkore.doorimpl;

import academy.hekiyou.door.model.Invoker;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BukkitInvoker implements Invoker {

    private static final UUID NON_PLAYER_UUID = new UUID(0, 0);
    private CommandSender ref;
    
    BukkitInvoker(CommandSender player){
        ref = player;
    }

    /**
     * @inheritDoc
     */
    @Override
    public @NotNull String getName(){
        return ref.getName();
    }

    /**
     * @inheritDoc
     */
    @Override
    public @NotNull String getID(){
        UUID uuid = NON_PLAYER_UUID;
        if(ref instanceof Player)
            uuid = ((Player) ref).getUniqueId();
        return uuid.toString();
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean hasPermission(@NotNull String permission){
        return ref.hasPermission(permission);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void sendMessage(@NotNull String message){
        if(message.isEmpty()) return;
        ref.sendMessage(message);
    }

    /**
     * @inheritDoc
     */
    @Override
    public @Nullable Object raw(){
        return ref;
    }
    
}
