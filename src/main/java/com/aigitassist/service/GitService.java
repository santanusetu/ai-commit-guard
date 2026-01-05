package com.aigitassist.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class GitService {

    /**
     * Retrieves the git diff of all staged changes
     * @param repositoryPath Path to the git repository
     * @return Git diff string of staged changes, empty if no changes
     */
    public String getStagedDiff(String repositoryPath) throws IOException, GitAPIException {
        File repoDir = new File(repositoryPath);
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            throw new IllegalArgumentException("Repository path does not exist: " + repositoryPath);
        }

        try (Git git = Git.open(repoDir);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             DiffFormatter diffFormatter = new DiffFormatter(outputStream);
             ObjectReader reader = git.getRepository().newObjectReader()) {

            Repository repository = git.getRepository();
            diffFormatter.setRepository(repository);

            // Get HEAD commit tree
            ObjectId headId = repository.resolve("HEAD");
            AbstractTreeIterator headTree = null;
            if (headId != null) {
                try (RevWalk walk = new RevWalk(repository)) {
                    RevCommit headCommit = walk.parseCommit(headId);
                    RevTree tree = headCommit.getTree();
                    headTree = new CanonicalTreeParser(null, reader, tree.getId());
                }
            }

            // Get index (staged changes) tree
            DirCache index = DirCache.read(repository);
            ObjectId indexTreeId;
            try (ObjectInserter inserter = repository.newObjectInserter()) {
                indexTreeId = index.writeTree(inserter);
            }
            
            AbstractTreeIterator indexTree = new CanonicalTreeParser(null, reader, indexTreeId);

            // Get diff between HEAD and index
            List<DiffEntry> diffEntries;
            if (headTree != null) {
                diffEntries = diffFormatter.scan(headTree, indexTree);
            } else {
                // First commit - all files are new
                diffEntries = diffFormatter.scan(null, indexTree);
            }

            if (diffEntries.isEmpty()) {
                return "";
            }

            diffFormatter.format(diffEntries);
            return outputStream.toString(StandardCharsets.UTF_8.name());
        }
    }

    /**
     * Checks if there are any staged changes in the repository
     * @param repositoryPath Path to the git repository
     * @return true if there are staged changes, false otherwise
     */
    public boolean hasStagedChanges(String repositoryPath) throws IOException, GitAPIException {
        File repoDir = new File(repositoryPath);
        try (Git git = Git.open(repoDir)) {
            Status status = git.status().call();
            return !status.getAdded().isEmpty() || 
                   !status.getChanged().isEmpty() || 
                   !status.getRemoved().isEmpty();
        }
    }

    /**
     * Gets the name of the current git branch
     * @param repositoryPath Path to the git repository
     * @return Current branch name
     */
    public String getCurrentBranch(String repositoryPath) throws IOException, GitAPIException {
        File repoDir = new File(repositoryPath);
        try (Git git = Git.open(repoDir)) {
            return git.getRepository().getBranch();
        }
    }

    /**
     * Commits all staged changes with the given commit message
     * @param repositoryPath Path to the git repository
     * @param commitMessage The commit message to use
     */
    public void commitChanges(String repositoryPath, String commitMessage) throws IOException, GitAPIException {
        File repoDir = new File(repositoryPath);
        try (Git git = Git.open(repoDir)) {
            git.add().addFilepattern(".").call();
            git.commit()
                    .setMessage(commitMessage)
                    .call();
        }
    }

    /**
     * Pushes commits to the remote repository
     * @param repositoryPath Path to the git repository
     * @param branch Branch name to push
     */
    public void pushChanges(String repositoryPath, String branch) throws IOException, GitAPIException {
        File repoDir = new File(repositoryPath);
        try (Git git = Git.open(repoDir)) {
            git.push()
                    .setRemote("origin")
                    .add(branch)
                    .call();
        }
    }
}

