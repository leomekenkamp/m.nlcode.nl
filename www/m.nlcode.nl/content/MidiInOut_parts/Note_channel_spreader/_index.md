+++
archetype = "default"
title = "Note channel spreader"
+++

## generic description
A *Note channel spreader* takes incoming notes from one specific MIDI channel and sends them in a
round-robin way via the specified MDI output channels. The first note goes to the channel with the
lowest number and which is selected for output. The second note goes to the second channel, etc.

If there are no more channels, the lowest channel will be used again. That way the incoming notes
are spread across the available channels. *Note off* messsages will be sent to the same channel as
where its predecessing *note on* message has been sent.

## typical use case
If you have a number of the same type of mono synthesizer (or a number of the same type of
synthesizer with a limited number of voices) without support for poly chaining, you can use a
*Note channel spreader* to distribute notes over those synthesizers.

## example
Link three Korg NTS-1 synthesizers to one MIDI hub. Assign each NTS-1 its own MIDI 
channel, 0 through 2. Create a *MIDI device link* and link it to the driver of your MIDI hub.
Create a *Note channel spreader* and let it send its output to that *MIDI device link*. Select
channels 0, 1 and 2 for output. Create another *MIDI device link*, link it to an external MIDI
keyboard and make it send its output to the *Note channel spreader*. Play a three note chord on your
external MIDI keyboard. You will hear the notes from your chord simultaneuously from the three NTS-1
synthesizers.

TODO: screenshots and setup diagram

