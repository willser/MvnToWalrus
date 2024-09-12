package com.github.willser.mvn.walrus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

/**
 * @program: mvn
 * @link:
 * @create: 2024-09-08
 **/

@Mojo(name = "upload")
public class Walrus extends AbstractMojo {


    @Parameter(property = "walrus.publisher", defaultValue = "https://publisher-devnet.walrus.space")
    private String publisher;

    @Parameter(property = "walrus.aggregator")
    private String aggregator;

    @Parameter(property = "walrus.numEpochs", defaultValue = "1")
    private String numEpochs;

    @Parameter(property = "filePath", required = true)
    private String filePath;

    @Parameter(property = "walrus.suiNetwork", defaultValue = "testnet")
    private String suiNetwork;

    private final String suiViewTxUrl = "https://suiscan.xyz/%s/tx/";

    private final String suiViewObjectUrl = "https://suiscan.xyz/%s/object/";


    public void execute() throws MojoExecutionException {

        getLog().info("Upload file `" + filePath + "` to Walrus");
        File file = new File(filePath);
        if (!file.exists()) {
            throw new MojoExecutionException("File not found: " + filePath);
        }

        try {
            URI uri = URI.create(publisher + "/v1/store?epochs=" + numEpochs);

            // Create the PUT request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .PUT(HttpRequest.BodyPublishers.ofFile(Path.of(filePath)))
                    .build();

            // Create the HttpClient instance
            HttpClient httpClient = HttpClient.newHttpClient();

            // Send the request and get the response
            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            // Check the status code
            if (response.statusCode() == 200) {
                // Parse JSON response
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode storageInfo = objectMapper.readTree(response.body());
                getLog().debug("Response info: " + objectMapper);

                if (storageInfo.has("alreadyCertified")) {
                    JsonNode alreadyCertified = storageInfo.get("alreadyCertified");
                    getLog().info("Status: Already certified");
                    getLog().info("BlobId: " + alreadyCertified.get("blobId").asText());
                    getLog().info("EndEpoch: " + alreadyCertified.get("endEpoch").asText());
                    getLog().info("SuiRefType: Previous Sui Certified Event");
                    getLog().info("SuiRef: " + alreadyCertified.get("event").get("txDigest").asText());
                    getLog().info("SuiBaseUrl: " + String.format(suiViewTxUrl, suiNetwork) + alreadyCertified.get("event").get("txDigest").asText());
                    if (aggregator != null) {
                        getLog().info("Download url: " + aggregator + "/v1/" + alreadyCertified.get("blobId").asText());
                    }
                } else if (storageInfo.has("newlyCreated")) {
                    JsonNode newlyCreated = storageInfo.get("newlyCreated").get("blobObject");
                    getLog().info("Status: Newly created");
                    getLog().info("BlobId: " + newlyCreated.get("blobId").asText());
                    getLog().info("EndEpoch: " + newlyCreated.get("storage").get("endEpoch").asText());
                    getLog().info("SuiRefType: Associated Sui Object");
                    getLog().info("SuiRef: " + newlyCreated.get("id").asText());
                    getLog().info("SuiBaseUrl: " + String.format(suiViewObjectUrl, suiNetwork) + newlyCreated.get("id").asText());
                    if (aggregator != null) {
                        getLog().info("Download url: " + aggregator + "/v1/" + newlyCreated.get("blobId").asText());
                    }
                } else {
                    throw new RuntimeException("Unhandled successful response,file upload fail!");
                }

            } else {
                throw new RuntimeException("Something went wrong when storing the blob! Status code: " + response.statusCode());
            }
        } catch (IOException | RuntimeException | InterruptedException e) {
            throw new MojoExecutionException("Error during file upload", e);
        }
    }

}
