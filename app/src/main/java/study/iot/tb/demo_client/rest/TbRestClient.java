package study.iot.tb.demo_client.rest;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
//import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.alarm.Alarm;
import org.thingsboard.server.common.data.asset.Asset;
import org.thingsboard.server.common.data.id.AssetId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.security.DeviceCredentials;
import org.thingsboard.server.common.data.security.DeviceCredentialsType;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
//import javax.xml.transform.stax.*;

import javax.security.auth.login.LoginException;

import study.iot.tb.demo_client.util.MsgHandler;

public class TbRestClient implements ClientHttpRequestInterceptor{
    private static final String TAG = "TbRestClient";
    private static final String JWT_TOKEN_HEADER_PARAM = "X-Authorization";
    private static RestTemplate restTemplate = null;
    private String token;
    private String baseURL="";
    private Context mContext;
    public static final int HTTP_LOGINOK = 1004; // 连接成功
    public static final int HTTP_UNAUTHORIZED = 1005;
    public static final int HTTP_LOGINFAILED = 1006;
    public static final int HTTP_NO_TOKEN = 1007;
    public static final int HTTP_CREATEOK = 1008;
    public static final int HTTP_EXIST_DEVICE = 1009;
    private ArrayList<MsgHandler> listenerList = new ArrayList<MsgHandler>();


    public TbRestClient(Context context) {

        restTemplate = new RestTemplate();
        mContext = context;
    }
    public void login(String username, String password,String server_address){
        baseURL=server_address;
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", username);
        loginRequest.put("password", password);
        ResponseEntity<JsonNode> tokenInfo = null;
        tokenInfo = restTemplate.postForEntity(baseURL + "/api/auth/login", loginRequest, JsonNode.class);
        HttpStatus responseCode= tokenInfo.getStatusCode();
        if(responseCode== HttpStatus.OK){
            this.token = tokenInfo.getBody().get("token").asText();
            dispachEvent(HTTP_LOGINOK);
        }
        else if(responseCode==HttpStatus.UNAUTHORIZED){
            dispachEvent(HTTP_UNAUTHORIZED);
        }
        else{
            dispachEvent(HTTP_LOGINFAILED);
        }
        //设置拦截器
        restTemplate.setInterceptors(Collections.<ClientHttpRequestInterceptor>singletonList(this));
    }

    public void findDevice(String name) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("deviceName", name);
        try {
            ResponseEntity<Device> deviceEntity = restTemplate.getForEntity(baseURL + "/api/tenant/devices?deviceName={deviceName}", Device.class, params);
            //return Optional.of(deviceEntity.getBody());
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                //return Optional.empty();
            } else {
                throw exception;
            }
        }
    }

    public void findCustomer(String title) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("customerTitle", title);
        try {
            ResponseEntity<Customer> customerEntity = restTemplate.getForEntity(baseURL + "/api/tenant/customers?customerTitle={customerTitle}", Customer.class, params);
            //return Optional.of(customerEntity.getBody());
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                //return Optional.empty();
            } else {
                throw exception;
            }
        }
    }

    public void findAsset(String name) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("assetName", name);
        try {
            ResponseEntity<Asset> assetEntity = restTemplate.getForEntity(baseURL + "/api/tenant/assets?assetName={assetName}", Asset.class, params);
            //return Optional.of(assetEntity.getBody());
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                //return Optional.empty();
            } else {
                throw exception;
            }
        }
    }

    public void getAttributes(String accessToken, String clientKeys, String sharedKeys) {
        Map<String, String> params = new HashMap<>();
        params.put("accessToken", accessToken);
        params.put("clientKeys", clientKeys);
        params.put("sharedKeys", sharedKeys);
        try {
            ResponseEntity<JsonNode> telemetryEntity = restTemplate.getForEntity(baseURL + "/api/v1/{accessToken}/attributes?clientKeys={clientKeys}&sharedKeys={sharedKeys}", JsonNode.class, params);
            //return Optional.of(telemetryEntity.getBody());
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                //return Optional.empty();
            } else {
                throw exception;
            }
        }
    }

    public Customer createCustomer(Customer customer) {
        return restTemplate.postForEntity(baseURL + "/api/customer", customer, Customer.class).getBody();
    }

    public Customer createCustomer(String title) {
        Customer customer = new Customer();
        customer.setTitle(title);
        return restTemplate.postForEntity(baseURL + "/api/customer", customer, Customer.class).getBody();
    }

    public Device createDevice(String name, String type,String server_address) {
        baseURL = server_address;
        Device device = new Device();
        device.setName(name);
        device.setType(type);
        ResponseEntity<Device> deviceInfo = null;
        try {
            deviceInfo = restTemplate.postForEntity(baseURL + "/api/device", device, Device.class);
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            HttpStatus errorCode=e.getStatusCode();
            if (errorCode == HttpStatus.BAD_REQUEST) {
                dispachEvent(HTTP_EXIST_DEVICE);
                Log.i(TAG, "createDevice: Device already exist!");
                return null;
            } else if (errorCode == HttpStatus.UNAUTHORIZED) {
                dispachEvent(HTTP_NO_TOKEN);
                Log.i(TAG, "createDevice:No login token or token out of date,pls login first.");
                return null;
            }else{
                Log.i(TAG, "error code="+errorCode);
                return null;
            }
        }
        Log.i(TAG, "createDevice: "+deviceInfo);
        HttpStatus responseCode = deviceInfo.getStatusCode();
        if (responseCode == HttpStatus.OK) {
            Log.i(TAG, "createDevice: ======+"+ deviceInfo.getBody());
            dispachEvent(HTTP_CREATEOK);
            return deviceInfo.getBody();
        }
        return null;
    }

    public DeviceCredentials updateDeviceCredentials(DeviceId deviceId, String token) {
        DeviceCredentials deviceCredentials = getCredentials(deviceId);
        deviceCredentials.setCredentialsType(DeviceCredentialsType.ACCESS_TOKEN);
        deviceCredentials.setCredentialsId(token);
        return saveDeviceCredentials(deviceCredentials);
    }

    public DeviceCredentials saveDeviceCredentials(DeviceCredentials deviceCredentials) {
        return restTemplate.postForEntity(baseURL + "/api/device/credentials", deviceCredentials, DeviceCredentials.class).getBody();
    }

    public Device createDevice(Device device) {
        return restTemplate.postForEntity(baseURL + "/api/device", device, Device.class).getBody();
    }

    public Asset createAsset(Asset asset) {
        return restTemplate.postForEntity(baseURL + "/api/asset", asset, Asset.class).getBody();
    }

    public Asset createAsset(String name, String type) {
        Asset asset = new Asset();
        asset.setName(name);
        asset.setType(type);
        return restTemplate.postForEntity(baseURL + "/api/asset", asset, Asset.class).getBody();
    }

    public Alarm createAlarm(Alarm alarm) {
        return restTemplate.postForEntity(baseURL + "/api/alarm", alarm, Alarm.class).getBody();
    }

    public void deleteCustomer(CustomerId customerId) {
        restTemplate.delete(baseURL + "/api/customer/{customerId}", customerId);
    }

    public void deleteDevice(DeviceId deviceId) {
        restTemplate.delete(baseURL + "/api/device/{deviceId}", deviceId);
    }

    public void deleteAsset(AssetId assetId) {
        restTemplate.delete(baseURL + "/api/asset/{assetId}", assetId);
    }

    public Device assignDevice(CustomerId customerId, DeviceId deviceId) {
        return restTemplate.postForEntity(baseURL + "/api/customer/{customerId}/device/{deviceId}", null, Device.class,
                customerId.toString(), deviceId.toString()).getBody();
    }

    public Asset assignAsset(CustomerId customerId, AssetId assetId) {
        return restTemplate.postForEntity(baseURL + "/api/customer/{customerId}/asset/{assetId}", null, Asset.class,
                customerId.toString(), assetId.toString()).getBody();
    }

    public EntityRelation makeRelation(String relationType, EntityId idFrom, EntityId idTo) {
        EntityRelation relation = new EntityRelation();
        relation.setFrom(idFrom);
        relation.setTo(idTo);
        relation.setType(relationType);
        return restTemplate.postForEntity(baseURL + "/api/relation", relation, EntityRelation.class).getBody();
    }

    public DeviceCredentials getCredentials(DeviceId id) {
        return restTemplate.getForEntity(baseURL + "/api/device/" + id.getId().toString() + "/credentials", DeviceCredentials.class).getBody();
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public String getToken() {
        return token;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] bytes, ClientHttpRequestExecution execution) throws IOException {
        HttpRequest wrapper = new HttpRequestWrapper(request);
        wrapper.getHeaders().set(JWT_TOKEN_HEADER_PARAM, "Bearer " + token);
        return execution.execute(wrapper, bytes);
    }
    public void dispachEvent(int event) {
        Log.i(TAG, "dispachEvent listenerList size " + listenerList.size());
        for (MsgHandler msgHandler : listenerList)
        {
            Log.i(TAG, "dispatchEvent event " + event);
            msgHandler.onEvent(event);
        }
    }

    public void addListener(MsgHandler msgHandler) {
        if (!listenerList.contains(msgHandler)) {
            listenerList.add(msgHandler);
        }
    }

}
