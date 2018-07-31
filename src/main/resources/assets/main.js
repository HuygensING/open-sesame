$(document).ready(function() {
  greet(function(str) {
    $('#greeting').text(str)
  });
})

$('#federated').click(function() {
  loginFederated();
})

$('#google').click(function() {
  loginGoogle();
})
