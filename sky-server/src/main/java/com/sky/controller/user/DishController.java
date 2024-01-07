package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "客户端菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类ID查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类ID查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        log.info("根据分类ID查询菜品: {}",categoryId);

        //组装Redis中的key
        String key = "dish" + categoryId;
        //1.查询Redis是否存在该分类商品数据
        //放进去是什么类型，取出来就是什么类型
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if(list !=null && !list.isEmpty()) {
            //如果存在，直接返回
            return Result.success(list);
        }

        //2.如果不存在，查询数据库，并将查询到的信息放在Redis中
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        list = dishService.listWithFlavor(dish);

        //3.放到Redis中
        redisTemplate.opsForValue().set(key, list);

        return Result.success(list);
    }

}
