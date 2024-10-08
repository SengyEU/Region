package cz.sengycraft.region.utils;

public class Pair<K, V> {
    private K left;
    private V right;

    public Pair(K key, V right) {
        this.left = key;
        this.right = right;
    }

    public K getLeft() {
        return left;
    }

    public V getRight() {
        return right;
    }

}
