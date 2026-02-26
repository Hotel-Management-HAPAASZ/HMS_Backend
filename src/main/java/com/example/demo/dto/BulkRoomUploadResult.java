package com.example.demo.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
@Data
public class BulkRoomUploadResult {

    private int totalRows;
    private int successCount;
    private int updatedCount;
    private int failureCount;
    private boolean upsert;

    private List<Long> createdRoomIds = new ArrayList<>();
    private List<RowError> errors = new ArrayList<>();

    @Data
    public static class RowError {
        private int rowNumber;
        private String roomNumber;
        private String reason;

        // ✅ Add this constructor
        public RowError(int rowNumber, String roomNumber, String reason) {
            this.rowNumber = rowNumber;
            this.roomNumber = roomNumber;
            this.reason = reason;
        }

        // Optional: allow empty constructor for Jackson
        public RowError() {}
    }
}