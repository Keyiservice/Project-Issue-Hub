package com.company.opl.controller;

import com.company.opl.common.Result;
import com.company.opl.dto.dict.DictItemSaveDTO;
import com.company.opl.dto.dict.DictTypeSaveDTO;
import com.company.opl.service.DictService;
import com.company.opl.vo.dict.DictItemVO;
import com.company.opl.vo.dict.DictTypeDetailVO;
import com.company.opl.vo.dict.DictTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dicts")
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @GetMapping("/types")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "字典类型列表")
    public Result<List<DictTypeVO>> listTypes() {
        return Result.success(dictService.listTypes());
    }

    @GetMapping("/types/{typeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "字典类型详情")
    public Result<DictTypeDetailVO> getTypeDetail(@PathVariable Long typeId) {
        return Result.success(dictService.getTypeDetail(typeId));
    }

    @PostMapping("/types")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建字典类型")
    public Result<Long> createType(@Valid @RequestBody DictTypeSaveDTO dto) {
        return Result.success(dictService.createType(dto));
    }

    @PutMapping("/types/{typeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新字典类型")
    public Result<Void> updateType(@PathVariable Long typeId, @Valid @RequestBody DictTypeSaveDTO dto) {
        dictService.updateType(typeId, dto);
        return Result.success("保存成功", null);
    }

    @DeleteMapping("/types/{typeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除字典类型")
    public Result<Void> deleteType(@PathVariable Long typeId) {
        dictService.deleteType(typeId);
        return Result.success("删除成功", null);
    }

    @PostMapping("/types/{typeId}/items")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建字典项")
    public Result<Long> createItem(@PathVariable Long typeId, @Valid @RequestBody DictItemSaveDTO dto) {
        return Result.success(dictService.createItem(typeId, dto));
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新字典项")
    public Result<Void> updateItem(@PathVariable Long itemId, @Valid @RequestBody DictItemSaveDTO dto) {
        dictService.updateItem(itemId, dto);
        return Result.success("保存成功", null);
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除字典项")
    public Result<Void> deleteItem(@PathVariable Long itemId) {
        dictService.deleteItem(itemId);
        return Result.success("删除成功", null);
    }

    @GetMapping("/options/{dictCode}")
    @PreAuthorize("hasAnyRole('SITE_USER','RESP_ENGINEER','PROJECT_MANAGER','MANAGEMENT','ADMIN')")
    @Operation(summary = "按字典编码获取启用项")
    public Result<List<DictItemVO>> listEnabledItems(@PathVariable String dictCode) {
        return Result.success(dictService.listEnabledItemsByCode(dictCode));
    }
}
