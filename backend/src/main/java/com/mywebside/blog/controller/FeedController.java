package com.mywebside.blog.controller;

import com.mywebside.blog.domain.Article;
import com.mywebside.blog.repo.ArticleRepository;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeedController {
  private final ArticleRepository articleRepo;
  private static final DateTimeFormatter RFC822 = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z")
      .withZone(ZoneId.of("Asia/Shanghai"));
  private static final DateTimeFormatter W3C = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
      .withZone(ZoneId.of("Asia/Shanghai"));

  public FeedController(ArticleRepository articleRepo) {
    this.articleRepo = articleRepo;
  }

  @GetMapping(value = {"/rss.xml", "/api/feed/rss"}, produces = MediaType.APPLICATION_XML_VALUE)
  public String rss() {
    List<Article> articles = articleRepo.pagePublic(null, null, null,
        PageRequest.of(0, 20)).getContent();
    StringBuilder sb = new StringBuilder("""
        <?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
        <channel>
        <title>维寒一念</title>
        <link>https://mywebside.vercel.app</link>
        <description>个人技术博客</description>
        <language>zh-CN</language>
        <atom:link href="https://mywebside.vercel.app/rss.xml" rel="self" type="application/rss+xml"/>
        """);
    for (Article a : articles) {
      String pubDate = a.getPublishedAt() != null ? RFC822.format(a.getPublishedAt()) : "";
      sb.append("<item>\n")
          .append("<title>").append(escape(a.getTitle())).append("</title>\n")
          .append("<link>https://mywebside.vercel.app/article/").append(a.getId()).append("</link>\n")
          .append("<description>").append(escape(a.getSummary() != null ? a.getSummary() : "")).append("</description>\n")
          .append("<guid>https://mywebside.vercel.app/article/").append(a.getId()).append("</guid>\n")
          .append("<pubDate>").append(pubDate).append("</pubDate>\n")
          .append("</item>\n");
    }
    sb.append("</channel>\n</rss>");
    return sb.toString();
  }

  @GetMapping(value = {"/sitemap.xml", "/api/feed/sitemap"}, produces = MediaType.APPLICATION_XML_VALUE)
  public String sitemap() {
    List<Article> articles = articleRepo.pagePublic(null, null, null,
        PageRequest.of(0, 1000)).getContent();
    StringBuilder sb = new StringBuilder("""
        <?xml version="1.0" encoding="UTF-8"?>
        <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
        <url><loc>https://mywebside.vercel.app</loc><priority>1.0</priority></url>
        <url><loc>https://mywebside.vercel.app/blog</loc><priority>0.9</priority></url>
        """);
    for (Article a : articles) {
      String lastmod = a.getUpdatedAt() != null ? W3C.format(a.getUpdatedAt()) : "";
      sb.append("<url>\n")
          .append("<loc>https://mywebside.vercel.app/article/").append(a.getId()).append("</loc>\n")
          .append("<lastmod>").append(lastmod).append("</lastmod>\n")
          .append("<priority>0.7</priority>\n")
          .append("</url>\n");
    }
    sb.append("</urlset>");
    return sb.toString();
  }

  private String escape(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&apos;");
  }
}
