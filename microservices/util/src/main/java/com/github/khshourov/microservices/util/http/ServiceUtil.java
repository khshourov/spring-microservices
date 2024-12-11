package com.github.khshourov.microservices.util.http;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceUtil {
  private final String port;
  private final String serviceAddress;

  @Autowired
  public ServiceUtil(@Value("${server.port}") String port) {
    this.port = port;
    this.serviceAddress = this.findMyHostname() + "/" + this.findMyIpAddress() + ":" + this.port;
  }

  public String getPort() {
    return this.port;
  }

  public String getServiceAddress() {
    return this.serviceAddress;
  }

  private String findMyHostname() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return "unknown host name";
    }
  }

  private String findMyIpAddress() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      return "unknown IP address";
    }
  }
}
