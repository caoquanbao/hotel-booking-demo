package com.example.demo.controller;

import com.example.demo.dto.InventorySyncRequest;
import com.example.demo.dto.InventorySyncResponse;
import com.example.demo.queue.InventorySyncProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/partners/inventory")
@RequiredArgsConstructor
public class InventorySyncController {

    private final InventorySyncProducer inventorySyncProducer;

    @PostMapping("/sync")
    public ResponseEntity<InventorySyncResponse> sync(@Valid @RequestBody InventorySyncRequest request) {
        inventorySyncProducer.publish(request);
        return ResponseEntity.ok(InventorySyncResponse.builder()
                .requestId(request.getRequestId())
                .status("SUCCESS")
                .message("Inventory sync request accepted")
                .build());
    }
}
