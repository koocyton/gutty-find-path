package com.doopp.findroute.filter;

import com.doopp.gutty.filter.Filter;
import com.doopp.gutty.filter.FilterChain;
import com.google.inject.Singleton;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

@Singleton
public class WsFilter implements Filter {

    @Override
    public void doFilter(ChannelHandlerContext ctx, FullHttpRequest httpRequest, FullHttpResponse httpResponse, FilterChain filterChain) {

        try {
            filterChain.doFilter(ctx, httpRequest, httpResponse);
        }
        catch (Exception e) {
            e.printStackTrace();
            ctx.channel().writeAndFlush(new TextWebSocketFrame(e.getMessage()));
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            ctx.channel().close();
        }
    }
}
