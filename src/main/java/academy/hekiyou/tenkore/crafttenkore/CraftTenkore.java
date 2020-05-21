package academy.hekiyou.tenkore.crafttenkore;

import academy.hekiyou.door.FrontDoor;
import academy.hekiyou.door.Settings;
import academy.hekiyou.door.model.Register;
import academy.hekiyou.tenkore.Tenkore;
import academy.hekiyou.tenkore.crafttenkore.basemodules.CoreModule;
import academy.hekiyou.tenkore.crafttenkore.doorimpl.BukkitInterpreters;
import academy.hekiyou.tenkore.crafttenkore.doorimpl.BukkitRegister;
import academy.hekiyou.tenkore.crafttenkore.loader.WhiteboxLoader;
import academy.hekiyou.tenkore.plugin.Loader;
import academy.hekiyou.tenkore.plugin.LoaderManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point for CraftBukkit-based Tenkore implementations
 */
public class CraftTenkore extends JavaPlugin implements Tenkore {

	public static final String WHITEBOX_EXTENSION = "wb";
	private static WeakReference<CraftTenkore> INSTANCE;
	private static final Logger LOGGER = Logger.getLogger("CraftTenkore");

	private WhiteboxLoader loader;
	private BukkitRegister register;

	@Override
	public void onEnable(){
		INSTANCE = new WeakReference<>(this);

		// setup Tenkore
		if(!LoaderManager.registerLoader(WhiteboxLoader.class)){
			LOGGER.log(Level.SEVERE, "CATASTROPHIC FAILURE: WhiteboxLoader failed to register!");
			return;
		}
		
		loader = (WhiteboxLoader)LoaderManager.getLoaderFor(WHITEBOX_EXTENSION);
		
		// setup flow for command processing
		register = new BukkitRegister();
		FrontDoor.initialize(createDoorSettings(), register);
		BukkitInterpreters.register();
		FrontDoor.load(CoreModule.class);
		
		// now try to load everything under wherever CraftTenkore is
		// we _have_ to do it immediately under onEnable, or else PacketPlayOutCommand becomes stale
		loadPlugins(getDataFolder().getParentFile().toPath());
	}

	@Override
	public void onDisable(){
		Loader loader = LoaderManager.getLoaderFor(WHITEBOX_EXTENSION);
		if(loader instanceof WhiteboxLoader){
			loader.getLoadedPlugins().forEach(loader::unloadPlugin);
			LoaderManager.unregisterLoader(WhiteboxLoader.class);
		}
	}
	
	@Override
	public Register getCommandRegister(){
		return register;
	}
	
	@NotNull
	public WhiteboxLoader getLoader() {
		return loader;
	}

	@NotNull
	public static Logger logger(){
		return LOGGER;
	}

	@NotNull
	public static CraftTenkore instance(){
		CraftTenkore inst = INSTANCE.get();
		if(inst == null)
			throw new IllegalStateException("Core plugin unloaded!");
		return inst;
	}
	
	/**
	 * Creates settings for Door with strings nice for Minecraft.
	 */
	private static Settings createDoorSettings(){
		return new Settings.Builder()
				.errorPrefix(ChatColor.GRAY.toString())
				.invalidArgumentPrefix(ChatColor.RED.toString())
				.usageErrorFormat(ChatColor.RED + "Usage: %s")
				.permissionError(ChatColor.RED + "You don't have permission to do that!")
				.invalidSubcommandError(ChatColor.RED + "Sub-command not found; these are available: %s")
				.build();
	}
	
}
