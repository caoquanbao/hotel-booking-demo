package com.example.demo.service;

import com.example.demo.client.NotificationClient;
import com.example.demo.dto.DateRange;
import com.example.demo.dto.InventoryItem;
import com.example.demo.dto.InventorySyncRequest;
import com.example.demo.dto.NotificationMetadata;
import com.example.demo.dto.NotificationRecipient;
import com.example.demo.dto.NotificationRequest;
import com.example.demo.dto.Restrictions;
import com.example.demo.entity.RequestLog;
import com.example.demo.entity.RoomInventoryDaily;
import com.example.demo.exception.InventoryBusinessException;
import com.example.demo.exception.InventoryValidationException;
import com.example.demo.repository.HotelPartnerRepository;
import com.example.demo.repository.HotelRoomTypeRepository;
import com.example.demo.repository.RequestLogRepository;
import com.example.demo.repository.RoomInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final HotelPartnerRepository hotelPartnerRepository;
    private final HotelRoomTypeRepository hotelRoomTypeRepository;
    private final RequestLogRepository requestLogRepository;
    private final RoomInventoryRepository roomInventoryRepository;
    private final NotificationClient notificationClient;

    @Transactional
    public void processInventorySync(InventorySyncRequest request) {
        if (requestLogRepository.findByRequestId(request.getRequestId()).isPresent()) {
            log.info("Skip duplicated request_id={}", request.getRequestId());
            return;
        }

        requestLogRepository.save(RequestLog.builder()
                .requestId(request.getRequestId())
                .status("PROCESSING")
                .build());

        validateBusinessRules(request);

        for (InventoryItem item : request.getInventories()) {
            upsertByDateRange(request.getHotelId(), item, request.getRequestId());
        }

        requestLogRepository.findByRequestId(request.getRequestId()).ifPresent(logEntity -> {
            logEntity.setStatus("PROCESSED");
            requestLogRepository.save(logEntity);
        });
    }

    private void validateBusinessRules(InventorySyncRequest request) {
        if (!hotelPartnerRepository.existsByHotelId(request.getHotelId())) {
            throw new InventoryValidationException("hotel_id does not exist: " + request.getHotelId());
        }

        for (InventoryItem item : request.getInventories()) {
            if (!hotelRoomTypeRepository.existsByHotelIdAndRoomTypeId(request.getHotelId(), item.getRoomTypeId())) {
                throw new InventoryValidationException("room_type_id does not belong to hotel: " + item.getRoomTypeId());
            }

            DateRange range = item.getDateRange();
            if (range.getStart().isAfter(range.getEnd())) {
                throw new InventoryValidationException("start date must be <= end date");
            }

            long totalDays = ChronoUnit.DAYS.between(range.getStart(), range.getEnd()) + 1;
            if (totalDays > 90) {
                throw new InventoryValidationException("date_range cannot exceed 90 days");
            }
        }
    }

    private void upsertByDateRange(String hotelId, InventoryItem item, String requestId) {
        LocalDate start = item.getDateRange().getStart();
        LocalDate end = item.getDateRange().getEnd();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            RoomInventoryDaily daily = roomInventoryRepository
                    .findByHotelIdAndRoomTypeIdAndRatePlanIdAndStayDate(
                            hotelId,
                            item.getRoomTypeId(),
                            item.getRatePlanId(),
                            currentDate
                    )
                    .orElseGet(() -> RoomInventoryDaily.builder()
                            .hotelId(hotelId)
                            .roomTypeId(item.getRoomTypeId())
                            .ratePlanId(item.getRatePlanId())
                            .stayDate(currentDate)
                            .soldInventory(0)
                            .build());

            if (item.getTotalInventory() < safeSold(daily.getSoldInventory())) {
                sendInventoryAbnormalNotificationSafely(hotelId, item, currentDate, daily, requestId);
                throw new InventoryBusinessException("Cannot reduce inventory below sold rooms");
            }

            Restrictions restrictions = item.getRestrictions();
            daily.setTotalInventory(item.getTotalInventory());
            daily.setStatus(item.getStatus());
            daily.setMinStay(restrictions == null ? null : restrictions.getMinStay());
            daily.setMaxStay(restrictions == null ? null : restrictions.getMaxStay());
            daily.setClosedToArrival(restrictions != null && Boolean.TRUE.equals(restrictions.getClosedToArrival()));
            daily.setClosedToDeparture(restrictions != null && Boolean.TRUE.equals(restrictions.getClosedToDeparture()));

            roomInventoryRepository.save(daily);
        }
    }

    private void sendInventoryAbnormalNotificationSafely(String hotelId,
                                                         InventoryItem item,
                                                         LocalDate currentDate,
                                                         RoomInventoryDaily daily,
                                                         String requestId) {
        try {
            notificationClient.send(NotificationRequest.builder()
                    .type("INVENTORY_ABNORMAL")
                    .recipient(NotificationRecipient.builder().build())
                    .payload(Map.of(
                            "hotelId", hotelId,
                            "roomTypeId", item.getRoomTypeId(),
                            "date", currentDate.toString(),
                            "availableRooms", item.getTotalInventory(),
                            "requestedRooms", safeSold(daily.getSoldInventory()),
                            "message", "Inventory update would oversell existing sold rooms"
                    ))
                    .metadata(NotificationMetadata.builder()
                            .idempotencyKey("inventory-abnormal-" + requestId + "-" + hotelId + "-" + item.getRoomTypeId() + "-" + currentDate)
                            .build())
                    .build());
        } catch (Exception exception) {
            log.warn("Failed to send inventory abnormal notification for hotel {} roomType {} date {}",
                    hotelId, item.getRoomTypeId(), currentDate, exception);
        }
    }

    private int safeSold(Integer soldInventory) {
        return soldInventory == null ? 0 : soldInventory;
    }
}
