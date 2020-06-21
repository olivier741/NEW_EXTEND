/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tatsinktech.extend.thread.process;

import com.tatsinktech.extend.beans.Param;
import com.tatsinktech.extend.beans.Process_Request;
import com.tatsinktech.extend.beans.WS_Request;
import com.tatsinktech.extend.services.BillingClient;
import com.tatsinktech.extend.config.Load_Configuration;
import com.tatsinktech.extend.model.register.Extend_Hist;
import com.tatsinktech.extend.model.register.Product;
import com.tatsinktech.extend.model.register.Promotion;
import com.tatsinktech.extend.model.register.Reduction_Type;
import com.tatsinktech.extend.model.register.Register;
import com.tatsinktech.extend.model.repository.RegisterRepository;
import com.tatsinktech.extend.services.CommunService;
import com.tatsinktech.extend.thread.sender.Sender;
import com.tatsinktech.extend.util.Utils;
import java.net.InetAddress;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.tatsinktech.extend.model.repository.Extend_HistRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;

/**
 *
 * @author olivier
 */
@Component
public class ExtendProcess implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ExtendProcess.class);

    private static int sleep_duration;
    private static int number_thread;
    private static int maxRow;
    private static InetAddress address;

    private int threadID;

    @Autowired
    private RegisterRepository registerRepo;

    @Autowired
    private Extend_HistRepository extendhisRepo;

    @Autowired
    private CommunService communsrv;

    @Autowired
    private Load_Configuration commonConfig;

    @Autowired
    private BillingClient billClient;

    public int getThreadID() {
        return threadID;
    }

    public void setThreadID(int threadID) {
        this.threadID = threadID;
    }

    @PostConstruct
    private void init() {
        ExtendProcess.sleep_duration = Integer.parseInt(commonConfig.getApplicationExtendSleepDuration());
        ExtendProcess.number_thread = Integer.parseInt(commonConfig.getApplicationExtendNumberThread());
        ExtendProcess.maxRow = Integer.parseInt(commonConfig.getApplicationExtendMaxRegRecord());
        ExtendProcess.address = Utils.gethostName();

    }

    @Override
    public void run() {

        logger.info("################################## START PROCESS EXTEND ###########################");
        while (true) {
            // Removing an element from the Queue using poll()
            // The poll() method returns null if the Queue is empty.

            Date current_time = new Date();
            PageRequest pageable = PageRequest.of(0, maxRow, Direction.ASC, "id");
            int passage = 0;
            logger.info("################ START SCAN BY "+Thread.currentThread().getName()+"###############");
            if (number_thread < 2) {
                
                List<Register> listActiveRegister = registerRepo.findActiveExtend(current_time, pageable);
                if (listActiveRegister != null && !listActiveRegister.isEmpty()) {
                    passage = 1;
                    processExtend(listActiveRegister);

                }

                List<Register> listPendingRegister = registerRepo.findPendingExtend(pageable);
                if (listPendingRegister != null && !listPendingRegister.isEmpty()) {
                    passage = 1;
                    processExtend(listPendingRegister);
                }

            } else {
                List<Register> listActiveRegister = registerRepo.findActiveExtendByThread(current_time, number_thread, threadID, pageable);
                if (listActiveRegister != null && !listActiveRegister.isEmpty()) {
                    passage = 1;
                    processExtend(listActiveRegister);

                }

                List<Register> listPendingRegister = registerRepo.findPendingExtendByThread(number_thread, threadID, pageable);
                if (listPendingRegister != null && !listPendingRegister.isEmpty()) {
                    passage = 1;
                    processExtend(listPendingRegister);
                }
            }
            try {
                Thread.sleep(sleep_duration);
            } catch (Exception e) {
            }

        }
    }

    private void processExtend(List<Register> listRegister) {

        logger.info("#################### START NEW SCAN side = " + listRegister.size() + " ##################");
        for (Register reg : listRegister) {
            
            String msisdn = reg.getMsisdn();
            String transaction_id = reg.getTransactionId();
            Product product = reg.getProduct();
            long number_reg = reg.getNumberReg();
            int status = reg.getStatus();
            String product_code = product.getProductCode();
            String service_name = product.getService().getServiceName();
            String service_channel = product.getService().getSendChannel();
            Promotion promo = product.getPromotion();
            Timestamp receive_time = new Timestamp((new Date()).getTime());
            long charge_fee;
            int charge_status = 0;
            
            String status_value = "ACTIVE";
            if (status == 0){
                status_value = "CANCEL";
            }else if (status == 2){
                status_value = "PENDING";
            }else if (status == -1){
                status_value = "CANCEL_OPERATOR";
            }else if (status == -2){
                status_value = "OFFER_EXPIRE";
            }
            
            logger.info("msisdn = "+msisdn);
            logger.info("transaction_id = "+transaction_id);
            logger.info("productCode = "+product_code);
            logger.info("service Name = "+service_name);
            logger.info("status Before = "+status +" --> "+status_value);
            logger.info("expire Time Before = "+reg.getExpireTime());
            logger.info("renew  Time Before = "+reg.getRenewTime());
            logger.info("number Reg  Before = "+reg.getNumberReg());

            Process_Request process_mo = new Process_Request();
            process_mo.setMsisdn(msisdn);
            process_mo.setProductCode(product_code);
            process_mo.setReceiveTime(receive_time);
            process_mo.setTransaction_id(transaction_id);
            process_mo.setServiceName(service_name);
            process_mo.setSendChannel(service_channel);

            Extend_Hist extend_hist = new Extend_Hist();
            String extend_his_desc = "";

            charge_fee = product.getExtendFee();
            // get restric offer
            List<String> listRestric_product = null;
            if (!StringUtils.isBlank(product.getRestrictProduct())) {
                Pattern ptn = Pattern.compile("\\|");
                listRestric_product = Arrays.asList(ptn.split(product.getRestrictProduct().toUpperCase().trim()));
            }

            // get day of registration in the week
            List<String> listRestrictDay = null;
            if (!StringUtils.isBlank(product.getRestrictConstantValidity())) {
                Pattern ptn = Pattern.compile("\\|");
                listRestrictDay = Arrays.asList(ptn.split(product.getRestrictConstantValidity().trim()));
            }

            // get day of registration in the week
            List<String> listFrameTime = null;
            if (!StringUtils.isBlank(product.getFrameTimeValidity())) {
                Pattern ptn = Pattern.compile("\\-");
                listFrameTime = Arrays.asList(ptn.split(product.getFrameTimeValidity().trim()));
            }

            Time startFrameTime = null;
            Time endFrameTime = null;
            if (listFrameTime != null && !listFrameTime.isEmpty()) {
                startFrameTime = getTimeToString(listFrameTime.get(0));
                endFrameTime = getTimeToString(listFrameTime.get(1));
            }
            // get live duration of offer
            Date prod_start_date = product.getStartTime();
            Date prod_end_date = product.getEndTime();

            boolean isframeVal = false;
            if (product.getIsFrameValidity() != null) {
                isframeVal = product.getIsFrameValidity();
            }

            boolean isNotifyExt = false;
            if (product.getIsNotifyExtend() != null) {
                isNotifyExt = product.getIsNotifyExtend();
            }

            String validity = product.getValidity();
            String pending = product.getPendingDuration();

            Calendar c = Calendar.getInstance();
            c.setTime(receive_time);
            String dayOfWeek = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
            Timestamp expire_time = getExpire_Time(validity, receive_time);

            int useproduct = 0;

            // step 1
            if (useproduct == 0) {
                if (!isframeVal) {
                    if (prod_start_date != null && prod_end_date != null) {
                        if (prod_start_date.after(prod_end_date)) {
                            useproduct = 1;               // start time is after end time. wrong time configuration
                            logger.warn("OFFER :" + product_code + " have START-TIME=" + prod_start_date + " which is after END-TIME =" + prod_end_date);
                        } else {
                            if (prod_start_date.after(receive_time)) {
                                useproduct = 2;            // start time is after receive time customer cannot register to product. product not available.
                                logger.warn("OFFER :" + product_code + " have START-TIME=" + prod_start_date + " which is after CURRENT-TIME =" + receive_time);
                            }
                            if (prod_end_date.before(receive_time)) {
                                useproduct = 3;            // end time is before receive time customer cannot register to product. product is expire
                                logger.warn("OFFER :" + product_code + " have END-TIME=" + prod_end_date + " which is before CURRENT-TIME =" + receive_time);
                            }
                        }
                    } else if (prod_start_date != null) {
                        if (prod_start_date.after(receive_time)) {
                            useproduct = 2;            // start time is after receive time customer cannot register to product. product not available.
                            logger.warn("OFFER :" + product_code + " have START-TIME=" + prod_start_date + " which is after CURRENT-TIME =" + receive_time);
                        }
                    } else if (prod_end_date != null) {
                        if (prod_end_date.before(receive_time)) {
                            useproduct = 3;            // end time is before receive time customer cannot register to product. product is expire
                            logger.warn("OFFER :" + product_code + " have END-TIME=" + prod_end_date + " which is before CURRENT-TIME =" + receive_time);

                        }
                    }

                } else {
                    if (startFrameTime != null && endFrameTime != null && startFrameTime.after(endFrameTime)) {
                            useproduct = 1;               // start time is after end time. wrong time configuration
                            logger.warn("OFFER :" + product_code + " have START-TIME=" + startFrameTime + " which is after END-TIME =" + endFrameTime);
                    } 
                }
            }
            // step 2
            if (useproduct == 0) {
                useproduct = executePromotion(msisdn, transaction_id, product, receive_time, promo);
                if (useproduct == 0) {
                    useproduct = executeProductWithoutPromotion(msisdn, transaction_id, product);
                }

            }

            switch (useproduct) {
                case 0:
                    reg.setAutoextend(product.getIsExtend());
                    reg.setCancelTime(null);
                    reg.setExpireTime(expire_time);
                    reg.setMsisdn(msisdn);
                    reg.setNumberReg(number_reg + 1);
                    reg.setProduct(product);
                    reg.setProductCode(product_code);
                    reg.setRenewTime(new Date());
                    reg.setStatus(1);
                    reg.setTransactionId(transaction_id);
                    reg.setUnregTime(null);

                    process_mo.setNotificationCode("EXTEND-PRODUCT-SUCCESS-" + product_code);
                    extend_his_desc = "EXTEND-PRODUCT-SUCCESS-" + product_code;
                    charge_status = 0;
                    status_value = "ACTIVE";
                    registerRepo.save(reg);

                    if (isNotifyExt) {
                        Sender.addMo_Queue(process_mo);
                    }
                    
                    logger.info("Expire Time After = "+expire_time);
                    logger.info("renew  Time After = "+reg.getRenewTime());
                    logger.info("number Reg  After = "+reg.getNumberReg());
                    break;

                case 1:
                case 2:
                case 3:
                    process_mo.setNotificationCode("EXTEND-PRODUCT-WRONG-TIME-" + product_code);
                    extend_his_desc = "EXTEND-PRODUCT-WRONG-TIME-" + product_code;
                    charge_status = 1;
                    status_value = "OFFER_EXPIRE";
                    reg.setCancelTime(new Date());
                    reg.setStatus(-2);
                    registerRepo.save(reg);
                    break;

                case 4:
                    process_mo.setNotificationCode("EXTEND-PRODUCT-NOT-MONEY-" + product_code);
                    extend_his_desc = "EXTEND-PRODUCT-NOT-MONEY-" + product_code;
                    charge_status = 2;
                    Date renewTime = reg.getRenewTime();
                    Timestamp cancel_time = getExpire_Time(pending, new Timestamp(renewTime.getTime()));
                    if (receive_time.after(cancel_time)) {
                        reg.setStatus(0);
                        status_value = "CANCEL";
                        reg.setCancelTime(new Date());
                        registerRepo.save(reg);
                    } else if (status != 2) {
                        reg.setStatus(2);
                        status_value = "PENDING";
                        registerRepo.save(reg);
                    }
                    break;
                case 5:
                    reg.setStatus(-1);
                    reg.setCancelTime(new Date());
                    registerRepo.save(reg);
                    process_mo.setNotificationCode("EXTEND-PRODUCT-CUSTOMER-BLOCK-" + product_code);
                    extend_his_desc = "EXTEND-PRODUCT-CUSTOMER-BLOCK-" + product_code;
                    charge_status = 3;
                    status_value = "CANCEL_OPERATOR";
                    break;
                case 6:
                    process_mo.setNotificationCode("REG-PRODUCT-WRONG-API-CONNECTION-" + product_code);
                    extend_his_desc = "REG-PRODUCT-WRONG-API-CONNECTION-" + product_code;
                    charge_status = 4;
                    break;

            }
            logger.info("status After = "+status +" --> "+status_value);
            logger.info("Resutl Extend = "+extend_his_desc);
            
            Timestamp last_time = new Timestamp(System.currentTimeMillis());
            long diffInMS = (last_time.getTime() - receive_time.getTime());

            extend_hist.setChannel(process_mo.getSendChannel());
            extend_hist.setMsisdn(msisdn);
            extend_hist.setDuration(diffInMS);
            extend_hist.setReceiveTime(receive_time);
            extend_hist.setTransactionId(transaction_id);
            extend_hist.setProcessUnit("Extend");
            extend_hist.setIpAddress(address.getHostName() + "@" + address.getHostAddress());
            extend_hist.setDescription(extend_his_desc);
            extend_hist.setServiceName(process_mo.getServiceName());

            extendhisRepo.save(extend_hist);

            logger.info("insert into extend_his");

        }
    }

    private int executePromotion(String msisdn, String transaction_id, Product product, Date receive_time, Promotion promo) {
        long charge_fee = 0;
        int useproduct = 0;
        int result = 0;
        if (promo != null && promo.getIsExtend() && communsrv.authorizationPromo(msisdn.trim(), promo)) {  // offer have promotion and allow to get promotion
            String promoName = promo.getPromotionName();
            String product_code = product.getProductCode();
            Date promo_start_time = promo.getStartTime();
            Date promo_end_time = promo.getEndTime();

            if (promo_start_time != null && promo_end_time != null) {
                if (promo_start_time.after(promo_end_time)) {
                    // start time is after end time wrong time configuration     
                    useproduct = 1;
                    logger.warn("OFFER :" + product_code + " with PROMOTION =" + promoName + " will not be take care");
                    logger.warn("OFFER :" + product_code + " with PROMOTION =" + promoName + " have PROMO-START-TIME =" + promo_start_time + " which is after PROMO-END-TIME =" + promo_end_time);

                } else {
                    if (promo_start_time.after(receive_time)) {
                        // start time is after receive time customer cannot register to promotion. promotion not available.
                        logger.warn("OFFER :" + product_code + " with PROMOTION =" + promoName + " will not be take care");
                        logger.warn("OFFER :" + product_code + " with PROMOTION =" + promoName + " have PROMO-START-TIME =" + promo_start_time + " which is after CURRENT-TIME =" + receive_time);
                        useproduct = 2;
                    }
                    if (promo_end_time.before(receive_time)) {
                        logger.warn("OFFER :" + product_code + " with PROMOTION =" + promoName + " will not be take care");
                        logger.warn("OFFER :" + product_code + " with PROMOTION =" + promoName + " have PROMO-END-TIME =" + promo_end_time + " which is before CURRENT-TIME =" + receive_time);
                        useproduct = 3;            // end time is before receive time customer cannot register to promotion. promotion is expire
                    }
                }
            } else if (promo_start_time != null) {
                if (promo_start_time.after(receive_time)) {
                    // start time is after receive time customer cannot register to promotion. promotion not available.
                    logger.warn("OFFER :" + product_code + " with PROMOTION =" + promoName + " will not be take care");
                    logger.warn("OFFER :" + product_code + " with PROMOTION =" + promoName + " have PROMO-START-TIME =" + promo_start_time + " which is after CURRENT-TIME =" + receive_time);
                    useproduct = 4;
                }
            } else if (promo_end_time != null) {
                if (promo_end_time.before(receive_time)) {
                    // end time is before receive time customer cannot register to promotion. promotion is expire
                    logger.warn("OFFER :" + product_code + " with PROMOTION =" + promoName + " will not be take care");
                    logger.warn("OFFER :" + product_code + " with PROMOTION =" + promoName + " have PROMO-END-TIME =" + promo_end_time + " which is before CURRENT-TIME =" + receive_time);
                    useproduct = 5;
                }
            }

            // step 6
            if (useproduct == 0) {

                if (product.getExtendFee() > 0 || promo.getPromotionExtFee() > 0) {
                    charge_fee = product.getExtendFee();
                    long reductPerc = 0;
                    long prod_fee = product.getExtendFee();
                    long reduct_val = 0;
                    Reduction_Type reductMode = promo.getReductionMode();
                    switch (reductMode) {
                        case PERCENTAGE:
                            reductPerc = promo.getPercentageExt();
                            reduct_val = prod_fee * reductPerc / 100;
                            charge_fee = Math.abs(prod_fee - reduct_val);

                            break;
                        case VALUE:
                            reduct_val = promo.getPromotionExtFee();
                            charge_fee = Math.abs(prod_fee - reduct_val);
                            break;
                    }

                    logger.info("Charge Fee = "+charge_fee);
                    List<Param> listParam = new ArrayList<Param>();
                    listParam.add(new Param(commonConfig.getChargingAliasAmount(), String.valueOf(charge_fee)));
                    listParam.add(new Param(commonConfig.getChargingAliasMsisdn(), msisdn.trim()));
                    listParam.add(new Param(commonConfig.getChargingAliasProduct(), product_code));
                    listParam.add(new Param(commonConfig.getChargingAliasTransaction(), transaction_id));
                    listParam.add(new Param(commonConfig.getChargingAliasDescription(), "Charge " + product_code + " for " + charge_fee));

                    WS_Request wsRequest = new WS_Request();
                    wsRequest.setAmount(charge_fee);
                    wsRequest.setCharge_reason("Charge " + product_code + " for " + charge_fee);
                    wsRequest.setMsisdn(msisdn);
                    wsRequest.setProcessUnit("Process_Reg");
                    wsRequest.setTransactionId(transaction_id);
                    wsRequest.setRequest_time(new Date());
                    wsRequest.setWsClientlogin(commonConfig.getChargingClientName());
                    wsRequest.setWsClientpassword(commonConfig.getChargingPassword());
                    wsRequest.setWs_AccessMgntName(commonConfig.getChargingWsManagement());
                    wsRequest.setProductCode(product_code);
                    wsRequest.setWSparam(listParam);

                    int resp = billClient.charge(wsRequest);

                    if (resp == 1) {  // not enough money
                        logger.warn("OFFER :" + product_code + " with PROMOTION =" + promoName + " MSISDN = " + msisdn + " don't have enough money");
                        result = 4;
                    }

                    if (resp == 2) {  // customer is block or inactive or cancel
                        logger.warn("OFFER :" + product_code + " with PROMOTION =" + promoName + " MSISDN = " + msisdn + " is inactive or Block or Cancel");
                        result = 5;
                    }

                    if (resp == -1) {  // webservice error
                        logger.warn("OFFER :" + product_code + " with PROMOTION =" + promoName + " MSISDN = " + msisdn + " WEBSERVICE FAIL or NOT PROCESS REQUEST");
                        result = 6;
                    }
                }
            }
        }
        return result;
    }

    private int executeProductWithoutPromotion(String msisdn, String transaction_id, Product product) {
        int result = 0;
        long charge_fee = 0;
        String product_code = product.getProductCode();

        if (product.getExtendFee() > 0) {
            charge_fee = product.getExtendFee();
            logger.info("Charge Fee = "+charge_fee);
            List<Param> listParam = new ArrayList<Param>();
            listParam.add(new Param(commonConfig.getChargingAliasAmount(), String.valueOf(charge_fee)));
            listParam.add(new Param(commonConfig.getChargingAliasMsisdn(), msisdn.trim()));
            listParam.add(new Param(commonConfig.getChargingAliasProduct(), product_code));
            listParam.add(new Param(commonConfig.getChargingAliasTransaction(), transaction_id));
            listParam.add(new Param(commonConfig.getChargingAliasDescription(), "Charge " + product_code + " for " + charge_fee));

            WS_Request wsRequest = new WS_Request();
            wsRequest.setAmount(charge_fee);
            wsRequest.setCharge_reason("Charge " + product_code + " for " + charge_fee);
            wsRequest.setMsisdn(msisdn);
            wsRequest.setProcessUnit("Process_Reg");
            wsRequest.setTransactionId(transaction_id);
            wsRequest.setRequest_time(new Date());
            wsRequest.setWsClientlogin(commonConfig.getChargingClientName());
            wsRequest.setWsClientpassword(commonConfig.getChargingPassword());
            wsRequest.setWs_AccessMgntName(commonConfig.getChargingWsManagement());
            wsRequest.setProductCode(product_code);
            wsRequest.setWSparam(listParam);

            int resp = billClient.charge(wsRequest);

            if (resp == 1) {  // not enough money
                logger.warn("OFFER :" + product_code + " MSISDN = " + msisdn + " don't have enough money");
                result = 4;
            }

            if (resp == 2) {  // customer is block or inactive or cancel
                logger.warn("OFFER :" + product_code + " MSISDN = " + msisdn + " is inactive or Block or Cancel");

                result = 5;
            }

            if (resp == -1) {  // webservice error
                logger.warn("OFFER :" + product_code + " MSISDN = " + msisdn + " WEBSERVICE FAIL or NOT PROCESS REQUEST");
                result = 6;
            }
        }
        return result;
    }

    private Timestamp getExpire_Time(String validity, Timestamp current_time) {
        Timestamp result = null;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(current_time.getTime());

        if (validity.toUpperCase().startsWith("D")) {
            String value = validity.replace("D", "");
            try {
                int nbDay = Integer.parseInt(value);
                cal.add(Calendar.DAY_OF_MONTH, nbDay);
                result = new Timestamp(cal.getTime().getTime());
            } catch (Exception e) {

            }
        } else if (validity.toUpperCase().startsWith("H")) {
            String value = validity.replace("H", "");
            try {
                int nbHour = Integer.parseInt(value);
                cal.add(Calendar.HOUR_OF_DAY, nbHour);
                result = new Timestamp(cal.getTime().getTime());
            } catch (Exception e) {

            }
        }
        return result;
    }

    private Time getTimeToString(String time_value) {

        Time time = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss"); //if 24 hour format
            java.util.Date d1 = (java.util.Date) format.parse(time_value);
            time = new java.sql.Time(d1.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;

    }

    public static void executeRunnables(final ExecutorService service, List<Runnable> runnables) {
        //On exécute chaque "Runnable" de la liste "runnables"
        for (Runnable r : runnables) {

            service.execute(r);
        }
        service.shutdown();
    }

}
