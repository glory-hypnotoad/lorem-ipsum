package com.alvaria.loremipsum.redblacktree;

public class RedBlackTree<V extends Comparable<V>> {
    private Node<V> root;

    public Node<V> findValue(V value) {
        Node<V> node = root;
        while (node != null) {
            if (value.compareTo(node.data) == 0) {
                return node;
            } else if (value.compareTo(node.data) < 0) {
                node = node.left;
            } else {
                node = node.right;
            }
        }

        return null;
    }

    public void insertNode(V value) {
        Node<V> node = root;
        Node<V> parent = null;

        // Find the correct leaf in the tree where we can put the new value
        while (node != null) {
            parent = node;

            if (value.compareTo(node.data) < 0) {
                node = node.left;
            } else if (value.compareTo(node.data) > 0) {
                node = node.right;
            } else {
                throw new IllegalArgumentException("RedBlackTree: Node already exists");
            }
        }

        // Insert new Node to the place found
        Node<V> newNode = new Node<V>(value);
        if (parent == null) {
            // The tree is empty, simply put the new value to the root
            root = new Node<V>(value);
        } else if (value.compareTo(parent.data) < 0) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }
        newNode.parent = parent;

        // Finally, need to repair the Red-Black properties of the tree
        repairRedBlackProperties(newNode);
    }

    public Node<V> findMinimum() {
        Node<V> node = root;
        while (node.left != null) {
            node = node.left;
        }

        return node;
    }

    public Node<V> findMaximum() {
        Node<V> node = root;
        while (node.right != null) {
            node = node.right;
        }

        return node;
    }

    // ---------------------------- Private methods ----------------------------

    private void repairRedBlackProperties(Node<V> node) {
        Node<V> parent = node.parent;

        if (parent == null) {
            // Node is root. I just want to PAINT IT BLACK
            node.color = Node.Color.BLACK;
            return;
        }

        if (parent.color == Node.Color.BLACK) {
            // If parent is black then all properties are met already
            return;
        }

        // Now the parent is RED

        // NB: Grandparent cannot be null because RED parent cannot be root
        Node<V> grandparent = parent.parent;

        // Get the uncle. If it is null consider its color as BLACK
        Node<V> uncle = getUncle(parent);

        // If uncle is RED (as well as parent) then simply repaint uncle,
        // parent and grandparent. Then we need to recursively repair the red-black
        // properties for grandparent
        if (uncle != null && uncle.color == Node.Color.RED) {
            parent.color = Node.Color.BLACK;
            uncle.color = Node.Color.BLACK;
            grandparent.color = Node.Color.RED;

            repairRedBlackProperties(grandparent);
        } else if (parent == grandparent.left) { // If parent is LEFT child of grandparent
            // If uncle is BLACK (or null which is the same) and node is "inner" child
            if (node == parent.right) {
                rotateLeft(parent);

                // Let the parent point to the new root of this sub-tree. We will repaint
                // it on the next sub-step
                parent = node;
            }

            // Now we are sure that uncle is BLACK and the node is the "outer child"
            rotateRight(grandparent);

            parent.color = Node.Color.BLACK;
            grandparent.color = Node.Color.RED;
        } else { // If parent is RIGHT child of grandparent
            // If uncle is BLACK (or null which is the same) and node is "inner" child
            if (node == parent.left) {
                rotateRight(parent);

                // Let the parent point to the new root of this sub-tree. We will repaint
                // it on the next sub-step
                parent = node;
            }

            // Now we are sure that uncle is BLACK and the node is the "outer child"
            rotateLeft(grandparent);

            parent.color = Node.Color.BLACK;
            grandparent.color = Node.Color.RED;
        }


    }

    private Node<V> getUncle(Node<V> parent) {
        Node<V> grandparent = parent.parent;

        if (grandparent.left == parent) {
            return grandparent.right;
        } else if (grandparent.right == parent) {
            return grandparent.left;
        } else {
            throw new IllegalStateException("RedBlackTree: The parent is not a child of grandparent");
        }
    }

    private void rotateRight(Node<V> node) {
        Node<V> parent = node.parent;
        Node<V> leftChild = node.left;

        node.left = leftChild.right;
        if (leftChild.right != null) {
            leftChild.right.parent = node;
        }

        leftChild.right = node;
        node.parent = leftChild;

        replaceParentsChild(parent, node, leftChild);
    }

    private void rotateLeft(Node<V> node) {
        Node<V> parent = node.parent;
        Node<V> rightChild = node.right;

        node.right = rightChild.left;
        if (rightChild.left != null) {
            rightChild.left.parent = node;
        }

        rightChild.left = node;
        node.parent = rightChild;

        replaceParentsChild(parent, node, rightChild);
    }

    private void replaceParentsChild(Node<V> parent, Node<V> oldChild, Node<V> newChild) {
        if (parent == null) {
            root = newChild;
        } else if (parent.left == oldChild) {
            parent.left = newChild;
        } else if (parent.right == oldChild) {
            parent.right = newChild;
        } else {
            throw new IllegalStateException("RedBlackTree: node is not a child of its parent");
        }

        if (newChild != null) {
            newChild.parent = parent;
        }
    }
}
