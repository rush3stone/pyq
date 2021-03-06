package com.mdp.pyq.pojo;

public class Paper {

    int id;
    String title;
    String cover;
    String author;
    String date;
    String press;
    String abs;
    int cid;
    public static final String Title = "title";
    public static final String Press = "press";

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getCover() {return cover;}
    public void setCover(String cover) {this.cover = cover; }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author= author;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getPress() {
        return press;
    }
    public void setPress(String press) {
        this.press = press;
    }

    public String getAbs() {
        return abs;
    }
    public void setAbs(String abs) {
        this.abs = abs;
    }
    public int getCid() {return cid;}
    public void setCid(int cid) {this.cid = cid;}
    @Override
    public String toString() {
        return "Paper title=" + title + ", author=" + author + ", date=" + date + ", press="
                + press + ", abs=" + abs + "]";
    }

}
