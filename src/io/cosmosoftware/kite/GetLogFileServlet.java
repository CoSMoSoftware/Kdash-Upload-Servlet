package io.cosmosoftware.kite;

import static io.cosmosoftware.kite.Utils.isLinuxBased;
import static io.cosmosoftware.kite.Utils.isWindowsBased;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.json.JsonArray;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(
        description = "Check result status",
        urlPatterns = {"/get-log"}
)

public class GetLogFileServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public GetLogFileServlet() {
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    this.handleRequest(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    this.handleRequest(request, response);
  }

  public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String tagName = request.getParameter("tagName");
    String fileName = request.getParameter("fileName");
    String archives = request.getParameter("archives");
    String pathToLogFolder = (isWindowsBased() ? "C:\\nginx\\html\\allure\\" : "/var/www/allure/")
        // the archive is a bit complicated path
        + (archives == null ? "" : (isWindowsBased() ? "archives\\" : "archives/")
          + fileName.replace(".log", "") + ((isWindowsBased() ? "\\" : "/") )
          )
        //
        + tagName + ((isWindowsBased() ? "\\" : "/") )
        + fileName;

    File file = new File(pathToLogFolder);
    if (file.exists()) {
      response.setHeader("Content-Type", getServletContext().getMimeType(fileName));
      response.setHeader("Content-Length", String.valueOf(file.length()));
      response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
      Files.copy(file.toPath(), response.getOutputStream());
    } else {
      response.sendError(404, "Could not find the file -> " + pathToLogFolder);
    }
  }

}

