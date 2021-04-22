# mac-codesigner project

Commandline utility to not going insane when codesigning and notarizing application for Apple Devices. 

This utility allows to:
* Codesign your applications
* Package as DMG
* Package as PKG
* Notarize the DMG and PKG
* Staple DMG and PKG artifact

## Synopsis

To print all available commandline options 
```shell script
./mac-codesigner-1.0.0-SNAPSHOT-runner -h
```
Example:
```shell script
./mac-codesigner-1.0.0-SNAPSHOT-runner  --app-path=/path/to/my/Sample.app \
 --developer-certificate-key=1111111111111111 \
 --with-dmg \
 --with-pkg \
 --installer-certificate-key=1111111111 \
 --pkg-identifier=my.application.identifier.pkg \
 --entitlements=/path/to/entitlements.plist \
 --with-notarization \
 --notarization-primary-bundle-id=my.application.id \
 --notarization-username=john@doe.com \
 --notarization-password="johndoe-secret"
 --with-staple
```

## Building

The application can be packaged using:
```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/mac-codesigner-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.
