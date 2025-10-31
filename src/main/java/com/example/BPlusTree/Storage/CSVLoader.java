package com.example.BPlusTree.Storage;

import java.io.*;
import java.util.*;

public class CSVLoader {

    public static List<Record> loadRecords(String filePath) throws IOException {
        List<Record> records = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        boolean firstLine = true;

        while ((line = reader.readLine()) != null) {
            if (firstLine) {  // Skip header
                firstLine = false;
                continue;
            }

            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            System.out.println("DEBUG: " + Arrays.toString(parts));

            if (parts.length < 9) {
                System.err.println("⚠️ Skipped malformed line: " + line);
                continue;
            }

            try {
                Record r = new Record(
                        parts[0].trim(),      // NAME
                        parts[1].trim(),      // SSN
                        parts[2].trim(),      // DEPARTMENTCODE
                        parts[3].replace("\"", "").trim(), // ADDRESS
                        parts[4].trim(),      // PHONE
                        parts[5].trim(),      // BIRTHDATE
                        parts[6].trim().charAt(0), // SEX
                        parts[7].trim(),      // JOBCODE
                        Float.parseFloat(parts[8].trim()) // SALARY
                );
                records.add(r);


            } catch (Exception e) {

                System.err.println(" Error parsing line: " + line);
                e.printStackTrace();
            }
        }

        reader.close();
        return records;
    }
}
