package com.sky.service;

import com.sky.vo.SetmealVO;

public interface SetmealService {
    /**
     * 根据ID查询套餐
     * @param id
     * @return
     */
    SetmealVO getById(Long id);
}
