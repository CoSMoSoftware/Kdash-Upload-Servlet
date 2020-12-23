package io.cosmosoftware.kite;
import static io.cosmosoftware.kite.Utils.checkLogFilePath;
import static io.cosmosoftware.kite.Utils.isLinuxBased;
import static io.cosmosoftware.kite.Utils.isWindowsBased;

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
  private static final int MAX_FILE_COUNT = 5000;
  public ResultListServlet() {
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      this.handleRequest(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    this.handleRequest(request, response);
  }

  public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    File allureDirectory;
    long startTime = System.currentTimeMillis();
    String json = request.getParameter("json");
    String startString = request.getParameter("start");
    String tagName = request.getParameter("tagName");
    ArrayList<String> statusFilters = new ArrayList<>();
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
    String archives = request.getParameter("archives");
    String record = request.getParameter("record");

    String path;

    if (isWindowsBased()) {
      path = "C:\\nginx\\html\\allure\\" + (archives == null ? "" : "archives\\" + record);
    } else if (isLinuxBased()) {
      path = "/var/www/allure/" + (archives == null ? "" : "archives/" + record);
    } else {
      response.sendError(
              HttpServletResponse.SC_BAD_REQUEST,
              "Only Windows and Linux are supported.");
      return;
    }

    allureDirectory = new File(path);

    File[] resultList;
    resultList = allureDirectory.listFiles(getFilter(allureDirectory, tagName, statusFilters));
    if (resultList == null) {
      resultList = new File[0];
    }
    Arrays.sort(resultList, Comparator.comparingLong(File::lastModified).reversed());
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    JsonObjectBuilder statBuilder = Json.createObjectBuilder();
    String[] bannedFolders = new String[]{"plugins", "data", "index.html", "favicon.ico", "history", "widgets", "styles.css", "app.js", "export", "archives"};

    if(start > resultList.length) {
      start = start - perPage;
      if (start < 0) {
        start = 0;
      }
    }
    HashMap<String, Integer> overAllStatus = new HashMap<>();
    for (int i = start; i < resultList.length && i < MAX_FILE_COUNT; i++) {
      File result = resultList[i];
      JsonObject status = Utils.countStatus(result.getAbsolutePath());
      for (String key : status.keySet()) {
        if (!overAllStatus.containsKey(key)) {
          overAllStatus.put(key, 0);
        }
        overAllStatus.put(key, overAllStatus.get(key) + status.getInt(key));
      }
      if (Arrays.stream(bannedFolders).noneMatch(result.getName()::equals)) {
        if(i - start < perPage) {
          JsonObjectBuilder fileJsonBuilder = Json.createObjectBuilder();
          fileJsonBuilder.add("status", status);
          fileJsonBuilder.add("name", result.getName());
          fileJsonBuilder.add("lastModified", result.lastModified());
          fileJsonBuilder.add("logs", checkLogFilePath(result.getName()));

          fileJsonBuilder.add("size", Utils.readableFileSize(FileUtils.sizeOfDirectory(result)));
          fileJsonBuilder
              .add("allureURL", "https://" + request.getServerName() + "/" + result.getName());
          arrayBuilder.add(fileJsonBuilder.build());
        }
      }
    }
    for (String key: overAllStatus.keySet()) {
      statBuilder.add(key, overAllStatus.get(key));
    }
    statBuilder.add("foldersCount", resultList.length);
//    statBuilder.add("usedSpace", Utils.readableFileSize(FileUtils.sizeOfDirectory(allureDirectory)));
    statBuilder.add("freeSpace", Utils.readableFileSize(allureDirectory.getFreeSpace()));
    statBuilder.add("processTime", System.currentTimeMillis() - startTime);

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

  private FilenameFilter getFilter(File path, String tagName, ArrayList<String> statusFilters) {
    return (dir, name) ->
        name.contains(tagName == null ? "" : tagName)
            && (statusFilters.isEmpty() || Utils.isReport(path + (isWindowsBased() ? "\\" : "/") + name, statusFilters));
  }

}

