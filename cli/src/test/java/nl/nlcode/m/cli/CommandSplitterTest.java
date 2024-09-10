package nl.nlcode.m.cli;

import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jq59bu
 */
public class CommandSplitterTest {

    public CommandSplitterTest() {
    }

    /**
     * Test of splitCommand method, of class CommandSplitter.
     */
    @Test
    public void trivial() {
        List<String> result = CommandSplitter.splitCommand("");
        assertThat(result, is(empty()));
    }

    @Test
    public void oneWord() {
        List<String> result = CommandSplitter.splitCommand("one");
        assertThat(result.get(0), is("one"));
        assertThat(result.size(), is(1));
    }

    @Test
    public void trimSpaces() {
        List<String> result = CommandSplitter.splitCommand("   one two  three  ");
        assertThat(result.get(0), is("one"));
        assertThat(result.get(1), is("two"));
        assertThat(result.get(2), is("three"));
        assertThat(result.size(), is(3));
    }

    @Test
    public void groupDoubleQuotesSpacedStart() {
        List<String> result = CommandSplitter.splitCommand("   \"one  two\" three \"four five\"");
        assertThat(result.get(0), is("one  two"));
        assertThat(result.get(1), is("three"));
        assertThat(result.get(2), is("four five"));
        assertThat(result.size(), is(3));
    }

    @Test
    public void groupDoubleQuotesStartImmediately() {
        List<String> result = CommandSplitter.splitCommand("\"one  two\" three \"four five\"");
        assertThat(result.get(0), is("one  two"));
        assertThat(result.get(1), is("three"));
        assertThat(result.get(2), is("four five"));
        assertThat(result.size(), is(3));
    }

    @Test
    public void groupDoubleQuotesNoEndQuote() {
        // FIXME: Should this be allowed, or should this throw an exception?
        List<String> result = CommandSplitter.splitCommand("   \"one two\" three \"four five ");
        assertThat(result.get(0), is("one two"));
        assertThat(result.get(1), is("three"));
        assertThat(result.get(2), is("four five "));
        assertThat(result.size(), is(3));
    }

    @Test
    public void oneEscape() {
        List<String> result = CommandSplitter.splitCommand("\\");
        assertThat(result.get(0), is(""));
        assertThat(result.size(), is(1));
    }

    @Test
    public void escapedEscape() {
        List<String> result = CommandSplitter.splitCommand("\\\\");
        assertThat(result.get(0), is("\\"));
        assertThat(result.size(), is(1));
    }

    @Test
    public void groupOnOneEscapedSpace() {
        List<String> result = CommandSplitter.splitCommand("one\\ two");
        assertThat(result.get(0), is("one two"));
        assertThat(result.size(), is(1));
    }

    @Test
    public void twoSeparatedEscapes() {
        List<String> result = CommandSplitter.splitCommand("\\ \\");
        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(" "));
    }

    @Test
    public void noGroupWhenEscapedQuotes() {
        List<String> result = CommandSplitter.splitCommand("\\ \\\"one two\\\\\" three");
        assertThat(result.get(0), is(" \"one"));
        assertThat(result.get(1), is("two\\"));
        assertThat(result.get(2), is(" three"));
        assertThat(result.size(), is(3));
    }

    @Test
    public void noGroupWhenEscapedQuotesSpacedStart() {
        List<String> result = CommandSplitter.splitCommand("   \\\"one two\\\" three");
        assertThat(result.get(0), is("\"one"));
        assertThat(result.get(1), is("two\""));
        assertThat(result.get(2), is("three"));
        assertThat(result.size(), is(3));
    }

    @Test
    public void quoteInsideWord() {
        List<String> result = CommandSplitter.splitCommand("one\"two three");
        assertThat(result.get(0), is("one"));
        assertThat(result.get(1), is("two three"));
        assertThat(result.size(), is(2));
    }

    @Test
    public void escapedQuoteInsideWord() {
        List<String> result = CommandSplitter.splitCommand("one\\\"two");
        assertThat(result.get(0), is("one\"two"));
        assertThat(result.size(), is(1));
    }

    @Test
    public void escapedSpaceQuotInsideWord() {
        List<String> result = CommandSplitter.splitCommand("one\\ two");
        assertThat(result.get(0), is("one two"));
        assertThat(result.size(), is(1));
    }

    @Test
    public void realWorldExample() {
        List<String> result = CommandSplitter.splitCommand("midiDevice open --name=Gervill,\\ Software\\ MIDI\\ Synthesizer\\ -\\ OpenJDK\\ (receiver) ");
        assertThat(result.get(0), is("midiDevice"));
        assertThat(result.get(1), is("open"));
        assertThat(result.get(2), is("--name=Gervill, Software MIDI Synthesizer - OpenJDK (receiver)"));
        assertThat(result.size(), is(3));
    }

    @Test
    public void realWorldExample2() {
        List<String> result = CommandSplitter.splitCommand("midiDevice open --name=Gervill,\\ Software\\ MIDI\\ Synthesizer\\ -\\ OpenJDK\\ (receiver) ");
        assertThat(result.get(0), is("midiDevice"));
        assertThat(result.get(1), is("open"));
        assertThat(result.get(2), is("--name=Gervill, Software MIDI Synthesizer - OpenJDK (receiver)"));
        assertThat(result.size(), is(3));
    }
    
    @Test
    public void hell() {
        List<String> result = CommandSplitter.splitCommand("so --foo=\"foo bar \\bla zo\\ink und\\\"so\"\\ weiter");
        assertThat(result.get(0), is("so"));
        assertThat(result.get(1), is("--foo="));
        assertThat(result.get(2), is("foo bar \\bla zo\\ink und\"so"));
        assertThat(result.get(3), is(" weiter"));
        assertThat(result.size(), is(4));
    }
    
}
