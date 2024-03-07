package java.util;

public class HashKey {
    private Integer key;

    public HashKey(Integer key) {
        this.key = key;
    }

    public Integer getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        return key % 16;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HashKey && ((HashKey) obj).getKey().equals(key);
    }

    @Override
    public String toString() {
        return "key= "+key + ", hash=" + key % 16;
    }
}
