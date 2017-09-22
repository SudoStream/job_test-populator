package io.sudostream.timetoteach.testpopulator

import org.mongodb.scala.{Completed, Document, MongoCollection}

import scala.concurrent.Future

object Main extends App
  with MongoDbHelper with UsersDenormalisedInserter with ConsoleMessages {

  println(startupMessage)
  val users: List[User] = decodeUsersForDatabaseInjestion
  val usersCollection: MongoCollection[Document] = getUsersCollection
  val insertToDbFuture: Future[Completed] = insertUsersToDatabase(users, usersCollection)

  while (!insertToDbFuture.isCompleted) {
    println(s"Waiting for db inserts of Users to complete...")
    Thread.currentThread().join(100L)
  }

  println(finishedMessage)
  System.exit(0)

}

trait ConsoleMessages {
  lazy val startupMessage = "TestPopulator starting..."
  lazy val finishedMessage = "TestPopulator done."
}
