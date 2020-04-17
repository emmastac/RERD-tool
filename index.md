
                         RERD tool


What is it?
-----------
RERD is a Requirements Specification & Validation tool for the specification, formalization and validation of requirements. 
RERD supports users in the following tasks: 
  - pattern-based specification of requirements
  - guided derivation of formal properties that capture the semantics of requirements
  - ontology-based validation of requirements
  - model-based system design using the [BIP component framework](http://www-verimag.imag.fr/Rigorous-Design-of-Component-Based.html) that allows to incrementally enforce formal properties and perform properties verification 

Please find the following related articles:
1. Stachtiari, E., Mavridou, A., Katsaros, P., Bliudze, S. and Sifakis, J., 2018. Early validation of system requirements and design through correctness-by-construction. Journal of Systems and Software, 145, pp.52-78. [[doi]](https://doi.org/10.1016/j.jss.2018.07.053) 
2. Mavridou, A., Stachtiari, E., Bliudze, S., Ivanov, A., Katsaros, P. and Sifakis, J., 2016, October. Architecture-based design: A satellite on-board software case study. In International Workshop on Formal Aspects of Component Software (pp. 260-279). Springer, Cham. [[doi]](https://link.springer.com/chapter/10.1007/978-3-319-57666-4_16) 


Latest Version
------------------

Currently, only one version is available and can be downloaded [here](https://github.com/emmastac/RERD-tool.git)

Dependencies
--------------
All dependencies are under rerd-tool_lib/


Contents
-----------

  - A /src folder with the RERD's classes
  - A /architectureDefinitions folder with BIP models for arcitectures
  - A /componentModels folder with BIP models for system components
  - A /ontologies folder with files for the ontologies 
  - A SyntaxtText.txt file that defines the Boilerplate grammar
  - A Properties.txt file that defines the Property pattern grammar


Installation
------------

No installation is needed for the tool. A Java version (1.7+) is required. 

Execution
------------

RERD has only a grapical user interface.

Run the jar found in jars/rerd.jar with the following command:

	   $ java -jar rerd.jar

Licensing
---------

Licensing information is in a separate file named LICENSE.

 
Related Tools
-------------

[BIP component framework] http://www-verimag.imag.fr/Rigorous-Design-of-Component-Based.html

Contact
--------

To report bugs or to ask support with the use of RERD, feel free to contact me (Emmanouela Stachtiari, Ph.D) at emmanouela.stachtiari@unige.ch.
