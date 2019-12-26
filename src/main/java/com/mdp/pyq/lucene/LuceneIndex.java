package com.mdp.pyq.lucene;


import com.mdp.pyq.pojo.Paper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.hsqldb.lib.StringUtil;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

//import com.crossoverJie.util.*;

/**
 * 各种检索方式汇总:
 * @BooleanQuery
 * @author 3stone
 */
public class LuceneIndex {

    /** Query接口
     *
     */
    private static String indexLocation = "./data/dataIndex";
    public static List<Paper> luceneQueryDemo(String q) throws Exception{

        // BooleanQuery
        return LuceneIndex.booleanQueryDemo(q);
        // FuzzyQuery
//         return LuceneIndex.fuzzyQueryDemo(q);
    }

    /**
     * BooleanQuery 并 高亮显示
     *  可以对多个Field合并查询!目前只添加两个!
     * @param q 查询字符串
     * @return List<Paper>
     * @throws Exception
     */
    public static List<Paper> booleanQueryDemo(String q) throws Exception {
        // 索引存放位置
        Directory dir = FSDirectory.open(Paths.get(indexLocation)); //索引存放的位置
        // 待改进: 创建Reader开销很大, 应该整个检索过程让一个Reader始终开着, 而不是每次查询都新建
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher is = new IndexSearcher(reader);
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder(); // 构造BooleanQuery
        SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer(); // 中文分词

        /** 选择我们需要进行查找的各个字段, 此处先选两个
         * 同时在存放索引的时候要使用TextField类进行存放
         */
        QueryParser parser = new QueryParser("title", analyzer); // 对字段1构造 解析器
        Query query = parser.parse(q);

        QueryParser parser2 = new QueryParser("press", analyzer); //对字段2构造 解析器
        Query query2 = parser2.parse(q);

        //加入FuzzyQuery
        Term term = new Term(Paper.Title, q);
        Query fuzzyQuery  = new FuzzyQuery(term, 1);

        booleanQuery.add(query, BooleanClause.Occur.SHOULD); // 布尔查询, 添加对于两个字段的查询
        booleanQuery.add(query2, BooleanClause.Occur.SHOULD);
        booleanQuery.add(fuzzyQuery, BooleanClause.Occur.SHOULD);
        TopDocs hits = is.search(booleanQuery.build(), 50); //返回前50个
        QueryScorer scorer = new QueryScorer(query);  // 评分

        return getHighligthResultList(is, analyzer, hits, scorer);
    } //booleanQueryDemo


    /**
     * fuzzyQuery 并 高亮显示
     *  模糊搜索: 基于编辑距离算法的近似搜索
     * @param q 查询字符串
     * @return List<Paper>
     * @throws Exception
     */
    public static List<Paper> fuzzyQueryDemo(String q) throws Exception {
        // 索引存放位置
        Directory dir = FSDirectory.open(Paths.get(indexLocation)); //索引存放的位置
        //
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher is = new IndexSearcher(reader);
        SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer(); // 中文分词

        /*
          FuzzyQuery到底怎么用, 还没懂! 其实Term的原理也没有彻底明白
          Lucene in Action 3.5.8的用法不可行
         */
        Term term = new Term(Paper.Title, q);
        Query fuzzyQuery  = new FuzzyQuery(term, 1);

        TopDocs hits = is.search(fuzzyQuery, 50); //返回前50个
        QueryScorer scorer = new QueryScorer(fuzzyQuery);  // 评分

        return getHighligthResultList(is, analyzer, hits, scorer);
    } //fuzzyQueryDemo


    /**
     * 高亮返回List
     * @param is searcher
     * @param analyzer
     * @param hits
     * @param scorer
     * @return List<Paper>
     * @throws IOException
     * @throws InvalidTokenOffsetsException
     */
    public static List<Paper> getHighligthResultList(
            IndexSearcher is, SmartChineseAnalyzer analyzer, TopDocs hits,
            QueryScorer scorer) throws IOException, InvalidTokenOffsetsException {
        /**
         * 这里可以根据自己的需要来自定义查找关键字高亮时的样式。
         */
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer); //高亮
        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color='red'>", "</font></b>");
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
        highlighter.setTextFragmenter(fragmenter);

        List<Paper> paperList = new LinkedList<Paper>(); // 把所有检索结果(doc)加入List
        for (ScoreDoc scoreDoc : hits.scoreDocs) { // 遍历所有检索结果

            Document doc = is.doc(scoreDoc.doc);
            Paper paper = new Paper();
            paper.setId(Integer.parseInt(doc.get("id")));
            paper.setAuthor(doc.get("author"));
            paper.setDate(doc.get("date"));
            paper.setAbs(doc.get("abs"));
            String strField1 = "title";
            String strField2 = "press";
            String field1 = doc.get(strField1);
            String field2 = doc.get(strField2);

            // 要查询的字段1不为空
            if (field1 != null) {
                TokenStream tokenStream = analyzer.tokenStream(strField1, new StringReader(field1));
                String hfield1 = highlighter.getBestFragment(tokenStream, field1);
                if (StringUtil.isEmpty(hfield1)) { // 判断高亮是否为空, 为空则把原有field1加入
                    paper.setTitle(field1);
                } else {                             // 否则加入其高亮的字段(带有html标签)
                    paper.setTitle(hfield1);
                }
            }
            // 要查询的字段2不为空
            if (field2 != null) {
                TokenStream tokenStream = analyzer.tokenStream(strField1, new StringReader(field2));
                String hField2 = highlighter.getBestFragment(tokenStream, field2);
                if (StringUtil.isEmpty(hField2)) {
                    if (field2.length() <= 200) { // 太长时, 进行截取
                        paper.setPress(field2);
                    } else {
                        paper.setPress(field2.substring(0, 200));
                    }
                } else {
                    paper.setPress(hField2);
                }
            }
            // 把此检索结果加入List
            paperList.add(paper);
        }
        return paperList;
    }



//---------------------------- 借鉴SSM(二) ---------------------------------------
// 或许之后对于代码优化参考价值

    private Directory dir = null;

    /**
     * 获取IndexWriter实例
     */
    private IndexWriter getWriter() throws Exception {
        /**
         * 可以根据自己的需要放在具体位置
         */
        dir = FSDirectory.open(Paths.get("/data/lucene"));
        SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(dir, iwc);
        return writer;
    }

    /**
     * 添加数据
     * @param paper
     */
    public void addIndex(Paper paper) throws Exception {
        IndexWriter writer = getWriter();
        Document doc = new Document();
        doc.add(new StringField("id", String.valueOf(paper.getId()), Field.Store.YES));
        /**
         * yes是会将数据存进索引，如果查询结果中需要将记录显示出来就要存进去，如果查询结果
         * 只是显示标题之类的就可以不用存，而且内容过长不建议存进去
         * 使用TextField类是可以用于查询的。
         */
//        doc.add(new TextField("papername", paper.getPapername(), Field.Store.YES));
//        doc.add(new TextField("description", paper.getDescription(), Field.Store.YES));
        writer.addDocument(doc); // 注意对比下面updateDoc
        writer.close();
    }

    /**
     * 更新博客索引:
     * @param paper
     * @throws Exception
     */
    public void updateIndex(Paper paper) throws Exception {
        IndexWriter writer = getWriter();
        Document doc = new Document();
        doc.add(new StringField("id", String.valueOf(paper.getId()), Field.Store.YES));
//        doc.add(new TextField("papername", paper.getPapername(), Field.Store.YES));
//        doc.add(new TextField("description", paper.getDescription(), Field.Store.YES));
        writer.updateDocument(new Term("id", String.valueOf(paper.getId())), doc);
        writer.close();
    }

    /**
     * 删除指定博客的索引: 删除doc
     * @param paperId
     * @throws Exception
     */
    public void deleteIndex(String paperId) throws Exception {
        IndexWriter writer = getWriter();
        writer.deleteDocuments(new Term("id", paperId));
        writer.forceMergeDeletes(); // 强制删除
        writer.commit();
        writer.close();
    }

} // LuceneIndex

