package com.example.xiaobai.yynote.bean;


public class Article   {
    private String ArticleTitle;   //标题
    private String ArticleUrl;     //链接地址
    private String ArticleWho;    //来源
    private String Content;

//    public Article(String ArticleTitle, String ArticleUrl, String ArticleWho,String Content) {
//        this.ArticleTitle = ArticleTitle;
//        this.ArticleUrl = ArticleUrl;
//        this.ArticleWho = ArticleWho;
//        this.Content=Content;
//    }


    public String getArticleWho() {
        return ArticleWho;
    }

    public void setArticleWho(String newsTime) {
        this.ArticleWho = newsTime;
    }

    public String getArticleTitle() {
        return ArticleTitle;
    }

    public void setArticleTitle(String newsTitle) {
        this.ArticleTitle = newsTitle;
    }
    public void setArticleContent(String newsContent) {
        this.Content = newsContent;
    }
    public String getArticleUrl() {
        return ArticleUrl;
    }
    public String getContents() {
        return Content;
    }
    public void setArticleUrl(String newsUrl) {
        this.ArticleUrl = newsUrl;
    }
}
