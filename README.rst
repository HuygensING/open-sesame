===========
Open Sesame
===========

Open Sesame is a proof-of-concept application demonstrating how to use
Huygens Federated authentication and Google authentication in a Dropwizard
app.

Getting the source
------------------

Clone from ``github``::

    $ git clone git@github.com:HuygensING/open-sesame.git

Building
--------

Use the gradle wrapper ``./gradlew`` to build::

    $ ./gradlew build

Prerequisites for running
-------------------------

For the Google authentication part to work you will have to obtain a
``Client ID`` and ``Client Secrets`` using the `Google Dashboard` [#]_:

* Visit `Google API Console <https://console.developers.google.com/>`_,
  then:

    - Select the ``Credentials`` setting from the sidebar on the left.
    - Select ``Create credentials`` for an ``OAuth client ID``.
    - Select ``Web application`` as the *Application type*.
    - Leave *Authorized JavaScript origins* empty.
    - Under *Authorized redirect URIs*, enter the proper URL where Google
      will redirect the browser after authentication is complete, passing
      auth token and state:

      - For local testing, use ``http://localhost:8080/api/google/oauth2``
        if your server will be running on the default ``localhost``,
        port ``8080``.
      - Multiple URLs are allowed in the dashboard and changes in the
        dashboard are effective immediately
      - Ultimately (in production) this URL **must match**
        what you setup in `nl.knaw.huc.di.sesame.resources.GoogleLogin <https://github.com/HuygensING/open-sesame/blob/master/src/main/java/nl/knaw/huc/di/sesame/resources/GoogleLogin.java>`_ (q.v.)

* Copy ``config-template.yaml`` to, e.g., ``config.yaml``, then edit
  ``config.yaml`` to setup:

  - your Google ``Client ID`` and ``Client Secret``::

      google:
        clientId: your-client-id-here.apps.googleusercontent.com
        clientSecret: your-client-secret-here

  - your ``Huygens Security Server`` secret (supplied by Concern Infrastructure)::

      federatedAuthentication:
        credentials: Huygens security-server-key-here
        url: security-server-url-here


Running
-------

Now, you should be good to go::

  $ java -jar ./build/libs/open-sesame-full.jar server config.yaml

and visit `<http://localhost:8080/argos/index.html>`_

Source
------

Some stuff is currently still hardwired, which can easily be migrated
to configuration time bindings:

  * HOCR files' location is hardwired in
    ``nl.knaw.huc.di.sesame.SesameApplication#registerResources``
  * uploaded files are dumped in ``/tmp`` as dictated by
    ``nl.knaw.huc.di.sesame.resources.argos.Argos#putText``

If you wish to dive straight into *using* authorization,
`nl.knaw.huc.di.sesame.auth.DefaultAuthorizer
<https://github.com/HuygensING/open-sesame/blob/master/src/main/java/nl/knaw/huc/di/sesame/auth/DefaultAuthorizer.java>`_
and
`nl.knaw.huc.di.sesame.resources.argos.Argos
<https://github.com/HuygensING/open-sesame/blob/master/src/main/java/nl/knaw/huc/di/sesame/resources/argos/Argos.java>`_
form a good starting point.

.. [#] For background and more info see:
       "*Obtain OAuth 2.0 credentials from the Google API Console*"
       in `Using OAuth 2.0 to Access Google APIs
       <https://developers.google.com/identity/protocols/OAuth2>`_
