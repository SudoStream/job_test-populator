package io.sudostream.timetoteach.testpopulator

import argonaut.Argonaut._
import argonaut._
import org.mongodb.scala.{Completed, Document, MongoCollection}

import scala.concurrent.Future
import scala.io.Source

trait UsersDenormalisedInserter {

  def decodeUsersForDatabaseInjestion: List[User] = {
    val filename = "/users-denormalised.json"
    val filenameAsInputStream = getClass.getResourceAsStream(filename)
    val input = Source.fromInputStream(filenameAsInputStream).mkString
    input.decodeOption[List[User]].getOrElse(Nil)
  }

  def insertUsersToDatabase(users: List[User],
                            usersCollection: MongoCollection[Document]): Future[Completed] = {
    val docsToInsert: Seq[Document] = users.map {
      user =>
        val emailsAsDocuments: List[Document] = user.emails.map {
          emailDetails =>
            Document(
              "emailAddress" -> emailDetails.emailAddress,
              "validated" -> emailDetails.validated,
              "preferred" -> emailDetails.preferred
            )
        }

        val schoolsAsDocuments: List[Document] = user.schools.map {
          school =>
            Document(
              "_id" -> school.id,
              "name" -> school.name,
              "address" -> school.address,
              "postCode" -> school.postCode,
              "telephone" -> school.telephone,
              "localAuthority" -> school.localAuthority,
              "country" -> school.country
            )
        }

        Document(
          "_id" -> user.timeToTeachId,
          "fullName" -> user.fullName,
          "givenName" -> user.givenName,
          "familyName" -> user.familyName,
          "imageUrl" -> user.imageUrl,
          "emails" -> emailsAsDocuments,
          "userRole" -> user.userRole,
          "schools" -> schoolsAsDocuments
        )
    }
    usersCollection.insertMany(docsToInsert).toFuture
  }


}


case class User(timeToTeachId: String,
                fullName: String,
                givenName: String,
                familyName: String,
                imageUrl: String,
                emails: List[EmailDetails],
                userRole: String,
                schools: List[School]
               )

object User {
  implicit def UserCodecJson: CodecJson[User] =
    casecodec8(User.apply, User.unapply)(
      "_id",
      "fullName",
      "givenName",
      "familyName",
      "imageUrl",
      "emails",
      "userRole",
      "schools"
    )
}

case class EmailDetails(emailAddress: String,
                        validated: Boolean,
                        preferred: Boolean
                       )

object EmailDetails {
  implicit def EmailDetailsCodecJson: CodecJson[EmailDetails] =
    casecodec3(EmailDetails.apply, EmailDetails.unapply)(
      "emailAddress",
      "validated",
      "preferred"
    )
}

case class School(id: String,
                  name: String,
                  address: String,
                  postCode: String,
                  telephone: String,
                  localAuthority: String,
                  country: String
                 )

object School {
  implicit def SchoolCodecJson: CodecJson[School] =
    casecodec7(School.apply, School.unapply)(
      "_id",
      "name",
      "address",
      "postCode",
      "telephone",
      "localAuthority",
      "country"
    )
}
