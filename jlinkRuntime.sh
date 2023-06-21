#!/bin/sh
jlink \
  --strip-native-commands \
  --no-header-files \
  --no-man-pages  \
  --compress=2  \
  --strip-debug \
  --add-modules "jdk.localedata,java.base,java.desktop,java.management,java.naming,java.prefs,java.scripting,java.sql,jdk.jfr,jdk.jsobject,jdk.unsupported,jdk.xml.dom" \
  --include-locales=en,nl \
  --output build/java-runtime
