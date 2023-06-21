#!/bin/sh
jpackage \
--type dmg \
--dest build/distributions \
--input build/jars \
--name m_nlcode_nl \
--main-class nl/nlcode/m/JfxLauncher \
--main-jar m_nlcode_nl-1.0.0.jar \
--java-options -Xmx2048m \
--runtime-image build/java-runtime \
--icon src/main/resources/m.nlcode.nl.icns \
--app-version 1.0.0 \
--vendor "nlcode.nl" \
--copyright "Copyright © 2023 Leo Mekenkamp" \
--mac-package-identifier m_nlcode_nl \
--mac-package-name m.nlcode.nl

