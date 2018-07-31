function getParameterByName(name) {
  name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
  var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
  results = regex.exec(location.search);
  return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function loginFederated() {
  var hsVia = 'clarin'; // options are: 'surf' and 'clarin'

  var wl = window.location;
  var loginURL = 'https://secure.huygens.knaw.nl/saml2/login';
  var returnURL = wl.origin + wl.pathname;//'http://localhost:8080/index.html';

  var form = $('<form>').attr({
      method: 'POST',
      action: loginURL
  });

  hsUrlEl = $('<input>').attr({
      name: 'hsurl', value: returnURL, type: 'hidden'
  });
  form.append(hsUrlEl);

  hsViaEl = $('<input>').attr({
      name: 'hsvia', value: hsVia, type: 'hidden'
  });
  form.append(hsViaEl);

  $('body').append(form);
  form.submit();
}

function loginGoogle() {
  var wl = window.location;
  var here = wl.origin + wl.pathname;
  var loginURL = wl.origin + '/api/google/login?returnURL=' + here;

  location.replace(loginURL);
}