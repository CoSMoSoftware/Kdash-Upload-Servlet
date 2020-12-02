package io.cosmosoftware.kite;

import javax.json.*;
import javax.servlet.http.Part;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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


  public static boolean deleteDirectory(File directoryToBeDeleted) {
    File[] allContents = directoryToBeDeleted.listFiles();
    if (allContents != null) {
      for (File file : allContents) {
        deleteDirectory(file);
      }
    }
    return directoryToBeDeleted.delete();
  }


  public static void moveDirectory(File directoryToBeMoved, File destinationDirectory) {
    try {
      Files.move(directoryToBeMoved.toPath(), destinationDirectory.toPath());
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public static void unzip(Part part, String outputDirectory) throws IOException {
    byte[] buffer = new byte[2048];
    File testFile = new File(outputDirectory);
    if (!testFile.exists()) {
      testFile.mkdirs();
    }
    InputStream theFile = part.getInputStream();
    ZipInputStream stream = new ZipInputStream(theFile);
    String outdir = outputDirectory;
    try {
      System.out.println("Unzipping data to " + outputDirectory);
      ZipEntry entry;
      while ((entry = stream.getNextEntry()) != null) {
        String outpath = outdir + "/" + entry.getName();
        FileOutputStream output = null;
        if (entry.getName().endsWith("/")) {
          File x = new File(outpath);
          x.mkdir();
        } else {
          try {
            output = new FileOutputStream(outpath);
            int len = 0;
            while ((len = stream.read(buffer)) > 0) {
              output.write(buffer, 0, len);
            }
          } finally {
            if (output != null) output.close();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      stream.close();
    }
  }

  public static String executeCommand(String[] command)
          throws IOException, IllegalArgumentException {
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    System.out.println("*** Executing: ");
    for (String component : command) {
      System.out.print(component + " ");
    }
    Process process = processBuilder.start();
    String line;
    BufferedReader stdInput = null;
    boolean error = false;
    try {

      if (process.getInputStream().available() > 0) {
        stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
      } else {
        stdInput = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      }
      while ((line = stdInput.readLine()) != null) {
        line = String.format(command[0] + " stdout: %s\n", line);
        System.out.println(line);
        if (line.toLowerCase().contains("fail")
                || line.toLowerCase().contains("fatal")
                || line.toLowerCase().contains("error")) {
          error = true;
        }
      }
    } catch (IOException e) {
      // logger.error("Exception while reading the Input Stream", e);
    } finally {
      if (stdInput != null) {
        try {
          stdInput.close();
        } catch (IOException e) {
        }
      }
    }
    if (error) {
      throw new IllegalArgumentException(
              "Something is wrong with the command execution, please check the log file for more details");
    }
    String output = buildOutput(process, null, null);
    return output;
  }

  public static String buildOutput(Process process, List<String> stringList, String filter) {
    Scanner scanner = new Scanner(process.getInputStream());

    StringBuilder builder = new StringBuilder();
    System.out.println("*** BEGIN OUTPUT ***");
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      System.out.println(line);
      builder.append(line);
      if (stringList != null) {
        if (filter == null) {
          stringList.add(line);
        } else if (line.startsWith(filter)) {
          stringList.add(line);
        }
      }
    }
    System.out.println("*** END OUTPUT ***");
    scanner.close();

    return builder.toString();
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

  public static String checkLogFilePath(String resultName) {
    String res = "No logs found";
    String pathToLogFolder = (isWindowsBased() ? "C:\\nginx\\html\\kite-logs\\" : "/var/www/kite-logs/") + resultName;
    File logFolder = new File(pathToLogFolder);
    if (logFolder.exists()) {
      for (File subFile : logFolder.listFiles()) {
        if (subFile.isFile() && subFile.getName().endsWith(".log")) {
          res = subFile.getName();
        }
      }
    }
    return res;
  }
}
