package com.example.cloud.domain;

import lombok.Data;

/**
 * @author dengzhiming
 * @date 2020/3/16 1:38
 */
@Data
public class Storage {

    private Long id;
    /**
     * 产品id
     */
    private Long productId;
    /**
     * 总库存
     */
    private Integer total;
    /**
     * 已用库存
     */
    private Integer used;
    /**
     * 剩余库存
     */
    private Integer residue;
}
