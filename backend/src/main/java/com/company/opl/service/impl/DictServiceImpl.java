package com.company.opl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.opl.dto.dict.DictItemSaveDTO;
import com.company.opl.dto.dict.DictTypeSaveDTO;
import com.company.opl.entity.DictItem;
import com.company.opl.entity.DictType;
import com.company.opl.exception.BusinessException;
import com.company.opl.mapper.DictItemMapper;
import com.company.opl.mapper.DictTypeMapper;
import com.company.opl.service.DictService;
import com.company.opl.vo.dict.DictItemVO;
import com.company.opl.vo.dict.DictTypeDetailVO;
import com.company.opl.vo.dict.DictTypeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DictServiceImpl implements DictService {

    private final DictTypeMapper dictTypeMapper;
    private final DictItemMapper dictItemMapper;

    public DictServiceImpl(DictTypeMapper dictTypeMapper, DictItemMapper dictItemMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictItemMapper = dictItemMapper;
    }

    @Override
    public List<DictTypeVO> listTypes() {
        List<DictType> types = dictTypeMapper.selectList(new LambdaQueryWrapper<DictType>()
                .orderByAsc(DictType::getDictCode));
        if (types.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Integer> itemCountMap = buildItemCountMap(types);
        return types.stream().map(item -> toTypeVO(item, itemCountMap.getOrDefault(item.getId(), 0))).toList();
    }

    @Override
    public DictTypeDetailVO getTypeDetail(Long typeId) {
        DictType dictType = loadType(typeId);
        DictTypeDetailVO vo = new DictTypeDetailVO();
        BeanUtils.copyProperties(dictType, vo);
        vo.setItems(listItemsByTypeId(typeId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createType(DictTypeSaveDTO dto) {
        validateTypeCodeUnique(dto.getDictCode(), null);
        DictType dictType = new DictType();
        BeanUtils.copyProperties(dto, dictType);
        dictType.setDictCode(dto.getDictCode().trim().toUpperCase());
        dictType.setDictName(dto.getDictName().trim());
        dictType.setStatus(dto.getStatus().trim().toUpperCase());
        dictType.setRemark(trimToNull(dto.getRemark()));
        dictTypeMapper.insert(dictType);
        return dictType.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateType(Long typeId, DictTypeSaveDTO dto) {
        DictType dictType = loadType(typeId);
        validateTypeCodeUnique(dto.getDictCode(), typeId);
        dictType.setDictCode(dto.getDictCode().trim().toUpperCase());
        dictType.setDictName(dto.getDictName().trim());
        dictType.setStatus(dto.getStatus().trim().toUpperCase());
        dictType.setRemark(trimToNull(dto.getRemark()));
        dictTypeMapper.updateById(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteType(Long typeId) {
        loadType(typeId);
        dictItemMapper.delete(new LambdaQueryWrapper<DictItem>().eq(DictItem::getDictTypeId, typeId));
        dictTypeMapper.deleteById(typeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createItem(Long typeId, DictItemSaveDTO dto) {
        loadType(typeId);
        validateItemValueUnique(typeId, dto.getItemValue(), null);
        DictItem dictItem = new DictItem();
        applyItem(dto, dictItem, typeId);
        dictItemMapper.insert(dictItem);
        return dictItem.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(Long itemId, DictItemSaveDTO dto) {
        DictItem dictItem = loadItem(itemId);
        validateItemValueUnique(dictItem.getDictTypeId(), dto.getItemValue(), itemId);
        applyItem(dto, dictItem, dictItem.getDictTypeId());
        dictItemMapper.updateById(dictItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long itemId) {
        loadItem(itemId);
        dictItemMapper.deleteById(itemId);
    }

    @Override
    public List<DictItemVO> listEnabledItemsByCode(String dictCode) {
        if (!StringUtils.hasText(dictCode)) {
            return Collections.emptyList();
        }
        DictType dictType = dictTypeMapper.selectOne(new LambdaQueryWrapper<DictType>()
                .eq(DictType::getDictCode, dictCode.trim().toUpperCase())
                .eq(DictType::getStatus, "ENABLED")
                .last("limit 1"));
        if (dictType == null) {
            return Collections.emptyList();
        }
        return dictItemMapper.selectList(new LambdaQueryWrapper<DictItem>()
                        .eq(DictItem::getDictTypeId, dictType.getId())
                        .eq(DictItem::getStatus, "ENABLED")
                        .orderByDesc(DictItem::getIsDefault)
                        .orderByAsc(DictItem::getSortNo)
                        .orderByAsc(DictItem::getId))
                .stream()
                .map(this::toItemVO)
                .toList();
    }

    private void applyItem(DictItemSaveDTO dto, DictItem dictItem, Long typeId) {
        dictItem.setDictTypeId(typeId);
        dictItem.setItemLabel(dto.getItemLabel().trim());
        dictItem.setItemValue(dto.getItemValue().trim().toUpperCase());
        dictItem.setItemColor(trimToNull(dto.getItemColor()));
        dictItem.setSortNo(dto.getSortNo());
        dictItem.setIsDefault(Boolean.TRUE.equals(dto.getIsDefault()) ? 1 : 0);
        dictItem.setStatus(dto.getStatus().trim().toUpperCase());
        dictItem.setRemark(trimToNull(dto.getRemark()));
    }

    private void validateTypeCodeUnique(String dictCode, Long excludeId) {
        String normalizedCode = dictCode == null ? "" : dictCode.trim().toUpperCase();
        DictType exists = dictTypeMapper.selectOne(new LambdaQueryWrapper<DictType>()
                .eq(DictType::getDictCode, normalizedCode)
                .ne(excludeId != null, DictType::getId, excludeId)
                .last("limit 1"));
        if (exists != null) {
            throw new BusinessException("字典编码已存在");
        }
    }

    private void validateItemValueUnique(Long typeId, String itemValue, Long excludeId) {
        String normalizedValue = itemValue == null ? "" : itemValue.trim().toUpperCase();
        DictItem exists = dictItemMapper.selectOne(new LambdaQueryWrapper<DictItem>()
                .eq(DictItem::getDictTypeId, typeId)
                .eq(DictItem::getItemValue, normalizedValue)
                .ne(excludeId != null, DictItem::getId, excludeId)
                .last("limit 1"));
        if (exists != null) {
            throw new BusinessException("字典值已存在");
        }
    }

    private DictType loadType(Long typeId) {
        DictType dictType = dictTypeMapper.selectById(typeId);
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }
        return dictType;
    }

    private DictItem loadItem(Long itemId) {
        DictItem dictItem = dictItemMapper.selectById(itemId);
        if (dictItem == null) {
            throw new BusinessException("字典项不存在");
        }
        return dictItem;
    }

    private List<DictItemVO> listItemsByTypeId(Long typeId) {
        return dictItemMapper.selectList(new LambdaQueryWrapper<DictItem>()
                        .eq(DictItem::getDictTypeId, typeId)
                        .orderByDesc(DictItem::getIsDefault)
                        .orderByAsc(DictItem::getSortNo)
                        .orderByAsc(DictItem::getId))
                .stream()
                .map(this::toItemVO)
                .toList();
    }

    private DictItemVO toItemVO(DictItem item) {
        DictItemVO vo = new DictItemVO();
        BeanUtils.copyProperties(item, vo);
        vo.setIsDefault(item.getIsDefault() != null && item.getIsDefault() == 1);
        return vo;
    }

    private DictTypeVO toTypeVO(DictType item, int itemCount) {
        DictTypeVO vo = new DictTypeVO();
        BeanUtils.copyProperties(item, vo);
        vo.setItemCount(itemCount);
        return vo;
    }

    private Map<Long, Integer> buildItemCountMap(List<DictType> types) {
        if (types.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> typeIds = types.stream().map(DictType::getId).toList();
        Map<Long, Integer> map = new LinkedHashMap<>();
        dictItemMapper.selectList(new LambdaQueryWrapper<DictItem>()
                        .in(DictItem::getDictTypeId, typeIds))
                .forEach(item -> map.merge(item.getDictTypeId(), 1, Integer::sum));
        return map;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
