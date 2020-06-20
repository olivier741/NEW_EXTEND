/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tatsinktech.extend.model.repository;

import com.tatsinktech.extend.model.register.Register;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author olivier
 */
@Repository
public interface RegisterRepository extends JpaRepository<Register, Long> {

    @Query("SELECT reg FROM Register reg WHERE reg.status = 1 AND reg.autoextend = 1 AND reg.expireTime <= :expireTime AND MOD(reg.id, :number_thread ) = :thead_id ")
    List<Register> findActiveExtendByThread(Date expireTime, int number_thread, int thead_id, Pageable pageable);

    @Query("SELECT reg FROM Register reg WHERE reg.status = 1 AND reg.expireTime <= :expireTime ")
    List<Register> findActiveExtend(Date expireTime,Pageable pageable);

    @Query("SELECT reg FROM Register reg WHERE reg.status = 2 AND reg.autoextend = 1 AND MOD(reg.id, :number_thread ) = :thead_id ")
    List<Register> findPendingExtendByThread(int number_thread, int thead_id, Pageable pageable);

    @Query("SELECT reg FROM Register reg WHERE reg.status = 2 AND reg.autoextend = 1 ")
    List<Register> findPendingExtend(Pageable pageable);
}
