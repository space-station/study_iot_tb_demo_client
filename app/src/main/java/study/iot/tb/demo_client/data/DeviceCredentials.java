package study.iot.tb.demo_client.data;

import study.iot.tb.demo_client.data.DeviceCredentialsId;
import study.iot.tb.demo_client.data.DeviceId;

public class DeviceCredentials {
    private DeviceId deviceId;
    private DeviceCredentialsType credentialsType;
    private String credentialsId;
    private String credentialsValue;

    public DeviceCredentials() {
    }

    public DeviceCredentials(DeviceCredentialsId id) {
        this.credentialsId= id.getId();
    }

    public DeviceCredentials(DeviceCredentials deviceCredentials) {
        this.deviceId = deviceCredentials.getDeviceId();
        this.credentialsType = deviceCredentials.getCredentialsType();
        this.credentialsId = deviceCredentials.getCredentialsId();
        this.credentialsValue = deviceCredentials.getCredentialsValue();
    }

    public DeviceId getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(DeviceId deviceId) {
        this.deviceId = deviceId;
    }

    public DeviceCredentialsType getCredentialsType() {
        return this.credentialsType;
    }

    public void setCredentialsType(DeviceCredentialsType credentialsType) {
        this.credentialsType = credentialsType;
    }

    public String getCredentialsId() {
        return this.credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getCredentialsValue() {
        return this.credentialsValue;
    }

    public void setCredentialsValue(String credentialsValue) {
        this.credentialsValue = credentialsValue;
    }

}
