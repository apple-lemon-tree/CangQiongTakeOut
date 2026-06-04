package com.abc.service;

import com.abc.entity.AddressBook;

import java.util.List;

public interface AddressBookService {
    List<AddressBook> list(Long userId);

    void addAddress(AddressBook addressBook);

    AddressBook getById(Long id);

    void updateAddress(AddressBook addressBook);

    void setDefault(AddressBook addressBook);

    void deleteById(Long id);
}
