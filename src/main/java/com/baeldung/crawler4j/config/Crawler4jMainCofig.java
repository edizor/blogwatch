package com.baeldung.crawler4j.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;

import com.baeldung.common.GlobalConstants;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

@Configuration
@ComponentScan("com.baeldung.crawler4j")
@PropertySource({ "classpath:common.properties" })
public class Crawler4jMainCofig {

    @Bean
    @Scope("prototype")
    public CrawlConfig crawlConfig() {
        CrawlConfig crawlConfig = new CrawlConfig();
        crawlConfig.setCrawlStorageFolder(GlobalConstants.CRAWLER4J_STORAGE_FOLDER);
        crawlConfig.setPolitenessDelay(1500);
        crawlConfig.setMaxDepthOfCrawling(-1);
        crawlConfig.setMaxPagesToFetch(-1);
        crawlConfig.setIncludeBinaryContentInCrawling(false);
        crawlConfig.setResumableCrawling(false);
        crawlConfig.setMaxDownloadSize(Integer.MAX_VALUE);
        return crawlConfig;
    }

    @Bean
    @Scope("prototype")
    public PageFetcher pageFetcher() {
        return new PageFetcher(crawlConfig());
    }

    @Bean
    @Scope("prototype")
    public RobotstxtConfig robotstxtConfig() {
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        robotstxtConfig.setEnabled(false);
        return robotstxtConfig;
    }

    @Bean
    @Scope("prototype")
    public RobotstxtServer robotstxtServer() {
        return new RobotstxtServer(robotstxtConfig(), pageFetcher());
    }

    @Bean
    @Scope("prototype")
    public CrawlController crawlController() throws Exception {
        return new CrawlController(crawlConfig(), pageFetcher(), robotstxtServer());
    }

}
