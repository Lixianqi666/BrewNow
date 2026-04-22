package com.brewnow.service;

import com.brewnow.entity.Address;

import java.util.List;

public interface AddressService {
    List<Address> getUserAddresses(Integer userId);

    Address getUserDefaultAddress(Integer userId);

    Address addAddress(Integer userId, Address address);

    boolean updateAddress(Integer userId, Address address);

    boolean deleteAddress(Integer userId, Integer addressId);

    boolean setDefaultAddress(Integer userId, Integer addressId);
}

