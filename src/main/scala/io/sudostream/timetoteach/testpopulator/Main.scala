package io.sudostream.timetoteach.testpopulator

import org.mongodb.scala.{Completed, Document, MongoCollection}

import scala.concurrent.Future

object Main extends App
  with MongoDbHelper
  with UsersDenormalisedInserter
  with SchoolsInserter
  with ConsoleMessages {

  println(startupMessage)
  println("is it MINIKUBE????????????????????")

  val mongoKeystorePassword = try {
    sys.env("MONGODB_KEYSTORE_PASSWORD")
  } catch {
    case e: Exception => ""
  }

  val isMinikubeRun  :Boolean = try {
    if (sys.env("MINIKUBE_RUN") == "true") {
      println("MINIKUBE = yes")
      System.setProperty("javax.net.ssl.keyStore", "/etc/ssl/cacerts")
      System.setProperty("javax.net.ssl.keyStorePassword", mongoKeystorePassword)
      System.setProperty("javax.net.ssl.trustStore", "/etc/ssl/cacerts")
      System.setProperty("javax.net.ssl.trustStorePassword", mongoKeystorePassword)
      true
    } else {
      println("MINIKUBE = no")
      false
    }
  }
  catch {
    case e: Exception => ""
      println("MINIKUBE = no with error")
      false
  }

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
