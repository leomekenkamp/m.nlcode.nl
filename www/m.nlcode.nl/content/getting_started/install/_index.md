+++
archetype = "default"
title = "Installation"
weight = "2"
+++

{{% notice style="primary" title="WARNING: beta" icon="skull-crossbones" %}}
This project is in beta stage, which means that you should expect some bugs and rough edges.

Please note that although the version may be something like 1.0.*x*, with *x* being some digit,
this does not mean that **m.nlcode.nl** is out of beta. The first golden release will
have version number *1.1.0*. This is due to limitations of the build system.
{{% /notice %}}

## Downloads
Installers (and possibly other stuff you might need, like JAR files) can be downloaded from
[Github](https://github.com/leomekenkamp/m.nlcode.nl/releases). Look under 'Assets'.

So far the easy part. Unfortunately things are almost never easy. Read on.

## Supported platforms
**m.nlcode.nl** is written in Java with JavaFX. That means that every platform that has Java/JavaFX
support should be able to run **m.nlcode.nl**. So having only a Java JDK is not enough; a JavaFX
SDK is also needed. http://gluonhq.com provides SDKs for the most mainstream platforms. 

Packaging a Java/JavaFX application to run on
multiple platforms is still a bit of a challenge. Fortunately Github has multiple hardware stacks,
so in theory this could privide for OS X, Windows and Linux versions.

### UNVERIFIED - please let me know
Currenly I only have a Macbook Air with x86 Intel chip in it, and I can make use of one with an M1 Apple
chip. And I still need to setup Docker to run Linux containers for testing. That means that at this
point in time I cannot easily verify if the Linux and Windows versions actually work. That is why most
distributables have *UNVERIFIED* in their names. If you can run them, please let me know so I can
remove that *UNVERIFIED* text from the names of distributables that are known to work.

### MONOCLE support
If you know what this is an you think you need it, please contact me.

## Mac
Note that I have not yet enrolled in the Apple developer program, so I cannot at this point sign my
distributables. Which means you have to take the *System Settings | Privacy & Security* route when
you want to install or run **m.nlcode.nl**. High enough usage numbers of **m.nlcode.nl** will
entice me to shell out the yearly $99 needed to be enrolled. If this unsigned bothers you, you can
always clone the **m.nlcode.nl** repo, review the source code for bad stuff and run the release 
process in your clone yourself.

I have noticed that the OS X X86 version seems to run just fine on an M1. The OS X aarch64
version though ran neither on the M1, nor on my x86 Intel Macbook Air. This may be different for
you. If so, please let me know. Thing is that the libraries for a x86 and a M1 machine are supposed
to be different and (according to the docs) support only one of the two architectures.

## Windows
I have no Windows machines. I have been able to get Windows somewhat running on my Mackbook Air
under Virtualbox, but the sound was unreliable. Which explains the *UNVERIFIED* in the names of the
distributables. I did see a completed running installation of an AMD based Windows machine.

## Linux
I have not yet gotten around to setting up Docker with Linux containers on my Mac. Which explains
the *UNVERIFIED* in the names of the distributables.

## All platforms (Java SDK required, at least)
There are several options available from this point. The are described below, in order of complexity
with the simplest first.

{{% notice style="primary" title="WARNING: Java versions" icon="skull-crossbones" %}}
While one can use a JDK version 18 or higher, I would advise to use a JDK version 17.0.8 or a higher
version 17 JDK.
{{% /notice %}}

### Run the JAR
1. First install a JDK version 17 or higher. E.g. 'Temurin' from https://adoptium.net/ .
1. Download the latest so called 'shadow JAR' *m_nlcode_nl-<platform>-<version>-all.jar* from Github
for your specific platform.
1. Execute the shadow JAR file: *java -jar *m_nlcode_nl-<platform>-<version>-all.jar*

### Run from source
1. Only do this if you really know what you are doing.
1. First install a JDK version 17 or higher. E.g. 'Temurin' from https://adoptium.net/ .
1. Clone the Github repo locally, by e.g. *git clone https://github.com/leomekenkamp/m.nlcode.nl.git*
1. Go into that directory and execute **./gradlew run**.
The first time will take a while; consecutive runs will be a lot faster.
1. Note that if you run form souce, the version of **m.nlcode.nl** will always be reported as 1.0.0.

## Build installer from source (Java required, heavy other requirements)
1. Only do this if you really, really, *really* know what you are doing.
1. First install a JDK version 17 or higher. E.g. 'Temurin' from https://adoptium.net/ .
1. Clone the Github repo locally, by e.g. *git clone https://github.com/leomekenkamp/m.nlcode.nl.git*
1. Go into that directory and execute **./gradlew jpackage -PversionNumber=x.y.z**.
1. Step above may fail due to missing software on your machine. If it does not fail, you can find
the installer(s) in *./build/jpackage*.
