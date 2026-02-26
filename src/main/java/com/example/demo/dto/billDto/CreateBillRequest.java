package com.example.demo.dto.billDto;



import java.util.List;
import lombok.Data;

@Data
public class CreateBillRequest {

    private Long userId;
    private Long bookingId;

    private List<BillItemRequest> items;

    private Double taxAmount;
    private Double discountAmount;
    private Double totalAmount;
}
