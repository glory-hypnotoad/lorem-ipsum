package com.alvaria.loremipsum.redblacktree;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code RedBlackTree} class represents a Red-Black tree that allows to
 * search/insert/delete nodes with logarithmic complexity *
 */
public class RedBlackTree<V extends Comparable<V>> {
    private Node<V> root;

    public RedBlackTree() {
        root = null;
    }

    /**
     * Finds the given value in the tree
     * @param value to find
     * @return {@code Node} of given value if found;
     *         {@code null} otherwise
     */
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

    /**
     * Insert a node into the tree and repair the Red-Black properties
     * after that if required
     * @param value to insert
     */
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
                throw new IllegalArgumentException("RedBlackTree:insertNode(): Node already exists");
            }
        }

        // Insert new Node to the place found
        Node<V> newNode = new Node<V>(value);
        if (parent == null) {
            // The tree is empty, simply put the new value to the root
            root = new Node<V>(value);
            root.color = Node.Color.BLACK;
        } else if (value.compareTo(parent.data) < 0) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }
        newNode.parent = parent;

        // Finally, need to repair the Red-Black properties of the tree
        repairRedBlackPropertiesAfterInsert(newNode);
    }

    /**
     * Find the minimum Node
     * @return {@code node} if found;
     *         {@code null} otherwise
     */
    public Node<V> findMinimum() {
        return findMinimum(root);
    }

    /**
     * Find the maximum Node
     * @return {@code node} if found;
     *         {@code null} otherwise
     */
    public Node<V> findMaximum() {
        return findMaximum(root);
    }

    /**
     * Find maximum data element
     * @return {@code data} if found;
     *         {@code null} otherwise
     */
    public V findMaxData() {
        Node<V> maxNode = findMaximum(root);
        if (maxNode != null) {
            return maxNode.data;
        } else {
            return null;
        }
    }

    /**
     * Find minimum data element
     * @return {@code data} if found;
     *         {@code null} otherwise
     */
    public V findMinData() {
        Node<V> minNode = findMinimum(root);
        if (minNode != null) {
            return minNode.data;
        } else {
            return null;
        }
    }

    /**
     * Find the maximum value AND delete its node
     * @return {@code value} if the tree is not empty;
     *         {@code null} otherwise
     */
    public V pollMaximum() {
        Node<V> max = findMaximum();
        if (max != null) {
            V data = max.data;
            deleteNode(max);
            return data;
        } else {
            return null;
        }
    }

    /**
     * Find the minimum value AND delete its node
     * @return {@code value} if the tree is not empty;
     *         {@code null} otherwise
     */
    public V pollMinimum() {
        Node<V> min = findMinimum();
        if (min != null) {
            V data = min.data;
            deleteNode(min);
            return data;
        } else {
            return null;
        }
    }

    /**
     * Delete the node of the given value (if exists)
     * @param value to be deleted
     */
    public void deleteNode(V value) {
        Node<V> node = root;

        // Search for the node to be deleted
        while (node != null && node.data != value) {
            if (node.data.compareTo(value) < 0) {
                node = node.right;
            } else {
                node = node.left;
            }
        }

        deleteNode(node);
    }

    /**
     * Build an array of elements sorted from min to max
     * @return Sorted array of elements
     */
    public List<V> buildNodeList() {
        List<V> result = buildSubtreeList(root);
        return result;
    }

    // ---------------------------- Private methods ----------------------------

    private void repairRedBlackPropertiesAfterInsert(Node<V> node) {
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

            repairRedBlackPropertiesAfterInsert(grandparent);
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

    private void repairRedBlackPropertiesAfterDelete(Node<V> node) {
        if (node.parent == null) {
            // node is root; simply repaint it BLACK
            node.color = Node.Color.BLACK;
            return;
        }

        Node<V> sibling = getSibling(node);

        // Sibling is RED
        if (sibling.color == Node.Color.RED) {
            handleRedSibling(node, sibling);
            // Get new sibling for fall-through to the next case
            // Note that it is BLACK as it was a child of the original RED sibling
            sibling = getSibling(node);
        }

        // Case: BLACK sibling with two BLACK children
        if (isBlack(sibling.left) && isBlack(sibling.right)) {
            sibling.color = Node.Color.RED;

            // Case: Black sibling with two black children AND red parent
            if (node.parent.color == Node.Color.RED) {
                node.parent.color = Node.Color.BLACK;
            }

            // Case: Black sibling with two black children AND black parent
            else {
                repairRedBlackPropertiesAfterDelete(node.parent);
            }
        }

        // Case: Black sibling with at least one red child
        else {
            handleBlackSiblingWithAtLeastOneRedChild(node, sibling);
        }

    }

    private void handleRedSibling(Node<V> node, Node<V> sibling) {
        // Repaint the sibling and parent
        sibling.color = Node.Color.BLACK;
        node.parent.color = Node.Color.RED;

        // ...and rotate
        if (node == node.parent.left) {
            rotateLeft(node.parent);
        } else {
            rotateRight(node.parent);
        }
    }

    private void handleBlackSiblingWithAtLeastOneRedChild(Node<V> node, Node<V> sibling) {
        boolean nodeIsLeftChild = node == node.parent.left;

        // Case: Black sibling with at least one red child AND "outer nephew" is BLACK
        // Repaint sibling and its child, and rotate around sibling
        if (nodeIsLeftChild && isBlack(sibling.right)) {
            sibling.left.color = Node.Color.BLACK;
            sibling.color = Node.Color.RED;
            rotateRight(sibling);
            sibling = node.parent.right;
        } else if (!nodeIsLeftChild && isBlack(sibling.left)) {
            sibling.right.color = Node.Color.BLACK;
            sibling.color = Node.Color.RED;
            rotateLeft(sibling);
            sibling = node.parent.left;
        }

        // Case: Black sibling with at least one red child AND "outer nephew" is RED
        // Repaint sibling + parent + sibling's child, and rotate around parent
        sibling.color = node.parent.color;
        node.parent.color = Node.Color.BLACK;
        if (nodeIsLeftChild) {
            sibling.right.color = Node.Color.BLACK;
            rotateLeft(node.parent);
        } else {
            sibling.left.color = Node.Color.BLACK;
            rotateRight(node.parent);
        }
    }

    private Node<V> getUncle(Node<V> parent) {
        Node<V> grandparent = parent.parent;

        if (grandparent.left == parent) {
            return grandparent.right;
        } else if (grandparent.right == parent) {
            return grandparent.left;
        } else {
            throw new IllegalStateException("RedBlackTree:getUncle(): The parent is not a child of grandparent");
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
            throw new IllegalStateException("RedBlackTree:replaceParentsChild(): node is not a child of its parent");
        }

        if (newChild != null) {
            newChild.parent = parent;
        }
    }

    private Node<V> findMinimum(Node<V> node) {
        if (node == null) {
            return null;
        }

        while (node.left != null) {
            node = node.left;
        }

        return node;
    }

    private Node<V> findMaximum(Node<V> node) {
        if (node == null) {
            return null;
        }

        while (node.right != null) {
            node = node.right;
        }

        return node;
    }

    // Keep the following method private as we need to be sure that the
    // node is actually the part of the tree
    private void deleteNode (Node<V> node) {
        if (node == null) {
            return;
        }

        Node<V> movedUpNode; // The node where we start repairing the RB properties after deletion
        Node.Color deletedNodeColor;

        // Node has zero or one child
        if (node.left == null || node.right == null) {
            movedUpNode = deleteNodeWithZeroOrOneChild(node);
            deletedNodeColor = node.color;
        }
        // Node has two children
        else {
            // Find minimum node of right subtree ("inorder successor" of current node)
            Node<V> inOrderSuccessor = findMinimum(node.right);

            // Copy inorder successor's data to current node (keep its color!)
            node.data = inOrderSuccessor.data;

            // Delete inorder successor just as we would delete a node with 0 or 1 child
            movedUpNode = deleteNodeWithZeroOrOneChild(inOrderSuccessor);
            deletedNodeColor = inOrderSuccessor.color;
        }

        if (deletedNodeColor == Node.Color.BLACK) {
            repairRedBlackPropertiesAfterDelete(movedUpNode);

            // Remove the temporary nil node
            if (movedUpNode.getClass() == NilNode.class) {
                replaceParentsChild(movedUpNode.parent, movedUpNode, null);
            }
        }
    }

    private Node<V> deleteNodeWithZeroOrOneChild(Node<V> node) {
        // If the node has only one child then replace it with this child
        if (node.left != null) {
            replaceParentsChild(node.parent, node, node.left);
            return node.left; // moved-up node
        } else if (node.right != null) {
            replaceParentsChild(node.parent, node, node.right);
            return node.right; // moved-up node
        }

        // If node has no children then:
        // If the node is RED then simply delete it
        // If the node is BLACK then replace it with a temporary NilNode that is used to repair RB properties
        else {
            Node<V> newChild = (node.color == Node.Color.BLACK) ? new NilNode() : null;
            replaceParentsChild(node.parent, node, newChild);
            return newChild;
        }
    }

    private class NilNode extends Node<V> {
        private NilNode() {
            super(null);
            this.color = Color.BLACK;
        }
    }

    private boolean isBlack(Node<V> node) {
        return node == null || node.color == Node.Color.BLACK;
    }

    private Node<V> getSibling(Node<V> node) {
        Node<V> parent = node.parent;
        if (node == parent.left) {
            return parent.right;
        } else if (node == parent.right) {
            return parent.left;
        } else {
            throw new IllegalStateException("RedBlackTree:getSibling(): Node is not a child of its parent");
        }
    }

    private List<V> buildSubtreeList(Node<V> node) {
        List<V> leftList = new ArrayList<>();
        List<V> rightList = new ArrayList<>();
        List<V> result = new ArrayList<>();
        if (node == null) {
            return null;
        }

        if (node.left != null) {
            leftList = buildSubtreeList(node.left);
        }

        if (node.right != null) {
            rightList = buildSubtreeList(node.right);
        }

        if (!leftList.isEmpty()) {
            result.addAll(leftList);
        }

        result.add(node.data);

        if (!rightList.isEmpty()) {
            result.addAll(rightList);
        }

        return result;
    }
}
