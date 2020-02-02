package models;

import javafx.beans.property.SimpleStringProperty;

/**
 * Can be used to represent any word in the ontology in a full (namespace :
 * name) form or short (name) form
 * 
 * @author alexanpl
 *
 */
public class OntologyWordModel {
	private SimpleStringProperty namespace;
	private SimpleStringProperty shortName;
	private SimpleStringProperty fullName;
	
	public OntologyWordModel(){
		namespace = new SimpleStringProperty("");
		shortName = new SimpleStringProperty("");
		this.fullName = new SimpleStringProperty("");
	}

	public OntologyWordModel(String fullName) {
		this();
		if (fullName.contains(":")) {
			if (fullName.split(":").length == 2) {
				namespace.set(fullName.split(":")[0]);
				shortName.set(fullName.split(":")[1]);
				fullName = namespace + ":" + shortName;
			} else {
				if (fullName.toCharArray()[0] == ':') {
					shortName.set(fullName);
				} else {
					namespace.set(fullName);
				}
			}
		} else {
			shortName.set(fullName);
		}
		if (!(namespace.get()+shortName.get()).isEmpty())
			this.fullName.set(namespace.get()+":"+shortName.get());
		else 
			this.fullName.set(namespace.get()+shortName.get());
	}

	public OntologyWordModel(String namespace, String shortName) {
		this();
		this.namespace.set(namespace.replace(":", ""));
		this.shortName.set(shortName.replace(":", ""));
		this.fullName.set(this.namespace.get() + ":" + this.shortName.get());
	}

	public String getShortName() {
		return shortName.get();
	}

	public String getFullName() {
		return fullName.get();
	}

	public String getNamespace() {
		return namespace.get();
	}

	@Override
	public String toString() {
		return shortName.get();
	}
	
	@Override
	public boolean equals(Object o2){
		if (o2 instanceof OntologyWordModel){
			return ((OntologyWordModel) o2).getFullName().equals(fullName.get());
		}
		else {
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		return fullName.get().hashCode();
	}

	public void setSelf(OntologyWordModel ontWord) {
		this.namespace.set(ontWord.getNamespace());
		this.shortName.set(ontWord.getShortName());
		this.fullName.set(ontWord.getFullName());
	}
}
