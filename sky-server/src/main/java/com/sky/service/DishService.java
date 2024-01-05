package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

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

    /**
     * 根据ID查询菜品和口味数据
     * @param id
     * @return
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * 根据ID修改菜品和口味数据
     * @param dishDTO
     */
    void udpateWithFlavor(DishDTO dishDTO);
}
