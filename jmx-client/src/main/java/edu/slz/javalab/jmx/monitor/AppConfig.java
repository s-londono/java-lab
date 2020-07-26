package edu.slz.javalab.jmx.monitor;

import com.sun.istack.internal.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "jmxmonitor")
public class AppConfig {

  @NotNull
  private Boolean jmxremoteAuthenticate;

  @NotNull
  private Boolean jmxremoteSslEnabled;

  @NotNull
  private Boolean jmxremoteSslNeedClientAuth;

  @NotNull
  private Boolean jmxremoteRegistrySsl;

  public Boolean getJmxremoteAuthenticate() {
    return jmxremoteAuthenticate;
  }

  public void setJmxremoteAuthenticate(Boolean jmxremoteAuthenticate) {
    this.jmxremoteAuthenticate = jmxremoteAuthenticate;
  }

  public Boolean getJmxremoteSslEnabled() {
    return jmxremoteSslEnabled;
  }

  public void setJmxremoteSslEnabled(Boolean jmxremoteSslEnabled) {
    this.jmxremoteSslEnabled = jmxremoteSslEnabled;
  }

  public Boolean getJmxremoteSslNeedClientAuth() {
    return jmxremoteSslNeedClientAuth;
  }

  public void setJmxremoteSslNeedClientAuth(Boolean jmxremoteSslNeedClientAuth) {
    this.jmxremoteSslNeedClientAuth = jmxremoteSslNeedClientAuth;
  }

  public Boolean getJmxremoteRegistrySsl() {
    return jmxremoteRegistrySsl;
  }

  public void setJmxremoteRegistrySsl(Boolean jmxremoteRegistrySsl) {
    this.jmxremoteRegistrySsl = jmxremoteRegistrySsl;
  }
}
