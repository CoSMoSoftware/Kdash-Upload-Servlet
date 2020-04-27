package io.cosmosoftware.kite;

import javax.json.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@WebServlet(
        description = "Check result status",
        urlPatterns = {"/checkResult"}
)

public class CheckResultStatusServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public CheckResultStatusServlet() {
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    this.handleRequest(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    this.handleRequest(request, response);
  }

  public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String allureDirectory;
    String tagName = request.getParameter("tagName");
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

    JsonArray status = Utils.checkStatus(allureDirectory);

    response.setStatus(200);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().print(status);
    response.getWriter().flush();
  }

}

