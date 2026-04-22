package com.brewnow.service.impl;

import com.brewnow.entity.Address;
import com.brewnow.mapper.AddressMapper;
import com.brewnow.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<Address> getUserAddresses(Integer userId) {
        return addressMapper.selectByUserId(userId);
    }

    @Override
    public Address getUserDefaultAddress(Integer userId) {
        return addressMapper.selectDefaultByUserId(userId);
    }

    @Override
    @Transactional
    public Address addAddress(Integer userId, Address address) {
        address.setUserId(userId);
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressMapper.clearDefaultByUserId(userId);
        } else {
            Address currentDefault = addressMapper.selectDefaultByUserId(userId);
            if (currentDefault == null) {
                address.setIsDefault(true);
            }
        }
        addressMapper.insert(address);
        return addressMapper.selectById(address.getAddressId());
    }

    @Override
    @Transactional
    public boolean updateAddress(Integer userId, Address address) {
        Address existing = addressMapper.selectById(address.getAddressId());
        if (existing == null || !existing.getUserId().equals(userId)) {
            return false;
        }
        address.setUserId(userId);
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressMapper.clearDefaultByUserId(userId);
        }
        return addressMapper.updateById(address) > 0;
    }

    @Override
    @Transactional
    public boolean deleteAddress(Integer userId, Integer addressId) {
        Address existing = addressMapper.selectById(addressId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return false;
        }
        boolean wasDefault = Boolean.TRUE.equals(existing.getIsDefault());
        int affected = addressMapper.softDeleteById(addressId, userId);
        if (affected > 0 && wasDefault) {
            List<Address> remain = addressMapper.selectByUserId(userId);
            if (!remain.isEmpty()) {
                addressMapper.clearDefaultByUserId(userId);
                addressMapper.setDefaultAddress(remain.get(0).getAddressId(), userId);
            }
        }
        return affected > 0;
    }

    @Override
    @Transactional
    public boolean setDefaultAddress(Integer userId, Integer addressId) {
        Address existing = addressMapper.selectById(addressId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return false;
        }
        addressMapper.clearDefaultByUserId(userId);
        return addressMapper.setDefaultAddress(addressId, userId) > 0;
    }
}

