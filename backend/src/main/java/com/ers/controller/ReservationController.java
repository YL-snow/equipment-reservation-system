package com.ers.controller;

import com.ers.dto.*;
import com.ers.entity.Reservation;
import com.ers.entity.User;
import com.ers.entity.UserRole;
import com.ers.exception.BusinessException;
import com.ers.service.AuthService;
import com.ers.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final AuthService authService;

    @GetMapping
    public Result<List<Reservation>> listAll(HttpServletRequest request) {
        User user = requireUser(request);
        if (user.getRole() == UserRole.ADMIN) {
            return Result.success(reservationService.findAll());
        }
        return Result.success(reservationService.findByUserId(user.getId()));
    }

    @GetMapping("/conflict-check")
    public Result<Map<String, Object>> conflictCheck(
            HttpServletRequest servletRequest,
            @RequestParam Long equipmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        requireUser(servletRequest);
        List<Reservation> conflicts = reservationService.checkConflict(equipmentId, startTime, endTime);

        Map<String, Object> data = new HashMap<>();
        data.put("conflict", !conflicts.isEmpty());
        data.put("conflictReservations", conflicts.stream().map(r -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", r.getId());
            info.put("applicant", r.getApplicant());
            info.put("startTime", r.getStartTime().toString());
            info.put("endTime", r.getEndTime().toString());
            return info;
        }).collect(Collectors.toList()));

        return Result.success(data);
    }

    @PostMapping
    public Result<Map<String, Object>> create(HttpServletRequest servletRequest, @RequestBody CreateReservationRequest request) {
        User user = requireUser(servletRequest);
        request.setApplicant(user.getName());
        request.setUserId(user.getId());

        Reservation reservation = reservationService.createReservation(request);
        Map<String, Object> data = new HashMap<>();
        data.put("id", reservation.getId());
        data.put("status", reservation.getStatus().name());
        return Result.success("预约提交成功", data);
    }

    @PutMapping("/{id}/approve")
    public Result<Map<String, Object>> approve(HttpServletRequest servletRequest, @PathVariable Long id, @RequestBody ApproveRequest request) {
        User operator = requireAdmin(servletRequest);
        request.setOperator(operator.getName());

        Reservation reservation = reservationService.approveReservation(id, request.getOperator());
        Map<String, Object> data = new HashMap<>();
        data.put("id", reservation.getId());
        data.put("status", reservation.getStatus().name());
        return Result.success("审批通过，库存已扣减", data);
    }

    @PutMapping("/{id}/reject")
    public Result<Map<String, Object>> reject(HttpServletRequest servletRequest, @PathVariable Long id, @RequestBody ApproveRequest request) {
        User operator = requireAdmin(servletRequest);
        request.setOperator(operator.getName());

        Reservation reservation = reservationService.rejectReservation(id, request.getOperator());
        Map<String, Object> data = new HashMap<>();
        data.put("id", reservation.getId());
        data.put("status", reservation.getStatus().name());
        return Result.success("预约已驳回", data);
    }

    @PutMapping("/{id}/return")
    public Result<Map<String, Object>> returnReservation(HttpServletRequest servletRequest, @PathVariable Long id, @RequestBody ReturnRequest request) {
        User operator = requireAdmin(servletRequest);
        request.setOperator(operator.getName());

        Reservation reservation = reservationService.returnReservation(id, request.getOperator());
        Map<String, Object> data = new HashMap<>();
        data.put("id", reservation.getId());
        data.put("status", reservation.getStatus().name());
        return Result.success("归还成功，库存已加回", data);
    }

    private User requireUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("UNAUTHORIZED", "未授权，请先登录");
        }
        try {
            return authService.getUserFromToken(authHeader);
        } catch (Exception e) {
            throw new BusinessException("UNAUTHORIZED", "登录已失效，请重新登录");
        }
    }

    private User requireAdmin(HttpServletRequest request) {
        User user = requireUser(request);
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException("FORBIDDEN", "仅管理员可执行此操作");
        }
        return user;
    }
}
