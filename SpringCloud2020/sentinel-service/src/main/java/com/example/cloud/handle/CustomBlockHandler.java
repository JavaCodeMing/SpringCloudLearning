package com.example.cloud.handle;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.example.cloud.domain.CommonResult;

/**
 * @author dengzhiming
 * @date 2020/3/15 15:19
 */
public class CustomBlockHandler {
    static public CommonResult handleException(BlockException exception){
        return new CommonResult("自定义限流信息",200);
    }
}
