package com.mdp.pyq.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mdp.pyq.pojo.Paper;
import org.apache.commons.io.FileUtils;

public class PaperService {

//    public static void main(String[] args) throws IOException, InterruptedException, AWTException {
//
//        String fileName = "/home/stone/IdeaProjects/data/140k_products.txt";
//
//        List<Paper> products = file2list(fileName);
//
//        System.out.println(products.size());
//
//    }

    public static List<Paper> file2list(String fileName) throws IOException {
        File f = new File(fileName);
        List<String> lines = FileUtils.readLines(f,"UTF-8");
        List<Paper> papers = new ArrayList<>();
        for (String line : lines) {
            Paper p = line2product(line);
            papers.add(p);
        }
        System.out.println("一共转换了 " + papers.size() + "篇文章(图书)");
        return papers;
    }

    private static Paper line2product(String line) {
        Paper p = new Paper();
        String[] fields = line.split("|");
        p.setTitle(fields[0]);
        p.setCover(fields[1]);
        p.setAuthor(fields[2]);
        p.setDate(fields[3]);
        p.setPress(fields[4]);
        p.setAbs(fields[5]);
        p.setId(Integer.parseInt(fields[6]));
//        System.out.println("当前正在转换的是:" + fields[1]);
        return p;
    }

}
