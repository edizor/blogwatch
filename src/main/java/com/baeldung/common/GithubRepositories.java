package com.baeldung.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.baeldung.common.vo.GitHubRepoVO;

/**
 * Helper class to define and orchestrate {@link com.baeldung.common.vo.GitHubRepoVO} instances
 */
public class GithubRepositories {

    private static final String localRepoBasePath = System.getProperty("local.repo.basepath", "/var/lib/jenkins");

    private static final Set<GitHubRepoVO> repositories = new HashSet<>();

    static {
        final var map = YAMLProperties.fetchYMLPropertiesNestedMap("repositories.yaml");
        final var repoList = map.get("repositories");
        repoList.stream()
            .map(m -> new GitHubRepoVO(
                m.get("repoName"),
                m.get("repoUrl"),
                localRepoBasePath + m.get("repoLocalPath"),
                m.get("repoMasterHttpPath")
            ))
            .forEach(repositories::add);
    }

    public static List<GitHubRepoVO> getRepositories() {
        // return immutable list
        return List.copyOf(repositories);
    }

    public static GitHubRepoVO getRepositoryByName(String repositoryName) {
        return repositories.stream()
            .filter(repo -> repo.repoName().equals(repositoryName))
            .findFirst()
            .orElse(null);
    }

}
