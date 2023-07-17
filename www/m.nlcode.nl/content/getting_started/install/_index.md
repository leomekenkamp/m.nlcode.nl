+++
archetype = "default"
title = "Installation"
weight = "2"
+++

{{% notice style="primary" title="WARNING: early alpha" icon="skull-crossbones" %}}
This project is in early alfa stage. Which means that if you eventually succeed in getting this
application to run, it will have cost you your sanity, marriage and what have you not.

Please note that although the version may be something like 1.0.*x*, with *x* being some digit,
this does not mean that **m.nlcode.nl** is out of alpha/beta stage. The first golden release will
have version *1.1.0*. This is due to limitations of the build system.
{{% /notice %}}

## Mac
Download either a dmg image or a pkg installer from gitlab.

## Windows
Although **m.nlcode.nl** is a Java application, I currently have no access to a Windows build
environment needed to create a Windows distribution. For the time being, see **Other** below.

## Linux
Although **m.nlcode.nl** is a Java application, I currently have no access to a Linux build
environment needed to create a Linux distribution. For the time being, see **Other** below.

## Other (Java required)
1. First install a JDK version 17 or higher from https://adoptium.net/en-GB/.
1. Install JavaFX for your system, version 17 or higher, from https://openjfx.io/openjfx-docs/#install-javafx.
1. Clone the Gitlab repo, by e.g. git clone https://gitlab.com/nlcode.nl/m.nlcode.nl.git 
1. Go into that directory and execute **./gradlew run** or **gradlew run** if you are on Windows.
The first time will take a while; consecutive runs will be a lot faster.

## Build installer from source (Java required)
Follow the steps from "**Other (Java required)**" above, but execute **gradlew jpackage**" in the
last step. You can find the installer in *./build/jpackage*.