package cz.cuni.lf1.lge.ThunderSTORM.util;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IOUtils {

    public static void closeQuietly(Closeable closeable) {
        if(closeable != null) {
            try {
                closeable.close();
            } catch(Exception e) {

            }
        }
    }

    public static List<File> listFilesInFolder(final File folder, boolean recursive) {
        ArrayList<File> files = new ArrayList<File>();
        if (folder.exists() && folder.isDirectory()) {
            for (final File fileEntry : folder.listFiles()) {
                if (recursive && fileEntry.isDirectory()) {
                    files.addAll(listFilesInFolder(fileEntry, recursive));
                } else {
                    files.add(fileEntry);
                }
            }
        }
        return files;
    }
}
