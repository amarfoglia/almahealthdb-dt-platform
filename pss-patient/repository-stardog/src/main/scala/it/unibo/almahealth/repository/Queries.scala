package it.unibo.almahealth.repository

import org.hl7.fhir.r4.model.Resource

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
    CONSTRUCT {
      ?subject ?pred ?object.
    }
    WHERE {
      {
          SELECT ?compositionRef
          WHERE {
              ?patient a fhir:Patient ;
                  fhir:Patient.identifier  [
                      fhir:Identifier.system   / fhir:value  "http://terminology.hl7.it/CodeSystem/it-tipoEntita/mef"^^xsd:anyURI ;
                      fhir:Identifier.value    / fhir:value  ?fiscalCode ;
                  ] ,
                  [
                      fhir:Identifier.value / fhir:value  ?identifier ;
                      fhir:Identifier.use   / fhir:value "secondary"
                  ]

              BIND(CONCAT("Patient/", ?identifier) AS ?patientRef)

              ?compositionRef a fhir:Composition ;
                  fhir:Composition.date / fhir:value ?date ;
                  fhir:Composition.subject / fhir:Reference.reference / fhir:value ?patientRef ;
          }
          ORDER BY DESC(?date)
          LIMIT 1
      }

      ?compositionRef fhir:Composition.section [
          fhir:Composition.section.code / fhir:CodeableConcept.coding / fhir:Coding.code / fhir:value  "${sectionLoincCode}" ;
          fhir:Composition.section.entry / fhir:Reference.reference / fhir:value ?resourceRef
      ]

      BIND(REPLACE(?resourceRef, "${fhirResourceName}/", "") AS ?resourceId)

      ?resource a fhir:${fhirResourceName};
          fhir:${fhirResourceName}.identifier [
              fhir:Identifier.use / fhir:value "secondary" ;
              fhir:Identifier.value / fhir:value ?resourceId
          ] .

      ?resource  (<>|!<>)*  ?subject  .
      ?subject  ?pred       ?object .
    }""".stripMargin

  def insertResource(serialized: String, resource: Resource): String = s"""
    |INSERT { ${serialized} }
    |WHERE {
    |  FILTER NOT EXISTS {
    |    ?resource fhir:${resource.getResourceType()}.identifier [
    |      fhir:Identifier.value / fhir:value  "${resource.getId.drop(9)}" ;
    |      fhir:Identifier.use   / fhir:value "secondary"
    |    ]
    |  }
    |}
    """.stripMargin
