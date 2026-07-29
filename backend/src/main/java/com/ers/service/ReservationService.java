package com.ers.service;

import com.ers.dto.CreateReservationRequest;
import com.ers.entity.*;
import com.ers.exception.BusinessException;
import com.ers.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EquipmentRepository equipmentRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final UserService userService;

    public List<Reservation> findAll() {
        return reservationRepository.findAllWithEquipment();
    }

    public List<Reservation> findByUserId(Long userId) {
        return reservationRepository.findByUserIdWithEquipment(userId);
    }

    public List<Reservation> checkConflict(Long equipmentId, LocalDateTime startTime, LocalDateTime endTime) {
        return reservationRepository.findConflicting(
                equipmentId, startTime, endTime,
                List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED));
    }

    @Transactional
    public Reservation createReservation(CreateReservationRequest request) {
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("INVALID_TIME", "预约时间必须在当前时间之后");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("INVALID_TIME", "结束时间必须晚于开始时间");
        }

        if (request.getUserId() != null) {
            if (userService.isBlacklisted(request.getUserId())) {
                LocalDateTime until = userService.getBlacklistedUntil(request.getUserId());
                String untilStr = until != null ? until.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
                throw new BusinessException("BLACKLISTED", "您已被列为失信人员，至 " + untilStr + " 前无法预约");
            }
        }

        List<Reservation> conflicts = checkConflict(
                request.getEquipmentId(), request.getStartTime(), request.getEndTime());
        if (!conflicts.isEmpty()) {
            throw new BusinessException("TIME_CONFLICT", "该时段已有预约，请选择其他时间");
        }

        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new BusinessException("EQUIPMENT_NOT_FOUND", "设备不存在"));

        if (equipment.getAvailableQty() < request.getQuantity()) {
            throw new BusinessException("INSUFFICIENT_STOCK", "当前可用库存不足");
        }

        Reservation reservation = new Reservation();
        reservation.setEquipment(equipment);
        reservation.setApplicant(request.getApplicant());
        reservation.setUserId(request.getUserId());
        reservation.setQuantity(request.getQuantity());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setRemark(request.getRemark());
        reservation.setStatus(ReservationStatus.PENDING);

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation approveReservation(Long id, String operator) {
        Reservation reservation = reservationRepository.findByIdWithLock(id)
                .orElseThrow(() -> new BusinessException("RESERVATION_NOT_FOUND", "预约不存在"));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException("INVALID_STATUS", "当前状态不允许审批");
        }

        if (reservation.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("RESERVATION_EXPIRED", "预约已过期，无法审批");
        }

        Long equipmentId = reservation.getEquipment().getId();
        Integer quantity = reservation.getQuantity();

        List<Reservation> conflicts = reservationRepository.findConflicting(
                equipmentId, reservation.getStartTime(), reservation.getEndTime(),
                List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED));
        conflicts.removeIf(r -> r.getId().equals(id));
        if (!conflicts.isEmpty()) {
            throw new BusinessException("TIME_CONFLICT", "同一时段已有其他已通过或待审批的预约，无法审批");
        }

        Equipment equipment = equipmentRepository.findByIdWithLock(equipmentId)
                .orElseThrow(() -> new BusinessException("EQUIPMENT_NOT_FOUND", "设备不存在"));

        Integer qtyBefore = equipment.getAvailableQty();

        int affected = equipmentRepository.deductStock(equipmentId, quantity);
        if (affected != 1) {
            throw new BusinessException("INSUFFICIENT_STOCK", "库存已被其他预约占用，无法通过");
        }

        reservation.setStatus(ReservationStatus.APPROVED);
        reservationRepository.save(reservation);

        InventoryLog log = new InventoryLog();
        log.setEquipment(equipmentRepository.getReferenceById(equipmentId));
        log.setReservation(reservationRepository.getReferenceById(id));
        log.setChangeType("DEDUCT");
        log.setQtyBefore(qtyBefore);
        log.setQtyAfter(qtyBefore - quantity);
        log.setOperator(operator);
        inventoryLogRepository.save(log);

        return reservation;
    }

    @Transactional
    public Reservation rejectReservation(Long id, String operator) {
        Reservation reservation = reservationRepository.findByIdWithLock(id)
                .orElseThrow(() -> new BusinessException("RESERVATION_NOT_FOUND", "预约不存在"));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException("INVALID_STATUS", "仅 PENDING 状态的预约可以驳回");
        }

        reservation.setStatus(ReservationStatus.REJECTED);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation returnReservation(Long id, String operator) {
        Reservation reservation = reservationRepository.findByIdWithLock(id)
                .orElseThrow(() -> new BusinessException("RESERVATION_NOT_FOUND", "预约不存在"));

        if (reservation.getStatus() != ReservationStatus.APPROVED) {
            throw new BusinessException("INVALID_STATUS", "仅 APPROVED 状态的预约可以归还");
        }
        
        if (LocalDateTime.now().isAfter(reservation.getEndTime())) {
            userService.incrementOverdueCount(reservation.getUserId());
        }

        Long equipmentId = reservation.getEquipment().getId();
        Integer quantity = reservation.getQuantity();

        Equipment equipment = equipmentRepository.findByIdWithLock(equipmentId)
                .orElseThrow(() -> new BusinessException("EQUIPMENT_NOT_FOUND", "设备不存在"));

        if (equipment.getAvailableQty() + quantity > equipment.getTotalQuantity()) {
            throw new BusinessException("STOCK_OVERFLOW", "归还后库存将超出总库存上限，请联系管理员");
        }

        Integer qtyBefore = equipment.getAvailableQty();

        int affected = equipmentRepository.returnStock(equipmentId, quantity);
        if (affected != 1) {
            throw new BusinessException("STOCK_OVERFLOW", "归还后库存将超出总库存上限，请联系管理员");
        }

        reservation.setStatus(ReservationStatus.RETURNED);
        reservationRepository.save(reservation);

        InventoryLog log = new InventoryLog();
        log.setEquipment(equipmentRepository.getReferenceById(equipmentId));
        log.setReservation(reservationRepository.getReferenceById(id));
        log.setChangeType("RETURN");
        log.setQtyBefore(qtyBefore);
        log.setQtyAfter(qtyBefore + quantity);
        log.setOperator(operator);
        inventoryLogRepository.save(log);

        return reservation;
    }
}
