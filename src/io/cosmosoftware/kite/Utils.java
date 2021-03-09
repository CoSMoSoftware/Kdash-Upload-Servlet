package io.cosmosoftware.kite;

import javax.json.*;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;
import java.util.*;

public class Utils {
  public Utils() {

  }

  /**
   * Download file.
   *
   * @param urlStr   the url str
   * @param filePath the file path
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void downloadFile(String urlStr, String filePath) throws IOException {
    if (urlStr.contains("~")) {
      urlStr = urlStr.replaceAll(
              "~", "/" + System.getProperty("user.home").replaceAll("\\\\", "/"));
    }

    ReadableByteChannel rbc = null;
    FileOutputStream fos = null;
    try {
      URL url = new URL(urlStr);
      rbc = Channels.newChannel(url.openStream());
      fos = new FileOutputStream(filePath);
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
        }
      }
      if (rbc != null) {
        try {
          rbc.close();
        } catch (IOException e) {
        }
      }
    }
  }

  /**
   * Reads a json file into a JsonObject
   *
   * @param jsonFile the file to read
   * @return the jsonObject
   */
  public static JsonObject readJsonFile(String jsonFile) {


    FileReader fileReader = null;
    JsonReader jsonReader = null;
    JsonObject jsonObject = null;
    try {
      String fileStr = System.getProperty("java.io.tmpdir") + File.separator + "tmpfile.json";
      if (jsonFile.contains("file://")) {
        downloadFile(jsonFile, fileStr);
      } else {
        fileStr = jsonFile;
      }

      fileReader = new FileReader(new File(fileStr));
      jsonReader = Json.createReader(fileReader);
      jsonObject = jsonReader.readObject();
    } catch (Exception e) {
    } finally {
      if (fileReader != null) {
        try {
          fileReader.close();
        } catch (IOException e) {
        }
      }
      if (jsonReader != null) {
        jsonReader.close();
      }
    }
    return jsonObject;
  }

  public static JsonArray checkStatus(String filePath) {
    String allureDirectory = filePath + (isWindowsBased() ? "\\data\\test-cases\\" : "/data/test-cases/");
    File allureFolder = new File(allureDirectory);
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    try {
      for (File subFile: allureFolder.listFiles()) {
        JsonObject result = Utils.readJsonFile(subFile.getAbsolutePath());
        JsonObjectBuilder fileJsonBuilder = Json.createObjectBuilder();
        fileJsonBuilder.add("name", result.getString("name"));
        fileJsonBuilder.add("status", result.getString("status"));
        arrayBuilder.add(fileJsonBuilder.build());
      }
    } catch (Exception e) {
    }
    return arrayBuilder.build();
  }

  public static JsonObject countStatus(String filePath) {
    JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
    String allureDirectory = filePath + (isWindowsBased() ? "\\data\\" : "/data/");
    try {
      File file=new File(allureDirectory + "suites.csv");
      FileInputStream fis = new FileInputStream(file);
      byte[] bytesArray = new byte[(int)file.length()];
      fis.read(bytesArray);
      String s = new String(bytesArray);
      String [] passed = s.split("\"passed\"");
      if (passed.length > 1) {
        jsonBuilder.add("passed", passed.length - 1);
      }

      String [] failed = s.split("\"failed\"");
      if (failed.length > 1) {
        jsonBuilder.add("failed", failed.length - 1);
      }

      String [] broken = s.split("\"broken\"");
      if (broken.length > 1) {
        jsonBuilder.add("broken", broken.length - 1);
      }

    }
    catch(IOException e)
    {
      System.out.println("Could not get stats from " + filePath + " -> " + e.getLocalizedMessage());
    }

    return jsonBuilder.build();
  }

  public static boolean isReport(String filePath, ArrayList<String> statusFilters) {
    String allureDirectory = filePath + (isWindowsBased() ? "\\data\\" : "/data/");
    try {
      File file=new File(allureDirectory + "suites.csv");
      FileInputStream fis = new FileInputStream(file);
      byte[] bytesArray = new byte[(int)file.length()];
      fis.read(bytesArray);
      String s = new String(bytesArray);
      for (String status : statusFilters) {
        String [] data = s.split("\"" + status + "\"");
        if (data.length > 1) {
          return true;
        }
      }
    }
    catch(IOException e)
    {
      System.out.println("Could not get stats from " + filePath + " -> " + e.getLocalizedMessage());
    }

    return false;
  }

  public static String readableFileSize(long size) {
    if(size <= 0) {
      return "0";
    }
    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
  }

  public static boolean isWindowsBased() {
    String osName = System.getProperty("os.name").toLowerCase();
    return osName.contains("win");
  }

  public static boolean isLinuxBased() {
    String osName = System.getProperty("os.name").toLowerCase();
    return osName.contains("nix") || osName.contains("nux") || osName.contains("aix");
  }

}
