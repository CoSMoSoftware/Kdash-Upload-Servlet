<%@page import="javax.json.JsonArray" %>
<%@page import="javax.json.JsonObject" %>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Result Upload Servlet</title>
        <link rel="stylesheet" href="resources/css/main.css" />
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
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
        <div class="panel">
            <h1>Uploaded Results</h1>
            <div class="margin_top_15px">
                <a id="fileUpload" class="hyperLink" href="<%=request.getContextPath()%>/fileUpload.jsp">Upload file</a>
            </div>
            <table class="bordered_table">
                <thead>
                <tr align="center"><th>Number of reports</th><th>Free Storage</th><th>Used Storage</th></tr>
                </thead>
                <tbody>
                <tr>
                    <td align="center"><span> <%=stats.getInt("foldersCount") %></span></td>
                    <td align="center"><span> <%=stats.getString("freeSpace") %></span></td>
                    <td align="center"><span> <%=stats.getString("usedSpace") %></span></td>
                </tr>
                </tbody>
            </table>
            <div class="margin_top_15px">
                Filter by tag name <input type ="text" id="tagNameid"  value="<%= tagNamePlaceHolder %>"  />
                <button onclick="filterTagName()">Apply</button>
                <button onclick="unfilterTagName()">Clear</button>
            </div>
            <div class="margin_top_15px">
                <div style="display:inline-block">
                Filter by status
                    <div>
                        <% if(passed) { %>
                            <input type="checkbox" id="passedid" name="status" value="passed"
                                   checked>
                        <% } else { %>
                            <input type="checkbox" id="passedid" name="status" value="passed">
                        <% } %>

                        <label for="passedid">Passed</label>
                    </div>
                    <div>
                        <% if(failed) { %>
                        <input type="checkbox" id="failedid" name="status" value="failed"
                               checked>
                        <% } else { %>
                        <input type="checkbox" id="failedid" name="status" value="failed">
                        <% } %>
                        <label for="failedid">Failed</label>
                    </div>
                    <div>
                        <% if(broken) { %>
                        <input type="checkbox" id="brokenid" name="status" value="broken"
                               checked>
                        <% } else { %>
                        <input type="checkbox" id="brokenid" name="status" value="broken">
                        <% } %>
                        <label for="brokenid">Broken</label>
                    </div>
                </div>
                <div style="display:inline-block">
                    <button onclick="filterStatus()">Apply</button>
                    <button onclick="unfilterStatus()">Clear</button>
                </div>
            </div>
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
                     <td align="center"><a id="tagName" class="hyperLink" href='<%=results.getJsonObject(i).getString("allureURL")%>'><%=results.getJsonObject(i).getString("name") %></a></td>
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
                      <td align="center"><span id="delete"><button onclick="delete_report(this)" value='<%=results.getJsonObject(i).getString("name")%>'>Delete</button></span></td>
                  </tr>
                  <% }
                   } else { %>
                  <tr>
                     <td colspan="3" align="center"><span id="noFiles">No Files Uploaded.....!</span></td>
                  </tr>
                  <% } %>
               </tbody>
            </table>
         </div>
        <script>
            function delete_report(btn) {
                $.post('/kdash/delete', { tagName: btn.value },
                    function(data){
                        location.reload();
                });
            }

            function filterTagName() {
               let value = document.getElementById('tagNameid').value;
               location.href= "?tagName=" + value + "&" +( "<%=statusParams%>" == null ? "": "<%=statusParams%>") ;
            }

            function unfilterTagName() {
               let value = document.getElementById('tagNameid').value;
               location.href= "?" + ( "<%=statusParams%>" == null ? "": "<%=statusParams%>");
            }

            function filterStatus() {
               let params = "?";
               if(document.getElementById('passedid').checked) {
                    params += "passed=true&";
               }
               if(document.getElementById('brokenid').checked) {
                    params += "broken=true&";
               }
               if(document.getElementById('failedid').checked) {
                    params += "failed=true&"
               }
               location.href= params + ("<%=tagName%>" == "null" ? "": "tagName=<%=tagName%>");
            }

            function unfilterStatus() {
                location.href= ("<%=tagName%>" == "null" ? "?": "?tagName=<%=tagName%>");
            }
        </script>
     </body>
</html>