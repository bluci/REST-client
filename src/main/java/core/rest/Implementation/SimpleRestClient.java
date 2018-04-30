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

    /**
     * Gets all open pull requests from the GitHub-resource specified in the REST_URI constant
     * and returns them as List
     *
     * @return a List of all open pull requests
     */
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

    /**
     * Gets the number of pages the (paginated) GitHub-resource is separated in from the link element
     * of the HTTP-response header
     * @return the number of pages as integer
     */
    private int getNrOfPages() throws RestException {
        String linkString = webTarget.request(MediaType.APPLICATION_JSON).get().getHeaderString("Link");
        if (linkString == null) {
            throw new RestException("error getting number of pages");
        }
        linkString = linkString.substring(linkString.lastIndexOf(">") - 1);
        return Character.getNumericValue(linkString.charAt(0));
    }

    /**
     * Sends an asynchronous GET-request and returns the response of the server.
     * Since the requests are asynchronous a Future that wraps the actual response is returned.
     * @param pageNr the number of the page to send the GET-request to
     * @return a Future containing the response
     */
    private Future<Response> getItemsOfPage(final int pageNr) {
        Future<Response> serverResponse = webTarget
                .queryParam("page", pageNr)
                .request(MediaType.APPLICATION_JSON)
                .async()
                .get();
        logger.debug(pageNr + "response received");
        return serverResponse;
    }

    /**
     * Gets the response objects of the specified Futures and returns the PullRequests they contain.
     * @param futures the list of Futures that wrap the responses
     * @return list of the resulting PullRequests
     */
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

    /**
     * Maps the content of the given response to PullRequest objects and
     * returns them as List, or an empty List if no PullRequest could be mapped.
     * if the response was empty
     * @param serverResponse the response containing the received JSON data
     * @return list of the resulting pull requests
     */
    private List<PullRequest> getPullRequestListFromResponse(final Response serverResponse) throws IOException {
        List<PullRequest> result;
        result = new ObjectMapper().readValue(serverResponse.readEntity(byte[].class),
                new TypeReference<List<PullRequest>>() {
                });
        return result;
    }
}
