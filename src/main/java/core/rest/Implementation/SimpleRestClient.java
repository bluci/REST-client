package core.rest.Implementation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.entities.PullRequest;
import core.exception.RestException;
import core.rest.RestClient;
import core.util.ResourceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_OK;

public class SimpleRestClient implements RestClient {
    private final Logger logger = LoggerFactory.getLogger(SimpleRestClient.class);
    private final WebTarget webTarget;

    public SimpleRestClient() {
        this.webTarget = ClientBuilder.newClient()
                .target(ResourceConstants.REST_URI)
                .path(ResourceConstants.PATH_TO_PULLS)
                .queryParam("per_page", 100);
    }

    @Override
    public List<PullRequest> getAllOpenPullRequests() throws IOException, RestException {
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
        Response serverResponse = webTarget
                .queryParam("page", pageNr)
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (serverResponse.getStatus() != HTTP_OK) {
            throw new RestException("received error response: " + serverResponse.getEntity().toString());
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
