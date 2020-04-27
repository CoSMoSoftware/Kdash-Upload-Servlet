package io.cosmosoftware.kite;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.Date;

@WebServlet(
        description = "Clean up archives Folder",
        urlPatterns = {"/cleanup"}
)

public class CleanUpArchivesServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public CleanUpArchivesServlet() {
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    this.handleRequest(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    this.handleRequest(request, response);
  }

  public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String archivedDirectory;
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.indexOf("win") >= 0) {
      archivedDirectory = "C:\\nginx\\html\\archives\\";
    } else if (osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0 || osName.indexOf("aix") > 0) {
      archivedDirectory = "/var/www/archives/";
    } else {
      response.sendError(
              HttpServletResponse.SC_BAD_REQUEST,
              "Only Windows and Linux are supported.");
      return;
    }

    File archivedFolder = new File(archivedDirectory);
    for(File archive: archivedFolder.listFiles()) {
      long diff = new Date().getTime() - archive.lastModified();
      if (diff > 14 * 24 * 60 * 60 * 1000) {
        System.out.println("Deleting folder:" + archive.getName());
        Utils.deleteDirectory(archive);
      }
    }
    response.setStatus(200);
  }

}

