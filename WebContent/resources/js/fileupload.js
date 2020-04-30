/***** jQuery To Prevent The Form Submission In Case No File Is Selected For Upload ****/
$(document).ready(function() {        	 
	$("#fileUploadErr").hide();
    $("#tagNameErr").hide();
	/***** Hide The Error Message When The Attachment Btn Is Clicked. ****/
	$('#fileAttachment').click(function(eObj) {
		$("#fileUploadErr").hide();
        $("#tagNameErr").hide();
	});

	/***** Validating Whether The Attachment Is Uploaded Or Not. ****/
	$('#uploadBtn').click(function(eObj) {
		var file = $("#fileAttachment").map(function() {
			return $(this).val().trim() ? true : false;
		}).get();

		if (file.includes(true)) {
            var tagName = $("#tagName").map(function() {
                            return $(this).val().trim() ? true : false;
                        }).get();
                    if (tagName.includes(true)) {

                    } else {
                    	$("#tagNameErr").css({'color':'red', 'font-weight': 'bold'}).show();
                        eObj.preventDefault();
                    }

		} else {
			$("#fileUploadErr").css({'color':'red', 'font-weight': 'bold'}).show();
			eObj.preventDefault();
		}
	});
});