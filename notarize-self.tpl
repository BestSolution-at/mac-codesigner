codesign --strict --verbose=4 --deep --force --options=runtime --sign "xxxxxxx" mac-codesigner-1.0.0-SNAPSHOT-runner
hdiutil create -volname mac-codesigner -srcfolder mac-codesigner-1.0.0-SNAPSHOT-runner -ov -format UDZO mac-codesigner-1.0.0-alpha.1.dmg
xcrun altool  --notarize-app --primary-bundle-id "at.bestsolutio.mac-codesigner" --username="xxxxx" --password="xxxxx" -f mac-codesigner-1.0.0-alpha.1.dmg
xcrun stapler staple mac-codesigner-1.0.0-alpha.1.dmg