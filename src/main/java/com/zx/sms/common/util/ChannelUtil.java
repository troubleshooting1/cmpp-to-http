package com.zx.sms.common.util;

import cn.hutool.json.JSONUtil;
import com.sb.cmpptohttp.domain.RedisKey;
import com.sb.cmpptohttp.domain.dto.MessageDTO;
import com.zx.sms.BaseMessage;
import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.cmpp.wap.LongMessageFrameHolder;
import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;
import com.zx.sms.codec.smgp.msg.SMGPSubmitMessage;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.marre.sms.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChannelUtil {

    private static final Logger logger = LoggerFactory.getLogger(ChannelUtil.class);

    @Autowired
    private RedisTemplate redisTemplate;

    public static ChannelFuture asyncWriteToEntity(final EndpointEntity entity, final Object msg) {
        EndpointConnector connector = entity.getSingletonConnector();
        return asyncWriteToEntity(connector, msg, null);
    }

    public static ChannelFuture asyncWriteToEntity(String entity, Object msg) {
        EndpointEntity e = EndpointManager.INS.getEndpointEntity(entity);
        EndpointConnector connector = e.getSingletonConnector();
        return asyncWriteToEntity(connector, msg, null);
    }

    public static ChannelFuture asyncWriteToEntity(final EndpointEntity entity, final Object msg, GenericFutureListener listner) {

        EndpointConnector connector = entity.getSingletonConnector();
        return asyncWriteToEntity(connector, msg, listner);
    }

    public static ChannelFuture asyncWriteToEntity(final String entity, final Object msg, GenericFutureListener listner) {

        EndpointEntity e = EndpointManager.INS.getEndpointEntity(entity);
        EndpointConnector connector = e.getSingletonConnector();
        return asyncWriteToEntity(connector, msg, listner);
    }

    private static ChannelFuture asyncWriteToEntity(EndpointConnector connector, final Object msg, GenericFutureListener listner) {
        if (connector == null || msg == null)
            return null;

        ChannelFuture promise = connector.asynwrite(msg);

        if (promise == null)
            return null;

        if (listner == null) {
            promise.addListener(new GenericFutureListener() {
                @Override
                public void operationComplete(Future future) throws Exception {
                    // ?????????????????????????????????????????????
                    if (!future.isSuccess()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("SendMessage ").append(msg.toString()).append(" Failed. ");
                        logger.error(sb.toString(), future.cause());
                    }
                }
            });

        } else {
            promise.addListener(listner);
        }
        return promise;
    }

    public <T extends BaseMessage> List<Promise<T>> syncWriteLongMsgToEntity(EndpointEntity e, BaseMessage msg,
                                                                             MessageDTO messageDTO) throws Exception {

        EndpointConnector connector = e.getSingletonConnector();
        if (connector == null) return null;

        if (msg instanceof LongSMSMessage) {
            LongSMSMessage<BaseMessage> lmsg = (LongSMSMessage<BaseMessage>) msg;
            if (!lmsg.isReport()) {
                // ???????????????
                SmsMessage msgcontent = lmsg.getSmsMessage();
                List<LongMessageFrame> frameList = LongMessageFrameHolder.INS.splitmsgcontent(msgcontent);

                //??????????????????????????????????????????tcp????????????
                List<BaseMessage> msgs = new ArrayList<BaseMessage>();
                for (LongMessageFrame frame : frameList) {
                    BaseMessage basemsg = lmsg.generateMessage(frame);

                    logger.info("long sequenceId: {}, mobile: {}", basemsg.getSequenceNo(), messageDTO.getMobile());
                    redisTemplate.opsForValue().set(RedisKey.SMS_SEQID_PREFIX
                            + basemsg.getSequenceNo(), JSONUtil.toJsonStr(messageDTO), Duration.ofMinutes(3));

                    msgs.add(basemsg);
                }
                return connector.synwrite(msgs);
            }
        }

        logger.info("signle sequenceId: {}", msg.getSequenceNo());
        Promise promise = connector.synwrite(msg);
        if (promise == null) {
            // ??????????????????????????????,????????????
            return null;
        }
        List<Promise<T>> arrPromise = new ArrayList<Promise<T>>();
        arrPromise.add(promise);
        return arrPromise;
    }

    /**
     * ??????????????????????????? <br/>
     * ?????????????????????????????????????????????????????????????????????BusinessHandler??????write????????????
     */
    public <T extends BaseMessage> List<Promise<T>> syncWriteLongMsgToEntity(String entity, BaseMessage msg,
                                                                             MessageDTO messageDTO) throws Exception {
        EndpointEntity e = EndpointManager.INS.getEndpointEntity(entity);
        if (e == null) {
            logger.warn("EndpointEntity {} is null", entity);
            return null;
        }
        return syncWriteLongMsgToEntity(e, msg, messageDTO);
    }

    /**
     * ???????????????????????? <br/>
     * ???????????????????????????????????????????????????????????????BusinessHandler??????write????????????
     * ????????????Deliver???Submit????????????????????????????????????????????????PDU???????????????
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????
     * ??????????????????????????? syncWriteLongMsgToEntity ??????
     */
    public static <T extends BaseMessage> Promise<T> syncWriteBinaryMsgToEntity(String entity, BaseMessage msg) throws Exception {
        EndpointEntity e = EndpointManager.INS.getEndpointEntity(entity);
        EndpointConnector connector = e.getSingletonConnector();

        Promise<T> promise = connector.synwrite(msg);
        if (promise == null) {
            // ??????????????????????????????,????????????
            return null;
        }

        return promise;
    }
}
