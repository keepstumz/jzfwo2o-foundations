package com.jzo2o.foundations.controller.consumer;

import com.jzo2o.foundations.model.dto.response.ServeAggregationSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeAggregationTypeSimpleResDTO;
import com.jzo2o.foundations.model.dto.response.ServeCategoryResDTO;
import com.jzo2o.foundations.model.dto.response.ServeSimpleResDTO;
import com.jzo2o.foundations.service.HomeService;
import com.jzo2o.foundations.service.IServeService;
import com.jzo2o.foundations.service.ServeAggregationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Mushengda
 * @version 1.0
 * @time 2024-04-05 16:42
 * 用户端小程序首页门户服务
 */

@RestController("consumerFirstPageServeController")
@RequestMapping("/customer/serve")
@Slf4j
@Api(tags = "用户端 - 首页相关接口")
public class FirstPageServeController {

    @Resource
    private HomeService homeService;
    @Resource
    private IServeService serveService;
    @Resource
    private ServeAggregationService serveAggregationService;


    @GetMapping("/firstPageServeList")
    @ApiOperation("用户端首页门户查询")
    public List<ServeCategoryResDTO> firstPageQueryByRegionId(@RequestParam("regionId") Long regionId){
        return homeService.firstPageQueryByRegionId(regionId);
    }

    @GetMapping("/serveTypeList")
    @ApiOperation("服务类型列表查询")
    public List<ServeAggregationTypeSimpleResDTO> serveTypeListQueryByRegionId(@RequestParam("regionId") Long regionId){
        return homeService.serveTypeListQueryByRegionId(regionId);
    }

    @GetMapping("/hotServeList")
    @ApiOperation("首页热门服务列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "regionId", value = "区域id", required = true, dataTypeClass = Long.class)
    })
    public List<ServeAggregationSimpleResDTO> listHotServe(@NotNull(message = "regionId不能为空") @RequestParam("regionId") Long regionId) {
        return homeService.findHotServeListByRegionIdCache(regionId);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询服务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "服务id", required = true, dataTypeClass = Long.class)
    })
    public ServeAggregationSimpleResDTO findById(@NotNull(message = "id不能为空") @PathVariable("id") Long id) {
        return serveService.findDetailById(id);
    }

    @GetMapping("/search")
    @ApiOperation("首页服务搜索")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cityCode", value = "城市编码", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "serveTypeId", value = "服务类型id", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "keyword", value = "关键词", dataTypeClass = String.class)
    })
    public List<ServeSimpleResDTO> findServeList(@RequestParam("cityCode") String cityCode,
                                                 @RequestParam(value = "serveTypeId", required = false) Long serveTypeId,
                                                 @RequestParam(value = "keyword", required = false) String keyword) {
        return serveAggregationService.findServeList(cityCode, serveTypeId, keyword);
    }
}
