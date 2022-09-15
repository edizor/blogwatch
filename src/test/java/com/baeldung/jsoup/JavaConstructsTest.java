package com.baeldung.jsoup;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import com.baeldung.common.GithubRepositories;
import com.baeldung.common.GlobalConstants;
import com.baeldung.common.GlobalConstants.TestMetricTypes;
import com.baeldung.common.Utils;
import com.baeldung.common.vo.GitHubRepoVO;
import com.baeldung.common.vo.JavaConstruct;
import com.baeldung.utility.TestUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.RateLimiter;

public class JavaConstructsTest extends BaseJsoupTest {

    @Value("${base.url}")
    private String baseURL;

    @Value("${givenAllTheArticles_whenAnArticleLoads_thenJavaClassesAndMethodsCanBeFoundOnGitHub.file-for-javaConstructs-test}")
    private String fileForJavaConstructsTest;

    @Value("${redownload-repo}")
    protected String redownloadRepo;

    protected RateLimiter rateLimiter = RateLimiter.create(1);

    @BeforeEach
    public void loadGitHubRepositories() {
        logger.info("Loading Github repositories into local");
        for (GitHubRepoVO gitHubRepo : GithubRepositories.getRepositories()) {
            try {
                Utils.fetchGitRepo(redownloadRepo, Paths.get(gitHubRepo.repoLocalPath()), gitHubRepo.repoUrl());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Tag("matchJavaConstructs")
    @Test
    public final void givenAllTheArticles_whenAnArticleLoads_thenJavaClassesAndMethodsCanBeFoundOnGitHub() throws IOException {

        recordExecution(GlobalConstants.givenAllTheArticles_whenAnArticleLoads_thenJavaClassesAndMethodsCanBeFoundOnGitHub);

        Multimap<String, JavaConstruct> results = ArrayListMultimap.create();

        logger.info("Using article file: {}", fileForJavaConstructsTest);
        logger.info("Start - creating Map for Posts to Github Modules");

        Multimap<String, Path> postUrlsToGithubModuleLocalPaths = ArrayListMultimap.create();
        List<String> posts = Utils.fetchFileAsList(fileForJavaConstructsTest);
        for (String url : posts) {
            String postUrl = baseURL + url;
            logger.info("Processing: {}", postUrl);
            rateLimiter.acquire();
            List<String> gitHubUrls = Utils.getGitHubModuleUrl(postUrl);
            if (gitHubUrls.isEmpty()) {
                // no GitHub url found, no-op
                continue;
            }

            for (String gitHubUrl : gitHubUrls) {
                final Path modulePath = Utils.getLocalPathByGithubUrl(gitHubUrl);
                if (modulePath == null) {
                    logger.warn("cannot find local path, Github URL: {} in post: {}", gitHubUrl, postUrl);
                    continue;
                }
                postUrlsToGithubModuleLocalPaths.put(postUrl, modulePath);
            }

        }
        logger.info("Finished - creating Map for Posts to Github Modules");

        // collect all java constructs for all modules
        Map<Path, List<JavaConstruct>> moduleToJavaConstructs = new HashMap<>();
        postUrlsToGithubModuleLocalPaths.forEach((postUrl, module) -> {
            logger.info("Getting Java Constructs from Github Module: {}", module);
            // find all java files in the module
            final List<JavaConstruct> javaConstructsInModule = new ArrayList<>();
            final List<Path> javaFiles = TestUtils.findFilesInLocalModule(module, fileName -> fileName.endsWith(".java"));
            javaFiles.forEach(javaFile -> {
                try {
                    javaConstructsInModule.addAll(Utils.getJavaConstructsFromLocalJavaFile(javaFile));
                } catch (IOException e) {
                    logger.error("Error occurred while processing java file: {}", javaFile, e);
                }
            });
            moduleToJavaConstructs.put(module, javaConstructsInModule);
        });

        // we get the Java code in each post via HTTP, compare with what we found in our local repository before.
        Multimaps.asMap(postUrlsToGithubModuleLocalPaths).forEach((postUrl, modules) -> {
            try {
                logger.info("Getting Java Constructs from post: {}", postUrl);
                // get HTML of the post
                Document jSoupDocument = Utils.getJSoupDocument(postUrl);
                // get Java constructs from a post
                final List<JavaConstruct> javaConstructsOnPost = Utils.getJavaConstructsFromPreTagsInTheJSoupDocument(jSoupDocument)
                    .stream()
                    .filter(javaConstruct -> !javaConstruct.hasGeneratedAnnotation()) // filter out @Generated classes
                    .toList();
                // collect Java constructs from the modules of post
                final List<JavaConstruct> javaConstructsOnModules = modules.stream()
                    .flatMap(path -> moduleToJavaConstructs.get(path)
                        .stream())
                    .collect(Collectors.toList());
                // find Java constructs not found in GitHub module
                Utils.filterAndCollectJavaConstructsNotFoundOnGitHub(javaConstructsOnPost, javaConstructsOnModules, results, postUrl);
            } catch (Exception e) {
                logger.error("Error occurred while processing post: {}", postUrl, e);
            }
        });

        final int failingArticles = Utils.countArticlesWithProblems(results);
        if (failingArticles > 0) {
            recordMetrics(failingArticles, TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllTheArticles_whenAnArticleLoads_thenJavaClassesAndMethodsCanBeFoundOnGitHub, failingArticles);
            failTestWithLoggingTotalNoOfFailures("\n\nTest Results-->" + Utils.getErrorMessageForJavaConstructsTest(results, baseURL));
        }

    }

}

