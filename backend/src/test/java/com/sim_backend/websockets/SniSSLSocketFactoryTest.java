package com.sim_backend.websockets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

public class SniSSLSocketFactoryTest {

  private static final String DEFAULT_HOST = "defaultHost";
  private static final int DEFAULT_PORT = 443;

  private SSLSocketFactory delegate;
  private SniSSLSocketFactory sniSSLSocketFactory;

  @BeforeEach
  void setUp() {
    delegate = mock(SSLSocketFactory.class);
    sniSSLSocketFactory = new SniSSLSocketFactory(delegate, DEFAULT_HOST, DEFAULT_PORT);
  }

  /**
   * Verifies that the no-arg createSocket() calls delegate with default host and port, and sets SNI
   * on an SSLSocket.
   */
  @Test
  void testCreateSocket_noArg_usesDefaultHostAndPort() throws IOException {
    SSLSocket mockSslSocket = mock(SSLSocket.class);
    SSLParameters sslParameters = new SSLParameters();
    when(mockSslSocket.getSSLParameters()).thenReturn(sslParameters);

    // Configure delegate to return the mock SSLSocket for the default host/port
    when(delegate.createSocket(DEFAULT_HOST, DEFAULT_PORT)).thenReturn(mockSslSocket);

    // Test
    Socket socket = sniSSLSocketFactory.createSocket();

    // Verify that delegate's createSocket was called with defaultHost and defaultPort
    verify(delegate, times(1)).createSocket(DEFAULT_HOST, DEFAULT_PORT);

    // Verify that we got back our mock SSLSocket
    assertNotNull(socket);

    // Verify that setSSLParameters was called with the correct SNI
    verify(mockSslSocket).setSSLParameters(argThat(new SniMatcher(DEFAULT_HOST)));
  }

  /** Verifies that createSocket(host, port) calls the delegate correctly and sets SNI. */
  @Test
  void testCreateSocket_withHostAndPort_setsSNI() throws IOException {
    String customHost = "example.com";
    int customPort = 12345;

    // Mock an SSLSocket returned by the delegate
    SSLSocket mockSslSocket = mock(SSLSocket.class);
    SSLParameters sslParameters = new SSLParameters();
    when(mockSslSocket.getSSLParameters()).thenReturn(sslParameters);

    // Configure delegate to return the mock SSLSocket
    when(delegate.createSocket(customHost, customPort)).thenReturn(mockSslSocket);

    // Test
    Socket socket = sniSSLSocketFactory.createSocket(customHost, customPort);

    // Verify the delegate createSocket was called with the provided host and port
    verify(delegate, times(1)).createSocket(customHost, customPort);

    // Verify SNI was configured
    verify(mockSslSocket).setSSLParameters(argThat(new SniMatcher(customHost)));
    assertNotNull(socket);
  }

  /**
   * Verifies that if the underlying socket is not an SSLSocket, no SNI configuration is attempted.
   */
  @Test
  void testCreateSocket_notSSLSocket_noSNI() throws IOException {
    String host = "example.com";
    int port = 12345;

    // Mock a plain Socket (not SSLSocket)
    Socket mockSocket = mock(Socket.class);

    // Configure delegate to return the plain Socket
    when(delegate.createSocket(host, port)).thenReturn(mockSocket);

    // Test
    Socket socket = sniSSLSocketFactory.createSocket(host, port);

    // Verify the delegate createSocket was called
    verify(delegate, times(1)).createSocket(host, port);

    // Verify we did not set SSL parameters
    verifyNoMoreInteractions(mockSocket);
    assertNotNull(socket);
  }

  /** Helper to check the SNI host name. */
  private static class SniMatcher implements ArgumentMatcher<SSLParameters> {
    private final String expectedHost;

    private SniMatcher(String expectedHost) {
      this.expectedHost = expectedHost;
    }

    @Override
    public boolean matches(SSLParameters sslParameters) {
      List<SNIServerName> sniServerNames = sslParameters.getServerNames();
      if (sniServerNames == null || sniServerNames.size() != 1) {
        return false;
      }
      SNIServerName serverName = sniServerNames.get(0);
      if (serverName instanceof SNIHostName) {
        return ((SNIHostName) serverName).getAsciiName().equals(expectedHost);
      }
      return false;
    }
  }
}
