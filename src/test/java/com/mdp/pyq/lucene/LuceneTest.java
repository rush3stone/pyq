package com.mdp.pyq.lucene;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.hsqldb.index.Index;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import javax.print.Doc;


public class LuceneTest {
    private String[] ids={"1","2","3"};
    private String citys[]={"青岛","南京","上海"};
    private String descs[]={
            "青岛是一个漂亮的城市。",
            "南京是一个文化的城市。",
            "上海是一个繁华的城市。"
    };

//    // 下面是测试用到的数据
//    private String ids[] = { "1", "2", "3" };
//    private String citys[] = { "qingdao", "nanjing", "shanghai" };
//    private String descs[] = { "Qingdao is a beautiful city.", "Nanjing is a city of culture.",
//            "Shanghai is a bustling city." };

    //Directory对象
    private Directory dir;

    // 索引路径: 此处设置为绝对路径,之后部署时修改为相对路径
    String indexDir = "/home/stone/IdeaProjects/dataIndex";

    /**
     * 创建IndexWriter
     */
    public IndexWriter getWriter() throws IOException {
        //得到索引所在目录的路径
        dir = FSDirectory.open(Paths.get(indexDir));
        // 标准分词器
        Analyzer analyzer = new StandardAnalyzer();
        //保存用于创建IndexWriter的所有配置。
        IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);
        //实例化IndexWriter
        IndexWriter writer = new IndexWriter(dir, iwConfig);
        return writer;
    }

    /**
     *实例化中文indexerWriter
     */
    public IndexWriter getCNWriter()throws Exception{
        dir = FSDirectory.open(Paths.get(indexDir));
        //中文分词器
        SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
        IndexWriterConfig iwc=new IndexWriterConfig(analyzer);
        IndexWriter writer=new IndexWriter(dir, iwc);
        return writer;
    }

    /**
     * 创建索引
     */
    @Test
    public void testWriteIndex() throws Exception {
        //写入索引文档的路径
        dir = FSDirectory.open(Paths.get(indexDir));
//        IndexWriter writer = getWriter(); // 英文writer
        IndexWriter writer = getCNWriter(); // 中文writer
        for (int i = 0; i < ids.length; i++) {
            //创建文档对象，文档是索引和搜索的单位
            Document doc = new Document();
            doc.add(new StringField("id", ids[i], Field.Store.YES));
            doc.add(new StringField("city", citys[i], Field.Store.YES));
            doc.add(new TextField("desc", descs[i], Field.Store.YES));
            // 添加文档
            writer.addDocument(doc);
        }
        writer.close();
    }


    /**
     * 测试写了几个文档
     *
     * @throws Exception
     */
    @Test
    public void testIndexWriter() throws Exception {
        //写入索引文档的路径
        dir = FSDirectory.open(Paths.get(indexDir));
        IndexWriter writer = getWriter();
        System.out.println("写入了" + writer.numDocs() + "个文档");
        writer.close();
    }

    /**
     * 测试读取了几个文档
     *
     * @throws Exception
     */
    @Test
    public void testIndexReader() throws Exception {
        //写入索引文档的路径
        dir = FSDirectory.open(Paths.get(indexDir));
        IndexReader reader = DirectoryReader.open(dir);
        System.out.println("最大文档数：" + reader.maxDoc());
        System.out.println("实际文档数：" + reader.numDocs());
        reader.close();
    }

    /**
     * 测试删除 在合并前
     *
     * @throws Exception
     */
    @Test
    public void testDeleteBeforeMerge() throws Exception {
        //写入索引文档的路径
        dir = FSDirectory.open(Paths.get(indexDir));
        IndexWriter writer = getWriter();
        System.out.println("删除前：" + writer.numDocs());
        writer.deleteDocuments(new Term("id", "1"));
        writer.commit();
        System.out.println("writer.maxDoc()：" + writer.maxDoc());
        System.out.println("writer.numDocs()：" + writer.numDocs());
        writer.close();
    }


    /**
     * 测试删除 在合并后
     *
     * @throws Exception
     */
    @Test
    public void testDeleteAfterMerge() throws Exception {
        //写入索引文档的路径
        dir = FSDirectory.open(Paths.get(indexDir));
        IndexWriter writer = getWriter();
        System.out.println("删除前：" + writer.numDocs());
        writer.deleteDocuments(new Term("id", "1"));
        writer.forceMergeDeletes(); // 强制删除
        writer.commit();
        System.out.println("writer.maxDoc()：" + writer.maxDoc());
        System.out.println("writer.numDocs()：" + writer.numDocs());
        writer.close();
    }


    /**
     * 测试更新
     *
     * @throws Exception
     */
    @Test
    public void testUpdate() throws Exception {
        // 写入索引文档的路径
        dir = FSDirectory.open(Paths.get(indexDir));
        IndexWriter writer = getWriter();
        Document doc = new Document();
        doc.add(new StringField("id", "1", Field.Store.YES));
        doc.add(new StringField("city", "beijing", Field.Store.YES));
        doc.add(new TextField("desc", "beijing is a city.", Field.Store.NO));
        writer.updateDocument(new Term("id", "1"), doc);
        writer.close();
    }


}
