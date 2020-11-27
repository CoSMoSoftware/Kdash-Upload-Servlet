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
