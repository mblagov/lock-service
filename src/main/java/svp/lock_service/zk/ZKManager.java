package svp.lock_service.zk;

public interface ZKManager {
    public void create(String path, byte[] data);

    public void create(String path, String data);

    public Object getZNodeData(String path, boolean watchFlag);

    public void update(String path, byte[] data);

    public void delete(String path);

    public boolean exists(String path);
}
