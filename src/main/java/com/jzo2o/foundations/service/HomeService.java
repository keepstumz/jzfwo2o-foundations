package com.jzo2o.foundations.service;

import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.model.domain.ServeItem;
import com.jzo2o.foundations.model.dto.response.ServeAggregationSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeAggregationTypeSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeCategoryResDTO;

import java.util.List;


public interface HomeService {


    /**
     * 用户小程序门户首页查询
     * @param regionId
     * @return
     */
    List<ServeCategoryResDTO> firstPageQueryByRegionId(Long regionId);

    /**
     * 服务类型列表查询用于小程序页面展示
     * @param regionId
     * @return
     */
    List<ServeAggregationTypeSimpleResDTO> serveTypeListQueryByRegionId(Long regionId);

    /**
     * 热门服务类型查询
     * @param regionId
     * @return
     */
    List<ServeAggregationSimpleResDTO> findHotServeListByRegionIdCache(Long regionId);

    /**
     * 根据serveId查询服务信息
     * @param id
     * @return
     */
    Serve queryServeByIdCache(Long id);


    /**
     * 根据itemId查询服务项信息
     * @param id
     * @return
     */
    ServeItem queryServeItemByIdCache(Long id);

    /**
     * 根据区域id刷新缓存：首页图标、热门服务、服务类型
     * @param regionId
     */
    void refreshRegionRelateCaches(Long regionId);

    void queryActiveRegionListCache();
}
