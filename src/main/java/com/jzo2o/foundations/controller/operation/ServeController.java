package com.jzo2o.foundations.controller.operation;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import com.jzo2o.foundations.service.IServeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

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

    @PostMapping("/batch")
    @ApiOperation("批量添加区域服务")
    public void batchAdd(@RequestBody List<ServeUpsertReqDTO> serveUpsertReqDTOList){
        iServeService.batchAdd(serveUpsertReqDTOList);
    }

    @PutMapping("/{id}")
    @ApiOperation("修改服务价格")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "服务id", required = true, dataTypeClass = Long.class),
            @ApiImplicitParam(name = "price", value = "服务price", required = true, dataTypeClass = BigDecimal.class)
    })
    public void updatePrice(@PathVariable(name = "id") Long id, @RequestParam(name = "price") BigDecimal price){
        iServeService.updatePrice(id, price);
    }

    @PutMapping("/onSale/{id}")
    @ApiOperation("区域服务上架")
    public void onSale(@PathVariable(name = "id") Long id){
        iServeService.onSale(id);
    }

    @PutMapping("/offSale/{id}")
    @ApiOperation("区域服务下架")
    public void offSale(@PathVariable(name = "id") Long id){
        iServeService.offSale(id);
    }

    @PutMapping("/onHot/{id}")
    @ApiOperation("区域服务设置热门")
    public void onHot(@PathVariable(name = "id") Long id){
        iServeService.onHot(id);
    }

    @PutMapping("/offHot/{id}")
    @ApiOperation("区域服务设置热门")
    public void offHot(@PathVariable(name = "id") Long id){
        iServeService.offHot(id);
    }
}
