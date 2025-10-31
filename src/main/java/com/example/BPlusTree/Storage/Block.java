package com.example.BPlusTree.Storage;

import java.util.ArrayList;
import java.util.List;

public class Block {
    public static final int BLOCK_SIZE = 512;
    public static final int RECORD_SIZE = 115;

    private final List<Record> records = new ArrayList<>();

    public boolean addRecord(Record r) {
        if (getUsedBytes() + RECORD_SIZE <= BLOCK_SIZE) {
            records.add(r);
            return true;
        }
        return false;
    }

    public int getUsedBytes() {
        return records.size() * RECORD_SIZE;
    }

    public List<Record> getRecords() {
        return records;
    }


}
