package com.mdp.pyq.dao;

import com.mdp.pyq.pojo.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/** 数据库控制:数据库控制
 * 这里关键的地方在于方法的名字。由于使用了 JPA，无需手动构建 SQL 语句，
 * 而只需要按照规范提供方法的名字即可实现对数据库的增删改查
 */
public interface CategoryDAO extends JpaRepository<Category,Integer> {
   /* 这个 DAO 不需要额外构造的方法，JPA 提供的默认方法就够用了
   * */
}
