/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tatsinktech.extend;

import com.tatsinktech.extend.beans.Process_Request;
import com.tatsinktech.extend.config.Load_Configuration;
import com.tatsinktech.extend.thread.process.ExtendProcess;
import com.tatsinktech.extend.thread.sender.Sender;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author olivier.tatsinkou
 */
@Component
public class ApplicationLaucher implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(ApplicationLaucher.class);

    @Autowired
    private Load_Configuration commonConfig;

    @Autowired
    ConfigurableApplicationContext ConfAppContext;

    private static BlockingQueue<Process_Request> send_queue;

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("******************* InitializingBean *******************************");

        int send_thread_num = Integer.parseInt(commonConfig.getApplicationSenderNumberThread());
        int send_thread_pool = Integer.parseInt(commonConfig.getApplicationSenderThreadPool());
        int send_maxQueue = Integer.parseInt(commonConfig.getApplicationSenderMaxQueue());

        int extend_num = Integer.parseInt(commonConfig.getApplicationExtendNumberThread());
        int extend_pool = Integer.parseInt(commonConfig.getApplicationExtendThreadPool());

        send_queue = new ArrayBlockingQueue<>(send_maxQueue);

        List<Runnable> sender_runnables = new ArrayList<Runnable>();
        List<Runnable> extend_runnables = new ArrayList<Runnable>();

        // sender
        Sender.setSend_queue(send_queue);
        for (int i = 0; i < send_thread_num; i++) {
            Sender senderThread = (Sender) ConfAppContext.getBean(Sender.class);
            sender_runnables.add(senderThread);
        }
        ExecutorService send_Execute = Executors.newFixedThreadPool(send_thread_pool);
        Sender.executeRunnables(send_Execute, sender_runnables);
        ExecutorService process_Execute_extend = null;
        // process reg
        if (extend_num < 1) {
            ExtendProcess regProcessThread = (ExtendProcess) ConfAppContext.getBean(ExtendProcess.class);
            extend_runnables.add(regProcessThread);
            process_Execute_extend = Executors.newFixedThreadPool(1);
        } else {
            for (int i = 0; i < extend_num; i++) {
                ExtendProcess regProcessThread = (ExtendProcess) ConfAppContext.getBean(ExtendProcess.class);
                regProcessThread.setThreadID(i);
                extend_runnables.add(regProcessThread);
            }
            process_Execute_extend = Executors.newFixedThreadPool(extend_pool);
        }
        ExtendProcess.executeRunnables(process_Execute_extend, extend_runnables);
    }

}
