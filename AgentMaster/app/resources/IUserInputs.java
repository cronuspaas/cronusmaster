package resources;

import java.util.Map;

public interface IUserInputs<I> {

	/** apply overrides */
	public void setUserInputs(I inputs);
	
	/** get user input of a name */
	public I getUserInputs();

}
