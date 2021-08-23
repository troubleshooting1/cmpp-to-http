package com.sb.cmpptohttp.handler;


import com.zx.sms.handler.api.AbstractBusinessHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;


public abstract class SessionConnectedHandler extends AbstractBusinessHandler {

  private static final Logger logger = LoggerFactory.getLogger(SessionConnectedHandler.class);

  protected AtomicInteger totleCnt = new AtomicInteger(10);

  public AtomicInteger getTotleCnt() {
    return totleCnt;
  }

  public void setTotleCnt(AtomicInteger totleCnt) {
    this.totleCnt = totleCnt;
  }

  public SessionConnectedHandler() {
  }

  public SessionConnectedHandler(AtomicInteger t) {
    totleCnt = t;
  }

  @Override
  public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {

    ctx.fireUserEventTriggered(evt);
  }

  @Override
  public String name() {
    return "SessionConnectedHandler-Gate";
  }

  @Override
  public SessionConnectedHandler clone() throws CloneNotSupportedException {
    SessionConnectedHandler ret = (SessionConnectedHandler) super.clone();
    return ret;
  }

}
