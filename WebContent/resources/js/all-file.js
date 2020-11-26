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

//Get the button:
mybutton = document.getElementById("myBtn");

// When the user scrolls down 20px from the top of the document, show the button
window.onscroll = function() {scrollFunction()};

function scrollFunction() {
  if (document.body.scrollTop > 20 || document.documentElement.scrollTop > 20) {
    mybutton.style.display = "block";
  } else {
    mybutton.style.display = "none";
  }
}

// When the user clicks on the button, scroll to the top of the document
function topFunction() {
  document.body.scrollTop = 0; // For Safari
  document.documentElement.scrollTop = 0; // For Chrome, Firefox, IE and Opera
}