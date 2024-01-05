package com.sky.service;

import com.sky.dto.DishDTO;

public interface DishService {
    /**
     * 新增餐品和对应的口味
     * @param dishDTO
     */
    public void addDishWithFlavor(DishDTO dishDTO);
}
