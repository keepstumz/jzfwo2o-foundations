package com.jzo2o.foundations.service;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.model.domain.Serve;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;

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
}
