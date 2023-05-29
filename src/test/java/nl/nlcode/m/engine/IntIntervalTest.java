/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package nl.nlcode.m.engine;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author leo
 */
public class IntIntervalTest {
    
    @Test
    public void trivial_closed() {
        IntInterval instance = new IntInterval();
        instance.setMin(0);
        instance.setLow(3);
        instance.setHigh(7);
        instance.setMax(10);
        assertThat(instance.contains(0), is(false));
        assertThat(instance.contains(1), is(false));
        assertThat(instance.contains(2), is(false));
        assertThat(instance.contains(3), is(true));
        assertThat(instance.contains(4), is(true));
        assertThat(instance.contains(5), is(true));
        assertThat(instance.contains(6), is(true));
        assertThat(instance.contains(7), is(true));
        assertThat(instance.contains(8), is(false));
        assertThat(instance.contains(9), is(false));
        assertThat(instance.contains(10), is(false));
    }

    @Test
    public void trivial_open() {
        IntInterval instance = new IntInterval();
        instance.setMin(0);
        instance.setLow(3);
        instance.setHigh(7);
        instance.setMax(10);
        instance.setIntervalClosure(IntervalClosure.OPEN);
        assertThat(instance.contains(0), is(false));
        assertThat(instance.contains(1), is(false));
        assertThat(instance.contains(2), is(false));
        assertThat(instance.contains(3), is(false));
        assertThat(instance.contains(4), is(true));
        assertThat(instance.contains(5), is(true));
        assertThat(instance.contains(6), is(true));
        assertThat(instance.contains(7), is(false));
        assertThat(instance.contains(8), is(false));
        assertThat(instance.contains(9), is(false));
        assertThat(instance.contains(10), is(false));
    }

    @Test
    public void trivial_low_closed_high_open() {
        IntInterval instance = new IntInterval();
        instance.setMin(0);
        instance.setLow(3);
        instance.setHigh(7);
        instance.setMax(10);
        instance.setIntervalClosure(IntervalClosure.LOW_CLOSED_HIGH_OPEN);
        assertThat(instance.contains(0), is(false));
        assertThat(instance.contains(1), is(false));
        assertThat(instance.contains(2), is(false));
        assertThat(instance.contains(3), is(true));
        assertThat(instance.contains(4), is(true));
        assertThat(instance.contains(5), is(true));
        assertThat(instance.contains(6), is(true));
        assertThat(instance.contains(7), is(false));
        assertThat(instance.contains(8), is(false));
        assertThat(instance.contains(9), is(false));
        assertThat(instance.contains(10), is(false));
    }

    @Test
    public void trivial_low_open_high_closed() {
        IntInterval instance = new IntInterval();
        instance.setMin(0);
        instance.setLow(3);
        instance.setHigh(7);
        instance.setMax(10);
        instance.setIntervalClosure(IntervalClosure.LOW_OPEN_HIGH_CLOSED);
        assertThat(instance.contains(0), is(false));
        assertThat(instance.contains(1), is(false));
        assertThat(instance.contains(2), is(false));
        assertThat(instance.contains(3), is(false));
        assertThat(instance.contains(4), is(true));
        assertThat(instance.contains(5), is(true));
        assertThat(instance.contains(6), is(true));
        assertThat(instance.contains(7), is(true));
        assertThat(instance.contains(8), is(false));
        assertThat(instance.contains(9), is(false));
        assertThat(instance.contains(10), is(false));
    }

    @Test
    public void reversed_closed() {
        IntInterval instance = new IntInterval();
        instance.setMin(0);
        instance.setLow(7);
        instance.setHigh(3);
        instance.setMax(10);
        assertThat(instance.contains(0), is(true));
        assertThat(instance.contains(1), is(true));
        assertThat(instance.contains(2), is(true));
        assertThat(instance.contains(3), is(true));
        assertThat(instance.contains(4), is(false));
        assertThat(instance.contains(5), is(false));
        assertThat(instance.contains(6), is(false));
        assertThat(instance.contains(7), is(true));
        assertThat(instance.contains(8), is(true));
        assertThat(instance.contains(9), is(true));
        assertThat(instance.contains(10), is(true));
    }

    @Test
    public void reversed_open() {
        IntInterval instance = new IntInterval();
        instance.setMin(0);
        instance.setLow(7);
        instance.setHigh(3);
        instance.setMax(10);
        instance.setIntervalClosure(IntervalClosure.OPEN);
        assertThat(instance.contains(0), is(true));
        assertThat(instance.contains(1), is(true));
        assertThat(instance.contains(2), is(true));
        assertThat(instance.contains(3), is(false));
        assertThat(instance.contains(4), is(false));
        assertThat(instance.contains(5), is(false));
        assertThat(instance.contains(6), is(false));
        assertThat(instance.contains(7), is(false));
        assertThat(instance.contains(8), is(true));
        assertThat(instance.contains(9), is(true));
        assertThat(instance.contains(10), is(true));
    }


}
