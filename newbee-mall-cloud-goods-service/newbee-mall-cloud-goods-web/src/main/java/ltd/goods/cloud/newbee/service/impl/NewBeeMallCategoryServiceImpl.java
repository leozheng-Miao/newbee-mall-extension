/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本软件已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2022 程序员十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.goods.cloud.newbee.service.impl;

import ltd.common.cloud.newbee.NewBeeMallCategoryLevelEnum;
import ltd.common.cloud.newbee.ServiceResultEnum;
import ltd.common.cloud.newbee.dto.PageQueryUtil;
import ltd.common.cloud.newbee.dto.PageResult;
import ltd.common.cloud.newbee.util.BeanUtil;
import ltd.goods.cloud.newbee.controller.vo.NewBeeMallIndexCategoryVO;
import ltd.goods.cloud.newbee.controller.vo.SecondLevelCategoryVO;
import ltd.goods.cloud.newbee.controller.vo.ThirdLevelCategoryVO;
import ltd.goods.cloud.newbee.dao.GoodsCategoryMapper;
import ltd.goods.cloud.newbee.entity.GoodsCategory;
import ltd.goods.cloud.newbee.service.NewBeeMallCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class NewBeeMallCategoryServiceImpl implements NewBeeMallCategoryService {

    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;

    @Override
    public String saveCategory(GoodsCategory goodsCategory) {
        GoodsCategory temp = goodsCategoryMapper.selectByLevelAndName(goodsCategory.getCategoryLevel(), goodsCategory.getCategoryName());
        if (temp != null) {
            return ServiceResultEnum.SAME_CATEGORY_EXIST.getResult();
        }
        if (goodsCategoryMapper.insertSelective(goodsCategory) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updateGoodsCategory(GoodsCategory goodsCategory) {
        GoodsCategory temp = goodsCategoryMapper.selectByPrimaryKey(goodsCategory.getCategoryId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        GoodsCategory temp2 = goodsCategoryMapper.selectByLevelAndName(goodsCategory.getCategoryLevel(), goodsCategory.getCategoryName());
        if (temp2 != null && !temp2.getCategoryId().equals(goodsCategory.getCategoryId())) {
            //同名且不同id 不能继续修改
            return ServiceResultEnum.SAME_CATEGORY_EXIST.getResult();
        }
        goodsCategory.setUpdateTime(new Date());
        if (goodsCategoryMapper.updateByPrimaryKeySelective(goodsCategory) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public GoodsCategory getGoodsCategoryById(Long id) {
        return goodsCategoryMapper.selectByPrimaryKey(id);
    }

    @Override
    public Boolean deleteBatch(Long[] ids) {
        if (ids.length < 1) {
            return false;
        }
        //删除分类数据
        return goodsCategoryMapper.deleteBatch(ids) > 0;
    }

    @Override
    public PageResult getCategorisPage(PageQueryUtil pageUtil) {
        List<GoodsCategory> goodsCategories = goodsCategoryMapper.findGoodsCategoryList(pageUtil);
        int total = goodsCategoryMapper.getTotalGoodsCategories(pageUtil);
        PageResult pageResult = new PageResult(goodsCategories, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public List<GoodsCategory> selectByLevelAndParentIdsAndNumber(List<Long> parentIds, int categoryLevel) {
        return goodsCategoryMapper.selectByLevelAndParentIdsAndNumber(parentIds, categoryLevel, 0);//0代表查询所有
    }

    @Override
    public List<NewBeeMallIndexCategoryVO> getCategoriesForIndex() {

        //获取一级分类的固定数量的数据
        List<GoodsCategory> firstLevelCategories =
                goodsCategoryMapper.selectByLevelAndParentIdsAndNumber(
                        Collections.singletonList(0L),
                        NewBeeMallCategoryLevelEnum.LEVEL_ONE.getLevel(),
                        10
                );

        if (CollectionUtils.isEmpty(firstLevelCategories)) {
            return Collections.emptyList();
        }

        //提取一级分类id列表
        List<Long> firstLevelCategoryIds =
                firstLevelCategories.stream()
                        .map(GoodsCategory::getCategoryId).
                        collect(Collectors.toList());

        //获取二级分类的数据
        List<GoodsCategory> secondLevelCategories =
                goodsCategoryMapper.selectByLevelAndParentIdsAndNumber(
                        firstLevelCategoryIds,
                        NewBeeMallCategoryLevelEnum.LEVEL_TWO.getLevel(),
                        0
                );

        //构建二级分类id列表
        List<Long> secondLevelCategoryIds = CollectionUtils.isEmpty(secondLevelCategories)
                ? Collections.emptyList()
                : secondLevelCategories.stream()
                .map(GoodsCategory::getCategoryId)
                .collect(Collectors.toList());

        //获取三级分类的数据
        List<GoodsCategory> thirdLevelCategories =
                goodsCategoryMapper.selectByLevelAndParentIdsAndNumber(
                        secondLevelCategoryIds,
                        NewBeeMallCategoryLevelEnum.LEVEL_THREE.getLevel(),
                        0
                );

        //建立 三级分类map： parentId -> List <三级分类>
        Map<Long, List<GoodsCategory>> thirdLevelCategoryMap =
                thirdLevelCategories
                        .stream()
                        .collect(groupingBy(GoodsCategory::getParentId));

        //建立 二级分类map： parentId -> List <二级分类>
        Map<Long, List<SecondLevelCategoryVO>> secondLevelVOMap = new HashMap<>();

        for (GoodsCategory second : secondLevelCategories) {
            SecondLevelCategoryVO secondVO = new SecondLevelCategoryVO();
            BeanUtil.copyProperties(second, secondVO);
            //如果该二级分类下有数据则放入 secondLevelCategoryVOS 对象中
            List<GoodsCategory> thirdList = thirdLevelCategoryMap.getOrDefault(second.getCategoryId(), new ArrayList<>());
            secondVO.setThirdLevelCategoryVOS(
                    BeanUtil.copyList(thirdList, ThirdLevelCategoryVO.class)
            );

            //加入分组： 一级分类ID -> 二级分类 VO
            secondLevelVOMap.computeIfAbsent(second.getParentId(), k -> new ArrayList<>()).add(secondVO);
        }

        //处理一级分类VO 列表
        List<NewBeeMallIndexCategoryVO> indexCategoryVOList = new ArrayList<>();

        for (GoodsCategory first : firstLevelCategories) {
            NewBeeMallIndexCategoryVO firstVO = new NewBeeMallIndexCategoryVO();
            BeanUtil.copyProperties(first, firstVO);

            List<SecondLevelCategoryVO> secondVOs = secondLevelVOMap.getOrDefault(first.getCategoryId(), new ArrayList<>());
            firstVO.setSecondLevelCategoryVOS(secondVOs);
            indexCategoryVOList.add(firstVO);
        }

        return indexCategoryVOList;

    }

}
