'use strict';

Util.onReady(function () {
    var apiUrl = '/api'
    var argosUrl = apiUrl + '/argos';

    var documentId = getParameterByName('doc');

    if (!documentId) {
        documentId = sessionStorage.documentId;

        if (!documentId) {
            documentId = 'NL-HaNA_1.01.02_3795_0127'; // arbitrary default page
        }

        window.location = window.location.href.split('?')[0] + '?doc=' + documentId;
    }

    console.log('documentId: ' + documentId);
    sessionStorage.documentId = documentId;

    var hocrBaseUrl = argosUrl + '/' + documentId;

    var hocrProofreader = new HocrProofreader({
        layoutContainer: 'layout-container',
        editorContainer: 'editor-container'
    });

    document.getElementById('select-file').addEventListener('click', listFiles)

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

    document.getElementById('auth-federated').addEventListener('click', function () {
        saveState();
        loginFederated();
    });

    document.getElementById('auth-google').addEventListener('click', function () {
        saveState();
        loginGoogle();
    });

    document.getElementById('auth-logout').addEventListener('click', function () {
        saveState();
        logout();
    });

    document.getElementById('button-save').addEventListener('click', save);

    parseAuthParams();
    initAuthButtons();

    if (sessionStorage.hocr) {
        restoreState();
    }
    else  {
        var hocrUrl = hocrBaseUrl + '/hocr';
        Util.get(hocrUrl, function (err, hocr) {
            if (err) return Util.handleError(err);

            hocrProofreader.setHocr(hocr, hocrBaseUrl);
            document.title = 'hOCR-Proofreader :: ' + sessionStorage.documentId;
        });
    }

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

    function listFiles() {
        delete sessionStorage.hocr; // drop unsaved changes if you switch to another document

        var xhr = new XMLHttpRequest();
        xhr.onload = function () {
            var jsonResponse = xhr.response;
            var lc = document.getElementById('list-container')
            while (lc.firstChild) {
                lc.removeChild(lc.firstChild);
            }
            var ul = document.createElement("UL");
            lc.appendChild(ul);

            for (var i=0; i < jsonResponse.length; i++) {
                var documentId = jsonResponse[i];
                var li = document.createElement('LI');
                li.value = i;
                var a = document.createElement("A");
                var locationWithoutParams = window.location.href.split('?')[0];
                a.href = locationWithoutParams + '?doc=' + documentId;
                a.appendChild(document.createTextNode(documentId));
                li.appendChild(a);
                ul.appendChild(li);
            }
        };
        xhr.open('GET', argosUrl);
        xhr.responseType = 'json';
        xhr.send();
    }

    function logout(cleanup = true) {
        var auth = sessionStorage.auth;
        if (auth) {
            delete sessionStorage.auth;
            initAuthButtons();

            if (cleanup) {
                if (auth.startsWith('Google')) {
                    var sessionId = auth.substring(auth.indexOf(' ') + 1);
                    var deleteUrl = apiUrl + '/google/' + sessionId;
                    var xhr = new XMLHttpRequest();
                    xhr.open('DELETE', deleteUrl);
                    xhr.setRequestHeader('Authorization', auth);
                    /* In case you want to handle the result:
                    xhr.onreadystatechange = function() {
                        if (this.readyState == XMLHttpRequest.DONE) {
                            console.log('DELETE status: ' + this.status + ', response: ' + this.responseText);
                        }
                    };*/
                    xhr.send();
                }
            }
        }
    }

    function parseAuthParams() {
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
    }

    function reloadWithoutParams() {
        var locationWithoutParams = window.location.href.split('?')[0];
        var documentId = getParameterByName('doc');
        location.replace(locationWithoutParams + '?doc=' + documentId);
    }

    function save() {
        var hocr = hocrProofreader.getHocr();

        var xhr = new XMLHttpRequest();
        xhr.open('PUT', hocrBaseUrl + '/text');
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=utf-8');
        if (sessionStorage.auth) xhr.setRequestHeader('Authorization', sessionStorage.auth);
        xhr.onreadystatechange = function() {
            if (this.readyState == XMLHttpRequest.DONE) {
                // TODO: ik zou zeggen: maak er iets moois van.
                if (this.status == 204) {
                    alert("Opgeslagen op server.");
                    delete sessionStorage.hocr;
                }
                if (this.status == 401) {
                    logout(false);
                    alert("Je sessie is verlopen. Log opnieuw in.");
                }
                if (this.status == 403) alert("Toegang geweigerd.");
            }
        };
        xhr.send('hocr=' + encodeURIComponent(hocr));
    }

    function restoreState() {
        hocrProofreader.setHocr(sessionStorage.hocr, hocrBaseUrl);
    }

    function saveState() {
        sessionStorage.hocr = hocrProofreader.getHocr(); // save current state of text
    }

});
