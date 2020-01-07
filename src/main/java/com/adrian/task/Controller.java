package com.adrian.task;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    private final RepositoryDetails repositoryService;

    public Controller(RepositoryDetails repositoryService) {
        this.repositoryService = repositoryService;
    }

    @GetMapping("repositories/{owner}/{repository}")
    public ResponseEntity getUserRepos(@PathVariable String owner, @PathVariable String repository) {
        return ResponseEntity.ok(repositoryService.getRepositoryDetails(owner, repository));
    }
}
