package com.coderank.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeLogParserUtils {

    public static double[] parseTimeLog(String filePath) {
        double elapsedSeconds = 0.0;
        double maxMemoryKB = 0.0;

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("文件不存在：" + filePath);
            return new double[]{0.0, 0.0};
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            Pattern elapsedPattern = Pattern.compile("Elapsed \\(.*\\):\\s*(.*)");
            Pattern memoryPattern = Pattern.compile("Maximum resident set size.*:\\s*(\\d+)");

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                Matcher m1 = elapsedPattern.matcher(line);
                if (m1.find()) {
                    String timeStr = m1.group(1).trim();
                    elapsedSeconds = parseElapsedTime(timeStr);
                }

                Matcher m2 = memoryPattern.matcher(line);
                if (m2.find()) {
                    maxMemoryKB = Double.parseDouble(m2.group(1));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new double[]{elapsedSeconds, maxMemoryKB};
    }

    private static double parseElapsedTime(String timeStr) {
        double seconds = 0.0;

        if (timeStr.contains("m") && timeStr.contains("s")) {
            String[] parts = timeStr.split("m");
            double minutes = Double.parseDouble(parts[0].trim());
            double secs = Double.parseDouble(parts[1].replace("s", "").trim());
            seconds = minutes * 60 + secs;
        } else if (timeStr.contains(":")) {
            String[] parts = timeStr.split(":");
            double minutes = Double.parseDouble(parts[0].trim());
            double secs = Double.parseDouble(parts[1].trim());
            seconds = minutes * 60 + secs;
        } else if (timeStr.endsWith("s")) {
            seconds = Double.parseDouble(timeStr.replace("s", "").trim());
        } else {
            try {
                seconds = Double.parseDouble(timeStr.trim());
            } catch (NumberFormatException e) {
                seconds = 0.0;
            }
        }

        return seconds;
    }

}