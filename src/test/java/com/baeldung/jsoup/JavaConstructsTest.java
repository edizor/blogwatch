package com.baeldung.jsoup;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import com.baeldung.common.GlobalConstants;
import com.baeldung.common.GlobalConstants.TestMetricTypes;
import com.baeldung.common.Utils;
import com.baeldung.common.vo.GitHubRepoVO;
import com.baeldung.common.vo.JavaConstruct;
import com.baeldung.utility.TestUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class JavaConstructsTest extends BaseJsoupTest {

    @Value("${base.url}")
    private String baseURL;

    @Value("${givenAllTheArticles_whenAnArticleLoads_thenJavaClassesAndMethodsCanBeFoundOnGitHub.file-for-javaConstructs-test}")
    private String fileForJavaConstructsTest;

    @Value("${redownload-repo}")
    protected String redownloadRepo;

    @BeforeEach
    public void loadGitHubRepositories() {
        logger.info("Loading Github repositories into local");
        for (GitHubRepoVO gitHubRepo : GlobalConstants.tutorialsRepos) {
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
        logger.info("Start - creating Map for GitHub modules and Posts");
        List<String> posts = Utils.fetchFileAsList(fileForJavaConstructsTest);

        Map<GitHubRepoVO, Map<String, Path>> repoToUrls = new HashMap<>();
        GlobalConstants.tutorialsRepos.forEach(repo -> {
            Map<String, Path> urlToModule = TestUtils.createMapPostToGithubModuleLocal(repo,
                url -> posts.stream().anyMatch(Utils.removeTrailingSlash(url)::endsWith)
            );
            if (!urlToModule.isEmpty()) {
                logger.info("Repository found: {}, has ({}), posts: {}", repo.repoName(), urlToModule.size(), urlToModule.keySet());
                repoToUrls.put(repo, urlToModule);
            }
        });
        logger.info("Found repositories: {}", repoToUrls.size());
        logger.info("Finished - creating Map for GitHub modules and Posts");

        repoToUrls.forEach((repo, urlToModule) -> {

            // collect all java constructs from all modules in the repo
            Map<Path, List<JavaConstruct>> moduleToJavaConstructs = new HashMap<>();

            // for each module in the repo, we find all Java code and extract Java Constructs
            urlToModule.values().stream().distinct().forEach(module -> {
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

            // we get the Java code in each post via HTTP, compare with what we found in our local repository.
            urlToModule.forEach((post, module) -> {
                final String postUrl = Utils.changeLiveUrlWithBase(post, baseURL);
                try {
                    logger.info("Getting Java Constructs from post: {}", postUrl);
                    // get HTML of the post
                    Document jSoupDocument = Utils.getJSoupDocument(postUrl);
                    // get Java constructs from a post
                    List<JavaConstruct> javaConstructsOnPost = Utils.getJavaConstructsFromPreTagsInTheJSoupDocument(jSoupDocument);
                    List<JavaConstruct> javaConstructsOnModule = moduleToJavaConstructs.get(module);
                    // find Java constructs not found in GitHub module
                    Utils.filterAndCollectJavaConstructsNotFoundOnGitHub(javaConstructsOnPost, javaConstructsOnModule, results, postUrl);
                } catch (Exception e) {
                    logger.error("Error occurred while processing post: {}", postUrl, e);
                }
            });
        });

        final int failingArticles = Utils.countArticlesWithProblems(results);
        if (failingArticles > 0) {
            recordMetrics(failingArticles, TestMetricTypes.FAILED);
            recordFailure(GlobalConstants.givenAllTheArticles_whenAnArticleLoads_thenJavaClassesAndMethodsCanBeFoundOnGitHub, failingArticles);
            failTestWithLoggingTotalNoOfFailures("\n\nTest Results-->" + Utils.getErrorMessageForJavaConstructsTest(results, baseURL));
        }

    }

}

