package ltd.grunt.brainwallet.presenter.entities;

public class Fee {
    public final long luxury;
    public final long regular;
    public final long economy;
    public final long timestamp;

    public Fee(long luxury, long regular, long economy, long timestamp) {
        this.luxury = luxury;
        this.regular = regular;
        this.economy = economy;
        this.timestamp = timestamp;
    }
}
