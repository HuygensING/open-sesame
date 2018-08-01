'use strict';

Util.onReady(function () {
    var hocrBaseUrl = '/api/argos/NL-HaNA_1.01.02_3795_0127';

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
        request.send('hocr=' + encodeURIComponent(hocr));
    });

    document.getElementById('login-federated').addEventListener('click', function () {
        var auth = 'abc-123-def-ghi-456'
        sessionStorage.auth = auth;
    });

    document.getElementById('login-google').addEventListener('click', function() {
      console.log('auth: ' + sessionStorage.auth);
    });

    var hocrUrl = hocrBaseUrl + '/hocr';
    Util.get(hocrUrl, function (err, hocr) {
        if (err) return Util.handleError(err);

        hocrProofreader.setHocr(hocr, hocrBaseUrl);
    });
});
