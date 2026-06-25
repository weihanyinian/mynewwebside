package com.weihanyinian.website.config;

import com.weihanyinian.website.module.blog.entity.*;
import com.weihanyinian.website.module.blog.repository.*;
import com.weihanyinian.website.module.guestbook.entity.Guestbook;
import com.weihanyinian.website.module.guestbook.repository.GuestbookRepository;
import com.weihanyinian.website.module.comment.repository.CommentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepo;
    private final TagRepository tagRepo;
    private final ArticleRepository articleRepo;
    private final GuestbookRepository guestbookRepo;
    private final CommentRepository commentRepo;

    public DataSeeder(CategoryRepository categoryRepo, TagRepository tagRepo,
                      ArticleRepository articleRepo, GuestbookRepository guestbookRepo,
                      CommentRepository commentRepo) {
        this.categoryRepo = categoryRepo;
        this.tagRepo = tagRepo;
        this.articleRepo = articleRepo;
        this.guestbookRepo = guestbookRepo;
        this.commentRepo = commentRepo;
    }

    @Override
    public void run(String... args) {
        if (categoryRepo.count() > 0) return; // Already seeded

        // --- Categories ---
        Category catTech = categoryRepo.save(Category.builder().name("技术").slug("tech").build());
        Category catLife = categoryRepo.save(Category.builder().name("生活").slug("life").build());
        Category catNotes = categoryRepo.save(Category.builder().name("随笔").slug("notes").build());

        // --- Tags ---
        Tag tJava = tagRepo.save(Tag.builder().name("Java").build());
        Tag tSpring = tagRepo.save(Tag.builder().name("Spring Boot").build());
        Tag tVue = tagRepo.save(Tag.builder().name("Vue").build());
        Tag tDocker = tagRepo.save(Tag.builder().name("Docker").build());
        Tag tMiku = tagRepo.save(Tag.builder().name("初音未来").build());
        Tag tLife = tagRepo.save(Tag.builder().name("日常").build());
        Tag tDesign = tagRepo.save(Tag.builder().name("设计").build());

        // --- Articles ---
        createArticle(catTech, Set.of(tJava, tSpring),
            "Spring Boot 3 个人博客从零搭建指南",
            "从项目初始化到 Docker 部署，记录用 Spring Boot 3 + Vue 3 搭建个人网站的全过程。",
            """
            ## 前言

            一直想拥有一个自己的个人网站，用来记录技术笔记、分享生活碎碎念。
            经过几周的折腾，终于用 Spring Boot 3 + Vue 3 搭起来了。
            这篇文章记录整个搭建过程，希望对同样想建站的朋友有所帮助。

            ## 技术选型

            后端选择了 Spring Boot 3，主要考虑：
            - Java 17 原生支持，性能优秀
            - Spring Data JPA 简化数据库操作
            - Spring Security + JWT 实现认证
            - Redis 做缓存和浏览量统计

            前端选择了 Vue 3 + Vite + Tailwind CSS：
            - 开发体验极好，热更新秒级响应
            - 毛玻璃效果用 backdrop-filter 轻松实现
            - 响应式布局开箱即用

            ## 数据库设计

            ```sql
            CREATE TABLE articles (
              id BIGINT AUTO_INCREMENT PRIMARY KEY,
              title VARCHAR(200) NOT NULL,
              summary VARCHAR(500),
              content TEXT NOT NULL,
              status VARCHAR(10) DEFAULT 'DRAFT'
            );
            ```

            表结构简洁明了，文章、分类、标签、评论四大核心表。

            ## 部署上线

            使用 Docker Compose 一键部署，包含 MySQL、Redis、Nginx、后端服务和网易云音乐 API。
            视频背景文件因为太大没有入 Git，做了本地拷贝脚本。

            ## 总结

            建站是一个持续迭代的过程。从第一版简陋的 HTML 页面，到现在的全栈项目，
            每次优化都让我学到新东西。下一个目标是加上 AI 伴聊和小游戏模块。
            """);

        createArticle(catTech, Set.of(tVue, tDesign),
            "用 Vue 3 打造毛玻璃个人网站",
            "分享 Vue 3 + Tailwind CSS 实现毛玻璃效果的完整方案，包括日夜模式切换和视频背景。",
            """
            ## 为什么选择毛玻璃

            毛玻璃（Glassmorphism）是近年来非常流行的 UI 设计风格，
            通过半透明、模糊和边框来营造层次感，特别适合有视频背景的页面。

            ## CSS 实现

            ```css
            .glass-card {
              background: rgba(255, 255, 255, 0.12);
              backdrop-filter: blur(12px);
              border: 1px solid rgba(255, 255, 255, 0.2);
              border-radius: 16px;
              box-shadow: 0 8px 32px rgba(31, 38, 135, 0.15);
            }
            ```

            核心是 `backdrop-filter: blur()` 属性，注意要加 `-webkit-backdrop-filter` 兼容 Safari。

            ## 日夜模式切换

            使用 CSS 变量 + `html.dark` 类名切换：
            - 日间：白色调玻璃 + 深色文字
            - 夜间：深色调玻璃 + 亮色文字

            再配合 Pinia store 管理状态，localStorage 持久化偏好。

            ## 视频背景

            在背景播放静音循环视频，叠加暗色遮罩保证文字可读性。
            日间和夜间使用不同的视频素材——日间用 Redial 明亮风格，夜间用初音深色风格。
            """);

        createArticle(catNotes, Set.of(tMiku, tLife),
            "关于初音未来和我",
            "聊聊一个程序员为什么会喜欢初音未来，以及虚拟歌姬如何影响了我的技术审美。",
            """
            ## 初次遇见

            大概是大学的时候，在 B 站刷到了《世界第一的公主殿下》，被那种独特的电子音色吸引了。
            后来慢慢了解到 VOCALOID、Piapro、Crypton 这些名词，
            发现这背后其实是一个非常庞大的创作生态。

            ## 不只是听歌

            初音未来的魅力在于她是一个「平台」而不是一个「歌手」。
            任何人都可以为她写歌、画画、做动画、做游戏。
            Crypton 开放的创作协议让整个生态蓬勃发展，
            这种开源精神和技术社区的开放文化不谋而合。

            ## 技术角度的思考

            作为一个程序员，我对 VOCALOID 的语音合成技术也很感兴趣。
            从最早的拼接式合成到现在的 AI 驱动，
            技术迭代非常快。最近也在关注 AI 音乐生成的发展，
            也许以后可以在博客上写一些相关的技术分析。

            ## 为什么把 Miku 放在网站上

            这个网站的视频背景、配色方案都是初音主题的。
            对我来说，她不仅仅是一个虚拟偶像，
            更代表了一种「用技术实现创意」的精神——
            这也是我作为开发者一直追求的东西。
            """);

        createArticle(catTech, Set.of(tDocker, tSpring),
            "Docker Compose 部署 Spring Boot 全栈应用",
            "从 Dockerfile 编写到 docker-compose 编排，完整记录容器化部署的踩坑经验。",
            """
            ## 为什么容器化

            之前在服务器上手动部署，每次更新都要 SSH 上去 `git pull` + 重启服务，
            非常繁琐。Docker 化之后一个 `docker compose up -d --build` 搞定一切。

            ## 项目结构

            ```
            mywebsite/
            ├── backend/        # Spring Boot 3
            ├── frontend/       # Vue 3 + Vite
            ├── docker/
            │   ├── backend/Dockerfile
            │   └── frontend/Dockerfile
            ├── deploy/
            │   └── docker-compose.yml
            └── mysql/
                └── schema.sql
            ```

            ## Dockerfile 要点

            后端使用多阶段构建，先 Maven 编译再 JRE 运行：
            ```dockerfile
            FROM maven:3.9-eclipse-temurin-17 AS build
            COPY . /app
            WORKDIR /app
            RUN mvn clean package -DskipTests

            FROM eclipse-temurin:17-jre-alpine
            COPY --from=build /app/target/*.jar app.jar
            ENTRYPOINT ["java", "-jar", "app.jar"]
            ```

            前端直接用 Nginx 镜像：
            ```dockerfile
            FROM node:22-alpine AS build
            # ... npm install + npm run build ...

            FROM nginx:alpine
            COPY --from=build /app/dist /usr/share/nginx/html
            ```

            ## 常见问题

            - 容器间网络通信用 `network: app` 桥接
            - MySQL 用 healthcheck 确保启动顺序
            - .env 文件管理敏感信息，不要提交到 Git
            - 视频文件排除在 Git 外，部署时本地拷贝

            整个部署流程现在只需要 3 分钟。
            """);

        // --- Guestbook ---
        guestbookRepo.save(Guestbook.builder()
            .nickname("路过的网友")
            .content("网站做得真好看！初音主题太棒了 🎵")
            .build());

        guestbookRepo.save(Guestbook.builder()
            .nickname("技术宅小明")
            .content("Spring Boot + Vue 3 技术栈不错，收藏了，期待更多技术文章！")
            .build());

        guestbookRepo.save(Guestbook.builder()
            .nickname("MikuFan")
            .content("看到初音就点进来了，Redial 好听！博主品味不错 👍")
            .build());
    }

    private void createArticle(Category category, Set<Tag> tags, String title, String summary, String content) {
        Article article = Article.builder()
                .title(title)
                .summary(summary)
                .content(content)
                .viewCount(0L)
                .status(Article.ArticleStatus.PUBLISHED)
                .category(category)
                .tags(tags)
                .build();
        articleRepo.save(article);
    }
}
