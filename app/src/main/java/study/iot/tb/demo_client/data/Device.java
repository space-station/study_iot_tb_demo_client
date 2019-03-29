package study.iot.tb.demo_client.data;


public class Device {

    private TenantId tenantId;
    private CustomerId customerId;
    private String name;
    private String type;
    private DeviceId id;


    public Device(String name,String type) {
        this.name = name;
        this.type = type;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public void setTenantId(TenantId tenantId) {
        this.tenantId = tenantId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public void setCustomerId(CustomerId customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DeviceId getId() {
        return id;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Device [tenantId=");
        builder.append(tenantId);
        builder.append(", customerId=");
        builder.append(customerId);
        builder.append(", name=");
        builder.append(name);
        builder.append(", type=");
        builder.append(type);
//        builder.append(", additionalInfo=");
//        builder.append(getAdditionalInfo());
//        builder.append(", createdTime=");
//        builder.append(this.createdTime);
        builder.append(", id=");
        builder.append(this.id);
        builder.append("]");
        return builder.toString();
    }
}
