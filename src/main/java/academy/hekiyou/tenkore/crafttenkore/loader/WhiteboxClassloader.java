package academy.hekiyou.tenkore.crafttenkore.loader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.jar.JarFile;

/**
 * A secondary ClassLoader for WhiteBox files; which are really just glorified jars
 */
class WhiteboxClassloader extends ClassLoader {
    
    // required to unload and load commands dynamically
    
    private final JarFile primary;
    private Map<ClassLoader, Void> secondary = new WeakHashMap<>();
    private Map<Class<?>, Void> loadedClasses = new WeakHashMap<>();
    
    WhiteboxClassloader(@NotNull JarFile file){
        primary = file;
    }
    
    /**
     * Adds a secondary {@link ClassLoader} that we can search if we can't find anything in the jar
     * @param loader The {@link ClassLoader} to add as a secondary source
     */
    void addSecondary(@NotNull ClassLoader loader){
        secondary.put(loader, null);
    }

    /**
     * @inheritDoc
     */
    @Override
    public @Nullable Class<?> findClass(@NotNull String className){
        String jarPath = className.replace(".", "/");
        if(!jarPath.endsWith(".class"))
            jarPath += ".class";
    
        // try other classloaders
        for(ClassLoader loader : secondary.keySet()){
            try {
                Class<?> klass = loader.loadClass(className);
                if(klass != null)
                    return klass;
            } catch (ClassNotFoundException ignored){}
        }
        
        /// try our own jar
        try {
            byte[] classData = JarEntryUtils.readJarEntryFully(primary, jarPath);
            if(classData.length != 0)
                return register(defineClass(className, classData, 0, classData.length));
        } catch (IOException ignored){}
        
        return null;
    }
    
    Class<?>[] getLoadedClasses(){
        return loadedClasses.keySet().toArray(new Class[0]);
    }
    
    private Class<?> register(Class<?> klass){
        loadedClasses.put(klass,null);
        return klass;
    }

}
