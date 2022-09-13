package com.baeldung.filevisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baeldung.common.GithubRepositories;
import com.baeldung.common.GlobalConstants;
import com.baeldung.common.YAMLProperties;

public class ModuleAlignmentValidatorFileVisitor extends SimpleFileVisitor<Path> {

    private static final String TUTORIALS_REPOSITORY_LOCAL_PATH = GithubRepositories.TUTORIALS.repoLocalPath();

    protected Logger logger = LoggerFactory.getLogger(getClass());

    List<String> testExceptions = YAMLProperties.exceptionsForTests.get(GlobalConstants.givenTutorialsRepo_whenAllModulesAnalysed_thenFolderNameAndArtifiactIdAndModuleNameMatch);

    private List<String> invalidModules = new ArrayList<>();
    private List<String> unparsableModule = new ArrayList<>();

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.toString().equalsIgnoreCase(TUTORIALS_REPOSITORY_LOCAL_PATH + "/.git/") || dir.toString().equalsIgnoreCase(TUTORIALS_REPOSITORY_LOCAL_PATH + "/.git")) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws FileNotFoundException, IOException {

        if (TUTORIALS_REPOSITORY_LOCAL_PATH.concat("/pom.xml").equals(path.toString())) {
            return FileVisitResult.CONTINUE;
        }

        if (testExceptions.contains(removeRepoLocalPath(path.getParent().toString()))) {
            return FileVisitResult.CONTINUE;
        }
        File file = path.toFile();
        if (file.isFile() && file.getName().equalsIgnoreCase("pom.xml")) {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            try {
                logger.info("inspecting {}", path);
                Model pomModel = reader.read(getFileReader(path.toString()));
                String artiFactId = pomModel.getArtifactId();
                String moduleName = pomModel.getName();
                String directoryName = path.getParent().getFileName().toString();
                logger.info("artificatId:{} name:{} directory:{}", artiFactId, moduleName, directoryName);
                if (!artiFactId.equals(directoryName)) {
                    invalidModules.add(path.toString());
                } else if (StringUtils.isNotBlank(moduleName) && !artiFactId.equals(moduleName)) {
                    invalidModules.add(path.toString());
                }

            } catch (XmlPullParserException e) {
                logger.error("Error while parsing POM {}", path);
                unparsableModule.add(path.toString());
            }
        }
        return FileVisitResult.CONTINUE;
    }

    private Object removeRepoLocalPath(String directoryName) {
        return directoryName.replace(TUTORIALS_REPOSITORY_LOCAL_PATH, "");
    }

    private Reader getFileReader(String path) throws FileNotFoundException {
        return new FileReader(path);
    }

    public List<String> getInvalidModules() {
        return invalidModules;
    }

    public List<String> getUnparsableModule() {
        return unparsableModule;
    }

}
