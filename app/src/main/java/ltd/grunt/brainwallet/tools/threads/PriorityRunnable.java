package ltd.grunt.brainwallet.tools.threads;

import ltd.grunt.brainwallet.tools.threads.Priority;

public class PriorityRunnable implements Runnable {

    private final ltd.grunt.brainwallet.tools.threads.Priority priority;

    public PriorityRunnable(ltd.grunt.brainwallet.tools.threads.Priority priority) {
        this.priority = priority;
    }

    @Override
    public void run() {
        // nothing to do here.
    }

    public Priority getPriority() {
        return priority;
    }

}