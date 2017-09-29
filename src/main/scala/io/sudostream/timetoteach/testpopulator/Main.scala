package io.sudostream.timetoteach.testpopulator

import org.mongodb.scala.{Completed, Document, MongoCollection}

import scala.concurrent.Future

object Main extends App
  with MongoDbHelper
  with UsersDenormalisedInserter
  with SchoolsInserter
  with ConsoleMessages {

  println(startupMessage)
  insertUsers()
  insertSchools()
  println(finishedMessage)

  System.exit(0)


  private def insertUsers() {
    val users: List[User] = decodeUsersForDatabaseInjestion
    val usersCollection: MongoCollection[Document] = getUsersCollection
    val insertToDbFuture: Future[Completed] = insertUsersToDatabase(users, usersCollection)

    while (!insertToDbFuture.isCompleted) {
      println(s"Waiting for db inserts of Users to complete...")
      Thread.currentThread().join(100L)
    }
  }

  private def insertSchools() {
    val schools: List[School] = decodeSchoolsForDatabaseInjestion
    val schoolsCollection: MongoCollection[Document] = getSchoosCollection
    val insertToDbFuture: Future[Completed] = insertSchoolsToDatabase(schools, schoolsCollection)

    while (!insertToDbFuture.isCompleted) {
      println(s"Waiting for db inserts of Schools to complete...")
      Thread.currentThread().join(100L)
    }

  }

}

trait ConsoleMessages {
  lazy val startupMessage = "TestPopulator starting..."
  lazy val finishedMessage = "TestPopulator done."
}
