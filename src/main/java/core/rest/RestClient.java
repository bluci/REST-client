package core.rest;

import core.entities.PullRequest;
import core.exception.RestException;

import java.io.IOException;
import java.util.List;

public interface RestClient {
    /**
     * gets all open pull requests
     *
     * @return a list of open pull requests
     */
    List<PullRequest> getAllOpenPullRequests(String projectPath) throws IOException, RestException;
}
