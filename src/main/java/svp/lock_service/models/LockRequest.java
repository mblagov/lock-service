package svp.lock_service.models;

public class LockRequest {

    private boolean isLockNeeded;
    private String itemId;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public boolean isLockNeeded() {
        return isLockNeeded;
    }

    public void setLockNeeded(boolean lockNeeded) {
        isLockNeeded = lockNeeded;
    }
}