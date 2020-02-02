package childViews;

public interface SaveableObject {

	/**
	 * Returns a string that represents the Object's serialized form (e.g. to be
	 * saved permanently).
	 * 
	 * @return String
	 */
	public abstract String toBytes();

}
