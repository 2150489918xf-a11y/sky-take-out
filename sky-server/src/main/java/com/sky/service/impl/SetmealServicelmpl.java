package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServicelmpl implements SetmealService {

    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐,同时需要保存套餐和对应的菜品关联信息
     * @param setmealDTO
     */
    public void saveWithDish(SetmealDTO setmealDTO){

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //向套餐表插入一条数据
        setmealMapper.insert(setmeal);

        //获取生成的套餐id
        Long setmealId = setmeal.getId();  //套餐id是怎么生成的？  是在插入数据时，数据库自动生成的主键值，并通过MyBatis的映射机制回填到实体对象中的。

        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();  //获取套餐和菜品的关联数据
        setmealDishList.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });

        //保存套餐和菜品的关联信息
        setmealDishMapper.insertBatch(setmealDishList);
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO){
        int pageNum = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();

        PageHelper.startPage(pageNum,pageSize);
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids){
        //删除套餐表中的数据
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if (StatusConstant.ENABLE == setmeal.getStatus()){
                throw new RuntimeException("套餐正在售卖中，不能删除");
            }
        });

        ids.forEach(setmealId -> {
            //删除套餐表中的数据
            setmealMapper.deleteById(setmealId);
            //删除套餐和菜品的关联数据
            setmealDishMapper.deleteBySetmealId(setmealId);
        });
    }

    /**
     * 根据id查询套餐及其菜品信息
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        //查询套餐基本信息
        Setmeal setmeal = setmealMapper.getById(id);
        List<SetmealDish> setmealDishes= setmealDishMapper.getBySetmealId(id);  //查询套餐和菜品的关联信息

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;  //将套餐基本信息和套餐和菜品的关联信息封装到SetmealVO对象中，并返回给调用方。这样调用方就可以通过SetmealVO对象获取到套餐的详细信息，包括套餐的基本信息和对应的菜品信息。
    }

    /**
     * 修改套餐信息和对应的菜品信息
     * @param setmealDTO
     */
    public void update(SetmealDTO setmealDTO){
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //更新套餐表中的数据
        setmealMapper.update(setmeal);

        //套餐Id
        Long setmealId = setmeal.getId();

        //删除套餐和菜品的关联信息，操作setmeal_dish表，执行delete语句
        setmealDishMapper.deleteBySetmealId(setmealId);

        //保存套餐和菜品的关联信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 根据id修改套餐的启售状态
     * @param status
     * @param id
     */
    public void startOrstop(Integer status, Long id) {
        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示“套餐内有停售菜品，不能起售”
        if(status==StatusConstant.ENABLE){
            //select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = ?
            List<Dish>dishList=dishMapper.getBySetmealId(id);
            if (dishList!=null && dishList.size()>0){
                dishList.forEach(dish -> {
                    if (StatusConstant.DISABLE == dish.getStatus()){
                        throw new RuntimeException("套餐内有停售菜品，不能起售");
                    }
                });
            }
        }

        Setmeal setmeal =Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
