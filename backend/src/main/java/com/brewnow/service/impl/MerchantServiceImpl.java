package com.brewnow.service.impl;

import com.brewnow.entity.Merchant;
import com.brewnow.enums.MerchantStatus;
import com.brewnow.mapper.MerchantMapper;
import com.brewnow.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商家服务实现类
 */
@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public Merchant getMerchantById(String merchantId) {
        return merchantMapper.selectById(merchantId);
    }

    @Override
    public Merchant getMerchantByUserId(Integer userId) {
        return merchantMapper.selectByUserId(userId);
    }

    @Override
    public List<Merchant> getAllMerchants() {
        return merchantMapper.selectAll();
    }

    @Override
    public List<Merchant> getMerchantsByPage(int page, int size) {
        int offset = (page - 1) * size;
        return merchantMapper.selectByPage(offset, size);
    }

    @Override
    public List<Merchant> getMerchantsByStatus(MerchantStatus status) {
        return merchantMapper.selectByStatus(status);
    }

    @Override
    @Transactional
    public boolean addMerchant(Merchant merchant) {
        merchant.setCreateTime(LocalDateTime.now());
        merchant.setStatus(MerchantStatus.PENDING);
        return merchantMapper.insert(merchant) > 0;
    }

    @Override
    @Transactional
    public boolean updateMerchant(Merchant merchant) {
        return merchantMapper.updateById(merchant) > 0;
    }

    @Override
    @Transactional
    public boolean reviewMerchant(String merchantId, MerchantStatus status, String reason) {
        Merchant merchant = getMerchantById(merchantId);
        if (merchant == null) {
            return false;
        }

        merchant.setStatus(status);
        merchant.setApproveTime(LocalDateTime.now());

        return updateMerchant(merchant);
    }

    @Override
    @Transactional
    public boolean deleteMerchant(String merchantId) {
        return merchantMapper.deleteById(merchantId) > 0;
    }

    @Override
    public Integer getMerchantCount() {
        return merchantMapper.countAll();
    }

    @Override
    public Integer getPendingMerchantCount() {
        return merchantMapper.countByStatus(MerchantStatus.PENDING);
    }
}