package com.googlecode.waitrest;

import java.io.*;
import java.nio.file.Files;

public class Fixtures {

    public static String getPathToExportFile() throws IOException {
        final File tempFile = Files.createTempFile("export", ".txt").toFile();
        final PrintWriter printWriter = new PrintWriter(tempFile);
        final BufferedReader ordersFileReader = new BufferedReader(new InputStreamReader(Fixtures.class.getResourceAsStream("export.txt")));
        String line;
        while((line = ordersFileReader.readLine()) != null){
            printWriter.println(line);
        }
        printWriter.close();
        ordersFileReader.close();
        return tempFile.getAbsolutePath();
    }
}
