package springcloud.springcloudgateway.service.fabric;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author: 李浩然
 * @date: 2021/1/3 9:14 下午
 */
public class FileUtils {
    public static byte[] readFile(String filePath) throws IOException {
        FileInputStream inputStream = new FileInputStream(filePath);
        byte[] fileBytes = new byte[inputStream.available()];
        inputStream.read(fileBytes);
        return fileBytes;
    }

    public static void writeFile(String content, String fileName, boolean append) throws IOException {
        File file = new File(fileName);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }

        FileWriter fileWriter = new FileWriter(fileName, append);
        fileWriter.write(content);
        fileWriter.close();
    }
}
