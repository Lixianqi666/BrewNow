package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.entity.Address;
import com.brewnow.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/address")
@Tag(name = "地址模块", description = "用户收货地址管理")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("/list")
    @Operation(summary = "地址列表", description = "查询当前用户的全部收货地址")
    public Result<List<Address>> getUserAddresses(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return Result.success(addressService.getUserAddresses(userId));
    }

    @GetMapping("/default")
    @Operation(summary = "默认地址", description = "查询当前用户默认收货地址")
    public Result<Address> getDefaultAddress(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return Result.success(addressService.getUserDefaultAddress(userId));
    }

    @PostMapping("/add")
    @Operation(summary = "新增地址", description = "新增一条收货地址")
    public Result<Address> addAddress(@RequestBody Address address, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (address.getReceiverName() == null || address.getReceiverName().trim().isEmpty()) {
            return Result.error("收货人不能为空");
        }
        if (address.getContactPhone() == null || address.getContactPhone().trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }
        if (address.getDetailAddress() == null || address.getDetailAddress().trim().isEmpty()) {
            return Result.error("详细地址不能为空");
        }
        Address created = addressService.addAddress(userId, address);
        return Result.success("地址新增成功", created);
    }

    @PutMapping("/update")
    @Operation(summary = "更新地址", description = "更新当前用户的一条收货地址")
    public Result<Void> updateAddress(@RequestBody Address address, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (address.getAddressId() == null) {
            return Result.error("地址ID不能为空");
        }
        boolean success = addressService.updateAddress(userId, address);
        return success ? Result.success("地址更新成功", null) : Result.error("地址更新失败");
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "删除地址", description = "删除指定收货地址")
    public Result<Void> deleteAddress(@PathVariable Integer addressId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        boolean success = addressService.deleteAddress(userId, addressId);
        return success ? Result.success("地址删除成功", null) : Result.error("地址删除失败");
    }

    @PutMapping("/default/{addressId}")
    @Operation(summary = "设置默认地址", description = "将指定地址设为默认地址")
    public Result<Void> setDefaultAddress(@PathVariable Integer addressId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        boolean success = addressService.setDefaultAddress(userId, addressId);
        return success ? Result.success("默认地址设置成功", null) : Result.error("默认地址设置失败");
    }

    @PostMapping("/quick-add")
    @Operation(summary = "快捷新增地址", description = "在结算流程中快速新增收货地址")
    public Result<Address> quickAddAddress(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Address address = new Address();
        address.setReceiverName((String) payload.get("receiverName"));
        address.setContactPhone((String) payload.get("contactPhone"));
        address.setProvince((String) payload.get("province"));
        address.setCity((String) payload.get("city"));
        address.setDistrict((String) payload.get("district"));
        address.setDetailAddress((String) payload.get("detailAddress"));
        address.setTag((String) payload.getOrDefault("tag", "自定义"));
        address.setIsDefault(Boolean.TRUE.equals(payload.get("isDefault")));
        Address created = addressService.addAddress(userId, address);
        return Result.success("地址新增成功", created);
    }
}
