package java.util;

import org.junit.jupiter.api.Test;

public class HashMapTest {

    private static final int n = 1 << 8;
    private static final Character[] characters = new Character[n];

    private static Character getValue(int key) {
        Character character = characters[key];
        if (character == null) return characters[key] = 'A';
        else if (character != 'z') return characters[key] = ++character;
        else return character;
    }

    @Test
    void tableSizeFor() {
        HashMap<Integer, Integer> map = new HashMap<>(12);
    }

    @Test
    void put() {
        HashMap<HashKey, Character> map = new HashMap<>();
        for (int i = 0; i < n; i++) {
            HashKey hashKey = new HashKey(i);
            Character value = getValue(hashKey.hashCode());
            System.out.print(hashKey);
            System.out.print(", value = " + value);
            System.out.println();

            map.put(hashKey, value);
            if (value == 'z') break;
        }
        System.out.println("map = " + map);
    }

    @Test
    void resize() {
        int capacity = 4;
        Map<Integer, Integer> map = new HashMap<>(capacity, 1);
        for (int i = 0; i < capacity; i++) {
            map.put(i, i);
        }
    }

    @Test
    void constructor() {
        System.out.println("HashMapTest.constructor");
        for (int i = 1; i <= 8; i++) {
            System.out.println("i = " + i);
            HashMap<Object, Object> map = new HashMap<>(i, 1);
            for (int j = 1; j <= i; j++) {
                System.out.println("j = " + j);
                map.put(j, j);
            }
        }
    }


}
