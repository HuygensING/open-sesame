function greet(item) {
  var huygensId = getParameterByName('hsid');
  var googleId = getParameterByName('gsid');

  if (huygensId.length > 0) {
    $.ajax({
      headers: {'Authorization': 'Huygens ' + huygensId},
      url: 'http://localhost:8080/api/protected',
      error: function(data) {
        console.log(data)
      },
      success: function(data) {
        item.text(data + ' (via Huygens)')
      },
      type: 'GET'
    })
  }
  else if (googleId.length > 0) {
    $.ajax({
      headers: {'Authorization': 'Google ' + googleId},
      url: 'http://localhost:8080/api/protected',
      error: function(data) {
        console.log(data)
      },
      success: function(data) {
        item.text(data + ' (via Google)')
      },
      type: 'GET'
    })
  } else {
    item.text("Welcome. Please login.")
  }
}
