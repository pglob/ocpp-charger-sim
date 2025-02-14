package com.sim_backend.websockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.*;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;

/**
 * Custom SSLSocketFactory that ensures the Server Name Indication (SNI) is properly set on each
 * SSLSocket.
 *
 * <p>SNI allows a client to indicate the hostname it is connecting to during the TLS handshake.
 * This is important when multiple domains are served from the same IP address, so the server can
 * select the appropriate certificate.
 */
public class SniSSLSocketFactory extends SSLSocketFactory {
  // Underlying SSLSocketFactory that creates the actual socket instances
  private final SSLSocketFactory delegate;
  // Default hostname to use when no host is provided in createSocket()
  private final String defaultHost;
  // Default port to use when no port is provided in createSocket()
  private final int defaultPort;

  /**
   * Constructs a new SniSSLSocketFactory.
   *
   * @param delegate A SSLSocketFactory.
   * @param defaultHost The default hostname for socket creation.
   * @param defaultPort The default port for socket creation.
   */
  public SniSSLSocketFactory(SSLSocketFactory delegate, String defaultHost, int defaultPort) {
    this.delegate = delegate;
    this.defaultHost = defaultHost;
    this.defaultPort = defaultPort;
  }

  /**
   * Enables Server Name Indication (SNI) on the provided socket.
   *
   * @param socket The socket on which to enable SNI.
   * @param host The hostname to set for SNI.
   * @return The socket with SNI configured.
   */
  private Socket enableSNI(Socket socket, String host) {
    if (socket instanceof SSLSocket && host != null && !host.isEmpty()) {
      SSLSocket sslSocket = (SSLSocket) socket;
      SSLParameters sslParameters = sslSocket.getSSLParameters();
      List<SNIServerName> serverNames = Collections.singletonList(new SNIHostName(host));
      sslParameters.setServerNames(serverNames);
      sslSocket.setSSLParameters(sslParameters);
    }
    return socket;
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return delegate.getDefaultCipherSuites();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return delegate.getSupportedCipherSuites();
  }

  /**
   * Creates a connected socket using the default host and port.
   *
   * <p>This method ensures that if a library calls the no-argument createSocket(), a socket
   * connected to the intended default host and port is returned.
   *
   * @return A connected SSLSocket with SNI enabled.
   * @throws IOException If an I/O error occurs.
   */
  @Override
  public Socket createSocket() throws IOException {
    return createSocket(defaultHost, defaultPort);
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException {
    Socket socket = delegate.createSocket(host, port);
    return enableSNI(socket, host);
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
      throws IOException {
    Socket socket = delegate.createSocket(host, port, localHost, localPort);
    return enableSNI(socket, host);
  }

  @Override
  public Socket createSocket(InetAddress address, int port) throws IOException {
    Socket socket = delegate.createSocket(address, port);
    return enableSNI(socket, address.getHostName());
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException {
    Socket socket = delegate.createSocket(address, port, localAddress, localPort);
    return enableSNI(socket, address.getHostName());
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose)
      throws IOException {
    Socket socket = delegate.createSocket(s, host, port, autoClose);
    return enableSNI(socket, host);
  }
}
