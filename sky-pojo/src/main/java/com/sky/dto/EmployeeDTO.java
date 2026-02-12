package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeDTO implements Serializable {

    private Long id;

    private String username;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;

    /*
    该方法用于保存员工信息，实际开发中会调用数据库操作来持久化数据
     */
    public void save(EmployeeDTO employeeDTO) {
    }
}
