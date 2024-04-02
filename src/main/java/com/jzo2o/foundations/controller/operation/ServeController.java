package com.jzo2o.foundations.controller.operation;

import com.jzo2o.api.foundations.dto.response.ServeItemResDTO;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.model.dto.request.ServeItemPageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import com.jzo2o.foundations.service.IServeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Mushengda
 * @version 1.0
 * @time 2024-04-02 13:54
 */

@RestController("operationServeController")
@RequestMapping("/operation/serve")
@Slf4j
@Api(tags = "运营端-区域服务相关接口")
public class ServeController {

    @Resource IServeService iServeService;
    @GetMapping("/page")
    @ApiOperation("区域服务项分页查询")
    public PageResult<ServeResDTO> page(ServePageQueryReqDTO servePageQueryReqDTO) {
        return iServeService.page(servePageQueryReqDTO);
    }
}
