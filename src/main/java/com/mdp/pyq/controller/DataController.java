package com.mdp.pyq.controller;

//STEP 1. Import required packages
import com.mdp.pyq.pojo.Paper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataController {
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/white_jotter?characterEncoding=UTF-8";

    //  Database credentials
    static final String USER = "root";
    static final String PASS = "202621";

    private static Paper line2product(String line) {
        Paper p = new Paper();
        String[] fields = line.split(",");
        p.setId(Integer.parseInt(fields[0]));
        p.setTitle(fields[1]);
        p.setAuthor(fields[2]);
        p.setDate(fields[3]);
        p.setPress(fields[4]);
        p.setAbs(fields[5]);
//        System.out.println("当前正在转换的是:" + fields[1]);
        return p;
    }


    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Connection conn = null;
        Statement stmt = null;
        try{
            //STEP 2: Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            //STEP 3: Open a connection
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected database successfully...");

            //STEP 4: Execute a query
            System.out.println("Inserting records into the table...");
            stmt = conn.createStatement();

            // 存储数据
            File f = new File("/home/stone/IdeaProjects/data/140k_products.txt");
            List<String> lines = FileUtils.readLines(f, "UTF-8");
            List<Paper> papers = new ArrayList<>();
//            for (String line : lines) {

            for(int i = 2000; i < lines.size(); i++) {
                String line = lines.get(i);
                Paper p = line2product(line);
                papers.add(p);



                String sql = "INSERT INTO book VALUES (" + "'" +
                        String.valueOf(p.getId()) + "','" +
                        "pyq.edu.cn" + "', '" +
                        p.getTitle() + "','" +
                        p.getAuthor() + "','" +
                        p.getDate() + "','" +
                        p.getPress() + "','" +
                        p.getAbs() + "',";
                if(i < 3000) sql += "'1')";
                else if(i < 5000) sql += "'2')";
                else if(i < 8000) sql +="'3')";
                else if(i < 10000) sql += "'4')";
                else break;
                stmt.executeUpdate(sql);

            }

            System.out.println("Inserted Finished.");

        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(stmt!=null)
                    conn.close();
            }catch(SQLException se){
            }// do nothing
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!");
    }//end main
}//end JDBCExample
