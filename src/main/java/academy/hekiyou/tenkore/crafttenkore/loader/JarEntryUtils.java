package academy.hekiyou.tenkore.crafttenkore.loader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utility class to handle JarEntry objects
 */
public class JarEntryUtils {
    
    /**
     * Reads a {@link JarEntry} fully using {@link DataInputStream#readFully(byte[])}
     * @param file The {@link JarFile} file the entry is within
     * @param entryKey The name of the entry in the jar file.
     * @return The data of the {@link JarEntry} or a 0-length {@code byte[]} if none was found.
     * @throws IOException If any IO errors occur while reading the entry
     */
    public static byte[] readJarEntryFully(@NotNull JarFile file, @NotNull String entryKey) throws IOException{
        JarEntry entry = file.getJarEntry(entryKey);
        if(entry == null) return new byte[0];
    
        byte[] data = new byte[(int)entry.getSize()];
        
        try(DataInputStream dis = new DataInputStream(file.getInputStream(entry))) {
            dis.readFully(data);
        }
        
        return data;
    }
    
    /**
     * Create a {@link BufferedReader} of the entry inside of a {@link JarFile}.
     * @param file The {@link JarFile} to pull from
     * @param entryKey The key of the file (i.e file name)
     * @return A {@link BufferedReader} instance to read from the file associated with its entry, or null if none found.
     * @throws IOException If {@link BufferedReader} fails to construct
     */
    @Nullable
    public static BufferedReader createBufferedReader(@NotNull JarFile file, @NotNull String entryKey) throws IOException {
        JarEntry entry = file.getJarEntry(entryKey);
        if(entry == null) return null;
        return new BufferedReader(new InputStreamReader(file.getInputStream(entry), StandardCharsets.UTF_8));
    }
    
}
