package com.mdp.pyq.dao;

import com.mdp.pyq.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;

/** 数据库控制:数据库控制
 * 这里关键的地方在于方法的名字。由于使用了 JPA，无需手动构建 SQL 语句，
 * 而只需要按照规范提供方法的名字即可实现对数据库的增删改查
 */
public interface UserDAO extends JpaRepository<User,Integer> {
    // 通过username字段查询到对应行, 返回给User类
    User findByUsername(String username);
    // 通过username和password查询并返回User类
    User getByUsernameAndPassword(String username,String password);
}

