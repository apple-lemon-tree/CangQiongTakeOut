package com.abc.service.impl;


import com.abc.constant.MessageConstant;
import com.abc.constant.PasswordConstant;
import com.abc.constant.StatusConstant;
import com.abc.context.CurrentHolder;
import com.abc.dto.EmployeeDTO;
import com.abc.dto.EmployeeLoginDTO;
import com.abc.dto.EmployeePageQueryDTO;
import com.abc.entity.Employee;
import com.abc.exception.AccountNotFoundException;
import com.abc.exception.PasswordErrorException;
import com.abc.mapper.EmployeeMapper;
import com.abc.result.PageResult;
import com.abc.result.Result;
import com.abc.service.EmployeeService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;

    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        // 拿到当前正在登录的用户，根据其username去数据库里查
        Employee employee = employeeMapper.selectByName(employeeLoginDTO);
        //数据库中没这个用户
        if (employee == null){
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //这是数据库中的密码(MD5码密文)
        String right_password = employee.getPassword();
        //这是用户输入的密码(明文转密文)
        // TODO 为了方便调试，暂且忽略MD5加密
        // String input_password = DigestUtils.md5DigestAsHex(employeeLoginDTO.getPassword().getBytes());
        String input_password = employeeLoginDTO.getPassword();
        //有这个用户，但密码不对
        if (!right_password.equals(input_password)){
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        //密码对了，但是用户被冻结
        if (employee.getStatus() == StatusConstant.DISABLE){
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        //以上问题都没出现，返回用户对象
        return employee;
    }


    public void save(EmployeeDTO employeeDTO){

        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);

        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        employee.setStatus(StatusConstant.ENABLE);

        employeeMapper.insert(employee);
    }

    @Override
    public PageResult<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //引入mybatis工具类来分页查询
        //1，设置分页参数
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        //2.执行查询
        Page<Employee> page = employeeMapper.list(employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> records = page.getResult();
        //3.返回结果
        return new PageResult<>(total,records);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                        .id(id)
                        .status(status)
                        .build();

        employeeMapper.update(employee);
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("****");
        return employee;
    }

    @Override
    public void updata(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        employeeMapper.update(employee);
    }
}
