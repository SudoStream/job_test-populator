package io.sudostream.timetoteach.testpopulator

import java.util.concurrent.TimeUnit

import com.mongodb.connection.ClusterSettings
import com.typesafe.config.ConfigFactory
import io.sudostream.timetoteach.testpopulator.Main.mongoKeystorePassword
import org.mongodb.scala.connection.{NettyStreamFactoryFactory, SslSettings}
import org.mongodb.scala.{Document, MongoClient, MongoClientSettings, MongoCollection, MongoDatabase, ServerAddress}

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait MongoDbHelper {

  case class DatabaseName(value: String)

  case class CollectionName(value: String)

  private def getCollection(databaseName: DatabaseName, collectionName: CollectionName): MongoCollection[Document] = {
    val config = ConfigFactory.load()

    val mongoClient: MongoClient =
      if ((mongoKeystorePassword == "" || mongoKeystorePassword.isEmpty) && !Main.isMinikubeRun) {
        val mongoDbUri = config.getString("mongodb.connection_uri")
        println(s"mongo uri = '$mongoDbUri'")
        System.setProperty("org.mongodb.async.type", "netty")
        MongoClient(mongoDbUri)
      } else {
        System.setProperty("javax.net.ssl.keyStore", "/etc/ssl/cacerts")
        System.setProperty("javax.net.ssl.keyStorePassword", mongoKeystorePassword)
        System.setProperty("javax.net.ssl.trustStore", "/etc/ssl/cacerts")
        System.setProperty("javax.net.ssl.trustStorePassword", mongoKeystorePassword)

        val mongoDbHost = config.getString("mongodb.host")
        val mongoDbPort = config.getInt("mongodb.port")
        println(s"mongo host = '$mongoDbHost'")
        println(s"mongo port = '$mongoDbPort'")

        val clusterSettings: ClusterSettings = ClusterSettings.builder().hosts(
          List(new ServerAddress(mongoDbHost, mongoDbPort)).asJava).build()

        val mongoSslClientSettings = MongoClientSettings.builder()
          .sslSettings(SslSettings.builder()
            .enabled(true)
            .invalidHostNameAllowed(true)
            .build())
          .streamFactoryFactory(NettyStreamFactoryFactory())
          .clusterSettings(clusterSettings)
          .build()

        MongoClient(mongoSslClientSettings)
      }

    println(s"Now lets get the ${databaseName.value} database")
    val database: MongoDatabase = mongoClient.getDatabase(databaseName.value)
    println(s"Drop the ${databaseName.value} database to clean things up")
    val dbDropObservable = database.drop()
    // We want to wait here until the database is dropped
    println("Lets just give it 9 seconds")

    try {
      Await.result(dbDropObservable.toFuture, Duration(9, TimeUnit.SECONDS))
    } catch {
      case e: Exception => "Caught exception:\n" + e.getMessage + "\nBut ignoring."
    }

    println(s"Get the ${collectionName.value} collection")
    val collection: MongoCollection[Document] = database.getCollection(collectionName.value)

    println("Cool, we're done getting the collection")
    collection

  }

  def getUsersCollection: MongoCollection[Document] = {
    getCollection(DatabaseName("users"), CollectionName("users.denormalised"))
  }

  def getSchoosCollection: MongoCollection[Document] = {
    getCollection(DatabaseName("schools"), CollectionName("schools"))
  }

}
