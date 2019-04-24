package hello;

public class LockRequest {

    private boolean isLockNeeded;
    private String itemId;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }


    public boolean getLock() {
        return isLockNeeded;
    }

    public void setLock(boolean isLockNeeded) {
        this.isLockNeeded = isLockNeeded;
    }
}