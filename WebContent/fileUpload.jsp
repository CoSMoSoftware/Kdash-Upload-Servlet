<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
	<head>
	    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	    <title>Result Upload Servlet</title>

	    <link rel="stylesheet" href="resources/css/main.css" />
	    <script type="text/javascript" src="resources/js/jquery-3.2.1.min.js"></script>
	    <script type="text/javascript" src="resources/js/fileupload.js"></script>
	</head>
	<body>
	    <div class="panel" style="width: 849px;">
	        <h1>File Upload</h1>
	        <form id="fileUploadForm" method="post" action="upload" enctype="multipart/form-data">
	            <div class="form_group">
                    <div>
                        <label>Upload File</label><span id="colon">: </span><input id="fileAttachment" type="file" name="fileUpload" />
                        <span id="fileUploadErr">Please Upload A File!</span>
                        <span>Enter a tag name: <input type="text" name="tagName"  id="tagName" /> </span>
                        <span id="tagNameErr">Please Enter A Tag Name!</span>
	                </div>
	                <div>
	                    <span>File must be a .zip with attachments at the root folder</span>
	                </div>
	            </div>
	            <button id="uploadBtn" type="submit" class="btn btn_primary">Upload</button>
	        </form>
	    </div>

		<div class="panel" style="width: 849px;">
	        <a id="allFiles" class="hyperLink" href="<%=request.getContextPath()%>/resultList?jsp=true">List all uploaded files</a>
	    </div>
	</body>
</html>