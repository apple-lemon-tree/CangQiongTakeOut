package com.abc.mapper;


import com.abc.annotation.AutoFill;
import com.abc.dto.EmployeeLoginDTO;
import com.abc.dto.EmployeePageQueryDTO;
import com.abc.entity.Employee;
import com.abc.enumeration.OperationType;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {
    /**
     * 根据用户名查询员工
     * @param employeeLoginDTO
     * @return
     */
    @Select("select * from employee where username=#{username}")
    Employee selectByName(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 插入员工数据
     * @param employee
     */
    @Select("insert into employee(name,username,password,phone,sex,id_number,status,create_time,update_time,create_user,update_user) " +
            "values " +
            "(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    @AutoFill(value =  OperationType.INSERT)
    void insert(Employee employee);

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    Page<Employee> list(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 更新员工信息
     * @param employee
     */
    @AutoFill(value =  OperationType.UPDATE)
    void update(Employee employee);

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);
}
