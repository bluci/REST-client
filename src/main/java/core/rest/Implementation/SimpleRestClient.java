package core.rest.Implementation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.entities.PullRequest;
import core.exception.RestException;
import core.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleRestClient implements RestClient {
    private final Logger logger = LoggerFactory.getLogger(SimpleRestClient.class);
    private final String REST_URI = "https://api.github.com";
    private final String PATH_TO_PULLS = "/repos/Microsoft/TypeScript/pulls";
    private final Client restClient;

    public SimpleRestClient() {
        this.restClient = ClientBuilder.newClient();
    }

    @Override
    public List<PullRequest> getAllOpenPullRequests(final String projectPath) throws IOException, RestException {
        List<PullRequest> result = new ArrayList<>();
        int pageNr = 1;

        List<PullRequest> tmp = getItemsOfPage(pageNr);
        while (tmp.size() > 0) {
            result.addAll(tmp);
            tmp = getItemsOfPage(++pageNr);
        }
        logger.debug("result size: " + result.size());
        return result;
    }

    private List<PullRequest> getItemsOfPage(final int pageNr) throws RestException, IOException {
        List<PullRequest> result;
        Response serverResponse = restClient
                .target(REST_URI)
                .path(PATH_TO_PULLS).queryParam("per_page", 100).queryParam("page", pageNr)
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (serverResponse.getStatus() != 200) {
            throw new RestException("recieved error response: " + serverResponse.getEntity().toString());
        }
        result = getPullRequestListFromResponse(serverResponse);
        return result;
    }

    private List<PullRequest> getPullRequestListFromResponse(final Response serverResponse) throws IOException {
        List<PullRequest> result;
        result = new ObjectMapper().readValue(serverResponse.readEntity(String.class),
                new TypeReference<List<PullRequest>>() {
                });
        return result;
    }
}
