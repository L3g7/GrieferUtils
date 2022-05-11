package dev.l3g7.griefer_utils.event.event_bus;

public class Event {

    public static class Cancelable extends Event {

        private boolean isCanceled = false;

        public boolean isCanceled() {
            return isCanceled;
        }

        public void setCanceled(boolean canceled) {
            isCanceled = canceled;
        }

    }

}
