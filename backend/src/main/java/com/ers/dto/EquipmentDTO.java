package com.ers.dto;

import com.ers.entity.Equipment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentDTO {

    private Long id;
    private String name;
    private String model;
    private Integer totalQuantity;
    private Integer availableQty;
    private Integer status;
    private Long categoryId;
    private String categoryName;

    public static EquipmentDTO from(Equipment equipment, String categoryName) {
        EquipmentDTO dto = new EquipmentDTO();
        dto.setId(equipment.getId());
        dto.setName(equipment.getName());
        dto.setModel(equipment.getModel());
        dto.setTotalQuantity(equipment.getTotalQuantity());
        dto.setAvailableQty(equipment.getAvailableQty());
        dto.setStatus(equipment.getStatus());
        dto.setCategoryId(equipment.getCategoryId());
        dto.setCategoryName(categoryName);
        return dto;
    }
}
