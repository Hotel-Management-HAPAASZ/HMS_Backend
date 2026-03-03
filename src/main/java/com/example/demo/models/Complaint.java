package com.example.demo.models;
import com.example.demo.enums.ComplaintCategory;
import com.example.demo.enums.ComplaintStatus;
import com.example.demo.enums.PriorityOfComplaint;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "complaints",
    indexes = {
        @Index(name = "idx_complaint_ref", columnList = "reference_number", unique = true),
        @Index(name = "idx_complaint_status", columnList = "status"),
        @Index(name = "idx_complaint_booking", columnList = "booking_id")
    }
)
@EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_number", nullable = false, unique = true, updatable = false, length = 20)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintCategory category;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_preference", nullable = false, length = 10)
    private com.example.demo.enums.ContactPreference contactPreference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplaintStatus status;

    @Column(name = "expected_resolution_date")
    private LocalDateTime expectedResolutionDate;

    @org.springframework.data.annotation.CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @org.springframework.data.annotation.LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private PriorityOfComplaint priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id", nullable = true)
    private Staff assignedStaff;

    @Version
    private Long version;
}
