package com.coderank.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public final class FileUtils {

    public static String readFile(String dirPath, String fileName) {
        File file = new File(dirPath, fileName);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
