package io.cosmosoftware.kite;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;

@WebServlet(
        description = "Delete Result Folder",
        urlPatterns = {"/delete"}
)

public class DeleteResultServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public DeleteResultServlet() {
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    this.handleRequest(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    this.handleRequest(request, response);
  }

  public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String tagName = request.getParameter("tagName");
    long timeStamp = System.currentTimeMillis();
    String allureDirectory;
    String archivedDirectory;
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.indexOf("win") >= 0) {
      allureDirectory = "C:\\nginx\\html\\allure\\" + tagName;
      archivedDirectory = "C:\\nginx\\html\\archives\\" + tagName + timeStamp;
    } else if (osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0 || osName.indexOf("aix") > 0) {
      allureDirectory = "/var/www/allure/" + tagName;
      archivedDirectory = "/var/www/archives/" + tagName + timeStamp;
    } else {
      response.sendError(
              HttpServletResponse.SC_BAD_REQUEST,
              "Only Windows and Linux are supported.");
      return;
    }

    File directoryToBeDeleted = new File(allureDirectory);
    File destinationDirectory = new File(archivedDirectory);

    if(directoryToBeDeleted.exists()) {
      Utils.moveDirectory(directoryToBeDeleted, destinationDirectory);
      response.setStatus(200);
    } else {
      response.setStatus(400);
    }
  }


}
