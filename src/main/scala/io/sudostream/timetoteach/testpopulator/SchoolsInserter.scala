package io.sudostream.timetoteach.testpopulator

import argonaut.Argonaut._
import org.mongodb.scala.{Completed, Document, MongoCollection}

import scala.concurrent.Future
import scala.io.Source

trait SchoolsInserter {

  def decodeSchoolsForDatabaseInjestion: List[School] = {
    val filename = "/schools.json"
    val filenameAsInputStream = getClass.getResourceAsStream(filename)
    val input = Source.fromInputStream(filenameAsInputStream).mkString
    input.decodeOption[List[School]].getOrElse(Nil)
  }

  def insertSchoolsToDatabase(schools: List[School],
                              schoolsCollection: MongoCollection[Document]): Future[Completed] = {

    val docsToInsert: Seq[Document] = schools.map {
      school =>
        Document(
          "_id" -> java.util.UUID.randomUUID().toString,
          "name" -> school.name,
          "address" -> school.address,
          "postCode" -> school.postCode,
          "telephone" -> school.telephone,
          "localAuthority" -> school.localAuthority,
          "country" -> school.country
        )
    }

    schoolsCollection.insertMany(docsToInsert).toFuture
  }
}