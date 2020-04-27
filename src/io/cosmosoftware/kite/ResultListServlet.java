package io.cosmosoftware.kite;

import java.io.*;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@WebServlet(
        description = "Fetch Result List",
        urlPatterns = {"/resultList"}
)
public class ResultListServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public ResultListServlet() {
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      this.handleRequest(request, response);
  }
  public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    String osName = System.getProperty("os.name").toLowerCase();
    File allureDirectory;

    if (osName.indexOf("win") >= 0) {
      allureDirectory = new File("C:\\nginx\\html\\allure\\");
    } else if (osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0 || osName.indexOf("aix") > 0) {
      allureDirectory = new File("/var/www/allure/");
    } else {
      response.sendError(
              HttpServletResponse.SC_BAD_REQUEST,
              "Only Windows and Linux are supported.");
      return;
    }


    File[] resultList = allureDirectory.listFiles();
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    for (File result: resultList) {
      JsonArray status = Utils.checkStatus(result.getAbsolutePath());
      JsonObjectBuilder fileJsonBuilder = Json.createObjectBuilder();
      fileJsonBuilder.add("test cases", status);
      fileJsonBuilder.add("name", result.getName());
      fileJsonBuilder.add("lastModified", result.lastModified());
      arrayBuilder.add(fileJsonBuilder.build());
    }
    response.setStatus(200);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().print(arrayBuilder.build().toString());
    response.getWriter().flush();
  }

}

