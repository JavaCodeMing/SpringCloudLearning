package com.example.cloud.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author dengzhiming
 * @date 2020/3/16 13:38
 */
@Data
public class Account {
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 总额度
     */
    private BigDecimal total;

    /**
     * 已用额度
     */
    private BigDecimal used;

    /**
     * 剩余额度
     */
    private BigDecimal residue;
}
