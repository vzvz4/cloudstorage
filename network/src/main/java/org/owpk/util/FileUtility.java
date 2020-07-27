package org.owpk.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtility {

  public static void createFile(String fileName) throws IOException {
    File file = new File(fileName);
    if (!file.exists()) {
      file.createNewFile();
    }
  }

  public static void createDirectory(String dirName) throws IOException {
    File file = new File(dirName);
    if (!file.exists()) {
      file.mkdir();
    }
  }

  public static List<FileInfo> showDirs(String currentDir) throws IOException {
    System.out.println(Config.getSourceRoot());
    Path path = Paths.get(Config.getSourceRoot());
    System.out.println(path);
    return Files.list(path).map(FileInfo::new).collect(Collectors.toList());
  }

  public static void move(File dir, File file) throws IOException {
    String path = dir.getAbsolutePath() + "/" + file.getName();
    createFile(path);
    InputStream is = new FileInputStream(file);
    try (OutputStream os = new FileOutputStream(new File(path))) {
      byte[] buffer = new byte[8192];
      while (is.available() > 0) {
        int readBytes = is.read(buffer);
        System.out.println(readBytes);
        os.write(buffer, 0, readBytes);
      }
    }
  }

  public static void placeFile(DataInputStream is, File file) throws IOException {
    byte [] buffer = new byte[8192];
    try(FileOutputStream fos = new FileOutputStream(file)) {
        for (long i = 0; i < buffer.length; i++) {
          int count = is.read(buffer);
          fos.write(buffer, 0, count);
        }
    }
  }

  public static void sendFile(DataOutputStream os, File file) throws IOException {
    try(FileInputStream fis = new FileInputStream(file)) {
      byte[] buffer = new byte[8192];
      while (fis.available() > 0) {
        int count = fis.read(buffer);
        os.write(buffer, 0, count);
      }
    }
  }

}