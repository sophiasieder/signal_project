package com.data_management;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileDataReaderTest {
    @TempDir
    Path tempDir;

    private DataStorage storage;
    private FileDataReader reader;

    @BeforeEach
    void setUp() {
        storage = new DataStorage();
        reader = new FileDataReader(tempDir.toString());
    }

    /**
     * test to verify reading of a single CSV file with one data line,
     * creates, stores a single patient record in DataStorage
     * purpose: Ensures basic parsing of a single line and correct data storage.
     */
    @Test
    void readSingleCsv_createsOneRecord() throws IOException {
        //write a single-line CSV
        Path csv = tempDir.resolve("samples.csv");
        String line = "1000, 42, HR, 75.5\n";
        Files.writeString(csv, line);

        //act
        reader.readData(storage);

        //assert
        List<PatientRecord> recs = storage.getRecords(42, 0L, Long.MAX_VALUE);
        assertEquals(1, recs.size(), "Should have read exactly one record");
        PatientRecord r = recs.get(0);
        assertAll("fields",
                () -> assertEquals(1000L, r.getTimestamp()),
                () -> assertEquals(42,   r.getPatientId()),
                () -> assertEquals("HR", r.getRecordType()),
                () -> assertEquals(75.5, r.getMeasurementValue(), 1e-6)
        );
    }


    /**
     * test to verify reading from multiple CSV files
     * correctly creates and stores patient records from each file
     * purpose: ensures reader can handle multiple data sources in base directory
     */
    @Test
    void readMultipleCsv_createsRecordsFromEachFile() throws IOException {
        //arrange two CSV files
        Files.writeString(tempDir.resolve("a.csv"), "10, 1, BP, 120.0\n");
        Files.writeString(tempDir.resolve("b.csv"), "20, 2, BP,  80.0\n");

        //act
        reader.readData(storage);

        //assert
        assertEquals(1, storage.getRecords(1,0,Long.MAX_VALUE).size(),
                "Patient 1 should have one record");
        assertEquals(1, storage.getRecords(2,0,Long.MAX_VALUE).size(),
                "Patient 2 should have one record");
    }
}