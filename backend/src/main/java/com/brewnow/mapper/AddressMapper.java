package com.brewnow.mapper;

import com.brewnow.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AddressMapper {
    Address selectById(@Param("addressId") Integer addressId);

    List<Address> selectByUserId(@Param("userId") Integer userId);

    Address selectDefaultByUserId(@Param("userId") Integer userId);

    int insert(Address address);

    int updateById(Address address);

    int softDeleteById(@Param("addressId") Integer addressId, @Param("userId") Integer userId);

    int clearDefaultByUserId(@Param("userId") Integer userId);

    int setDefaultAddress(@Param("addressId") Integer addressId, @Param("userId") Integer userId);
}

