/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tatsinktech.extend.model.repository;

import com.tatsinktech.extend.model.register.Mo_Extend;
import com.tatsinktech.extend.model.register.Register;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author olivier.tatsinkou
 */
@Repository
public interface Mo_ExtendRepository extends JpaRepository<Mo_Extend, Long> {

    @Modifying
    @Transactional
    @Query(" INSERT INTO Mo_Extend (reg_id,transactionId,msisdn,status,autoextend,regTime,renewTime,expireTime,numberReg,productCode) \n"
            + " SELECT  reg.id, \n "
            + "         reg.transactionId , \n"
            + "         reg.msisdn , \n"
            + "         reg.status, \n"
            + "         reg.autoextend, \n"
            + "         reg.regTime, \n"
            + "         reg.renewTime, \n"
            + "         reg.expireTime,  \n"
            + "         reg.numberReg,  \n"
            + "         reg.productCode  \n"
            + " FROM Register reg \n"
            + " WHERE  reg.autoextend = 1 \n"
            + " AND  ( reg.status = 1 or reg.status = 2 ) \n"
            + " AND reg.expireTime <= :current_time \n"
            + " AND NOT EXISTS (  SELECT 1  FROM Mo_Extend mo_ext \n"
            + "                   WHERE mo_ext.reg_id = reg.id ) \n")
    void loadMoExtend(Date current_time);

    @Query("SELECT mo FROM Mo_Extend mo WHERE MOD(mo.id, :number_thread ) = :thead_id ")
    List<Mo_Extend> findMoExtend_Thread(int number_thread, int thead_id, Pageable pageable);

    @Query("SELECT mo FROM Mo_Extend mo ")
    List<Mo_Extend> findMoExtend(Pageable pageable);

}
