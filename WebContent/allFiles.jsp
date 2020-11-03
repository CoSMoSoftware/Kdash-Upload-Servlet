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
        <div class="panel">
            <h1>Uploaded Results</h1>
            <table class="bordered_table">
                <thead>
                <tr align="center"><th>Number of reports</th><th>Free Storage</th><th>Used Storage</th></tr>
                </thead>
                <tbody>
                <%  JsonObject stats = (JsonObject)request.getAttribute("stats");
                %>
                <tr>
                    <td align="center"><span> <%=stats.getInt("foldersCount") %></span></td>
                    <td align="center"><span> <%=stats.getString("freeSpace") %></span></td>
                    <td align="center"><span> <%=stats.getString("usedSpace") %></span></td>
                </tr>
                </tbody>
            </table>

            <table class="bordered_table">
               <thead>
               <tr align="center"><th>Tag Name</th><th>Last Update</th><th>Test Cases</th><th>Size</th></th><th>Action</th></tr>
               </thead>
               <tbody>
                 <%  JsonArray results = (JsonArray)request.getAttribute("allFiles");
                     if(results != null && results.size() > 0) {
                       for(int i=0; i<results.size(); i++) {
                  %>
                  <tr>
                     <td align="center"><a id="tagName" class="hyperLink" href='<%=results.getJsonObject(i).getString("allureURL")%>'><%=results.getJsonObject(i).getString("name") %></a></td>
                     <td align="center"><span id="fileSize"><%=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(results.getJsonObject(i).getJsonNumber("lastModified").longValue())  %></span></td>
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
                     <td align="center">
                         <%=results.getJsonObject(i).getString("size") %>
                     </td>
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
            <div class="margin_top_15px">
               <a id="fileUpload" class="hyperLink" href="<%=request.getContextPath()%>/fileUpload.jsp">Upload file</a>
            </div>
         </div>
        <script>
            function delete_report(btn) {
                $.post('/kdash/delete', { tagName: btn.value },
                    function(data){
                        location.reload();
                });
            }
        </script>
     </body>
</html>