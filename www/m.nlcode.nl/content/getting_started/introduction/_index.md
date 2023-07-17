+++
archetype = "default"
title = "Introduction"
weight = "1"
+++

{{% notice style="primary" title="WARNING: early alpha" icon="skull-crossbones" %}}
This project is in early alfa stage. Which means that if you eventually succeed in getting this
application to run, it will have cost you your sanity, marriage and what have you not.

Please note that although the version may be something like 1.0.*x*, with *x* being some digit,
this does not mean that **m.nlcode.nl** is out of alpha/beta stage. The first golden release will
have version *1.1.0*. This is due to limitations of the build system.
{{% /notice %}}

## What does one need to know before using **m.nlcode.nl**?
A requirement for use of this application is some basic knowledge on the *Musical Instrument Digital
Interface* or MIDI. You should know about MIDI messages like *note on*, *note off* and others.
You should also know about MIDI ports, both in hardware and software, and how every port supports
sixteen distinct MIDI channels.

Some knowledge on how to make music would also be handy.

## So what is this **m.nlcode.nl** thing anyway? 
The application **m.nlcode.nl** is a collection of relatively simple parts that are focussed on doing 
one thing with MIDI data. The basic thought behind this component strategy is to create something
with a limited functional reach, but a depth and or simplicity in what it does. Every part has a
narrow focus on a specific functional area. These parts can be connected to one another to chain
their data through an **m.nlcode.nl** project. The name of such a part is MidiInOut.
 
### sum of the parts
If you are accustomed to a unix style command prompt, then this 'do-one-thing-but-do-it-well' will
sound familiar. A unix command line usually provides a large number of specialized little
applications and commands. The output of one command can be used as the input for another command.
**m.nlcode.nl** works in a similar fashion. Big difference here is that it is very easy to make one
MidiInOut part send to multiple other MidiInOut parts. You can also let a MidiInOut part receive
data from multiple MidiInOut parts.

## Graphical User Interface
The graphical user interface (or GUI) of **m.nlcode.nl** is made to be as simple as possible. The basic
rule is (or should be) [Don't Make Me Think](https://en.wikipedia.org/wiki/Don%27t_Make_Me_Think 
"Wikipedia article on the book written by Steve Krug").
All clickable items are easily identified on sight without any further action. So there is no need
to 'hover' over a certain piece of text to see if it changes into a button of some sort.

All basic interaction is through left click only. No right-click, double-click, ctrl-alt-right-shift-click
or whatever should be needed UNLESS it is a standardised action. For example: if you have a list
where two items are selected and you control-click on a non-selected item, you have now three
selected items.

### usability
No support for the visually impaired. Sorry. If you have an idea on how to solve this AND you are or
know someone who can do the user acceptance testing, please contact me. I cannot guarantee anything,

### i18n - internationalisation
**m.nlcode.nl** is written in UK English. There is internal support for multiple languages however. If
you need a different language AND can do the translation as well, please contact me.
