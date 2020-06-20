/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tatsinktech.extend.model.repository;

import com.tatsinktech.extend.model.register.Extend_Hist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author olivier
 */
@Repository
public interface Extend_HistRepository extends JpaRepository<Extend_Hist, Long>{
    
}
