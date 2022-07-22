package com.itc.integration.pojo;

import java.io.Serializable;

/**
 * @ClassName User
 * @Author sussenn
 * @Version 1.0.0
 * @Date 2022/7/22
 */
public class User implements Serializable {

    private Integer id;
    private String name;
    private Integer age;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
