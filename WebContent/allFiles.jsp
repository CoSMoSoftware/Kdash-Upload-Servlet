<%@page import="javax.json.JsonArray" %>
<%@page import="javax.json.JsonObject" %>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Result Upload Servlet</title>

        <link rel="stylesheet" href="resources/css/main.css" />
    </head>
    <body>
        <div class="panel">
            <h1>Uploaded Results</h1>
            <table class="bordered_table">
               <thead>
                  <tr align="center"><th>Tag Name</th><th>Last Update</th><th>Test Cases</th><th>Action</th></tr>
               </thead>
               <tbody>
                 <%  JsonArray results = (JsonArray)request.getAttribute("allFiles");
                     if(results != null && results.size() > 0) {
                       for(int i=0; i<results.size(); i++) {
                  %>
                  <tr>
                     <td align="center"><span id="fileName"><%=results.getJsonObject(i).getString("name") %></span></td>
                     <td align="center"><span id="fileSize"><%=new java.sql.Date(results.getJsonObject(i).getJsonNumber("lastModified").longValue())  %></span></td>
                     <td>
                        <%
                         JsonObject status = results.getJsonObject(i).getJsonObject("status");
                            if(status != null && status.size() > 0) {
                         %>
                         <table>
                             <tbody>
                             <% for(String key: status.keySet()) { %>
                             <tr>
                                 <td align="center"><span> <%= key%>: <%= status.get(key)%></span></td>
                             </tr>
                             <% } %>
                             </tbody>
                         </table>
                         <% } %>
                     </td>
                     <td align="center"><span id="delete"><a id="downloadLink" class="hyperLink" href='<%=request.getContextPath()%>/delete?jsp=true&tagName=<%=results.getJsonObject(i).getString("name")%>'>Delete</a></span></td>
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
               <a id="fileUpload" class="hyperLink" href="<%=request.getContextPath()%>/fileUpload.jsp">Back</a>
            </div>
         </div>
     </body>
</html>