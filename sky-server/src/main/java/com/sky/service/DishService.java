package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface DishService {
    /**
     * 新增餐品和对应的口味
     * @param dishDTO
     */
    public void addDishWithFlavor(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO pageQueryDTO);

    /**
     * 菜品批量删除
     */
    void deleteBatch(List<Long> ids);
}
