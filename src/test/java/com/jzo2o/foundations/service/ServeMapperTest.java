package com.jzo2o.foundations.service;

import com.jzo2o.foundations.mapper.ServeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author Mushengda
 * @version 1.0
 * @time 2024-04-02 14:31
 */

@SpringBootTest
@Slf4j
public class ServeMapperTest {

    @Resource ServeMapper serveMapper;

//    @Test
//    void test_queryServeListByRegionId(){
//        PageResult<ServeResDTO> serveResDTOPageResult = serveMapper.queryServeListByRegionId(1686303222843662337L);
//        //Assert.notEmpty(serveResDTOPageResult, "列表为空");
//    }
}
