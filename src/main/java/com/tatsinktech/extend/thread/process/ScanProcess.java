/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tatsinktech.extend.thread.process;


import com.tatsinktech.extend.model.repository.Mo_ExtendRepository;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author olivier.tatsinkou
 */
@Component
public class ScanProcess {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private Mo_ExtendRepository loadRepo;

    @Scheduled(fixedDelayString  = "${application.extend.scheduler-fixedDelay}")
    public void scheduleTaskWithFixedDelay() {
        logger.info("############## LOAD MO_EXTEND #########################");
        loadRepo.loadMoExtend(new Date());
    }
}
