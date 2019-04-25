package svp.lock_service.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ZKManagerImpl implements ZKManager {

    private static ZooKeeper zooKeeper;
    private static ZKConnection zkConnection;

    public ZKManagerImpl() {
        zkConnection = new ZKConnection();
        try {
            zooKeeper = zkConnection.connect("localhost");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void create(String path, byte[] data) {
        try {
            zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void create(String path, String data) {
        try {
            zooKeeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Object getZNodeData(String path, boolean watchFlag) {
        byte[] data = new byte[0];
        try {
            data = zooKeeper.getData(path, watchFlag, null);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    public void update(String path, byte[] data) {
        int nodeVersion = 0;
        try {
            nodeVersion = zooKeeper.exists(path, true).getVersion();
            zooKeeper.setData(path, data, nodeVersion);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void delete(String path) {
        int nodeVersion = 0;
        try {
            nodeVersion = zooKeeper.exists(path, true).getVersion();
            zooKeeper.delete(path, nodeVersion);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            return zooKeeper.exists(path, true) != null;
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void closeConnection() {
        try {
            zkConnection.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
