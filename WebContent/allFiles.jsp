<%@page import="javax.json.JsonArray" %>
<%@page import="javax.json.JsonObject" %>
<%@page import="java.text.DecimalFormat" %>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Result Upload Servlet</title>
    <link rel="stylesheet" href="resources/css/bootstrap.css" />
    <link rel="stylesheet" href="resources/css/bootstrap-theme.css" />
    <link rel="stylesheet" href="resources/css/main.css" />
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="resources/js/bootstrap.js"></script>
    <script src="resources/js/all-file.js"></script>

  </head>
  <body>
    <%
      int start = (int)request.getAttribute("start");
      String tagName = (String)request.getAttribute("tagName");
      boolean failed = request.getAttribute("failed") == null ? false : request.getAttribute("failed").equals("true");
      boolean broken = request.getAttribute("broken") == null ? false : request.getAttribute("broken").equals("true");
      boolean passed = request.getAttribute("passed") == null ? false : request.getAttribute("passed").equals("true");

      String statusParams = "";
      if(failed) {
        statusParams += "failed=true&";
      }
      if(broken) {
        statusParams += "broken=true&";
      }
      if(passed) {
        statusParams += "passed=true&";
      }

      JsonObject stats = (JsonObject)request.getAttribute("stats");
      String tagNamePlaceHolder = "";
      if(tagName != null) {
        tagNamePlaceHolder = tagName;
      }
      int currentPage = start / 100 + 1;
      %>
    <div class="card">
      <div class="card-body">
        <table id="storage" style="width:70%;">
          <tbody>
            <tr>
              <td align="left" style="border: none;">
                <label style="font-size: 50px;">KITE Results </label>
              </td>

              <td align="left" style="border: none;">
                <button type="button" class="btn btn-primary btn-lg">
                  <a id="fileUpload" class="hyperLink"
                  href="<%=request.getContextPath()%>/fileUpload.jsp">Upload file</a>
                </button>
              </td>
            </tr>
            <tr>
              <td align="left" style="border: none;">

                <span> <b>Number of reports : </b><%=stats.getInt("foldersCount") %></span>
                <br>
                <span> <b>Free Storage : </b><%=stats.getString("freeSpace") %></span>
                <br>
                <b>Filter by tag name:</b>
                <br>
                <input type ="text" id="tagNameid"  value="<%= tagNamePlaceHolder %>"  />
                <button type="button" class="btn btn-link btn-sm" onclick="filterTagName()">Apply</button>
                <button type="button" class="btn btn-link btn-sm" onclick="unfilterTagName()">Clear</button>
              </td>
              <td align="left" style="border: none;">
                <b>Filter by status:</b>
                <button type="button" class="btn btn-link btn-sm" onclick="filterStatus()">Apply</button>
                <button type="button" class="btn btn-link btn-sm" onclick="unfilterStatus()">Clear</button>
                <%
                  int passedCnt = stats.getInt("passed", 0);
                  int failedCnt = stats.getInt("failed", 0);
                  int brokenCnt = stats.getInt("broken", 0);
                  int totalCnt = passedCnt + failedCnt + brokenCnt;
                  if (totalCnt == 0){
                    totalCnt =1;
                  }
                  DecimalFormat df = new DecimalFormat("0.00");
                %>
                <div class="row">
                  <div class="col-4">
                    <% if(passed) { %>
                    <input type="checkbox" id="passedid" name="status" value="passed" checked>
                    <% } else { %>
                    <input type="checkbox" id="passedid" name="status" value="passed">
                    <% } %>
                    <label for="passedid">Passed (<%=df.format((double)100*passedCnt/totalCnt)%>%)</label>
                  </div>
                  <div class="col-8">
                    <div class="progress">
                      <div class="progress-bar bg-success progress-bar-striped"
                      style="width:<%=df.format((double)100*passedCnt/totalCnt)%>%" aria-valuenow=<%=passedCnt%> aria-valuemin="0" aria-valuemax=<%=totalCnt%>>
                        <%=df.format((double)100*passedCnt/totalCnt)%>%
                      </div>
                    </div>
                  </div>
                </div>
                <div class="row">
                  <div class="col-4">
                    <% if(failed) { %>
                    <input type="checkbox" id="failedid" name="status" value="failed" checked>
                    <% } else { %>
                    <input type="checkbox" id="failedid" name="status" value="failed">
                    <% } %>
                    <label for="failedid">Failed (<%=df.format((double)100*failedCnt/totalCnt)%>%)</label>
                  </div>
                  <div class="col-8">
                    <div class="progress">
                      <div class="progress-bar bg-danger progress-bar-striped"
                      style="width:<%=100*failedCnt/totalCnt%>%" aria-valuenow=<%=failedCnt%> aria-valuemin="0" aria-valuemax=<%=totalCnt%>>
                        <%=df.format((double)100*failedCnt/totalCnt)%>%
                      </div>
                    </div>
                  </div>
                </div>
                <div class="row">
                  <div class="col-4">
                    <% if(broken) { %>
                    <input type="checkbox" id="brokenid" name="status" value="broken" checked>
                    <% } else { %>
                    <input type="checkbox" id="brokenid" name="status" value="broken">
                    <% } %>
                    <label for="brokenid">Broken (<%=df.format((double)100*brokenCnt/totalCnt)%>%)</label>
                  </div>
                  <div class="col-8">
                    <div class="progress">
                      <div class="progress-bar bg-warning progress-bar-striped"
                      style="width:<%=100*brokenCnt/totalCnt%>%" aria-valuenow=<%=brokenCnt%> aria-valuemin="0" aria-valuemax=<%=totalCnt%>>
                        <%=df.format((double)100*brokenCnt/totalCnt)%>%
                      </div>
                    </div>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="margin_top_15px">
          <% if(tagName == null) { %>
          <a href='?start=0&<%=statusParams%>'>First</a>
          <a href='?start=<%=start-100%>&<%=statusParams%>'><<</a>
          <span><%=currentPage %></span>
          <a href='?start=<%=start+100%>&<%=statusParams%>'>>></a>
          <a href='?start=<%=stats.getInt("foldersCount") - ( stats.getInt("foldersCount") % 100 ) %>&<%=statusParams%>'>Last</a>
          <% } else { %>
          <a href='?start=0&tagName=<%=tagName%>&<%=statusParams%>'>First</a>
          <a href='?start=<%=start-100%>&tagName=<%=tagName%>&<%=statusParams%>'><<</a>
          <span><%=currentPage %></span>
          <a href='?start=<%=start+100%>&tagName=<%=tagName%>&<%=statusParams%>'>>></a>
          <a href='?start=<%=stats.getInt("foldersCount") - 100 %>&tagName=<%=tagName%>&<%=statusParams%>'>Last</a>
          <% } %>
          <span>(Showing 100 results)</span>
        </div>
        <table class="bordered_table">
        <thead>
        <tr align="center"><th style="width:100px"></th><th>Tag Name</th><th>Last Update</th><th>Test Cases</th><th>Size</th></th><th>Action</th></tr>
        </thead>
        <tbody>
          <%  JsonArray results = (JsonArray)request.getAttribute("allFiles");
          if(results != null && results.size() > 0) {
          for(int i=0; i<results.size(); i++) {
          %>
          <tr>
            <td align="center"><%=start+i+1%></td>
            <td align="center"><a id="tagName" href='<%=results.getJsonObject(i).getString("allureURL")%>'><%=results.getJsonObject(i).getString("name") %></a></td>
            <td align="center"><span id="fileSize"><%=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(results.getJsonObject(i).getJsonNumber("lastModified").longValue())  %></span></td>
            <td align="center">
              <%
              JsonObject status = results.getJsonObject(i).getJsonObject("status");
              if(status != null && status.size() > 0) {
              %>
              <table>
                <tbody>
                  <tr>
                    <% for(String key: status.keySet()) { %>
                    <td align="center"  class="<%=key%>"><span> <%= key%>: <%= status.get(key)%></span></td>
                    <% } %>
                  </tr>
                </tbody>
              </table>
            <% } %>
          </td>
          <td align="center"><%=results.getJsonObject(i).getString("size") %></td>
          <td align="center"><span id="delete"><button type="button" class="btn btn-danger btn-small" onclick="delete_report(this)" value='<%=results.getJsonObject(i).getString("name")%>'>Delete</button></span></td>
        </tr>
        <% }
          } else { %>
        <tr>
        <td colspan="3" align="center"><span id="noFiles">No results found!</span></td>
        </tr>
        <% } %>
        </tbody>
      </table>
      </div>
    </div>
  </body>
</html>
