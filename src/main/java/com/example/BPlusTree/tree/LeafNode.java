package com.example.BPlusTree.tree;

import java.util.ArrayList;
import java.util.List;

public class LeafNode extends Node {
    private final List<Integer> recordPointers;
    private LeafNode next;
    private LeafNode prev;

    public LeafNode(int order) {
        super(true, order);
        this.recordPointers = new ArrayList<>();
        this.next = null;
        this.prev = null;
    }

    public List<Integer> getRecordPointers() {
        return recordPointers;
    }

    public LeafNode getNext() {
        return next;
    }

    public void setNext(LeafNode next) {
        this.next = next;
    }

    public LeafNode getPrev() {
        return prev;
    }

    public void setPrev(LeafNode prev) {
        this.prev = prev;
    }

    public void insertKey(int key, int recordPointer) {
        int insertIndex = 0;
        while (insertIndex < keys.size() && keys.get(insertIndex) <= key) {
            insertIndex++;
        }
        keys.add(insertIndex, key);
        recordPointers.add(insertIndex, recordPointer);
    }

    public void removeKey(int key) {
        int index = keys.indexOf(key);
        keys.remove(index);
        recordPointers.remove(index);
    }
}
