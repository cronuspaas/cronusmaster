package resources.job;

/**
 * base interval job
 * @author binyu
 *
 */
public abstract class BaseIntervalJob implements IntervalJob {
	
	/** name of the job, has to be unique */
	protected String name;
	
	/** interval in minutes */
	protected int intervalInMinute;
	
	/** job status */
	protected boolean enabled;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int getIntervalInMinute() {
		return intervalInMinute;
	}

	@Override
	public void setIntervalInMinute(int intervalInMinute) {
		this.intervalInMinute = intervalInMinute;
	}
	
	public abstract void runJobAsync();
}
