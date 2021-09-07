package com.sb.cmpptohttp.startup;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sb.cmpptohttp.domain.enums.ProtocolTypeEnum;
import com.sb.cmpptohttp.entity.Channel;
import com.sb.cmpptohttp.handler.CMPPSessionConnectedHandler;
import com.sb.cmpptohttp.handler.SGIPSessionConnectedHandler;
import com.sb.cmpptohttp.handler.SMGPSessionConnectedHandler;
import com.sb.cmpptohttp.handler.SMPPSessionConnectedHandler;
import com.sb.cmpptohttp.mapper.ChannelMapper;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.connect.manager.ServerEndpoint;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.connect.manager.sgip.SgipClientEndpointEntity;
import com.zx.sms.connect.manager.sgip.SgipServerChildEndpointEntity;
import com.zx.sms.connect.manager.sgip.SgipServerEndpointEntity;
import com.zx.sms.connect.manager.smgp.SMGPClientEndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPClientEndpointEntity;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 程序启动的时候，加载通道信息
 *
 */
@Component
@Slf4j
@DependsOn("springContextUtils")
@Order(99)
public class ChanStartUp {

    @Resource
    ChannelMapper channelMapper;

    /**
     * 系统连接的统一管理器
     */
    final EndpointManager manager = EndpointManager.INS;

    @Autowired
    private CMPPSessionConnectedHandler cmppSessionConnectedHandler;

    @Autowired
    private SGIPSessionConnectedHandler sgipSessionConnectedHandler;

    @Autowired
    private SMGPSessionConnectedHandler smgpSessionConnectedHandler;

    @Autowired
    private SMPPSessionConnectedHandler smppSessionConnectedHandler;

    /**
     * 初始化通道
     */
    @PostConstruct
    private void initChannel() throws Exception {

        log.info("start init channel");

        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("enabled", 1);
        // 协议类型，0：移动cmpp20，1：移动cmpp30，2：sgip联通，3：电信smgp，5：SMPP协议
        queryWrapper.in("protocol", Arrays.asList(0, 1, 2, 3, 4));

        List<Channel> smsChannelList = channelMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(smsChannelList)) {
            log.warn("smsChannelList empty");
            return;
        }

        log.info("smsChannelList: {}", JSONUtil.toJsonStr(smsChannelList));

        // SGIP协议比较特殊，状态报告信息不是通过长连接发送过来，是我们这边新开一个sgip服务端，运营商那边连接我们的服务端，把状态报告推送给我们
        addSgipServer();

        for (Channel channel : smsChannelList) {

            EndpointEntity client = null;
            if (ProtocolTypeEnum.CMPP20.code.equals(channel.getProtocol())
                    || ProtocolTypeEnum.CMPP30.code.equals(channel.getProtocol())) {
                client = getCmppClient(channel);
            } else if (ProtocolTypeEnum.SGIP.code.equals(channel.getProtocol())) {
                client = getSgipClient(channel);

                addSGIPServerChildClient(channel);
            } else if (ProtocolTypeEnum.SMGP.code.equals(channel.getProtocol())) {
                client = getSmgpClient(channel);
            } else if (ProtocolTypeEnum.SMPP.code.equals(channel.getProtocol())) {
                client = getSmppClient(channel);
            }
            // 添加到系统连接的统一管理器
            manager.addEndpointEntity(client);
        }

        manager.openAll();

        /**
         * 开启心跳连接检测
         */
        manager.startConnectionCheckTask();
    }

    /**
     * SGIP 服务端Child添加
     *
     * @param channel
     */
    private void addSGIPServerChildClient(Channel channel) {
        // 以协议类型作为ServerEndpoint的id
        ServerEndpoint serverEndpoint = (ServerEndpoint) EndpointManager.INS
                .getEndpointEntity(ProtocolTypeEnum.SGIP.name);

        EndpointEntity child = serverEndpoint.getChild(channel.getLoginName());

        // 如果不为空，先移除
        if (child != null) {
            //关闭所有该账户的连接
            EndpointManager.INS.close(child);
            serverEndpoint.removechild(child);
        }
        child = newSGIPServerChildEndpoint(channel.getLoginName(), channel.getPassword(),
                channel.getSpeed(), channel.getMaxConnect());

        log.info("addSGIPServerChildEndpoint: {}, child: {}", JSONUtil.toJsonStr(channel),
                JSONUtil.toJsonStr(child));
        serverEndpoint.addchild(child);
    }

    /**
     * new SGIPServerChildEndpoint
     *
     * @param userName
     * @param password
     * @param speed
     * @param maxConnect
     * @return
     */
    public EndpointEntity newSGIPServerChildEndpoint(String userName, String password, Integer speed,
                                                     Integer maxConnect) {
        EndpointEntity endpointEntity = getSGIPServerChildEndpoint(userName, password);

        endpointEntity.setValid(true);
        endpointEntity.setChannelType(EndpointEntity.ChannelType.DUPLEX);

        // 设置最大连接数
        endpointEntity.setMaxChannels(Convert.toShort(maxConnect));
        endpointEntity.setRetryWaitTimeSec((short) 30);
        endpointEntity.setMaxRetryCnt((short) 3);
        endpointEntity.setReSendFailMsg(false);
        endpointEntity.setIdleTimeSec((short) 60);

        // 设置单连接写入速度
        endpointEntity.setWriteLimit(speed);

        return endpointEntity;
    }

    /**
     * 获取服务器子通道
     *
     * @param userName
     * @param password
     * @return
     */
    private EndpointEntity getSGIPServerChildEndpoint(String userName, String password) {
        SgipServerChildEndpointEntity child = new SgipServerChildEndpointEntity();
        child.setId(userName);
        child.setLoginName(userName);
        child.setLoginPassowrd(password);

        List<BusinessHandlerInterface> serverHandlers = new ArrayList<BusinessHandlerInterface>();

        serverHandlers.add(new SGIPSessionConnectedHandler());
        child.setBusinessHandlerSet(serverHandlers);
        return child;
    }

    /**
     * 获取CMPP 客户端信息
     *
     * @param channel
     * @return
     */
    private CMPPClientEndpointEntity getCmppClient(Channel channel) {
        // 开始连接CMPP
        CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
        client.setId(channel.getChannelNo());
        client.setHost(channel.getChannelIp());
        client.setPort(channel.getPort());
        client.setChartset(Charset.forName("utf-8"));
        client.setGroupName(channel.getChannelNo());
        client.setUserName(channel.getLoginName());
        client.setPassword(channel.getPassword());
        client.setSpCode(channel.getSrcId());
        client.setMsgSrc(channel.getMsgSrc());
        // 最大连接数
        client.setMaxChannels(Convert.toShort(channel.getMaxConnect()));
        client.setCloseWhenRetryFailed(false);

        // CMPP协议版本，默认为3.0协议
        if (channel.getProtocol().equals(ProtocolTypeEnum.CMPP30.code)) {
            client.setVersion((short) 0x30);
        } else if (channel.getProtocol().equals(ProtocolTypeEnum.CMPP20.code)) {
            client.setVersion((short) 0x20);
        }
        client.setRetryWaitTimeSec((short) 30);
        client.setUseSSL(false);

        // 设置限速
        client.setWriteLimit(channel.getSpeed());

        // 默认不重发消息
        client.setReSendFailMsg(false);
        client.setSupportLongmsg(EndpointEntity.SupportLongMessage.BOTH);

        List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
        clienthandlers.add(cmppSessionConnectedHandler);
        client.setBusinessHandlerSet(clienthandlers);

        return client;
    }

    /**
     * 获取SMGP客户端
     *
     * @param channel
     * @return
     */
    private SMGPClientEndpointEntity getSmgpClient(Channel channel) {

        SMGPClientEndpointEntity client = new SMGPClientEndpointEntity();
        client.setId(channel.getChannelNo());
        client.setHost(channel.getChannelIp());
        client.setPort(channel.getPort());
        client.setClientID(channel.getLoginName());
        client.setPassword(channel.getPassword());
        client.setChannelType(EndpointEntity.ChannelType.DUPLEX);

        client.setMaxChannels(Convert.toShort(channel.getMaxConnect()));
        client.setRetryWaitTimeSec((short) 100);
        client.setUseSSL(false);
        client.setReSendFailMsg(false);
        client.setClientVersion((byte) 0x13);
        client.setCloseWhenRetryFailed(false);

        // 设置限速
        client.setWriteLimit(channel.getSpeed());

        List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();

        clienthandlers.add(smgpSessionConnectedHandler);
        client.setBusinessHandlerSet(clienthandlers);

        return client;
    }

    /**
     * 获取SMPP客户端
     *
     * @param channel
     * @return
     */
    private SMPPClientEndpointEntity getSmppClient(Channel channel) {

        SMPPClientEndpointEntity client = new SMPPClientEndpointEntity();
        client.setId(channel.getChannelNo());
        client.setHost(channel.getChannelIp());
        client.setPort(channel.getPort());
        client.setSystemId(channel.getLoginName());
        client.setPassword(channel.getPassword());
        client.setChannelType(EndpointEntity.ChannelType.DUPLEX);

        client.setMaxChannels(Convert.toShort(channel.getMaxConnect()));
        client.setRetryWaitTimeSec((short) 100);
        client.setUseSSL(false);
        client.setReSendFailMsg(false);
        client.setWriteLimit(channel.getSpeed());
        client.setCloseWhenRetryFailed(false);

        // 接收长短信时不自动合并
        client.setSupportLongmsg(EndpointEntity.SupportLongMessage.SEND);
        List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
        clienthandlers.add(smppSessionConnectedHandler);
        client.setBusinessHandlerSet(clienthandlers);

        return client;
    }

    /**
     * 获取SGIP客户端
     *
     * @param channel
     * @return
     */
    private SgipClientEndpointEntity getSgipClient(Channel channel) {

        SgipClientEndpointEntity client = new SgipClientEndpointEntity();
        client.setId(channel.getChannelNo());
        client.setHost(channel.getChannelIp());
        client.setPort(channel.getPort());
        client.setLoginName(channel.getLoginName());
        client.setLoginPassowrd(channel.getPassword());
        client.setChannelType(EndpointEntity.ChannelType.DUPLEX);
        client.setNodeId(channel.getId());
        client.setMaxChannels(Convert.toShort(channel.getMaxConnect()));
        client.setRetryWaitTimeSec((short) 100);
        client.setUseSSL(false);
        client.setReSendFailMsg(false);
        client.setIdleTimeSec((short) 120);
        client.setCloseWhenRetryFailed(false);
        client.setWriteLimit(channel.getSpeed());
        List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
//        clienthandlers.add(new SgipReportRequestMessageHandler());
        clienthandlers.add(sgipSessionConnectedHandler);
        client.setBusinessHandlerSet(clienthandlers);

        return client;
    }

    /**
     * SGIP 服务端添加
     */
    private void addSgipServer() {
        SgipServerEndpointEntity sgipServerEndpointEntity = getSgipServer();
        manager.addEndpointEntity(sgipServerEndpointEntity);
    }

    /**
     * SGIP 服务端获取
     *
     * @return
     */
    public SgipServerEndpointEntity getSgipServer() {
        SgipServerEndpointEntity server = new SgipServerEndpointEntity();
        server.setId(ProtocolTypeEnum.SGIP.name);
        server.setHost("0.0.0.0");
        server.setPort(8806);
        server.setValid(true);
        server.setIdleTimeSec((short) 60);
        //使用ssl加密数据流
        server.setUseSSL(false);
        return server;
    }
}
