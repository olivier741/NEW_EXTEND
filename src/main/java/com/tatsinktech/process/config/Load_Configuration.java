/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tatsinktech.process.config;

import com.tatsinktech.process.model.register.Command;
import com.tatsinktech.process.model.register.Notification_Conf;
import com.tatsinktech.process.model.register.Product;
import com.tatsinktech.process.model.register.Request_Conf;
import com.tatsinktech.process.model.repository.CommandRepository;
import com.tatsinktech.process.model.repository.Notification_ConfRepository;
import com.tatsinktech.process.model.repository.ProductRepository;
import com.tatsinktech.process.model.repository.Request_ConfRepository;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 *
 * @author olivier
 */
@Component
public class Load_Configuration implements Serializable {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Value("${application.sender.numberThread}")
    private String applicationSenderNumberThread;

    @Value("${application.sender.threadPool}")
    private String applicationSenderThreadPool;

    @Value("${application.sender.sleep-duration}")
    private String applicationSenderSleepDuration;

    @Value("${application.sender.maxQueue}")
    private String applicationSenderMaxQueue;
    

    @Value("${application.extend.numberThread}")
    private String applicationExtendNumberThread;

    @Value("${application.extend.threadPool}")
    private String applicationExtendThreadPool;

    @Value("${application.extend.sleep-duration}")
    private String applicationExtendSleepDuration;

    @Value("${application.extend.maxRegRecord}")
    private String applicationExtendMaxRegRecord;

    @Value("${security.oauth2.client.user-authorization-uri}")
    private String chargingUrl;

    @Value("${security.oauth2.client.access-token-uri}")
    private String chargingUriAuth;

    @Value("${security.oauth2.client.user-authorization-uri}")
    private String chargingUriCharge;

    @Value("${charging.client-name}")
    private String chargingClientName;

    @Value("${charging.password}")
    private String chargingPassword;

    @Value("${charging.ws-management}")
    private String chargingWsManagement;

    @Value("${charging.alias.msisdn}")
    private String chargingAliasMsisdn;

    @Value("${charging.alias.amount}")
    private String chargingAliasAmount;

    @Value("${charging.alias.product}")
    private String chargingAliasProduct;

    @Value("${charging.alias.transaction}")
    private String chargingAliasTransaction;

    @Value("${charging.alias.descripition}")
    private String chargingAliasDescription;

    @Value("${viewApi.url}")
    private String viewApiUrl;

    @Value("${viewApi.client-name}")
    private String viewApiClientName;

    @Value("${viewApi.password}")
    private String viewApiPassword;

    @Value("${viewApi.ws-management}")
    private String viewApiWsManagement;

    @Value("${viewApi.alias.msisdn}")
    private String viewApiAliasMsisdn;

    @Value("${viewApi.alias.descripition}")
    private String viewApiAliasDescription;

    @Value("${spring.kafka.producer.topic}")
    private String producer_topic;

    @Value("${spring.kafka.consumer.topic}")
    private String consumer_topic;

    @Value("${spring.kafka.zookeeper.host}")
    private String zookeeperHosts;

    @Value("${spring.kafka.topic.partitions}")
    private String partitions;

    @Value("${spring.kafka.topic.replication}")
    private String replicationFactor;

    @Value("${spring.kafka.topic.session-timeOut-in-ms}")
    private String sessionTimeOutInMs;

    @Value("${spring.kafka.topic.connection-timeOut-in-ms}")
    private String connectionTimeOutInMs;

    private HashMap<String, Command> SETCOMMAND = new HashMap<String, Command>();
    private HashMap<String, Notification_Conf> SETNOTIFICATION = new HashMap<String, Notification_Conf>();
    private HashMap<String, Product> SETPRODUCT = new HashMap<String, Product>();
    private List<Request_Conf> ListRequest_conf;

    @Autowired
    private CommandRepository commandRepo;

    @Autowired
    private Notification_ConfRepository notifConfRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private Request_ConfRepository requestConfRepo;

    @PostConstruct
    private void init() {
        loadNotificationConf();
        loadCommand();
        loadProduct();
        ListRequest_conf = requestConfRepo.findAll();
//        createTopicIfNotExists(consumer_topic);
//        createTopicIfNotExists(producer_topic);

        logger.info("************** LIST OF POTENTIAL REQUEST **************");
        for (Request_Conf req : ListRequest_conf) {
            logger.info(req.toString());
        }
    }

    public String getApplicationSenderNumberThread() {
        return applicationSenderNumberThread;
    }

    public String getApplicationSenderThreadPool() {
        return applicationSenderThreadPool;
    }

    public String getApplicationSenderSleepDuration() {
        return applicationSenderSleepDuration;
    }

    public String getApplicationSenderMaxQueue() {
        return applicationSenderMaxQueue;
    }

    public String getApplicationExtendNumberThread() {
        return applicationExtendNumberThread;
    }

    public String getApplicationExtendThreadPool() {
        return applicationExtendThreadPool;
    }

    public String getApplicationExtendSleepDuration() {
        return applicationExtendSleepDuration;
    }

    public String getApplicationExtendMaxRegRecord() {
        return applicationExtendMaxRegRecord;
    }

    public String getProducer_topic() {
        return producer_topic;
    }

    public String getConsumer_topic() {
        return consumer_topic;
    }

    public String getZookeeperHosts() {
        return zookeeperHosts;
    }

    public String getPartitions() {
        return partitions;
    }

    public String getReplicationFactor() {
        return replicationFactor;
    }

    public String getSessionTimeOutInMs() {
        return sessionTimeOutInMs;
    }

    public String getConnectionTimeOutInMs() {
        return connectionTimeOutInMs;
    }

    
    
    public String getChargingUrl() {
        return chargingUrl;
    }

    public String getChargingUriAuth() {
        return chargingUriAuth;
    }

    public String getChargingUriCharge() {
        return chargingUriCharge;
    }

    public String getChargingClientName() {
        return chargingClientName;
    }

   
    public String getChargingPassword() {
        return chargingPassword;
    }

    public String getChargingWsManagement() {
        return chargingWsManagement;
    }

    public String getChargingAliasMsisdn() {
        return chargingAliasMsisdn;
    }

    public String getChargingAliasAmount() {
        return chargingAliasAmount;
    }

    public String getChargingAliasProduct() {
        return chargingAliasProduct;
    }

    public String getChargingAliasDescription() {
        return chargingAliasDescription;
    }

    public String getViewApiUrl() {
        return viewApiUrl;
    }

   

    public String getViewApiClientName() {
        return viewApiClientName;
    }

 
    public String getViewApiPassword() {
        return viewApiPassword;
    }

    public String getViewApiWsManagement() {
        return viewApiWsManagement;
    }

    public String getViewApiAliasMsisdn() {
        return viewApiAliasMsisdn;
    }

    public String getViewApiAliasDescription() {
        return viewApiAliasDescription;
    }

    public HashMap<String, Command> getSETCOMMAND() {
        return SETCOMMAND;
    }

    public HashMap<String, Notification_Conf> getSETNOTIFICATION() {
        return SETNOTIFICATION;
    }

    public HashMap<String, Product> getSETPRODUCT() {
        return SETPRODUCT;
    }

    public List<Request_Conf> getListRequest_conf() {
        return ListRequest_conf;
    }

    public CommandRepository getCommandRepo() {
        return commandRepo;
    }

    public Notification_ConfRepository getNotifConfRepo() {
        return notifConfRepo;
    }

    public ProductRepository getProductRepo() {
        return productRepo;
    }

    public Request_ConfRepository getRequestConfRepo() {
        return requestConfRepo;
    }

    public String getChargingAliasTransaction() {
        return chargingAliasTransaction;
    }

    public void setChargingAliasTransaction(String chargingAliasTransaction) {
        this.chargingAliasTransaction = chargingAliasTransaction;
    }

    private void loadNotificationConf() {
        List<Notification_Conf> listNotif = notifConfRepo.findAll();
        SETNOTIFICATION.clear();
        for (Notification_Conf notif : listNotif) {
            SETNOTIFICATION.put(notif.getNoficationName(), notif);
        }
    }

    private void loadCommand() {
        List<Command> listCommand = commandRepo.findAll();
        SETCOMMAND.clear();
        for (Command comd : listCommand) {
            SETCOMMAND.put(comd.getCommandName(), comd);
        }
    }

    private void loadProduct() {
        List<Product> listProduct = productRepo.findAll();
        SETPRODUCT.clear();
        for (Product prod : listProduct) {
            SETPRODUCT.put(prod.getProductCode(), prod);
        }
    }

//    private  void createTopicIfNotExists(String topicName) {
//        ZkClient zkClient = null;
//        ZkUtils zkUtils = null;
//
//        int partition = Integer.parseInt(partitions);
//        int replication = Integer.parseInt(replicationFactor);
//        int sessionTimeOut = Integer.parseInt(sessionTimeOutInMs);
//        int connectionTimeOut = Integer.parseInt(connectionTimeOutInMs);
//
//        try {
//            zkClient = new ZkClient(zookeeperHosts, sessionTimeOut, connectionTimeOut, ZKStringSerializer$.MODULE$);
//            zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeperHosts), false);
//
//            Properties topicConfiguration = new Properties();
//
//            if (!AdminUtils.topicExists(zkUtils, topicName)) {
//                AdminUtils.createTopic(zkUtils, topicName, partition, replication, topicConfiguration, RackAwareMode.Enforced$.MODULE$);
//                logger.info("############# Topic " + topicName + " created ##############");
//            } else {
//                logger.info("############ Topic " + topicName + " already exists #################");
//            }
//
//        } catch (Exception ex) {
//            logger.error("cannot create topic : " + topicName, ex);
//        } finally {
//            if (zkClient != null) {
//                zkClient.close();
//            }
//        }
//    }

}
