package com.sb.cmpptohttp.service.impl;


import com.sb.cmpptohttp.cache.ChannelCache;
import com.sb.cmpptohttp.domain.dto.MessageDTO;
import com.sb.cmpptohttp.domain.enums.ProtocolTypeEnum;
import com.sb.cmpptohttp.domain.result.Result;
import com.sb.cmpptohttp.entity.Channel;
import com.sb.cmpptohttp.service.SmsService;
import com.sb.cmpptohttp.service.SendMsgService;
import com.zx.sms.BaseMessage;
import com.zx.sms.codec.cmpp.msg.CmppSubmitRequestMessage;
import com.zx.sms.codec.sgip12.msg.SgipSubmitRequestMessage;
import com.zx.sms.codec.smgp.msg.SMGPSubmitMessage;
import com.zx.sms.codec.smpp.Address;
import com.zx.sms.codec.smpp.msg.SubmitSm;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.marre.sms.SmppSmsDcs;
import org.marre.sms.SmsAlphabet;
import org.marre.sms.SmsMsgClass;
import org.marre.sms.SmsTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.sb.cmpptohttp.domain.Constants.MAX_SRC_ID_LENGTH;

/**
 * 短信发送服务
 *
 * @author chenqiang
 */
@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Autowired
    private ChannelCache channelCache;

    @Autowired
    private ChannelUtil channelUtil;

    @Autowired
    private SendMsgService sendMsgService;



    @Override
    public Result sendMsg(Long mid, String channelNo, String mobile, String content, String extend)
            throws Exception {

        log.info("sendMsg channelNo:{}, mid:{}, mobile:{}, content:{}, extend:{} ", channelNo, mid, mobile, content, extend);

        // TODO：可以改为异步，改为异步时需要需要考虑短信记录先保存，还是状态报告先保存的情况
        sendMsgService.SaveSendMsg(mid, channelNo, mobile, content, extend);

        // 根据channelNo获取通道信息
        EndpointEntity entity = EndpointManager.INS.getEndpointEntity(channelNo);
        if (entity == null || !entity.isValid() || entity.getSingletonConnector().getConnectionNum() < 1) {
            return Result.buildFaild(-1, "通道无效");
        }

        // 从通道缓存中获取通道信息
        Channel channel = channelCache.getChannelByChannelNo(channelNo);

        if (channel == null) {
            log.error("chan not found, channelNo: {}", channelNo);
            return Result.buildFaild(-1, "通道不存在");
        }

        /**
         * 封装短信发送消息
         */
        BaseMessage submitMsg = getSubmitMsg(mobile, content, extend, channel);

        long start = System.currentTimeMillis();

        MessageDTO messageDTO = new MessageDTO(mid, mobile, extend, content);

        // 因为长短信要拆分，因此返回一个promiseList.每个拆分后的短信对应一个promise
        List<Promise<BaseMessage>> futures = null;
        futures = channelUtil.syncWriteLongMsgToEntity(channelNo, submitMsg, messageDTO);

        Promise<BaseMessage> frefuture = null;

        if (futures != null) {
            try {
                for (Promise<BaseMessage> future : futures) {
                    future.addListener(new GenericFutureListener<Future<BaseMessage>>() {
                        @Override
                        public void operationComplete(Future<BaseMessage> future) throws Exception {
                            if (future.isSuccess()) {
                                log.info("SubmitRespMessage: {}", future.get());
                            } else {
                                log.error("submit excep", future.get());
                            }
                        }

                    });

                    frefuture = future;
                }

            } catch (Exception e) {
                log.error("sendMsg exception: ", e);
            }
        } else {
            //连接不可写了，等待上一个response回来
            //再把消息发出去
            io.netty.channel.Channel nettyChannel = entity.getSingletonConnector().fetch();
            if (nettyChannel != null) {
                nettyChannel.writeAndFlush(submitMsg);

                if (frefuture != null) {
                    frefuture.sync();
                    frefuture = null;
                }
            } else {
                log.error("channel is null, entity: {}, msg: {}", entity, submitMsg);
            }
        }
        long checkPoint = System.currentTimeMillis();
        log.info("checkPoint cost: {}", (checkPoint - start));
        return Result.buildSucc();
    }

    /**
     * 获取短信提交数据
     *
     * @return
     */
    private BaseMessage getSubmitMsg(String mobile, String content, String extend, Channel chan) {
        if (chan.getProtocol().equals(ProtocolTypeEnum.CMPP20.code) || chan.getProtocol().equals(ProtocolTypeEnum.CMPP30.code)) {
            CmppSubmitRequestMessage msg = new CmppSubmitRequestMessage();
            if (StringUtils.isBlank(extend)) {
                msg.setSrcId(chan.getSrcId());
            } else {
                if ((chan.getSrcId() + extend).length() > MAX_SRC_ID_LENGTH) {
                    msg.setSrcId((chan.getSrcId() + extend).substring(0, MAX_SRC_ID_LENGTH));
                } else {
                    msg.setSrcId((chan.getSrcId() + extend));
                }
            }
            msg.setMsgContent(content);
            msg.setRegisteredDelivery((short) 1);
            msg.setServiceId(chan.getSrcId());
            msg.setDestterminalId(mobile);

            return msg;
        } else if (chan.getProtocol().equals(ProtocolTypeEnum.SGIP.code)) {
            SgipSubmitRequestMessage requestMessage = new SgipSubmitRequestMessage();

            if (StringUtils.isBlank(extend)) {
                requestMessage.setSpnumber(chan.getSrcId());
            } else {
                if ((chan.getSrcId() + extend).length() > MAX_SRC_ID_LENGTH) {
                    requestMessage.setSpnumber((chan.getSrcId() + extend).substring(0, MAX_SRC_ID_LENGTH));
                } else {
                    requestMessage.setSpnumber(chan.getSrcId() + extend);
                }
            }

            requestMessage.setUsernumber(mobile);
            requestMessage.setMsgContent(content);
            requestMessage.setReportflag((short) 0);
            return requestMessage;

        } else if (chan.getProtocol().equals(ProtocolTypeEnum.SMGP.code)) {
            SMGPSubmitMessage pdu = new SMGPSubmitMessage();
            if (StringUtils.isBlank(extend)) {
                pdu.setSrcTermId(chan.getSrcId());
            } else {
                if ((chan.getSrcId() + extend).length() > MAX_SRC_ID_LENGTH) {
                    pdu.setSrcTermId((chan.getSrcId() + extend).substring(0, MAX_SRC_ID_LENGTH));
                } else {
                    pdu.setSrcTermId(chan.getSrcId() + extend);
                }
            }

            pdu.setDestTermIdArray(mobile);
            pdu.setMsgContent(content);

            return pdu;
        } else if (chan.getProtocol().equals(ProtocolTypeEnum.SMPP.code)) {
            SubmitSm pdu = new SubmitSm();

            String srcId = "";
            if (StringUtils.isBlank(extend)) {
                srcId = chan.getSrcId();
            } else {
                if ((chan.getSrcId() + extend).length() > MAX_SRC_ID_LENGTH) {
                    srcId = (chan.getSrcId() + extend).substring(0, MAX_SRC_ID_LENGTH);
                } else {
                    srcId = chan.getSrcId() + extend;
                }
            }

            pdu.setSourceAddress(new Address((byte) 0, (byte) 0, srcId));
            pdu.setDestAddress(new Address((byte) 0, (byte) 0, mobile));
            pdu.setSmsMsg(new SmsTextMessage(content, SmppSmsDcs.getGeneralDataCodingDcs(SmsAlphabet.GSM,
                    SmsMsgClass.CLASS_UNKNOWN)));

            return pdu;
        }

        return null;
    }


}
