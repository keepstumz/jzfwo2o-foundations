package com.jzo2o.foundations.handler;

import com.jzo2o.canal.listeners.AbstractCanalRabbitMqMsgListener;
import com.jzo2o.es.core.ElasticSearchTemplate;
import com.jzo2o.foundations.constants.IndexConstants;
import com.jzo2o.foundations.model.domain.ServeSync;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Mushengda
 * @version 1.0
 * @time 2024-04-05 22:20
 * 实现服务信息的同步程序，继承一个抽象类，重写新增和删除方法
 */

@Component
@Slf4j
public class ServeCanalDataSyncHandler extends AbstractCanalRabbitMqMsgListener<ServeSync> {

    @Resource
    private ElasticSearchTemplate elasticSearchTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "canal-mq-jzo2o-foundations", arguments={@Argument(name="x-single-active-consumer", value = "true", type = "java.lang.Boolean")}), // 绑定的队列
            exchange = @Exchange(name = "exchange.canal-jzo2o", type = ExchangeTypes.TOPIC), //绑定的交换机
            key = "canal-mq-jzo2o-foundations"), //routingKey
            concurrency = "1" //指定队列的消息但单线程
    )
    public void onMessage(Message message) throws Exception{
        parseMsg(message); // 程序的开头,相当于解析消息并执行对应的方法,这个方法一直被rabbit监听
    }


    /**
     * 向es文档增加/修改索引文档
     * @param data
     */
    @Override
    public void batchSave(List<ServeSync> data) {
        Boolean res = elasticSearchTemplate.opsForDoc().batchInsert(IndexConstants.SERVE, data);
        if(!res){
            // 新增或修改失败则重新发送
            try {
                Thread.sleep(1000);
            }catch (InterruptedException e){
                throw new RuntimeException(e);
            }
            throw new RuntimeException("同步失败");
        }
    }

    /**
     * 向es文档删除索引文档
     * @param ids
     */
    @Override
    public void batchDelete(List<Long> ids) {
        Boolean res = elasticSearchTemplate.opsForDoc().batchDelete(IndexConstants.SERVE, ids);
        if(!res){
            // 删除失败则重新发送
            try {
                Thread.sleep(1000);
            }catch (InterruptedException e){
                throw new RuntimeException(e);
            }
            throw new RuntimeException("同步失败");
        }
    }
}
