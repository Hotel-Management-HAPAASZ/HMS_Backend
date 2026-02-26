package com.example.demo.models;
import com.example.demo.enums.Department;
import jakarta.persistence.*;
import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="staff")
public class Staff{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false,unique=true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Department department;

   
    @Column(nullable=false)
    private Boolean isActive= true;
}