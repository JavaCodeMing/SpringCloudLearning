package com.example.cloud.service;

/**
 * @author dengzhiming
 * @date 2020/3/16 1:40
 */
public interface StorageService {
    /**
     * 扣减库存
     */
    void decrease(Long productId,Integer count);
}
