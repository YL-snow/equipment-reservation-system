package com.ers.controller;

import com.ers.dto.EquipmentDTO;
import com.ers.dto.PageResult;
import com.ers.dto.Result;
import com.ers.entity.EquipmentCategory;
import com.ers.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    public Result<List<EquipmentDTO>> listAll() {
        return Result.success(equipmentService.findAll());
    }

    @GetMapping("/search")
    public Result<PageResult<EquipmentDTO>> search(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<EquipmentDTO> result = equipmentService.findByConditions(name, categoryId, pageable);
        return Result.success(PageResult.from(result));
    }

    @GetMapping("/{id}")
    public Result<EquipmentDTO> getById(@PathVariable Long id) {
        return Result.success(equipmentService.findById(id));
    }

    @GetMapping("/categories")
    public Result<List<EquipmentCategory>> getCategories() {
        return Result.success(equipmentService.findAllCategories());
    }
}
