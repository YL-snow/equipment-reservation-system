package com.ers.repository;

import com.ers.entity.Reservation;
import com.ers.entity.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r JOIN FETCH r.equipment")
    java.util.List<Reservation> findAllWithEquipment();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT r FROM Reservation r WHERE r.equipment.id = :equipmentId " +
           "AND r.startTime < :endTime AND r.endTime > :startTime " +
           "AND r.status IN :statuses")
    java.util.List<Reservation> findConflicting(
            @Param("equipmentId") Long equipmentId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("statuses") Collection<ReservationStatus> statuses);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.equipment WHERE r.userId = :userId")
    java.util.List<Reservation> findByUserIdWithEquipment(@Param("userId") Long userId);
}
