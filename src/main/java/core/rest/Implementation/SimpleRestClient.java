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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.net.HttpURLConnection.HTTP_OK;

public class SimpleRestClient implements RestClient {
    private final Logger logger = LoggerFactory.getLogger(SimpleRestClient.class);
    private WebTarget webTarget;

    public SimpleRestClient() {
        this.webTarget = ClientBuilder.newClient()
                .target(ResourceConstants.REST_URI)
                .path(ResourceConstants.PATH_TO_PULLS)
                .queryParam("per_page", 30)
                .queryParam("status", "open");
    }

    @Override
    public List<PullRequest> getAllOpenPullRequests() throws IOException, RestException {
        int currentPageNr = 1;
        List<Future<Response>> futureResponses = new ArrayList<>();
        final int nrOfPages = getNrOfPages();

        while (currentPageNr <= nrOfPages) {
            futureResponses.add(getItemsOfPage(currentPageNr++));
        }
        return getPullRequestsFromFutures(futureResponses);
    }

    private int getNrOfPages() throws RestException {
        String linkString = webTarget.request(MediaType.APPLICATION_JSON).get().getHeaderString("Link");
        if (linkString == null) {
            throw new RestException("error getting number of pages");
        }
        int idxOfLast = linkString.indexOf("last");
        return Integer.parseInt(linkString.substring(idxOfLast - 9, idxOfLast - 8));
    }

    private Future<Response> getItemsOfPage(final int pageNr) {
        Future<Response> serverResponse = webTarget
                .queryParam("page", pageNr)
                .request(MediaType.APPLICATION_JSON)
                .async()
                .get();
        logger.debug(pageNr + "response received");
        return serverResponse;
    }

    private List<PullRequest> getPullRequestsFromFutures(List<Future<Response>> futures)
            throws IOException, RestException {
        List<PullRequest> result = new ArrayList<>();
        Response tmp;
        for (Future<Response> r : futures) {
            try {
                tmp = r.get();
                if (tmp.getStatus() != HTTP_OK) {
                    throw new RestException("received HTTP error: " + tmp.getStatusInfo());
                }
                logger.debug("constructing entities from response");
                result.addAll(getPullRequestListFromResponse(tmp));
            } catch (InterruptedException | ExecutionException e) {
                throw new RestException("error constructing response entities", e);
            }
        }
        return result;
    }

    private List<PullRequest> getPullRequestListFromResponse(final Response serverResponse) throws IOException {
        List<PullRequest> result;
        result = new ObjectMapper().readValue(serverResponse.readEntity(byte[].class),
                new TypeReference<List<PullRequest>>() {
                });
        return result;
    }
}
