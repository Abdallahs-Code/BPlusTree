package com.example.BPlusTree.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BPlusTree {
    private Node root;
    private final int order;
    private HashSet<Integer> ssns;

    public BPlusTree(int order) {
        if (order < 3) {
            throw new IllegalArgumentException("Order must be at least 3");
        }
        this.order = order;
        this.root = new LeafNode(order);
        this.ssns = new HashSet<>();
    }

    public Node getRoot() {
        return root;
    }

    public int getOrder() {
        return order;
    }

    private LeafNode findLeafNode(int key) {
        Node current = root;
        while (!current.isLeaf()) {
            InternalNode internal = (InternalNode) current;
            int i = 0;
            while (i < internal.getKeys().size() && key >= internal.getKeys().get(i)) {
                i++;
            }
            current = internal.getPointers().get(i);
        }
        return (LeafNode) current;
    }

    private void splitInternal(InternalNode node) {
        List<Integer> keys = node.getKeys();
        List<Node> pointers = node.getPointers();

        int mid = (keys.size() - 1) / 2;
        int promotedKey = keys.get(mid);
        InternalNode rightNode = new InternalNode(order);

        List<Integer> rightKeys = new ArrayList<>(keys.subList(mid + 1, keys.size()));
        rightNode.getKeys().addAll(rightKeys);

        List<Node> rightPointers = new ArrayList<>(pointers.subList(mid + 1, pointers.size()));
        rightNode.getPointers().addAll(rightPointers);

        keys.subList(mid, keys.size()).clear();
        pointers.subList(mid + 1, pointers.size()).clear();

        for (Node child : rightNode.getPointers()) {
            if (child != null) {
                child.setParent(rightNode);
            }
        }

        Node parent = node.getParent();

        if (parent == null) {
            InternalNode newRoot = new InternalNode(order);
            newRoot.getKeys().add(promotedKey);
            newRoot.getPointers().add(node);       
            newRoot.getPointers().add(rightNode); 
            node.setParent(newRoot);
            rightNode.setParent(newRoot);
            root = newRoot;
        } else {
            rightNode.setParent(parent);
            ((InternalNode) parent).insertKey(promotedKey, rightNode);
            handleOverflow(parent);
        }
    }

    private void splitLeaf(LeafNode leaf) {
        LeafNode rightLeaf = new LeafNode(order);
        int splitIndex = (leaf.getKeys().size()) / 2;

        rightLeaf.getKeys().addAll(leaf.getKeys().subList(splitIndex, leaf.getKeys().size()));
        rightLeaf.getRecordPointers().addAll(leaf.getRecordPointers().subList(splitIndex, leaf.getRecordPointers().size()));

        leaf.getKeys().subList(splitIndex, leaf.getKeys().size()).clear();
        leaf.getRecordPointers().subList(splitIndex, leaf.getRecordPointers().size()).clear();

        rightLeaf.setNext(leaf.getNext());
        leaf.setNext(rightLeaf);

        int promotedKey = rightLeaf.getKeys().get(0);
        Node parent = leaf.getParent();

        if (parent == null) {
            InternalNode newRoot = new InternalNode(order);
            newRoot.getKeys().add(promotedKey);
            newRoot.getPointers().add(leaf);
            newRoot.getPointers().add(rightLeaf);
            leaf.setParent(newRoot);
            rightLeaf.setParent(newRoot);
            root = newRoot;
        } else {
            rightLeaf.setParent(parent);
            ((InternalNode) parent).insertKey(promotedKey, rightLeaf);
            handleOverflow(parent);
        }
    }

    private void handleOverflow(Node node) {
        if (node.isLeaf()) {
            LeafNode leaf = (LeafNode) node;
            if (leaf.getKeys().size() > order) { 
                splitLeaf(leaf);
            }
        } else {
            InternalNode internal = (InternalNode) node;
            if (internal.getKeys().size() >= order) { 
                splitInternal(internal);
            }
        }
    }
    private void rebalanceLeaf(LeafNode leaf) {
        int minKeys = (int) Math.ceil(order / 2.0);
        if (leaf.getKeys().size() >= minKeys) return;

        InternalNode parent = (InternalNode) leaf.getParent();
        if (parent == null) return;
        LeafNode left = leaf.getPrev();
        LeafNode right = leaf.getNext();
        int index = parent.getPointers().indexOf(leaf);

        if (canBorrow(left, parent)) {
            borrowFromLeft(leaf, left, parent, index);
            return;
        }

        if (canBorrow(right, parent)) {
            borrowFromRight(leaf, right, parent, index);
            return;
        }

        mergeLeaf(leaf, left, right, parent, index);
        handleDeletion(parent);
    }
    
    private boolean canBorrow(LeafNode sibling, InternalNode parent) {
        return sibling != null && sibling.getParent() == parent &&
                sibling.getKeys().size() > Math.ceil(order / 2.0);
    }
    
    private boolean canBorrowInternal(InternalNode sibling) {
        return sibling != null && sibling.getKeys().size() > Math.ceil(order / 2.0) - 1;
    }
    
    
    private void borrowFromLeft(LeafNode leaf, LeafNode left, InternalNode parent, int index) {
        int borrowedKey = left.getKeys().remove(left.getKeys().size() - 1);
        int borrowedPtr = left.getRecordPointers().remove(left.getRecordPointers().size() - 1);

        leaf.getKeys().add(0, borrowedKey);
        leaf.getRecordPointers().add(0, borrowedPtr);

        parent.getKeys().set(index - 1, leaf.getKeys().get(0));
    }

    private void borrowFromRight(LeafNode leaf, LeafNode right, InternalNode parent, int index) {
        int borrowedKey = right.getKeys().remove(0);
        int borrowedPtr = right.getRecordPointers().remove(0);

        leaf.getKeys().add(borrowedKey);
        leaf.getRecordPointers().add(borrowedPtr);

        parent.getKeys().set(index, right.getKeys().get(0));
    }
    
    private void borrowFromLeftInternal(InternalNode node, InternalNode left, InternalNode parent, int index) {
        
        int parentKey = parent.getKeys().get(index - 1);
        
        Node borrowedChild = left.getPointers().remove(left.getPointers().size() - 1);
        int borrowedKey = left.getKeys().remove(left.getKeys().size() - 1);

        node.getPointers().add(0, borrowedChild);
        borrowedChild.setParent(node);
        node.getKeys().add(0, parentKey);

        
        parent.getKeys().set(index - 1, borrowedKey);
    }
    private void borrowFromRightInternal(InternalNode node, InternalNode right, InternalNode parent, int index) {
        
        int parentKey = parent.getKeys().get(index);

        
        Node borrowedChild = right.getPointers().remove(0);
        int borrowedKey = right.getKeys().remove(0);

        node.getKeys().add(parentKey);
        node.getPointers().add(borrowedChild);
        borrowedChild.setParent(node);

        parent.getKeys().set(index, borrowedKey);
    }
    private void mergeInternal(InternalNode node, InternalNode left, InternalNode right, InternalNode parent, int index) {
        if (left != null) {
            // Merge current node into left sibling
            int separatorKey = parent.getKeys().remove(index - 1);
            parent.getPointers().remove(node);

            left.getKeys().add(separatorKey);
            left.getKeys().addAll(node.getKeys());
            left.getPointers().addAll(node.getPointers());

            for (Node child : node.getPointers()) {
                child.setParent(left);
            }

        } else if (right != null) {
            // Merge right sibling into current node
            int separatorKey = parent.getKeys().remove(index);
            parent.getPointers().remove(right);

            node.getKeys().add(separatorKey);
            node.getKeys().addAll(right.getKeys());
            node.getPointers().addAll(right.getPointers());

            for (Node child : right.getPointers()) {
                child.setParent(node);
            }
        }
    }
    
    private void mergeLeaf(LeafNode leaf, LeafNode left, LeafNode right, InternalNode parent, int index) {
        if (left != null && left.getParent() == parent) {
            left.getKeys().addAll(leaf.getKeys());
            left.getRecordPointers().addAll(leaf.getRecordPointers());
            left.setNext(leaf.getNext());
            if (leaf.getNext() != null) leaf.getNext().setPrev(left);

            parent.getPointers().remove(leaf);
            parent.getKeys().remove(index - 1);
        } else if (right != null && right.getParent() == parent) {
            leaf.getKeys().addAll(right.getKeys());
            leaf.getRecordPointers().addAll(right.getRecordPointers());
            leaf.setNext(right.getNext());
            if (right.getNext() != null) right.getNext().setPrev(leaf);

            parent.getPointers().remove(right);
            parent.getKeys().remove(index);
        }
    }
    
    private void handleDeletion(Node node) {
    	if(node == null) return;
        if (node.isLeaf()) {
            rebalanceLeaf((LeafNode) node);
            return;
        } 
     // Case 2: Internal node
        InternalNode internal = (InternalNode) node;
        int minKeys = (int) Math.ceil(order / 2.0) - 1; 

        // Root case: if root has 0 keys and 1 child, make that child the new root
        if (internal == root && internal.getKeys().isEmpty()) {
            Node child = internal.getPointers().get(0);
            child.setParent(null);
            root = child;
            return;
        }
        // If internal node still has enough keys → no need to rebalance
        if (internal.getKeys().size() >= minKeys) return;
        
        InternalNode parent = (InternalNode) internal.getParent();
        if (parent == null) return; // root can't go  up
        
        int index = parent.getPointers().indexOf(internal);
        
        InternalNode leftSibling = (index > 0) ? (InternalNode) parent.getPointers().get(index - 1) : null;
        InternalNode rightSibling = (index < parent.getPointers().size() - 1)
                ? (InternalNode) parent.getPointers().get(index + 1) : null;
        // try to borrow from left
        if (canBorrowInternal(leftSibling)) {
            borrowFromLeftInternal(internal, leftSibling, parent, index);
            return;
        }
     // Try to borrow from right
        if (canBorrowInternal(rightSibling)) {
            borrowFromRightInternal(internal, rightSibling, parent, index);
            return;
        }
     // Otherwise, merge
        mergeInternal(internal, leftSibling, rightSibling, parent, index);
        
        handleDeletion(parent);
        
    }

    public boolean insert(int key, int recordPointer) {
        if (ssns.contains(key)) return false;
        LeafNode leaf = findLeafNode(key);
        leaf.insertKey(key, recordPointer);
        handleOverflow(leaf);
        ssns.add(key);
        return true;
    }
    
    public boolean delete(int key) {
        LeafNode leaf = findLeafNode(key);
        if (!ssns.contains(key)) return false;
        leaf.removeKey(key);
        ssns.remove(key);
        handleDeletion(leaf);
        return true;
    }
    
}