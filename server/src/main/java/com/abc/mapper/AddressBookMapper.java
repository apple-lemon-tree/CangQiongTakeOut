package com.abc.mapper;

import com.abc.entity.AddressBook;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;

import java.util.List;
@Mapper
public interface AddressBookMapper {

    @Select("select * from sky_take_out.address_book where user_id=#{userId}")
    List<AddressBook> list(Long userId);

    void add(AddressBook addressBook);

    @Select("select * from sky_take_out.address_book where id = #{id}")
    AddressBook getById(Long id);

    void update(AddressBook addressBook);

    @Update("update sky_take_out.address_book set is_default = #{isDefault} where user_id = #{userId}")
    void updateIsDefaultByUserId(AddressBook addressBook);

    @Delete("delete from sky_take_out.address_book where id = #{id}")
    void deleteById(Long id);
}
