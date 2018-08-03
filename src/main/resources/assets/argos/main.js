'use strict';

Util.onReady(function () {
    var apiUrl = '/api'
    var hocrBaseUrl = apiUrl + '/argos/NL-HaNA_1.01.02_3795_0127';

    var hocrProofreader = new HocrProofreader({
        layoutContainer: 'layout-container',
        editorContainer: 'editor-container'
    });

    document.getElementById('toggle-layout-image').addEventListener('click', function () {
        hocrProofreader.toggleLayoutImage();
    });

    document.getElementById('zoom-page-full').addEventListener('click', function () {
        hocrProofreader.setZoom('page-full');
    });

    document.getElementById('zoom-page-width').addEventListener('click', function () {
        hocrProofreader.setZoom('page-width');
    });

    document.getElementById('zoom-original').addEventListener('click', function () {
        hocrProofreader.setZoom('original');
    });

    document.getElementById('button-save').addEventListener('click', function () {
        var hocr = hocrProofreader.getHocr();

        var request = new XMLHttpRequest();
        request.open('PUT', hocrBaseUrl + '/text');
        request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=utf-8');
        if (sessionStorage.auth) request.setRequestHeader('Authorization', sessionStorage.auth);
        request.send('hocr=' + encodeURIComponent(hocr));
    });

    document.getElementById('auth-federated').addEventListener('click', function () {
        loginFederated();
    });

    document.getElementById('auth-google').addEventListener('click', function() {
        var returnUrl = window.location;
        loginGoogle();
    });

    document.getElementById('auth-logout').addEventListener('click', function() {
        console.log('auth: logout');
        var auth = sessionStorage.auth;
        if (auth) {
            console.log("Logout, auth=" + auth);
            if (auth.startsWith('Google')) {
                console.log('auth starts with "Google"');
                var sessionId = auth.substring(auth.indexOf(' ') + 1);
                var deleteUrl = apiUrl + '/google/' + sessionId;
                var xhr = new XMLHttpRequest();
                xhr.open('DELETE', deleteUrl);
                xhr.onreadystatechange = function() {
                    if (this.readyState == XMLHttpRequest.DONE) {
                        console.log('DELETE status: ' + this.status + ', response: ' + this.responseText);
                    }
                };
                xhr.send();
            }
            delete sessionStorage.auth;
            initAuthButtons();
        }
    });

    var gsId = getParameterByName('gsid');
    if (gsId) {
        sessionStorage.auth = 'Google ' + gsId;
        reloadWithoutParams();
    }

    var hsId = getParameterByName('hsid');
    if (hsId) {
        sessionStorage.auth = 'Huygens ' + hsId;
        reloadWithoutParams();
    }

    initAuthButtons();

    var hocrUrl = hocrBaseUrl + '/hocr';
    Util.get(hocrUrl, function (err, hocr) {
        if (err) return Util.handleError(err);

        hocrProofreader.setHocr(hocr, hocrBaseUrl);
    });

    function initAuthButtons() {
        document.getElementById('auth-federated').disabled = (sessionStorage.auth) ? true : false;
        document.getElementById('auth-google').disabled = (sessionStorage.auth) ? true : false;
        document.getElementById('auth-logout').disabled = (sessionStorage.auth) ? false : true;
        document.getElementById('button-save').disabled = (sessionStorage.auth) ? false : true;
    }

    function getParameterByName(name) {
      name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
      var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
      var results = regex.exec(location.search);
      return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    }

    function reloadWithoutParams() {
        var locationWithoutParams = window.location.href.split('?')[0];
        location.replace(locationWithoutParams);
    }
});
