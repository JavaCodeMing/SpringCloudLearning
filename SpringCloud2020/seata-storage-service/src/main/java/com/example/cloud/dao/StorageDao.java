package com.example.cloud.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author dengzhiming
 * @date 2020/3/16 1:42
 */
@Repository
public interface StorageDao {
    /**
     * 扣减库存
     */
    void decrease(@Param("productId") Long productId,@Param("count") Integer count);
}
