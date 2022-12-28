package it.unibo.almahealth.repository

private[repository] object Queries:

  val findById = """
  | CONSTRUCT {
  |   ?patient fhir:nodeRole fhir:treeRoot .
  |   ?s ?p ?o .
  | }
  | WHERE {
  |     ?patient a fhir:Patient ;
  |                 fhir:Patient.identifier  [
  |                     # fhir:Identifier.assigner / fhir:Reference.display / fhir:value  "MEF" ;
  |                     fhir:Identifier.system   / fhir:value  "http://terminology.hl7.it/CodeSystem/it-tipoEntita/mef"^^xsd:anyURI ;
  |                     fhir:Identifier.value    / fhir:value  ?identifier ;
  |                 ] .
  |
  |     ?patient (<>|!<>)* ?s .
  |     ?s ?p ?o .
  | }""".stripMargin

  def makeResource(fhirResourceName: String, sectionLoincCode: String) = s"""
  | CONSTRUCT {
  |   ?subject ?pred ?object.
  | }
  | WHERE {
  |   {
  |     {
  |       SELECT ?compositionRef
  |       WHERE {
  |           ?patient a fhir:Patient ;
  |               fhir:Patient.identifier  [
  |                   fhir:Identifier.system   / fhir:value  "http://terminology.hl7.it/CodeSystem/it-tipoEntita/mef"^^xsd:anyURI ;
  |                   fhir:Identifier.value    / fhir:value  ?identifier ;
  |               ] ;
  |               fhir:Resource.id / fhir:value  ?patientId .
  |
  |           BIND(CONCAT("Patient/", REPLACE(?patientId, "urn:uuid:", "")) AS ?patientRef)
  |
  |           ?compositionRef a fhir:Composition ;
  |               fhir:Composition.date / fhir:value ?date ;
  |               fhir:Composition.subject / fhir:Reference.reference / fhir:value ?patientRef ;
  |               fhir:Composition.section [
  |                   fhir:Composition.section.code / fhir:CodeableConcept.coding / fhir:Coding.code / fhir:value  "${sectionLoincCode}" ;
  |                   fhir:Composition.section.entry / fhir:Reference.reference / fhir:value ?resourceRef
  |               ]
  |       }
  |       ORDER BY DESC(?date)
  |       LIMIT 1
  |
  |     }
  |
  |     BIND(CONCAT("urn:uuid:", REPLACE(?resourceRef, "${fhirResourceName}/", "")) AS ?resourceId)
  |
  |     ?resource a fhir:${fhirResourceName};
  |             fhir:Resource.id / fhir:value ?resourceId .
  |   }
  |
  |   ?resource  (<>|!<>)*  ?subject  .
  |   ?subject  ?pred       ?object .
  | }""".stripMargin
