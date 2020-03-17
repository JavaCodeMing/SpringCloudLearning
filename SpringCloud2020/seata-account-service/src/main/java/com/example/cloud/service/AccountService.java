package com.example.cloud.service;

import java.math.BigDecimal;

/**
 * @author dengzhiming
 * @date 2020/3/16 13:39
 */
public interface AccountService {
    /**
     * 扣减账户余额
     * @param userId 用户id
     * @param money 金额
     */
    void decrease(Long userId, BigDecimal money);
}
