package com.example.cloud.domain;


import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author dengzhiming
 * @date 2020/3/16 0:33
 */
@Data
public class Order implements Serializable {

    private Long id;

    private Long userId;

    private Long productId;

    private Integer count;

    private BigDecimal money;

    /**
     * 订单状态：0：创建中；1：已完结
     */
    private Integer status;

}
