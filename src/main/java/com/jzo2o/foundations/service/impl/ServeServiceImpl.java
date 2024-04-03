package com.jzo2o.foundations.service.impl;

import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.expcetions.ForbiddenOperationException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.common.utils.ObjectUtils;
import com.jzo2o.foundations.FoundationsApplication;
import com.jzo2o.foundations.enums.FoundationIsHotEnum;
import com.jzo2o.foundations.enums.FoundationStatusEnum;
import com.jzo2o.foundations.mapper.RegionMapper;
import com.jzo2o.foundations.mapper.ServeItemMapper;
import com.jzo2o.foundations.model.domain.Region;
import com.jzo2o.foundations.model.domain.Serve;
import com.jzo2o.foundations.mapper.ServeMapper;
import com.jzo2o.foundations.model.domain.ServeItem;
import com.jzo2o.foundations.model.dto.request.ServePageQueryReqDTO;
import com.jzo2o.foundations.model.dto.request.ServeUpsertReqDTO;
import com.jzo2o.foundations.model.dto.response.ServeResDTO;
import com.jzo2o.foundations.service.IServeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.mysql.utils.PageHelperUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 服务表 服务实现类
 * </p>
 *
 * @author msd
 * @since 2024-04-02
 */
@Service
public class ServeServiceImpl extends ServiceImpl<ServeMapper, Serve> implements IServeService {


    @Resource
    private ServeItemMapper serveItemMapper;

    @Resource
    private RegionMapper regionMapper;

    /**
     * 区域服务分页查询接口实现
     * @param servePageQueryReqDTO
     * @return
     */
    @Override
    public PageResult<ServeResDTO> page(ServePageQueryReqDTO servePageQueryReqDTO) {
        return PageHelperUtils.selectPage(servePageQueryReqDTO, () -> baseMapper.queryServeListByRegionId(servePageQueryReqDTO.getRegionId()));
    }

    /**
     * 区域新增服务接口
     * 业务逻辑，当向表中进行增删改的操作时，要进行入参校验，校验失败抛出异常，由异常处理器统一对异常进行处理
     * 要新增服务，先查询该服务是否启用，未启用不能新增
     * @param serveUpsertReqDTOList
     */
    @Override
    @Transactional
    public void batchAdd(List<ServeUpsertReqDTO> serveUpsertReqDTOList) {
        // 根据传参的服务id查询serveItem是否启用
        for (ServeUpsertReqDTO serveUpsertReqDTO : serveUpsertReqDTOList) {
            ServeItem serveItem = serveItemMapper.selectById(serveUpsertReqDTO.getServeItemId());
            if(ObjectUtils.isNull(serveItem) || serveItem.getActiveStatus() != FoundationStatusEnum.ENABLE.getStatus())
                throw new ForbiddenOperationException("该服务未启用无法添加到区域下使用");

            // 检验是否重复添加
            Integer count = lambdaQuery()
                    .eq(Serve::getRegionId, serveUpsertReqDTO.getRegionId())
                    .eq(Serve::getServeItemId, serveUpsertReqDTO.getServeItemId())
                    .count();
            if(count > 0){
                // 有重复，抛出异常
                throw new ForbiddenOperationException(serveItem.getName() + "服务已存在");
            }

            // 新增服务
            Serve serve = BeanUtils.toBean(serveUpsertReqDTO, Serve.class);
            Region region = regionMapper.selectById(serveUpsertReqDTO.getRegionId());
            serve.setCityCode(region.getCityCode());
            baseMapper.insert(serve);
        }
    }

    /**
     * 修改服务的价格，传入serve的主键id并且传入要修改的价格
     * 业务逻辑校验id是否存在，校验价格是否合理
     * @param id
     * @param price
     */
    @Override
    @Transactional
    public Serve updatePrice(Long id, BigDecimal price) {

        boolean update = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getPrice, price)
                .update();

        if(!update){
            throw new CommonException("修改服务价格失败");
        }
        return baseMapper.selectById(id);
    }

    /**
     * 区域服务上架
     * 业务逻辑校验Serve表id是否存在,不存在则报异常
     * 存在查看当前状态是否为草稿或下架,不是则报异常
     * 查看服务项目状态是否启用,未启用则报异常
     * 全都通过则修改该条目状态为enable
     * @param id
     */
    @Override
    @Transactional
    public Serve onSale(Long id) {

        Serve serve = baseMapper.selectById(id);

        if(ObjectUtils.isNull(serve)){
            throw new CommonException("该区域服务项目不存在,禁止上架");
        }

        if(!(serve.getSaleStatus() == FoundationStatusEnum.DISABLE.getStatus() || serve.getSaleStatus() == FoundationStatusEnum.INIT.getStatus())){
            throw new ForbiddenOperationException("服务项目不是草稿或下架状态,禁止上架");
        }

        ServeItem serveItem = serveItemMapper.selectById(serve.getServeItemId());
        if(ObjectUtils.isNull(serveItem)){
            throw new ForbiddenOperationException("所属服务项目不存在");
        }

        if(serveItem.getActiveStatus() != FoundationStatusEnum.ENABLE.getStatus()){
            throw new ForbiddenOperationException(serveItem.getName() + "项目未启用,该区域不能上架");
        }

        boolean update = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getSaleStatus, FoundationStatusEnum.ENABLE.getStatus())
                .update();

        if(!update){
            throw new CommonException("服务上架失败...");
        }

        return baseMapper.selectById(id);

    }

    /**
     * 区域服务下架应该自动取消热门
     * @param id
     * @return
     */
    @Override
    @Transactional
    public Serve offSale(Long id) {

        Serve serve = baseMapper.selectById(id);

        if(ObjectUtils.isNull(serve)){
            throw new CommonException("该区域服务项目不存在,禁止下架");
        }

        if(serve.getSaleStatus() != FoundationStatusEnum.ENABLE.getStatus()) {
            throw new ForbiddenOperationException("服务项目未处于上架状态,禁止下架");
        }

        ServeItem serveItem = serveItemMapper.selectById(serve.getServeItemId());
        if(ObjectUtils.isNull(serveItem)){
            throw new ForbiddenOperationException("所属服务项目不存在");
        }

        boolean update = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getSaleStatus, FoundationStatusEnum.DISABLE.getStatus())
                .set(Serve::getIsHot, FoundationIsHotEnum.NOTHOT.getStatus())
                .update();

        if(!update){
            throw new CommonException("服务下架失败...");
        }

        return baseMapper.selectById(id);
    }

    /**
     * 区域服务设置热门
     * 存在则设置热门,不存咋报出异常,未上架不能设置热门
     * @param id
     * @return
     */
    @Override
    public Serve onHot(Long id) {

        Serve serve = baseMapper.selectById(id);

        if(ObjectUtils.isNull(serve)){
            throw new CommonException("该区域服务项目不存在,禁止设置热门");
        }

        if(serve.getSaleStatus() != FoundationStatusEnum.ENABLE.getStatus()) {
            throw new ForbiddenOperationException("服务项目未处于上架状态,禁止设置热门");
        }

        if(serve.getIsHot() != FoundationIsHotEnum.NOTHOT.getStatus()){
            throw new ForbiddenOperationException("服务不处于非热门状态,禁止设置热门");
        }

        ServeItem serveItem = serveItemMapper.selectById(serve.getServeItemId());
        if(ObjectUtils.isNull(serveItem)){
            throw new ForbiddenOperationException("所属服务项目不存在,禁止设置热门");
        }

        boolean update = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getIsHot, FoundationIsHotEnum.HOT.getStatus())
                .set(Serve::getHotTimeStamp, System.currentTimeMillis())
                .update();

        if(!update){
            throw new CommonException("服务设置热门失败...");
        }

        return baseMapper.selectById(id);
    }

    /**
     * 区域服务取消热门,必须是热门才能取消热门
     * @param id
     * @return
     */
    @Override
    public Serve offHot(Long id) {

        Serve serve = baseMapper.selectById(id);

        if(ObjectUtils.isNull(serve)){
            throw new CommonException("该区域服务项目不存在,禁止取消热门");
        }

        if(serve.getSaleStatus() != FoundationStatusEnum.ENABLE.getStatus()) {
            throw new ForbiddenOperationException("服务项目未处于上架状态,禁止取消热门");
        }

        if(serve.getIsHot() != FoundationIsHotEnum.HOT.getStatus()){
            throw new ForbiddenOperationException("服务不处于热门状态,禁止取消热门");
        }

        ServeItem serveItem = serveItemMapper.selectById(serve.getServeItemId());
        if(ObjectUtils.isNull(serveItem)){
            throw new ForbiddenOperationException("所属服务项目不存在,禁止取消热门");
        }

        boolean update = lambdaUpdate()
                .eq(Serve::getId, id)
                .set(Serve::getIsHot, FoundationIsHotEnum.NOTHOT.getStatus())
                .set(Serve::getHotTimeStamp, System.currentTimeMillis())
                .update();

        if(!update){
            throw new CommonException("服务取消热门失败...");
        }

        return baseMapper.selectById(id);
    }

    /**
     * 根据区域id和服务状态查询数量
     * @param id
     * @param status
     * @return
     */
    @Override
    public int queryServeCountByRegionIdAndSaleStatus(Long id, int status) {
        return lambdaQuery()
                .eq(Serve::getRegionId, id)
                .eq(Serve::getSaleStatus, status)
                .count();
    }

    /**
     * 根据服务项id和服务状态查询数量
     * @param id
     * @param status
     * @return
     */
    @Override
    public int queryServeCountByServeItemIdAndSaleStatus(Long id, int status) {
        return lambdaQuery()
                .eq(Serve::getServeItemId, id)
                .eq(Serve::getSaleStatus, status)
                .count();
    }
}
