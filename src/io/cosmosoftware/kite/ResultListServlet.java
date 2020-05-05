package io.cosmosoftware.kite;
import java.io.*;
import java.util.Arrays;
import javax.json.*;
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

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    this.handleRequest(request, response);
  }

  public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    String osName = System.getProperty("os.name").toLowerCase();
    File allureDirectory;
    String json = request.getParameter("json");
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
    String[] bannedFolders = new String[]{"plugins", "data", "index.html", "favicon.ico", "history", "widgets", "styles.css", "app.js", "export"};

    for (File result: resultList) {
      if(!Arrays.stream(bannedFolders).anyMatch(result.getName()::equals)) {
        JsonObject status = Utils.countStatus(result.getAbsolutePath());
        JsonObjectBuilder fileJsonBuilder = Json.createObjectBuilder();
        fileJsonBuilder.add("status", status);
        fileJsonBuilder.add("name", result.getName());
        fileJsonBuilder.add("lastModified", result.lastModified());
        fileJsonBuilder.add("allureURL", "https://" + request.getServerName() + "/" + result.getName());
        arrayBuilder.add(fileJsonBuilder.build());
      }
    }

    if(json == null) {
      request.setAttribute("allFiles", arrayBuilder.build());
      RequestDispatcher dispatcher = request.getRequestDispatcher("/allFiles.jsp");
      dispatcher.forward(request, response);
    } else {
      response.setStatus(200);
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      response.getWriter().print(arrayBuilder.build().toString());
      response.getWriter().flush();
    }
  }

}

