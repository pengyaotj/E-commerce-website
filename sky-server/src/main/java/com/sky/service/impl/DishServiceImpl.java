package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.DishDisableFailedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;


    /**
     * 新增菜品
     * @param dishDTO
     */
    //操作多个表，需要保证事务的一致性
    @Transactional
    @Override
    public void addDishWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //1.向菜品表插入一条数据
        dishMapper.insert(dish);

        // Long dishId = dish.getId();
        // 但是这样获得不到

        Long dishId = dish.getId();
        //2.向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors !=null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //直接批量插入
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO pageQueryDTO) {
        //基于PageHelper
        PageHelper.startPage(pageQueryDTO.getPage(),pageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(pageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 菜品批量删除
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //1.判断是否存在起售中的菜品
        //查这些菜品的status
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE) {
                //当前菜品起售中，抛异常
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //2.判断当前是否被套餐关联
        //查中间关系表
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIds !=null && !setmealIds.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //3.删除餐品数据
//        for (Long dishId : ids) {
//            //每次都会2次SQL，希望较少SQL语句的数量
//            dishMapper.deleteById(dishId);
//            //4.删除菜品关联的口味数据
//            //不管有没有直接删除
//            dishFlavorMapper.deleteByDishId(dishId);
//        }

        //根据餐品ID集合批量删除
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据ID查询菜品和口味数据
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //1.查菜品表
        Dish dish = dishMapper.getById(id);
        //2.查关联口味
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        //3.封装到VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    /**
     *
     * @param dishDTO
     */
    @Override
    public void udpateWithFlavor(DishDTO dishDTO) {
        //修改基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);

        //把当前口味数据删除
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //插入当前的口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors !=null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            //直接批量插入
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 根据分类ID查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        //遍历访问数据，封装到VO数据里
        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    /**
     * 菜品起售停售
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //停售菜品时，判断是否有仍在使用该菜品的套餐
        if(status == StatusConstant.DISABLE) {
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(new ArrayList<Long>(Arrays.asList(id)));
            if(setmealIds !=null && !setmealIds.isEmpty()) {
                setmealIds.forEach(setmealId -> {
                    Integer setmealStatus = setmealMapper.getById(setmealId).getStatus();
                    if(StatusConstant.ENABLE == setmealStatus) {
                        throw new DishDisableFailedException(MessageConstant.DISH_DISABLE_FAILED);
                    }
                });
            }
        }

        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();

        dishMapper.update(dish);
    }
}
