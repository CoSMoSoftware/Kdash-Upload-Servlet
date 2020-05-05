package io.cosmosoftware.kite;

import java.io.*;
import java.util.*;
import javax.servlet.RequestDispatcher;
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
    String json = request.getParameter("json");
    String tempFolder = applicationPath + "tempFolder/";
    long timeStamp = System.currentTimeMillis();
    String unzipDirectory = tempFolder + timeStamp;
    String[] command;
    String[] chmodCommand = null;
    String allureDirectory;
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.indexOf("win") >= 0) {
      allureDirectory = "C:\\nginx\\html\\allure\\" + tagName;
      command = new String[]{"cmd.exe", "/C", "allure", "generate", unzipDirectory, "--output", allureDirectory};
    } else if (osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0 || osName.indexOf("aix") > 0) {
      allureDirectory = "/var/www/allure/" + tagName;
      command = new String[]{"sudo", "allure", "generate", unzipDirectory, "--output", allureDirectory};
      // give nginx access
      chmodCommand = new String[]{"sudo", "chmod", "-R", "777" , allureDirectory};
    } else {
      response.sendError(
              HttpServletResponse.SC_BAD_REQUEST,
              "Only Windows and Linux are supported.");
      return;
    }

    RequestDispatcher dispatcher = request.getRequestDispatcher("/cleanup");
    dispatcher.include(request, response);

    for (Iterator iterator = request.getParts().iterator(); iterator.hasNext(); ) {
      Part part = (Part) iterator.next();
      if(part.getSubmittedFileName() != null) {
        Utils.unzip(part, unzipDirectory);
        File allureFolder = new File(allureDirectory);
        if (allureFolder.exists()) {
          dispatcher = request.getRequestDispatcher("/delete");
          dispatcher.include(request, response);
        }
        Utils.executeCommand(command);
        if (chmodCommand != null) {
          Utils.executeCommand(chmodCommand);
        }
        Utils.deleteDirectory(new File(unzipDirectory));
      }
    }

    if(json == null) {
      dispatcher = request.getRequestDispatcher("/resultList");
      dispatcher.forward(request, response);
    } else {
      response.setStatus(200);
    }
  }



}

