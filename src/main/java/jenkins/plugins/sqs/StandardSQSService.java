package jenkins.plugins.sqs;

import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.json.JSONArray;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.MessageAttributeValue;

public class StandardSQSService implements SQSService {

    private static final Logger logger = Logger.getLogger(StandardSQSService.class.getName());

    private String endpoint;
    private String secretKeyId;
    private String secretKey;
    private String[] roomIds;

    public StandardSQSService(String endpoint, String roomId, String secretKeyId, String secretKey) {
        super();
        this.endpoint = endpoint;
        this.secretKeyId = secretKeyId;
        this.secretKey = secretKey;
        this.roomIds = roomId.split("[,; ]+");
    }

    public boolean publish(String message) {
        return publish(message, "warning");
    }

    public boolean publish(String message, String color) {
        boolean result = true;
        String url = endpoint;
        String userId = "Jenkins";
        for (String userAndRoomId : roomIds) {
            String roomId = "";
            String[] splitUserAndRoomId = userAndRoomId.split("@");
            switch (splitUserAndRoomId.length) {
                case 1:
                    roomId = splitUserAndRoomId[0];
                    break;
                default: // should be 2
                    userId = splitUserAndRoomId[0];
                    roomId = splitUserAndRoomId[1];
                    break;
            }

            String roomIdString = roomId;

            if (StringUtils.isEmpty(roomIdString)) {
                roomIdString = "(default)";
            }

            logger.info("Sending message to " + url + " with room " + roomIdString + " and message " + message + " (" + color + ")");

            JSONObject json = new JSONObject();

            try {

                BasicAWSCredentials awsCreds = new BasicAWSCredentials(secretKeyId, secretKey);
                AmazonSQS sqs = AmazonSQSClientBuilder.standard()
                                        .withRegion(Regions.US_EAST_1)
                                        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                                        .build();

                SendMessageRequest send_msg_request = new SendMessageRequest()
                        .withQueueUrl(url)
                        .withMessageBody(message)
                        .addMessageAttributesEntry(
                          "user",
                          new MessageAttributeValue()
                              .withDataType("String")
                              .withStringValue(userId)
                        )
                        .addMessageAttributesEntry(
                          "room",
                          new MessageAttributeValue()
                              .withDataType("String")
                              .withStringValue(roomId)
                        )
                        .withDelaySeconds(0);
                sqs.sendMessage(send_msg_request);


            } catch (Exception e) {
                logger.log(Level.WARNING, "Error posting to SQS", e);
                result = false;
            } finally {
                logger.info("Posting succeeded");
            }
        }
        return result;
    }

    protected HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        if (Jenkins.getInstance() != null) {
            ProxyConfiguration proxy = Jenkins.getInstance().proxy;
            if (proxy != null) {
                if (isProxyRequired(proxy.getNoProxyHostPatterns())) {
                    client.getHostConfiguration().setProxy(proxy.name, proxy.port);
                    String username = proxy.getUserName();
                    String password = proxy.getPassword();
                    // Consider it to be passed if username specified. Sufficient?
                    if (username != null && !"".equals(username.trim())) {
                        logger.info("Using proxy authentication (user=" + username + ")");
                        // http://hc.apache.org/httpclient-3.x/authentication.html#Proxy_Authentication
                        // and
                        // http://svn.apache.org/viewvc/httpcomponents/oac.hc3x/trunk/src/examples/BasicAuthenticationExample.java?view=markup
                        client.getState().setProxyCredentials(AuthScope.ANY,
                                new UsernamePasswordCredentials(username, password));
                    }
                }
            }
        }
        return client;
    }

    protected boolean isProxyRequired(List<Pattern> noProxyHosts) {
        try {
            URL url = new URL(endpoint);
            for (Pattern p : noProxyHosts) {
                if (p.matcher(url.getHost()).matches())
                    return false;
            }
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, "A malformed URL [" + endpoint + "] is defined as endpoint, please check your settings");
            // default behavior : proxy still activated
            return true;
        }
        return true;
    }

    void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
