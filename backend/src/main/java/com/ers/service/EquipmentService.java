package com.ers.service;

import com.ers.dto.EquipmentDTO;
import com.ers.entity.Equipment;
import com.ers.entity.EquipmentCategory;
import com.ers.repository.EquipmentCategoryRepository;
import com.ers.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentCategoryRepository categoryRepository;

    public List<EquipmentDTO> findAll() {
        return equipmentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<EquipmentDTO> findByConditions(String name, Long categoryId, Pageable pageable) {
        Page<Equipment> page;
        if (name != null && !name.isEmpty() && categoryId != null) {
            page = equipmentRepository.findByNameContainingAndCategoryId(name, categoryId, pageable);
        } else if (name != null && !name.isEmpty()) {
            page = equipmentRepository.findByNameContaining(name, pageable);
        } else if (categoryId != null) {
            page = equipmentRepository.findByCategoryId(categoryId, pageable);
        } else {
            page = equipmentRepository.findAll(pageable);
        }
        return page.map(this::toDTO);
    }

    public EquipmentDTO findById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("设备不存在"));
        return toDTO(equipment);
    }

    public List<EquipmentCategory> findAllCategories() {
        return categoryRepository.findAll();
    }

    private EquipmentDTO toDTO(Equipment equipment) {
        String categoryName = null;
        if (equipment.getCategoryId() != null) {
            categoryName = categoryRepository.findById(equipment.getCategoryId())
                    .map(EquipmentCategory::getName)
                    .orElse(null);
        }
        return EquipmentDTO.from(equipment, categoryName);
    }
}
