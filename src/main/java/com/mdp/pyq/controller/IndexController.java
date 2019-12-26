package com.mdp.pyq.controller;

import com.mdp.pyq.lucene.Indexer;
import com.mdp.pyq.lucene.LuceneIndex;
import com.mdp.pyq.pojo.Book;
import com.mdp.pyq.pojo.Paper;
import com.mdp.pyq.pojo.Search;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class IndexController {

    // 索引类
    Indexer indexer = new Indexer();
    public IndexController() throws IOException {
    }

    //---------------------------------------------
    // lucene 初始加载展示信息: 返回所有内容, 有可调用的API吗?
    @CrossOrigin
    @GetMapping("/api/luceneSearch")
    public List<Paper> listPapers() throws Exception {
        return LuceneIndex.luceneQueryDemo("新品");
    }
    //----------------------------------------

    /**
     * 全文搜索: 集成Lucene
     * 接受检索请求: 通过Mapping()收到前端传回的 检索表达式
     * 反馈检索结果: 用luceneIndex()得到检索结果List, 然后怎样给前端呢??
     *              此处好像把list结果赋值给传入的Model参数, 前端就拿到了?
     */
    @CrossOrigin
    @PostMapping("/api/luceneSearch")
    public List<Paper> luceneSearch(@RequestBody Search s) throws Exception {
        if("".equals(s.getKeywords())) { // TODO: 思考如果表达式为空, 返回什么? 直接[]吗
            return LuceneIndex.luceneQueryDemo(s.getKeywords());
        } else {
            return LuceneIndex.luceneQueryDemo(s.getKeywords());
        }

    } // luceneSearch

    /**
     * 此处实现全文搜索的按类查询: 搜索field(cid)即可??
     * @param paper
     * @return
     * @throws Exception
     */
//    @CrossOrigin
//    @GetMapping("/api/categories/{cid}/books")
//    public List<Book> listByCategory(@PathVariable("cid") int cid) throws Exception {
//        if(0 != cid) {
//            return bookService.listByCategory(cid);
//        } else {
//            return list();
//        }
//    }

    // 新增操作
    @CrossOrigin
    @PostMapping("/api/luceneAdd")
    public Paper addOrUpdate(@RequestBody Paper paper) throws Exception {
        indexer.addDoc(paper);
        return paper;
    }

    // 删除操作
    @CrossOrigin
    @PostMapping("/api/luceneDelete")
    public void deleteById(@RequestBody Paper paper) throws Exception {
        indexer.deleteDoc(paper.getId());
    }

}

