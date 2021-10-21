package com.doopp.findroute;

import com.doopp.gutty.Gutty;
import com.doopp.gutty.json.JacksonMessageConverter;
import com.google.inject.*;
import com.google.inject.name.Named;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import com.google.inject.Module;

public class MVCApplication {

    public static void main(String[] args) {
        new Gutty()
            .loadProperties(args)
            .setBasePackages("com.doopp.findroute")
            .setMessageConverter(JacksonMessageConverter.class)
            // .addFilter("/ws", WsFilter.class)
            .addModules(
                new Module() {
                    @Override
                    public void configure(Binder binder) {
                    }

                    @Singleton
                    @Provides
                    @Named("bossEventLoopGroup")
                    public EventLoopGroup bossEventLoopGroup() {
                        return new NioEventLoopGroup();
                    }

                    @Singleton
                    @Provides
                    @Named("workerEventLoopGroup")
                    public EventLoopGroup workerEventLoopGroup() {
                        return new NioEventLoopGroup();
                    }

//                    @Singleton
//                    @Provides
//                    public IdWorker idWorker(@Named("idWorker.workerId") long workerId, @Named("idWorker.dataCenterId") long dataCenterId) {
//                        return new IdWorker(workerId, dataCenterId);
//                    }
                }
            )
            // .addInjectorConsumer(injector->{
            //    ScheduledExecutorService newScheduledThreadPool = Executors.newScheduledThreadPool(2);
            //    newScheduledThreadPool.scheduleWithFixedDelay(injector.getInstance(TestTask.class), 4, 180, TimeUnit.SECONDS);
            // })
            .start();
    }
}
