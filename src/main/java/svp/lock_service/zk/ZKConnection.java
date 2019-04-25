package svp.lock_service.zk;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZKConnection {
    private ZooKeeper zoo;
    private CountDownLatch connectionLatch = new CountDownLatch(1);

    public ZooKeeper connect(String host)
            throws IOException,
            InterruptedException {
        zoo = new ZooKeeper(host, 2000, we -> {
            if (we.getState().equals(Watcher.Event.KeeperState.SyncConnected)) {
                connectionLatch.countDown();
            }
        });

        connectionLatch.await();
        return zoo;
    }

    public void close() throws InterruptedException {
        zoo.close();
    }
}