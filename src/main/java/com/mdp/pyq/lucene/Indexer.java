package com.mdp.pyq.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner; //不同的扫描检索方式, 后细看

import com.mdp.pyq.pojo.Paper;
import com.mdp.pyq.service.PaperService;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory; //看一下被废弃的原因是什么?
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.eclipse.jdt.internal.compiler.ast.LabeledStatement;
import org.hsqldb.lib.StringUtil;

public class Indexer {

    private Directory indexDir; // 保存索引的位置
    private SmartChineseAnalyzer analyzer; // 中文分词器
    String dataLocation;  //数据文件的路径

    /**
     * 构造函数, 出还刷一些信息
     * @throws IOException
     */
    public Indexer() throws IOException {
        analyzer = new SmartChineseAnalyzer();
        indexDir = FSDirectory.open(Paths.get("./data/dataIndex"));
//        indexDir = FSDirectory.open(Paths.get("/home/stone/IdeaProjects/dataIndex"));
//        dataLocation = "/home/stone/IdeaProjects/data/140k_products.txt";
        dataLocation = "./data/1.txt";
//        createIndex(analyzer);  // 建一次索引就行, 之后部署上线是扇区注释
    }


    /**
     * 获得查询结果的列表
     * @param searcher
     * @param hits
     * @param query
     * @param analyzer
     * @return List<Paper>
     * @throws Exception
     */
    private List<Paper> getSearchResults(IndexSearcher searcher, ScoreDoc[] hits, Query query, SmartChineseAnalyzer analyzer) throws Exception {
        System.out.println("找到 " + hits.length + " 个命中.");
        // 高亮
        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<span style='color:red'>", "</span>");
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, new QueryScorer(query));

        List<Paper> paperList = new LinkedList<Paper>(); // 把所有检索结果(doc)加入List
        for (ScoreDoc scoreDoc : hits) { // 遍历所有检索结果

            // 获取对应文档
            Document doc = searcher.doc(scoreDoc.doc);

            // 新建存储此文档的类实例
            Paper paper = new Paper();
            paper.setId(Integer.parseInt(doc.get("id")));
//            paper.setName(doc.get("name"));
            paper.setAuthor(doc.get("author"));
            paper.setDate(doc.get("date"));
            paper.setPress(doc.get("press"));
            String papertitle = doc.get("title");
            String abs = doc.get("abs");

            // papername字段不为空
            if (papertitle != null) {
                TokenStream tokenStream = analyzer.tokenStream("title", new StringReader(papertitle));
                String hpapertitle = highlighter.getBestFragment(tokenStream, papertitle);
                if (StringUtil.isEmpty(hpapertitle)) { // 判断高亮是否为空, 为空则把原有papername加入
                    paper.setTitle(papertitle);
                } else {                             // 否则加入的是高亮的字段(应该是带有html标签的吧)
                    //paper.setTitle(hpapertitle);
                    paper.setTitle(papertitle); //暂时先不加高亮,显示有问题,直接把html代码贴出来了
                }
            }
            // description字段不为空
            if (abs != null) {
                TokenStream tokenStream = analyzer.tokenStream("abs", new StringReader(abs));
                String hContent = highlighter.getBestFragment(tokenStream, abs);
                if (StringUtil.isEmpty(hContent)) {
                    if (abs.length() <= 200) { // 太长时, 进行截取
                        paper.setAbs(abs);
                    } else {
                        paper.setAbs(abs.substring(0, 200));
                    }
                } else {
                    //paper.setAbs(hContent);
                    paper.setAbs(abs); //暂时先不加高亮,显示有问题,直接把html代码贴出来了
                }
            }
            // 把此检索结果加入List
            paperList.add(paper);
        } // for-存储所有hits
        return paperList;
    } //

    /**
     * 新建索引: 只在系统初始化时调用一次
     * 本项目在第一次在Indexer的构造函数中进行, 然后注销
     * @param analyzer
     * @throws IOException
     */
    private void createIndex(SmartChineseAnalyzer analyzer) throws IOException {

        List<Paper> products = PaperService.file2list(dataLocation);
        int total = products.size();
        int count = 0, per = 0, oldPer =0;
        IndexWriter writer = getWriter();

        for (Paper p : products) {
            addPerDoc(writer, p); // 加入文档
            count++;
            per = count*100/total;  //反应实时索引进度
            if(per!=oldPer){
                oldPer = per;
                System.out.printf("索引中，总共要添加 %d 条记录，当前添加进度是： %d%% %n",total,per);
            }
        }
        writer.close();
    }

    /**
     * 初始创建索引时, 从外部传入writer, 这样可以加快速度(不然等到天昏地暗...)
     * @param writer
     * @param p
     * @throws IOException
     */
    public void addPerDoc(IndexWriter writer, Paper p) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("title", p.getTitle(), Field.Store.YES));
        doc.add(new TextField("cover", p.getCover(), Field.Store.YES));
        doc.add(new TextField("author", p.getAuthor(), Field.Store.YES));
        doc.add(new TextField("date", p.getDate(), Field.Store.YES));
        doc.add(new TextField("press", p.getPress(), Field.Store.YES));
        doc.add(new TextField("abs", p.getAbs(), Field.Store.YES));
        doc.add(new TextField("cid", String.valueOf(p.getCid()), Field.Store.YES));
        writer.addDocument(doc);
    } // addDoc


    /**
     * 功能:每次重建writer, 并添加一篇文档; 使用初建完成后的更新或添加新索引
     * @param p
     * @throws IOException
     */
    public void addDoc(Paper p) throws IOException {
        IndexWriter writer = getWriter();
        Document doc = new Document();
        doc.add(new TextField("title", p.getTitle(), Field.Store.YES));
        doc.add(new TextField("cover", p.getCover(), Field.Store.YES));
        doc.add(new TextField("author", p.getAuthor(), Field.Store.YES));
        doc.add(new TextField("date", p.getDate(), Field.Store.YES));
        doc.add(new TextField("press", p.getPress(), Field.Store.YES));
        doc.add(new TextField("abs", p.getAbs(), Field.Store.YES));
        doc.add(new TextField("cid", String.valueOf(p.getCid()), Field.Store.YES));
        writer.addDocument(doc);
        writer.close();
    } // addDoc


    //-------------------------------------------------------------------
    // -------------------- 新添加内容  ------------------------------
    /**
     * 获取IndexWriter实例
     */
    private IndexWriter getWriter() throws IOException {
        SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(indexDir, iwc);
        return writer;
    }

    /**
     * 用id删除doc
     */
    public void deleteDoc(int paperId) throws Exception {
        IndexWriter writer = getWriter();
        writer.deleteDocuments(new Term("id", String.valueOf(paperId)));
        writer.forceMergeDeletes(); // 强制删除
        writer.commit();
        writer.close();
    }//deleteIndex





}
