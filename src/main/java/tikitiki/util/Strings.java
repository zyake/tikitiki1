package tikitiki.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class Strings {

    private Strings() {
    }

    public static byte[] compress(String cleanedOutput) {
        try {
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream outputStream = new GZIPOutputStream(arrayOutputStream);
            outputStream.write(cleanedOutput.getBytes());
            outputStream.flush();
            outputStream.close();
            arrayOutputStream.flush();
            return arrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
