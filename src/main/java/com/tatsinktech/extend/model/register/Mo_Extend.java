/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tatsinktech.extend.model.register;

import com.tatsinktech.extend.model.AbstractModel;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UpdateTimestamp;

/**
 *
 * @author olivier.tatsinkou
 */
@Entity
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "mo_ext") 
public class Mo_Extend extends AbstractModel<Long> {

    @Column(name = "reg_id")
    private long reg_id;
    
    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "msisdn")
    private String msisdn;

    /**
     * 1 = active|active, 0 = active|cancel, 2 = active|pending, -1=
     * cancel|cancel (state in network|state in service) -2= active|expire
     * (state in network|offer expire)
     */
    @Column(name = "status")
    private int status;

    @Column(name = "autoextend")
    private boolean autoextend;

    @Column(name = "reg_time")
    private Date regTime;

    @Column(name = "renew_time")
    private Date renewTime;

    @Column(name = "expire_time")
    private Date expireTime;

    
    @Column(name = "number_reg")
    private long numberReg;

    @Column(name = "product_code")
    private String productCode;
}
