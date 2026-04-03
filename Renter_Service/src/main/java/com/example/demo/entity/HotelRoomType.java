package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hotel_room_types", indexes = {
        @Index(name = "idx_hotel_room_type_hotel", columnList = "hotelId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelRoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String hotelId;

    @Column(nullable = false, length = 64)
    private String roomTypeId;
}
