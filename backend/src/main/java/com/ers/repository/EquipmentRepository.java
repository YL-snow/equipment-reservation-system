package com.ers.repository;

import com.ers.entity.Equipment;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Page<Equipment> findByNameContaining(String name, Pageable pageable);

    Page<Equipment> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Equipment> findByNameContainingAndCategoryId(String name, Long categoryId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Equipment e WHERE e.id = :id")
    Optional<Equipment> findByIdWithLock(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Equipment e SET e.availableQty = e.availableQty - :quantity, e.version = e.version + 1 WHERE e.id = :id AND e.availableQty >= :quantity")
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Equipment e SET e.availableQty = e.availableQty + :quantity, e.version = e.version + 1 WHERE e.id = :id AND e.availableQty + :quantity <= e.totalQuantity")
    int returnStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
