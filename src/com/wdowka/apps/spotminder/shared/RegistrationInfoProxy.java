package com.wdowka.apps.spotminder.shared;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

/**
 * A proxy object containing device registration information:
 * email account name, device id, and device registration id.
 */
@ProxyForName("com.wdowka.apps.spotminder.server.RegistrationInfo")
public interface RegistrationInfoProxy extends ValueProxy {
  String getAccountName();
  String getDeviceId();
  String getDeviceRegistrationId();
  void setAccountName(String accountName);
  void setDeviceId(String deviceId);
  void setDeviceRegistrationId(String deviceRegistrationId);
}
