package com.abc.controller.user;

import com.abc.context.CurrentHolder;
import com.abc.entity.AddressBook;
import com.abc.result.Result;
import com.abc.service.AddressBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/addressBook")
public class AddressBookController {
    private final AddressBookService addressBookService;

    /**
     * 查询当前用户的所有地址
     * @return
     */
    @GetMapping("/list")
    public Result<List<AddressBook>> list(){
        log.info("查询当前用户所有地址");
        Long userId = CurrentHolder.getCurrentId();
        List<AddressBook> address = addressBookService.list(userId);
        return Result.success("查询地址成功",address);
    }

    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public Result addNewAddress(@RequestBody AddressBook addressBook){
        log.info("新增地址：{}",addressBook);
        addressBookService.addAddress(addressBook);
        return Result.success("新增地址成功");
    }

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<AddressBook> getById(@PathVariable Long id){
        log.info("根据id查询地址，{}",id);
        AddressBook addressBook = addressBookService.getById(id);
        return Result.success("OK",addressBook);
    }

    /**
     * 根据id修改地址
     * @param addressBook
     * @return
     */
    @PutMapping
    public Result updateAddress(@RequestBody AddressBook addressBook){
        log.info("根据id修改地址,{}",addressBook);
        addressBookService.updateAddress(addressBook);
        return Result.success("ok");
    }

    @PutMapping("/default")
    public Result setDefault(@RequestBody AddressBook addressBook){
        log.info("设置默认地址");
        addressBookService.setDefault(addressBook);
        return Result.success("ok");
    }

    /**
     * 根据id删除地址
     *
     * @param id
     * @return
     */
    @DeleteMapping("/")
    public Result deleteById(Long id) {
        log.info("根据id删除地址");
        addressBookService.deleteById(id);
        return Result.success("ok");
    }

    /**
     * 查询默认地址
     */
    @GetMapping("default")
    public Result<AddressBook> getDefault() {
        Long userId = CurrentHolder.getCurrentId();
        List<AddressBook> list = addressBookService.list(userId);

        if (list != null && list.size() == 1) {
            return Result.success("ok",list.get(0));
        }

        return Result.error("没有查询到默认地址");
    }



}
