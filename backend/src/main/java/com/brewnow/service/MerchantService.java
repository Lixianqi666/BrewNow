package com.brewnow.service;

import com.brewnow.entity.Merchant;
import com.brewnow.enums.MerchantStatus;

import java.util.List;

/**
 * 商家服务接口
 */
public interface MerchantService {

    /**
     * 根据ID查询商家
     */
    Merchant getMerchantById(String merchantId);

    /**
     * 根据用户ID查询商家
     */
    Merchant getMerchantByUserId(Integer userId);

    /**
     * 获取所有商家
     */
    List<Merchant> getAllMerchants();

    /**
     * 分页获取商家列表
     */
    List<Merchant> getMerchantsByPage(int page, int size);

    /**
     * 根据状态查询商家
     */
    List<Merchant> getMerchantsByStatus(MerchantStatus status);

    /**
     * 添加商家
     */
    boolean addMerchant(Merchant merchant);

    /**
     * 更新商家
     */
    boolean updateMerchant(Merchant merchant);

    /**
     * 审核商家
     */
    boolean reviewMerchant(String merchantId, MerchantStatus status, String reason);

    /**
     * 删除商家
     */
    boolean deleteMerchant(String merchantId);

    /**
     * 获取商家总数
     */
    Integer getMerchantCount();

    /**
     * 获取待审核商家数量
     */
    Integer getPendingMerchantCount();
}