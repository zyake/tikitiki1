package tikitiki.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ClassPathResourceLoader {

    public static String load(String classpath) {
        URL resource = ClassPathResourceLoader.class.getClassLoader().getResource(classpath);
        try {
            return FileUtils.readFileToString(new File(resource.getFile()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
