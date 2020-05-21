package academy.hekiyou.tenkore.crafttenkore.loader;

import academy.hekiyou.door.FrontDoor;
import academy.hekiyou.tenkore.crafttenkore.CraftTenkore;
import academy.hekiyou.tenkore.plugin.Loader;
import academy.hekiyou.tenkore.plugin.TenkorePlugin;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple loader that loads WhiteBox files, which are really just glorified jars.
 */
public class WhiteboxLoader implements Loader {
    
    private Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());
    private Map<String, TenkorePlugin> loadedPlugins = new HashMap<>();

    /**
     * @inheritDoc
     */
    @Override @NotNull
    public TenkorePlugin loadPlugin(@NotNull Path path){
        logger.log(Level.INFO, "Loading " + path.getFileName().toString() + "...");
    
        Properties pkgInfo = new Properties();
        TenkorePlugin plugin;
        String name;

        BufferedReader reader = null;

        // bit of a visual issue; note that reader is within try-with-resource
        try {
            JarFile jar = new JarFile(path.toFile());
            reader = JarEntryUtils.createBufferedReader(jar, "pkg-info");

            if(reader == null) throw new WhiteboxException("invalid whitebox file - missing pkg-info");
            pkgInfo.load(reader);
            
            // get and check name and main-class properties
            name = pkgInfo.getProperty("name");
            String mainClass = pkgInfo.getProperty("main-class");
            if(name == null || mainClass == null)
                throw new WhiteboxException("pkg-info missing name or main-class");
            
            WhiteboxClassloader cl;
            cl = AccessController.doPrivileged((PrivilegedAction<WhiteboxClassloader>)() -> new WhiteboxClassloader(jar));
            cl.addSecondary(getClass().getClassLoader()); // bridge the main class loader
            
            Class<?> klassMaybe = cl.findClass(mainClass);
            if(klassMaybe == null)
                throw new WhiteboxException("cannot find main-class " + mainClass);
    
            Class<? extends TenkorePlugin> pluginClass;
            try {
                pluginClass = klassMaybe.asSubclass(TenkorePlugin.class);
                plugin = pluginClass.getConstructor().newInstance();
                plugin.__init__(CraftTenkore.instance(), name);
            } catch (ClassCastException | NoSuchMethodException | IllegalAccessException exc){
                throw new WhiteboxException(mainClass + " is not a valid TenkorePlugin");
            } catch (InstantiationException | InvocationTargetException exc){
                throw new WhiteboxException("error while setting up " + name, exc);
            }
        } catch (IOException exc){
            throw new WhiteboxException(exc);
        } finally {
            try {
                if(reader != null)
                    reader.close();
            } catch (IOException ignored){}
        }
    
        // finally, we can enable the plugin and add it as a loaded plugin
        plugin.enable();
        loadedPlugins.put(name, plugin);
        logger.log(Level.INFO, "Loaded " + plugin.getName() + "!");
        return plugin;
    }

    /**
     * @inheritDoc
     */
    @Override @Nullable
    public TenkorePlugin unloadPlugin(@NotNull String pluginName){
        TenkorePlugin plugin = loadedPlugins.get(pluginName);
        if(plugin == null) return null;
    
        logger.log(Level.INFO, "Unloading " + pluginName + "...");
        
        // let the plugin clean up anything it needs to
        plugin.disable();
        
        ClassLoader cl = plugin.getClass().getClassLoader();
        if(cl instanceof WhiteboxClassloader){
            Class[] classes = ((WhiteboxClassloader) cl).getLoadedClasses();
            for(Class<?> klass : classes){
                // first unload all commands
                FrontDoor.unload(klass);
                
                // then unregister all listeners
                if(Listener.class.isAssignableFrom(klass))
                    unregisterListeners(klass);
            }
        }
        
        logger.log(Level.INFO, "Unloaded " + pluginName + "!");
        return plugin;
    }

    /**
     * @inheritDoc
     */
    @Override @NotNull
    public List<String> getLoadedPlugins(){
        return new ArrayList<>(loadedPlugins.keySet());
    }

    /**
     * @inheritDoc
     */
    @Override @NotNull
    public String[] compatibleExtensions(){
        return new String[]{ CraftTenkore.WHITEBOX_EXTENSION };
    }

    /**
     * Handles unregistering all {@link Listener} from a given class
     * @param klass The {@link Class} who should be unregistered
     */
    private void unregisterListeners(Class<?> klass){
        HandlerList.getHandlerLists().forEach(handler -> {
            for(RegisteredListener registered : handler.getRegisteredListeners()){
                Listener listener = registered.getListener();
                if(listener.getClass() == klass){
                    handler.unregister(listener);
                }
            }
        });
    }
    
}
