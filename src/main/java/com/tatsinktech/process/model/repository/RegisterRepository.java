/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tatsinktech.process.model.repository;

import com.tatsinktech.process.model.register.Register;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author olivier
 */
@Repository
public interface RegisterRepository extends JpaRepository<Register, Long> {

    @Query("SELECT reg FROM Register reg WHERE reg.status = 1 AND reg.msisdn=: msisdn")
    List<Register> findAllActiveRegisterByMsisdn(String msisdn);

    @Query("SELECT reg FROM Register reg WHERE reg.status = 1 AND reg.msisdn= :msisdn AND reg.product.productCode= :product_code")
    Register findAllActiveRegisterByMsisdnByProduct(String msisdn, String product_code);
    
    @Query("SELECT reg FROM Register reg WHERE reg.msisdn= :msisdn AND reg.product.productCode= :product_code")
    Register findRegisterByMsisdnAndProduct(String msisdn, String product_code);
    
    @Query("SELECT reg FROM Register reg WHERE reg.status = 1 AND reg.autoextend = 0 AND reg.expire_time <= :expire_time AND reg.id % :number_thread = :thead_id ")
    List<Register> findActiveExtendByThread(Date expireTime,int number_thread,int thead_id,Pageable pageable);
    
     @Query("SELECT reg FROM Register reg WHERE reg.status = 1 AND reg.autoextend = 0 AND reg.expire_time <= :expire_time ")
    List<Register> findActiveExtend(Date expireTime,int number_thread,int thead_id,Pageable pageable);
    
    @Query("SELECT reg FROM Register reg WHERE reg.status = 2 AND reg.autoextend = 0 AND reg.id % :number_thread = :thead_id ")
    List<Register> findPendingExtendByThread(Date expireTime,int number_thread,int thead_id,Pageable pageable);
    
    @Query("SELECT reg FROM Register reg WHERE reg.status = 2 AND reg.autoextend = 0 ")
    List<Register> findPendingExtend(Date expireTime,int number_thread,int thead_id,Pageable pageable);
}
