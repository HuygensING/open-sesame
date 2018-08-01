function greet(func) {
  var huygensId = getParameterByName('hsid');
  var googleId = getParameterByName('gsid');

  if (huygensId.length > 0) {
    getGreeting('Huygens ' + huygensId, func)
  }
  else if (googleId.length > 0) {
    getGreeting('Google ' + googleId, func)
  }
  else {
    func("Welcome. Please login.")
  }
}

function getParameterByName(name) {
  name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
  var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
  results = regex.exec(location.search);
  return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function getGreeting(auth, func) {
    $.ajax({
      headers: {'Authorization': auth},
      url: 'http://localhost:8080/api/protected',
      error: function(data) {
        console.log(data)
      },
      success: func,
      type: 'GET'
    })
}
