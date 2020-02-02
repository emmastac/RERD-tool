package models;

import java.util.Set;
import utils.Memory;

public class GenericBoilerPlate extends AbstractBoilerPlate{

	public GenericBoilerPlate(String blpCat, String... rawComponents) {
		super(blpCat,rawComponents);
	}
	
	public GenericBoilerPlate(String serializedForm) {
		super(serializedForm);
	}

	@Override
	public String serialize(){
		genBlpID();
		return super.serialize();
	}
	
	public void genBlpID(){
		Set<String> keyset = Memory.getBoilerplatesInMemory().keySet();
		super.genID(keyset);
	}

}