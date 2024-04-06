package com.jzo2o.foundations.service.impl;

import com.jzo2o.common.utils.ObjectUtils;
import com.jzo2o.foundations.constants.RedisConstants;
import com.jzo2o.foundations.enums.FoundationStatusEnum;
import com.jzo2o.foundations.mapper.RegionMapper;
import com.jzo2o.foundations.mapper.ServeMapper;
import com.jzo2o.foundations.model.domain.Region;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.domain.ServeItem;
import com.jzo2o.foundations.model.dto.response.ServeAggregationSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeAggregationTypeSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeCategoryResDTO;
import com.jzo2o.foundations.model.dto.response.ServeSimpleResDTO;
import com.jzo2o.foundations.service.HomeService;
import com.jzo2o.foundations.service.IRegionService;
import com.jzo2o.foundations.service.IServeItemService;
import com.jzo2o.foundations.service.IServeService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-07-03
 */
@Service
public class HomeServiceImpl implements HomeService {


    @Resource
    private ServeMapper serveMapper;
    @Resource
    private IRegionService regionService;
    @Resource
    private IServeItemService serveItemService;
    @Resource
    private IServeService serveService;
    @Resource
    private HomeService homeService;

    @Override
    @Caching(
            cacheable = {
                    //result为null时,属于缓存穿透情况，缓存时间30分钟
                    @Cacheable(value = RedisConstants.CacheName.SERVE_ICON, key = "#regionId", unless = "#result.size() != 0", cacheManager = RedisConstants.CacheManager.THIRTY_MINUTES),
                    //result不为null时,永久缓存
                    @Cacheable(value = RedisConstants.CacheName.SERVE_ICON, key = "#regionId", unless = "#result.size() == 0", cacheManager = RedisConstants.CacheManager.FOREVER)
            }
    )
    public List<ServeCategoryResDTO> firstPageQueryByRegionId(Long regionId) {

        // 1.校验当前城市是否为启用状态
        Region curRegion = regionService.getById(regionId);
        if(ObjectUtils.isEmpty(curRegion) || curRegion.getActiveStatus() != FoundationStatusEnum.ENABLE.getStatus()){
            return Collections.emptyList();
        }

        // 2.校验通过则查询该区域的服务
        List<ServeCategoryResDTO> serveCategoryResDTOS = serveMapper.firstPageQueryByRegionId(regionId);
        if(ObjectUtils.isEmpty(serveCategoryResDTOS)){
            return Collections.emptyList();
        }

        // 3.如果有服务类型,则取前两个,具体服务项取前四个
        int serveTypeEndIndex = Math.min(serveCategoryResDTOS.size(), 2);
        List<ServeCategoryResDTO> newList = new ArrayList<>(serveCategoryResDTOS.subList(0, serveTypeEndIndex));
        newList.forEach(item -> {
            List<ServeSimpleResDTO> serveResDTOList = item.getServeResDTOList();
            int serveItemEndIndex = Math.min(item.getServeResDTOList().size(), 4);
            List<ServeSimpleResDTO> serveSimpleResDTOS = new ArrayList<>(serveResDTOList.subList(0, serveItemEndIndex));
            item.setServeResDTOList(serveSimpleResDTOS);
        });

        return newList;
    }

    /**
     * 根据区域id查询服务类型
     * 业务逻辑:
     *          1.检验传入的区域id参数是否合法
     *          2.调用serveMapper查询服务类型
     * @param regionId
     * @return
     */
    @Override
    @Caching(cacheable = {
            @Cacheable(value = RedisConstants.CacheName.SERVE_TYPE, key = "#regionId", cacheManager = RedisConstants.CacheManager.THIRTY_MINUTES, unless = "#result.size() != 0"),
            @Cacheable(value = RedisConstants.CacheName.SERVE_TYPE, key = "#regionId", cacheManager = RedisConstants.CacheManager.FOREVER, unless = "#result.size() == 0")
    })
    public List<ServeAggregationTypeSimpleResDTO> serveTypeListQueryByRegionId(Long regionId) {
        // 1.检验传入的区域id参数是否合法
        Region curRegion = regionService.getById(regionId);
        if(ObjectUtils.isEmpty(curRegion) || curRegion.getActiveStatus() != FoundationStatusEnum.ENABLE.getStatus()){
            return Collections.emptyList();
        }

        // 2.调用serveMapper查询服务类型
        List<ServeAggregationTypeSimpleResDTO> list = serveMapper.queryServeTypeListByRegionId(regionId);
        if(list.isEmpty()){
            return Collections.emptyList();
        }
        return list;
    }

    /**
     * 热门服务类型查询
     * 业务逻辑:
     *          1.检验区域id,若不存在则返回空集合并缓存入redis
     *          2.若存在则增长查询返回热门服务列表
     * @param regionId
     * @return
     */
    @Override
    @Caching(
            cacheable = {
                    //result为null时,属于缓存穿透情况，缓存时间30分钟
                    @Cacheable(value = RedisConstants.CacheName.HOT_SERVE, key = "#regionId", unless = "#result.size() != 0", cacheManager = RedisConstants.CacheManager.THIRTY_MINUTES),
                    //result不为null时,永久缓存
                    @Cacheable(value = RedisConstants.CacheName.HOT_SERVE, key = "#regionId", unless = "#result.size() == 0", cacheManager = RedisConstants.CacheManager.FOREVER)
            }
    )
    public List<ServeAggregationSimpleResDTO> findHotServeListByRegionIdCache(Long regionId) {
        // 1.检验传入的区域id参数是否合法
        Region curRegion = regionService.getById(regionId);
        if(ObjectUtils.isEmpty(curRegion) || curRegion.getActiveStatus() != FoundationStatusEnum.ENABLE.getStatus()){
            return Collections.emptyList();
        }

        // 2.调用serveMapper查询服务类型
        List<ServeAggregationSimpleResDTO> list = serveMapper.findHotServeListByRegionIdCache(regionId);
        if(list.isEmpty()){
            return Collections.emptyList();
        }
        return list;
    }

//    /**
//     * 服务详情info
//     * 业务逻辑:
//     *          1.检验区域id是否存在,检验服务项id是否存在
//     *          2.查询Serve表和ServeItem表展示info
//     * @param regionId
//     * @param serveItemId
//     * @return
//     */
//    @Override
//    public ServeAggregationSimpleResDTO listServeInfoByRegionIdAndServeItemId(Long regionId, Long serveItemId) {
//        // 1.检验传入的区域id参数是否合法
//        Region curRegion = regionService.getById(regionId);
//        if(ObjectUtils.isEmpty(curRegion) || curRegion.getActiveStatus() != FoundationStatusEnum.ENABLE.getStatus()){
//            return null;
//        }
//        // 2.检验服务项id是否存在
//        ServeItem curServeItem = serveItemService.getById(serveItemId);
//        if(ObjectUtils.isNull(curServeItem) || curServeItem.getActiveStatus() != FoundationStatusEnum.ENABLE.getStatus()){
//            return null;
//        }
//
//        // 3.查询数据库
//        ServeAggregationSimpleResDTO serveAggregationSimpleResDTO = serveMapper.listServeInfo(regionId, serveItemId);
//        if(ObjectUtils.isNull(serveAggregationSimpleResDTO)){
//            return null;
//        }
//        return serveAggregationSimpleResDTO;
//    }

    /**
     * 根据id查询区域服务信息
     *
     * @param id 服务id
     * @return 服务
     */
    @Override
    @Cacheable(value = RedisConstants.CacheName.SERVE, key = "#id", cacheManager = RedisConstants.CacheManager.ONE_DAY)
    public Serve queryServeByIdCache(Long id) {
        return serveService.getById(id);
    }

    /**
     * 根据id查询服务项
     *
     * @param id 服务项id
     * @return 服务项
     */
    @Override
    @Cacheable(value = RedisConstants.CacheName.SERVE_ITEM, key = "#id", cacheManager = RedisConstants.CacheManager.ONE_DAY)
    public ServeItem queryServeItemByIdCache(Long id) {
        return serveItemService.getById(id);
    }

    /**
     * 刷新区域id相关缓存：首页图标、热门服务、服务分类
     *
     * @param regionId 区域id
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = RedisConstants.CacheName.SERVE_ICON, key = "#regionId", beforeInvocation = true),
            @CacheEvict(value = RedisConstants.CacheName.HOT_SERVE, key = "#regionId", beforeInvocation = true),
            @CacheEvict(value = RedisConstants.CacheName.SERVE_TYPE, key = "#regionId", beforeInvocation = true)
    })
    public void refreshRegionRelateCaches(Long regionId) {
        //刷新缓存：首页图标、热门服务、服务类型
        homeService.firstPageQueryByRegionId(regionId);
        homeService.findHotServeListByRegionIdCache(regionId);
        homeService.serveTypeListQueryByRegionId(regionId);
    }

    @Override
    public void queryActiveRegionListCache() {
        regionService.queryActiveRegionList();
    }
}
