package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "room_types", indexes = @Index(name = "idx_roomtype_hotel", columnList = "hotelId"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long hotelId;
    private String name;

    // base price just for preview/search estimate (real pricing engine mày làm sau)
    @Column(precision = 19, scale = 2)
    private BigDecimal basePricePerNight;
}