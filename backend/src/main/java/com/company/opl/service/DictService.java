package com.company.opl.service;

import com.company.opl.dto.dict.DictItemSaveDTO;
import com.company.opl.dto.dict.DictTypeSaveDTO;
import com.company.opl.vo.dict.DictItemVO;
import com.company.opl.vo.dict.DictTypeDetailVO;
import com.company.opl.vo.dict.DictTypeVO;

import java.util.List;

public interface DictService {

    List<DictTypeVO> listTypes();

    DictTypeDetailVO getTypeDetail(Long typeId);

    Long createType(DictTypeSaveDTO dto);

    void updateType(Long typeId, DictTypeSaveDTO dto);

    void deleteType(Long typeId);

    Long createItem(Long typeId, DictItemSaveDTO dto);

    void updateItem(Long itemId, DictItemSaveDTO dto);

    void deleteItem(Long itemId);

    List<DictItemVO> listEnabledItemsByCode(String dictCode);
}
