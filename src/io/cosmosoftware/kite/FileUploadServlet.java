package io.cosmosoftware.kite;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
@WebServlet(
        description = "Upload File To The Server",
        urlPatterns = {"/upload"}
)
@MultipartConfig(
        fileSizeThreshold = 31457280,
        maxFileSize = 314572800L,
        maxRequestSize = 471859200L
)
public class FileUploadServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public FileUploadServlet() {  
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      this.handleRequest(request, response);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, InterruptedException {
    String applicationPath = this.getServletContext().getRealPath("");
    String tagName = request.getParameter("tagName");
    String tempFolder = applicationPath + "tempFolder/";
    long timeStamp = System.currentTimeMillis();
    String unzipDirectory = tempFolder + timeStamp;
    String[] command;
    String[] chmodCommand = null;
    String allureDirectory;
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.indexOf("win") >= 0) {
      allureDirectory = "C:\\nginx\\html\\allure\\" + tagName;
      command = new String[]{"cmd.exe", "/C", "allure", "generate", unzipDirectory, "--clean", "--output", allureDirectory};
    } else if (osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0 || osName.indexOf("aix") > 0) {
      allureDirectory = "/var/www/allure/" + tagName;
      command = new String[]{"sudo", "allure", "generate", unzipDirectory, "--clean", "--output", allureDirectory};
      // give nginx access
      chmodCommand = new String[]{"sudo", "chmod", "-R", "755" , allureDirectory};
    } else {
      response.sendError(
              HttpServletResponse.SC_BAD_REQUEST,
              "Only Windows and Linux are supported.");
      return;
    }
    for (Iterator iterator = request.getParts().iterator(); iterator.hasNext(); ) {
      Part part = (Part) iterator.next();

      unzip(part, unzipDirectory);
      executeCommand(command);
      if (chmodCommand !=null) {
        executeCommand(chmodCommand);
      }


      deleteDirectory(new File(unzipDirectory));
    }
    response.setStatus(200);
  }

  private void unzip(Part part, String outputDirectory) throws IOException {
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
    }
    finally {
      stream.close();
    }
  }

  public static String executeCommand(String[] command)
          throws IOException, InterruptedException, IllegalArgumentException {
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
    String output =  buildOutput(process, null, null);
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

  private static boolean deleteDirectory(File directoryToBeDeleted) {
    File[] allContents = directoryToBeDeleted.listFiles();
    if (allContents != null) {
      for (File file : allContents) {
        deleteDirectory(file);
      }
    }
    return directoryToBeDeleted.delete();
  }
}

