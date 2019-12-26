package com.mdp.pyq.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.print.Doc;
import javax.xml.datatype.DatatypeConfigurationException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SearchTest {
    private Directory dir;
    private IndexReader reader;
    private IndexSearcher is;
    String indexDir = "/home/stone/IdeaProjects/dataIndex";

    // 测试前
    @Before
    public void setup() throws Exception {
        dir = FSDirectory.open(Paths.get(indexDir));
        reader = DirectoryReader.open(dir);
        is = new IndexSearcher(reader);
    }

    // 测试后
    @After
    public void tearDown() throws Exception{
        reader.close();
    }


    /**
     * 对特定单词查询 & 模糊查询
     */
    @Test
    public void testTermQuery() throws Exception {
        String searchField = "contents";
        // 所给出的必须是单词，不然差不到
        String q = "author";

        // 一个Term表示来自文本的一个单词。
        Term t = new Term(searchField, q);
        // 为Term构造查询。
        Query query = new TermQuery(t);
        TopDocs hits = is.search(query, 10);
        // hits.totalHits：查询的总命中次数。即在几个文档中查到给定单词
        System.out.println("匹配 '" + q + "'，总共查询到" + hits.totalHits + "个文档");
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = is.doc(scoreDoc.doc);
            System.out.println(doc.get("fullPath"));
        }

        /**参数1: 需要根据条件查询
         * 参数2: 最大可编辑数，取值范围0，1，2 (即:允许我的查询条件的值，可以错误几个字符) */
        Query query2 = new FuzzyQuery(new Term(searchField,"authoo"),1);
        TopDocs hits2 = is.search(query2, 10);
        // hits.totalHits：查询的总命中次数。即在几个文档中查到给定单词
        System.out.println("匹配 '" + "authoo"+ "'，总共查询到" + hits2.totalHits + "个文档");
        for (ScoreDoc scoreDoc : hits2.scoreDocs) {
            Document doc = is.doc(scoreDoc.doc);
            System.out.println(doc.get("fullPath"));
        }
    }

    /**
     * 解析查询表达式
     * query不再简单直接由单词构成, 而是用parser先解析,然后构造query
     */
    @Test
    public void testQueryParser() throws Exception {
        // 标准分词器
        Analyzer analyzer = new StandardAnalyzer();  //analyzer:标准分词器实例
        String searchField = "contents";  //searchField:要查询的字段；
        String q = "All materials";

        // 建立查询解析器
        QueryParser parser = new QueryParser(searchField, analyzer);
        Query query = parser.parse(q);
        //返回查询到的前10项（查到100个相关内容的话也只会返回10个）
        TopDocs hits = is.search(query, 10);
        System.out.println("匹配 " + q + "查询到" + hits.totalHits + "个记录");
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = is.doc(scoreDoc.doc);
            System.out.println(doc.get("fullPath"));
        }

    } // testQueryParse



}
