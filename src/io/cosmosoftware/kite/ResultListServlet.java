package io.cosmosoftware.kite;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;
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
    String startString = request.getParameter("start");
    String tagName = request.getParameter("tagName");
    ArrayList<String> statusFilters = new ArrayList<String>();
    String failed = request.getParameter("failed");
    String passed = request.getParameter("passed");
    String broken = request.getParameter("broken");

    if(failed != null && failed.equals("true")) {
      statusFilters.add("failed");
    }
    if(passed != null && passed.equals("true")) {
      statusFilters.add("passed");
    }
    if(broken != null && broken.equals("true")) {
      statusFilters.add("broken");
    }
    if(startString == null || startString.contains("-")) {
      startString = "0";
    }
    int start = Integer.parseInt(startString);
    int perPage = 100;

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
    File[] resultList;
    FilenameFilter filter;
    if(tagName != null) {
      if(statusFilters.size() == 0) {
        filter = (dir, name) -> name.contains(tagName);
      } else {
        filter = (dir, name) -> name.contains(tagName) && Utils.isReport(allureDirectory + (osName.indexOf("win") >= 0 ? "\\" : "/") + name, statusFilters);
      }
      resultList = allureDirectory.listFiles(filter);
    } else {
      if(statusFilters.size() == 0) {
        resultList = allureDirectory.listFiles();
      } else {
        filter = (dir, name) ->  Utils.isReport(allureDirectory + (osName.indexOf("win") >= 0 ? "\\" : "/") + name, statusFilters);
        resultList = allureDirectory.listFiles(filter);
      }
    }
    Arrays.sort(resultList, Comparator.comparingLong(File::lastModified).reversed());
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    JsonObjectBuilder statBuilder = Json.createObjectBuilder();
    String[] bannedFolders = new String[]{"plugins", "data", "index.html", "favicon.ico", "history", "widgets", "styles.css", "app.js", "export"};

    if(start > resultList.length) {
      start = start - perPage;
      if (start < 0) {
        start = 0;
      }
    }

    for (int i = start; i < resultList.length && i - start < perPage; i++) {
      File result = resultList[i];
      if(!Arrays.stream(bannedFolders).anyMatch(result.getName()::equals)) {
        JsonObject status = Utils.countStatus(result.getAbsolutePath());
        JsonObjectBuilder fileJsonBuilder = Json.createObjectBuilder();
        fileJsonBuilder.add("status", status);
        fileJsonBuilder.add("name", result.getName());
        fileJsonBuilder.add("lastModified", result.lastModified());
        fileJsonBuilder.add("size", Utils.readableFileSize(FileUtils.sizeOfDirectory(result)));
        fileJsonBuilder.add("allureURL", "https://" + request.getServerName() + "/" + result.getName());
        arrayBuilder.add(fileJsonBuilder.build());
      }
    }

    statBuilder.add("foldersCount", resultList.length);
    statBuilder.add("usedSpace", Utils.readableFileSize(FileUtils.sizeOfDirectory(allureDirectory)));
    statBuilder.add("freeSpace", Utils.readableFileSize(allureDirectory.getFreeSpace()));

    if(json == null) {
      request.setAttribute("allFiles", arrayBuilder.build());
      request.setAttribute("stats", statBuilder.build());
      request.setAttribute("start", start);
      request.setAttribute("tagName", tagName);
      request.setAttribute("failed", failed);
      request.setAttribute("passed", passed);
      request.setAttribute("broken", broken);

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

