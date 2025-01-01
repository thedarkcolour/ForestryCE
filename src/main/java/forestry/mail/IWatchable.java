package forestry.mail;

public interface IWatchable {
    void setDirty();

    boolean registerUpdateWatcher(Watcher updateWatcher);

    boolean unregisterUpdateWatcher(Watcher updateWatcher);

    interface Watcher {
        void onWatchableUpdate();
    }
}
