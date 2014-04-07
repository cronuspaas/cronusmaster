package resources;

import java.util.Map;

public interface IUserInputs<I> {

	/** overrides allowed */
	public I getSampleUserInputs();
	
	/** apply overrides */
	public void setUserInputs(I inputs);
	
	/** get user input of a name */
	public I getUserInputs();

}
