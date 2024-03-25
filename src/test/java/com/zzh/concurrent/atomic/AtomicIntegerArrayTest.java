package com.zzh.concurrent.atomic;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class AtomicIntegerArrayTest {
    @Test
    void test() {
        AtomicIntegerArray array = new AtomicIntegerArray(10);
        array.set(0, 1);
        array.set(1, 2);
        int i = array.get(0);
    }
}
