package com.mdp.pyq.dao;


import com.mdp.pyq.pojo.Book;
import com.mdp.pyq.pojo.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookDAO extends JpaRepository<Book, Integer> {
    /* 这里延续之前 JPA 的写法，findAllByCategory() 之所以能实现，是因为在 Book 类中有如下注解：
     * @ManyToOne
     * @JoinColumn(name="cid")
     * private Category category;
     * 实际上是把 category 对象的 id 属性作为 cid 进行了查询
     * */
    List<Book> findAllByCategory(Category category);
    /* 这个 findAllByTitleLikeOrAuthorLike，翻译过来就是“根据标题或作者进行模糊查询”，
     * 参数是两个 String，分别对应标题或作者
     * */
    List<Book> findAllByTitleLikeOrAuthorLike(String keyword1, String keyword2);


}
