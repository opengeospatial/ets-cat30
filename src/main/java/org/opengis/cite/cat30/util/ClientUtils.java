package org.opengis.cite.cat30.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.util.EnumMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.urlconnection.HttpURLConnectionFactory;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;
import java.net.URI;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.w3c.dom.Document;

/**
 * Provides various utility methods for creating and configuring HTTP client
 * components.
 */
public class ClientUtils {

    /**
     * Builds a client component for interacting with HTTP endpoints. The client
     * will automatically redirect to the URI declared in 3xx responses. Request
     * and response messages may be logged to a JDK logger (in the namespace
     * "com.sun.jersey.api.client").
     *
     * @return A Client component.
     */
    public static Client buildClient() {
        ClientConfig config = new DefaultClientConfig();
        config.getProperties().put(
                ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
        Client client = Client.create(config);
        client.addFilter(new LoggingFilter());
        return client;
    }

    /**
     * Constructs a client component that uses a specified web proxy. Proxy
     * authentication is not supported. Configuring the client to use an
     * intercepting proxy can be useful when debugging a test.
     *
     * @param proxyHost The host name or IP address of the proxy server.
     * @param proxyPort The port number of the proxy listener.
     *
     * @return A Client component that submits requests through a web proxy.
     */
    public static Client buildClientWithProxy(final String proxyHost,
            final int proxyPort) {
        ClientConfig config = new DefaultClientConfig();
        config.getProperties().put(
                ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
        Client client = new Client(new URLConnectionClientHandler(
                new HttpURLConnectionFactory() {
                    SocketAddress addr = new InetSocketAddress(proxyHost, proxyPort);
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);

                    @Override
                    public HttpURLConnection getHttpURLConnection(URL url) throws IOException {
                        return (HttpURLConnection) url.openConnection(proxy);
                    }
                }), config);
        client.addFilter(new LoggingFilter());
        return client;
    }

    /**
     * Builds an HTTP request message that uses the GET method.
     *
     * @param endpoint A URI indicating the target resource.
     * @param qryParams A Map containing query parameters (may be null);
     * @param mediaTypes A list of acceptable media types; if not specified,
     * generic XML ("application/xml") is preferred.
     *
     * @return A ClientRequest object.
     */
    public static ClientRequest buildGetRequest(URI endpoint,
            Map<String, String> qryParams, MediaType... mediaTypes) {
        UriBuilder uriBuilder = UriBuilder.fromUri(endpoint);
        if (null != qryParams) {
            for (Map.Entry<String, String> param : qryParams.entrySet()) {
                uriBuilder.queryParam(param.getKey(), param.getValue());
            }
        }
        URI uri = uriBuilder.build();
        ClientRequest.Builder reqBuilder = ClientRequest.create();
        if (null == mediaTypes || mediaTypes.length == 0) {
            reqBuilder = reqBuilder.accept(MediaType.APPLICATION_XML_TYPE);
        } else {
            reqBuilder = reqBuilder.accept(mediaTypes);
        }
        ClientRequest req = reqBuilder.build(uri, HttpMethod.GET);
        return req;
    }

    /**
     * Extracts details about a response message into the given EnumMap object.
     * The map keys (of type {@link HttpMessagePart}) correspond to various
     * parts of the message. The message body is stored as a byte[] array.
     *
     * @param rsp An object representing an HTTP response message.
     * @param infoMap The collection into which message elements are put; if
     * null, a new one is created. Existing values may be replaced.
     */
    public static void extractResponseInfo(ClientResponse rsp,
            EnumMap<HttpMessagePart, Object> infoMap) {
        if (null == infoMap) {
            infoMap = new EnumMap(HttpMessagePart.class);
        }
        infoMap.put(HttpMessagePart.STATUS, rsp.getStatus());
        infoMap.put(HttpMessagePart.HEADERS, rsp.getHeaders());
        if (rsp.hasEntity()) {
            // response entities are rarely very large
            byte[] body = rsp.getEntity(byte[].class);
            infoMap.put(HttpMessagePart.BODY, body);
        }
    }

    /**
     * Extracts details about a request message into the given EnumMap object.
     * The map keys (of type {@link HttpMessagePart}) correspond to various
     * parts of the message. If the request contains a message body, it should
     * be represented as a DOM Document node or as an object having a meaningful
     * toString() implementation.
     *
     * @param req An object representing an HTTP request message.
     * @param infoMap The collection into which message elements are put; if
     * null, a new one is created. Existing values may be replaced.
     */
    public static void extractRequestInfo(ClientRequest req,
            EnumMap<HttpMessagePart, Object> infoMap) {
        if (null == infoMap) {
            infoMap = new EnumMap(HttpMessagePart.class);
        }
        infoMap.put(HttpMessagePart.URI, req.getURI());
        infoMap.put(HttpMessagePart.HEADERS, req.getHeaders());
        Object entity = req.getEntity();
        if (null != entity) {
            String body;
            if (Document.class.isInstance(entity)) {
                Document doc = Document.class.cast(entity);
                body = XMLUtils.writeNodeToString(doc);
            } else {
                body = entity.toString();
            }
            infoMap.put(HttpMessagePart.BODY, body);
        }
    }

    /**
     * Creates a copy of the given MediaType object but without any parameters.
     *
     * @param mediaType A MediaType descriptor.
     * @return A new (immutable) MediaType object having the same type and
     * subtype.
     */
    public static MediaType removeParameters(MediaType mediaType) {
        return new MediaType(mediaType.getType(), mediaType.getSubtype());
    }
}
