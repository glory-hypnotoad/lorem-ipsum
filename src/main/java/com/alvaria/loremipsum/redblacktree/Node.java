package com.alvaria.loremipsum.redblacktree;

/**
 * The {@code Node}  class represents a single node within a red-black tree.
 * The default color is RED but the node may be "repainted" after insertion
 * to a tree.
 *
 * @author Nikita Nikolaev
 */
public class Node<V extends Comparable<V>> {
    public enum Color {RED, BLACK};

    V data;

    Node<V> left;
    Node<V> right;
    Node<V> parent;
    Color color;

    public Node(V data) {
        this.data = data;
        this.left = null;
        this.right = null;
        this.parent = null;
        this.color = Color.RED;
    }

    public V getData() {
        return data;
    }
}
