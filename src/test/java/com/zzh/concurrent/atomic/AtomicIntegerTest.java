package com.zzh.concurrent.atomic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

class AtomicIntegerTest {

    @Test
    void getAndSet() {
        AtomicInteger atomicInteger = new AtomicInteger(10);
        int old = atomicInteger.getAndSet(20);
        Assertions.assertEquals(10, old);
    }

    @Test
    void compareAndSet() {
    }

    @Test
    void weakCompareAndSet() {
    }

    @Test
    void getAndIncrement() {
    }

    @Test
    void getAndDecrement() {
    }

    @Test
    void getAndAdd() {
    }

    @Test
    void incrementAndGet() {
    }

    @Test
    void decrementAndGet() {
    }

    @Test
    void addAndGet() {
    }

    @Test
    void getAndUpdate() {
    }

    @Test
    void updateAndGet() {
    }

    @Test
    void getAndAccumulate() {
    }

    @Test
    void accumulateAndGet() {
    }

}