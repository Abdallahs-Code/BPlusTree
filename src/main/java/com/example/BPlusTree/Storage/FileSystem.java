package com.example.BPlusTree.Storage;

import java.util.ArrayList;
import java.util.List;

import static com.example.BPlusTree.Storage.Block.BLOCK_SIZE;

public class FileSystem {
    private final List<Block> blocks = new ArrayList<>();

    public int insertRecord(Record record) {
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            if (block.addRecord(record)) {
                int recordIndex = block.getRecords().size() - 1;
                return (i * (BLOCK_SIZE / Block.RECORD_SIZE)) + recordIndex;
            }
        }
        Block newBlock = new Block();
        newBlock.addRecord(record);
        blocks.add(newBlock);
        int blockIndex = blocks.size() - 1;
        return (blockIndex * (BLOCK_SIZE / Block.RECORD_SIZE));
    }

    public boolean deleteRecordByPointer(int pointer) {
        if (pointer < 0) return false;

        int blockIndex = pointer / 4;  // 4 records per block
        int slotIndex = pointer % 4;

        if (blockIndex >= blocks.size()) return false;

        Block block = blocks.get(blockIndex);
        if (slotIndex >= block.getRecords().size()) return false;

        Record record = block.getRecords().get(slotIndex);
        if (!record.isDeleted()) {
            record.markDeleted();
            System.out.println("✓ Deleted pointer " + pointer +
                    " at Block " + blockIndex + ", Slot " + slotIndex);
            return true;
        }else{
            System.out.println("Oopsie");
        }
        return false;
    }

    public void printBlocks() {
        System.out.println("---- File Blocks ----");
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            System.out.println("Block " + i + " (" + block.getRecords().size() + " records)");
            for (int j = 0; j < block.getRecords().size(); j++) {
                Record r = block.getRecords().get(j);
                String status = r.isDeleted() ? "[DELETED]" : "[ACTIVE]";
                System.out.printf("  Slot %d: %s %s%n", j, status, r.toString());
            }
            System.out.println();
        }
    }



    public List<Block> getBlocks() {
        return blocks;
    }
}
