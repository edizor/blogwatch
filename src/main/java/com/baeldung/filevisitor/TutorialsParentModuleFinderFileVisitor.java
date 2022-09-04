package com.baeldung.filevisitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baeldung.common.GithubRepositories;

public class TutorialsParentModuleFinderFileVisitor extends SimpleFileVisitor<Path> {

    private static final String TUTORIALS_REPOSITORY_LOCAL_PATH = GithubRepositories
        .getRepositoryByName("tutorials").repoLocalPath();

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private final String artificateId;

    private List<String> childModules = new ArrayList<>();

    public TutorialsParentModuleFinderFileVisitor(String artifactId) {
        this.artificateId = artifactId;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.toString().equalsIgnoreCase(TUTORIALS_REPOSITORY_LOCAL_PATH + "/.git/") || dir.toString().equalsIgnoreCase(TUTORIALS_REPOSITORY_LOCAL_PATH + "/.git")) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        Function<Path, String> getParentArtifactId = filePath -> {
            try {
                return Optional.of(Jsoup.parse(filePath.toFile(), "UTF-8")).map(doc -> doc.getElementsByTag("parent")).map(elements -> elements.first()).map(parentTag -> parentTag.getElementsByTag("artifactId")).map(parentTags -> parentTags.first())
                        .map(artififactIdTag -> artififactIdTag.text()).orElse("not-found");
            } catch (IOException e1) {
                return "not-found";
            }
        };
        File file = path.toFile();
        if (file.isFile() && file.getName().equalsIgnoreCase("pom.xml")) {
            logger.info("inspecting {}", path);
            String parentArtificatId = getParentArtifactId.apply(path);
            if (artificateId.equalsIgnoreCase(parentArtificatId)) {
                logger.info("Child module found: {}", path.toString());
                childModules.add(path.toString());
            }

        }
        return FileVisitResult.CONTINUE;
    }

    public List<String> getChildModules() {
        return childModules;
    }

    public String getArtificateId() {
        return artificateId;
    }

}
