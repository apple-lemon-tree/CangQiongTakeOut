package com.abc.service;


import com.abc.dto.EmployeeDTO;
import com.abc.dto.EmployeeLoginDTO;
import com.abc.dto.EmployeePageQueryDTO;
import com.abc.entity.Employee;
import com.abc.result.PageResult;

public interface EmployeeService {
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    void save(EmployeeDTO employeeDTO);

    PageResult<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    void startOrStop(Integer status, Long id);

    Employee getById(Long id);

    void updata(EmployeeDTO employeeDTO);
}
