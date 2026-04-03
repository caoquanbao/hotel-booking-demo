package com.example.demo.controller;

import com.example.demo.dto.CustomerHotelSearchRequest;
import com.example.demo.dto.CustomerHotelSearchResponse;
import com.example.demo.service.CustomerSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/hotels")
@RequiredArgsConstructor
public class CustomerSearchController {

    private final CustomerSearchService customerSearchService;

    @PostMapping("/search")
    public ResponseEntity<List<CustomerHotelSearchResponse>> search(@RequestBody CustomerHotelSearchRequest request) {
        return ResponseEntity.ok(customerSearchService.search(request));
    }
}
