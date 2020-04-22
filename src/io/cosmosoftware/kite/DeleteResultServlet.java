package io.cosmosoftware.kite;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@WebServlet(
        description = "Delete Result Folder",
        urlPatterns = {"/upload"}
)

public class DeleteResultServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public DeleteResultServlet() {
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      this.handleRequest(request, response);
  }
  public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String tagName = request.getParameter("tagName");
    String allureDirectory;
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.indexOf("win") >= 0) {
      allureDirectory = "C:\\nginx\\html\\allure\\" + tagName;
    } else if (osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0 || osName.indexOf("aix") > 0) {
      allureDirectory = "/var/www/allure/" + tagName;
    } else {
      response.sendError(
              HttpServletResponse.SC_BAD_REQUEST,
              "Only Windows and Linux are supported.");
      return;
    }

    File directoryToBeDeleted = new File(allureDirectory);
    if(directoryToBeDeleted.exists()) {
      deleteDirectory(directoryToBeDeleted);
      response.setStatus(200);
    } else {
      response.setStatus(400);
    }
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

