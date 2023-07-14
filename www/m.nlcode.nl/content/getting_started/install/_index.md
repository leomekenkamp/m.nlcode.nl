+++
archetype = "default"
title = "Installation"
weight = "2"
+++

{{% notice style="primary" title="WARNING: early alpha" icon="skull-crossbones" %}}
This project is in early alfa stage. Which means that if you eventually succeed in getting this
application to run, it will have cost you your sanity, marriage and what have you not.
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
Please first install a JDK version 17 or higher from https://adoptium.net/en-GB/ . Then download

## Build from source (Java required)
1. First install a JDK version 17 or higher from https://adoptium.net/en-GB/.
1. Install JavaFX for your system, version 17 or higher, from https://openjfx.io/openjfx-docs/#install-javafx.
1. Clone the Gitlab repo, by e.g. git clone https://gitlab.com/nlcode.nl/m.nlcode.nl.git 
1. Go into that directory and execute **./gradlew run** or **gradlew run** if you are on Windows.
The first time will take a while; consecutive runs will be a lot faster.
