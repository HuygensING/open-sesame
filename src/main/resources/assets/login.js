function loginFederated() {
  var hsVia = 'clarin'; // options are: 'surf' and 'clarin'

  var wl = window.location;
  var loginURL = 'https://secure.huygens.knaw.nl/saml2/login';
  var returnURL = wl.origin + wl.pathname;//'http://localhost:8080/index.html';

  var form = document.createElement('FORM');
  form.method = 'POST';
  form.action = loginURL;

  var hsUrlEl = document.createElement('INPUT');
  hsUrlEl.name = 'hsurl';
  hsUrlEl.value = returnURL;
  form.appendChild(hsUrlEl);

  var hsViaEl = document.createElement('INPUT');
  hsViaEl.name = 'hsvia';
  hsViaEl.value = hsVia;
  form.appendChild(hsViaEl);

  document.body.appendChild(form);
  form.submit();
}

function loginGoogle() {
  var wl = window.location;
  var here = wl.origin + wl.pathname;
  var loginURL = wl.origin + '/api/google/login?returnURL=' + here;

  location.replace(loginURL);
}