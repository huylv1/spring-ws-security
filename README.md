# Read Me First
Sample Spring WS SOAP web service which sets up various WS-Security protocols:

Two implementations of WS-Security, WSS4J and XWSS, are supported for each authentication methods:

Unsecure.
Plain password.
Digested password.
Signature.
Encryption.

### How to use keytool
Keystore is used by a server to store private keys, and Truststore is used by third party client to store public keys provided by server to access. I have done that in my production application. Below are the steps for generating java certificates for SSL communication:

Generate a certificate using keygen command in windows:
keytool -genkey -keystore server.keystore -alias mycert -keyalg RSA -keysize 2048 -validity 3950

Self certify the certificate:
keytool -selfcert -alias mycert -keystore server.keystore -validity 3950

Export certificate to folder:
keytool -export -alias mycert -keystore server.keystore -rfc -file mycert.cer

Import Certificate into client Truststore:
keytool -importcert -alias mycert -file mycert.cer -keystore truststore

