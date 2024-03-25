package com.zzh.concurrent.atomic;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLongArray;

public class AtomicLongArrayTest {
    @Test
    void test() {
        AtomicLongArray array = new AtomicLongArray(10);
        array.set(0, 1);
        array.set(1, 2);
        long i = array.get(0);
    }
}
