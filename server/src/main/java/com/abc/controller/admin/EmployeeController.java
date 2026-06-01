package com.abc.controller.admin;


//import cn.hutool.db.PageResult;
//import cn.hutool.jwt.JWTUtil;
import com.abc.dto.EmployeeDTO;
import com.abc.dto.EmployeeLoginDTO;
import com.abc.dto.EmployeePageQueryDTO;
import com.abc.entity.Employee;
import com.abc.result.PageResult;
import com.abc.result.Result;
import com.abc.service.EmployeeService;
import com.abc.utils.JwtUtil;
import com.abc.vo.EmployeeLoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final JwtUtil jwtUtil;
    /**
     * 登录
     * @param employeeLoginDTO 登录的员工信息
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO){
        log.info("员工登录：{}",employeeLoginDTO);
        Employee employee = employeeService.login(employeeLoginDTO);


        Map<String,Object> claims = new HashMap<>();
        claims.put("id",employee.getId());
        claims.put("username",employee.getUsername());
        //拿到jwt令牌
        String token = jwtUtil.generateToken(employee.getName(),claims);
        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success("登录成功",employeeLoginVO);
    }

    /**
     * 退出
     */
    @PostMapping("/logout")
    public Result<String> logout(){
        return Result.success("退出成功");
    }

    /**
     * 新加员工employee
     * @param employeeDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工：{}",employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success("添加新员工成功");
    }

    /**
     * 分页查询员工信息
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult<Employee>> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页查询，参数为：{}",employeePageQueryDTO);
        PageResult<Employee> pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success("分页查询成功",pageResult);
    }

    /**
     * 启用/禁用 员工账号
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    public Result startorStop(@PathVariable Integer status,Long id){
        log.info("弃用/禁用员工张浩:{},{}",status,id);
        employeeService.startOrStop(status,id);
        return Result.success("修改权限成功");
    }

    /**
     * 根据员工id查询信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        return Result.success("根据ID查询员工成功",employee);
    }

    @PutMapping
    public Result update(@RequestBody EmployeeDTO employeeDTO){
        log.info("修改员工信息：{}",employeeDTO);
        employeeService.updata(employeeDTO);
        return Result.success("修改员工信息成功");
    }


}
