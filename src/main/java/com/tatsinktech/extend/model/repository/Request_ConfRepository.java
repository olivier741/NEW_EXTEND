/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tatsinktech.extend.model.repository;

import com.tatsinktech.extend.model.register.Request_Conf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author olivier
 */
@Repository
public interface Request_ConfRepository extends JpaRepository<Request_Conf, Long> {
    
}
