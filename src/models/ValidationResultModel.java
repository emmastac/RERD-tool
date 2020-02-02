package models;

import java.util.ArrayList;
import java.util.HashSet;

public class ValidationResultModel {
	HashSet<ArrayList<OntologyWordModel>> allResults;
	
	public ValidationResultModel(){
		allResults = new HashSet<>();
	}
	
	public void addResults(ArrayList<OntologyWordModel> resultSet){
		allResults.add(resultSet);
	}

	public HashSet<ArrayList<OntologyWordModel>> getAllResults() {
		return allResults;
	}
	
	
}
