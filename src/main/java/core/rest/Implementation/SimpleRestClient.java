package core.rest.Implementation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.entities.PullRequest;
import core.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

public class SimpleRestClient implements RestClient {
    private final Logger logger = LoggerFactory.getLogger(SimpleRestClient.class);
    private final String REST_URI = "https://api.github.com";
    private final Client restClient;

    public SimpleRestClient() {
        this.restClient = ClientBuilder.newClient();
    }

    @Override
    public List<PullRequest> getAllOpenPullRequests(final String projectPath) throws IOException {
        List<PullRequest> result;
        String pathToPulls = "/repos" + projectPath + "/pulls";
        Response serverResponse = restClient
                .target(REST_URI)
                .path(pathToPulls)
                .request(MediaType.APPLICATION_JSON)
                .get();
        result = getPullRequestListFromResponse(serverResponse);
        return result;
    }

    private List<PullRequest> getPullRequestListFromResponse(final Response serverResponse) throws IOException {
        List<PullRequest> result;
        ObjectMapper mapper = new ObjectMapper();
        result = mapper.readValue(serverResponse.readEntity(String.class), new TypeReference<List<PullRequest>>() {
        });
        logger.debug(result.toString());
        return result;
    }
}
