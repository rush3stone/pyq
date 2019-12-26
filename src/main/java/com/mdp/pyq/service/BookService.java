package com.mdp.pyq.service;


import com.mdp.pyq.dao.BookDAO;
import com.mdp.pyq.pojo.Book;
import com.mdp.pyq.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    @Autowired
    BookDAO bookDAO;
    @Autowired
    CategoryService categoryService;

    /*查出所有书籍*/
    public List<Book> list() {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        return bookDAO.findAll(sort);
    }

    /*增加或更新书籍*/
    public void addOrUpdate(Book book) {
        // save()方法: 当主键存在时更新数据，当主键不存在时插入数据
        bookDAO.save(book);
    }

    /*通过id删除书籍*/
    public void deleteById(int id) {
        bookDAO.deleteById(id);
    }

    /*通过Category查出书籍*/
    public List<Book> listByCategory(int cid) {
        Category category = categoryService.get(cid);
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        return bookDAO.findAllByCategory(category);
    }

    /*通过标题或者作者查询, 返回列表
    * DAO 里是两个参数，所以在 Service 里把同一个参数写了两遍。
    * 用户在搜索时无论输入的是作者还是书名，都会对两个字段进行匹配
    * */
    public List<Book> Search(String keywords) {
        return bookDAO.findAllByTitleLikeOrAuthorLike('%' + keywords + '%', '%' + keywords + '%');
    }

    /*
    * 保存List中的所有数据
     */
    public void addBookList(List<Book> bookList) {
        bookDAO.saveAll(bookList);
    }

}
