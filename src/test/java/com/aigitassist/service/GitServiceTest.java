package com.aigitassist.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitServiceTest {

    @TempDir
    Path repo;

    private final GitService gitService = new GitService();

    @BeforeEach
    void initRepoWithOneCommit() throws Exception {
        try (Git git = Git.init().setDirectory(repo.toFile()).call()) {
            write("App.java", "class App {}\n");
            git.add().addFilepattern("App.java").call();
            git.commit().setMessage("initial").setAuthor("Test", "test@example.com")
                    .setCommitter("Test", "test@example.com").setSign(false).call();
        }
    }

    private void write(String name, String content) throws IOException {
        Files.write(repo.resolve(name), content.getBytes(StandardCharsets.UTF_8));
    }

    private void stage(String name) throws Exception {
        try (Git git = Git.open(repo.toFile())) {
            git.add().addFilepattern(name).call();
        }
    }

    private List<String> filesInHeadCommit() throws Exception {
        try (Git git = Git.open(repo.toFile())) {
            RevCommit head = git.log().setMaxCount(1).call().iterator().next();
            List<String> files = new ArrayList<>();
            try (TreeWalk walk = new TreeWalk(git.getRepository())) {
                walk.addTree(head.getTree());
                walk.setRecursive(true);
                while (walk.next()) {
                    files.add(walk.getPathString());
                }
            }
            return files;
        }
    }

    @Test
    void noStagedChangesOnCleanRepo() throws Exception {
        assertFalse(gitService.hasStagedChanges(repo.toString()));
        assertEquals("", gitService.getStagedDiff(repo.toString()));
    }

    @Test
    void stagedDiffContainsOnlyStagedChanges() throws Exception {
        write("App.java", "class App { int staged; }\n");
        stage("App.java");
        write("Notes.txt", "not staged\n");

        assertTrue(gitService.hasStagedChanges(repo.toString()));
        String diff = gitService.getStagedDiff(repo.toString());
        assertTrue(diff.contains("+class App { int staged; }"));
        assertFalse(diff.contains("Notes.txt"));
    }

    @Test
    void commitIncludesStagedFilesButNotUnstagedOrUntrackedOnes() throws Exception {
        write("App.java", "class App { int staged; }\n");
        stage("App.java");
        write("secrets.txt", "password = SuperSecretValue123\n"); // untracked, never scanned

        gitService.commitChanges(repo.toString(), "feat: staged change", Collections.emptyList());

        List<String> committed = filesInHeadCommit();
        assertTrue(committed.contains("App.java"));
        assertFalse(committed.contains("secrets.txt"));
    }

    @Test
    void commitAlsoIncludesFilesTheToolWrote() throws Exception {
        write("App.java", "class App { int staged; }\n");
        stage("App.java");
        write("README.md", "# App\n"); // written by the tool, not staged by the user

        gitService.commitChanges(repo.toString(), "docs: readme", Collections.singletonList("README.md"));

        assertTrue(filesInHeadCommit().contains("README.md"));
    }

    @Test
    void reportsCurrentBranch() throws Exception {
        String branch = gitService.getCurrentBranch(repo.toString());
        assertTrue(branch.equals("master") || branch.equals("main"));
    }
}
