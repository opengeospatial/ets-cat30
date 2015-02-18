package org.opengis.cite.cat30.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.util.EnumMap;
import java.util.logging.Level;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.urlconnection.HttpURLConnectionFactory;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;

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
     * Extracts details about a response message into the given EnumMap object.
     * The map keys (of type {@link HttpMessagePart}) correspond to various
     * parts of the message.
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
            byte[] body = new byte[rsp.getLength()];
            DataInputStream dis = new DataInputStream(rsp.getEntityInputStream());
            try {
                dis.readFully(body);
            } catch (IOException ex) {
                TestSuiteLogger.log(Level.WARNING,
                        "extractResponseInfo: Failed to read entity.", ex);
            }
            infoMap.put(HttpMessagePart.BODY, body);
        }
    }
}
