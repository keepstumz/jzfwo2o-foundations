package com.jzo2o.foundations.service;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.model.domain.Serve;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 服务表 服务类
 * </p>
 *
 * @author msd
 * @since 2024-04-02
 */
public interface IServeService extends IService<Serve> {
    

    PageResult<ServeResDTO> page(ServePageQueryReqDTO servePageQueryReqDTO);

    void batchAdd(List<ServeUpsertReqDTO> serveUpsertReqDTOList);

    Serve updatePrice(Long id, BigDecimal price);

    Serve onSale(Long id);

    Serve offSale(Long id);

    Serve onHot(Long id);

    Serve offHot(Long id);

    int queryServeCountByRegionIdAndSaleStatus(Long id, int status);

    int queryServeCountByServeItemIdAndSaleStatus(Long id, int status);
}
