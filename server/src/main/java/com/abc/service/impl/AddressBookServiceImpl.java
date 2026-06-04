package com.abc.service.impl;

import com.abc.context.CurrentHolder;
import com.abc.entity.AddressBook;
import com.abc.mapper.AddressBookMapper;
import com.abc.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AddressBookServiceImpl implements AddressBookService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    @Override
    public List<AddressBook> list(Long userId) {

        return addressBookMapper.list(userId);
    }

    @Override
    public void addAddress(AddressBook addressBook) {
        addressBook.setUserId(CurrentHolder.getCurrentId());
        addressBook.setIsDefault(0);
        addressBookMapper.add(addressBook);
    }

    @Override
    public AddressBook getById(Long id) {
        return addressBookMapper.getById(id);
    }

    @Override
    public void updateAddress(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    @Override
    public void setDefault(AddressBook addressBook) {
        //将当前用户所有地址修改为非默认地址
        addressBook.setIsDefault(0);
        addressBook.setUserId(CurrentHolder.getCurrentId());
        addressBookMapper.updateIsDefaultByUserId(addressBook);

        //将当前地址改为默认地址
        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);
    }

    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }
}
