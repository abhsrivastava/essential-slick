package com.abhi

import slick.driver.MySQLDriver.api._
import slick.jdbc.meta.MTable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import scala.concurrent.Await
import scala.concurrent.duration._
/**
  * Created by abhsrivastava on 8/28/16.
  */

object MySlickApp extends App {
   val db = Database.forConfig("essential-slick ")
   val messages = TableQuery[MessageTable]
   createSchema
   query1
   query2
   query3
   (0 to 3).foreach(query4 _)
   count
   filterUsingFor
   val id = insertAndReturnId("HAL", "Goodbye Dave")
   println(id)
   val newMessage = insertAndReturnWholeRecord("DAVE", "Point Taken")
   println(newMessage)
   val partialMessage = insertSingleColumn("DAVE")
   println(partialMessage)
   update("Ha Ha!!!")
   insertMultipleAndReturnObjects.foreach(println)

   def insertMultipleAndReturnObjects() : Seq[Message] = {
      val seq = Seq(
         Message("Foo", Some("Foo1")),
         Message("Bar", Some("Bar1")),
         Message("Baz", Some("Baz1"))
      )
      val action = messages returning messages.map(_.id) into {(message, id) => message.copy(id = id)} ++= seq
      Await.result(db.run(action), 2 seconds)
   }
   def update(content: String) : Unit = {
      val action = messages.filter(_.id === 7L).map(_.content).update(Option(content))
      val future = db.run(action)
      Await.result(future, 2 seconds)
   }
   def insertSingleColumn(sender: String) : Long = {
      val action = messages.map(_.sender) returning messages.map(_.id) += sender
      val future = db.run(action)
      Await.result(future, 2 seconds)
   }

   // this behaviour differs from db to db. some dbs will support
   // messages returning messages += Message("x", "y")
   def insertAndReturnWholeRecord(sender: String, content: String) : Message = {
      val action = messages returning messages.map(_.id) into {(message, id) =>
         message.copy(id = id)
      } += Message(sender, Option(content))
      val future = db.run(action)
      Await.result(future, 2 seconds)
   }

   def insertAndReturnId(sender: String, content: String) : Long = {
      val action = messages returning messages.map(_.id) += Message(sender, Option(content))
      val future = db.run(action)
      Await.result(future, 2 seconds)
   }

   def exists : Unit = {
      val query = for {
         m <- messages if m.sender === "HAL"
      } yield m
      val future = db.run(query.exists.result)
      for {
         result <- future
      } println(result)
   }

   def filterUsingFor : Unit = {
      val query = for {
         m <- messages if m.id === 1L
      } yield m
      val future = db.run(query.result)
      for {
         result <- future
      } {
         println("---------- filter using for ----------")
         println(result)
      }
   }
   def count : Unit = {
      val action = messages.size.result
      val future = db.run(action)
      for {
         length <- future
      } println("the number of items in table is " + length)
   }
   def query4(n: Int) : Unit = {
      val action = messages.drop(n).take(1).result
      val future = db.run(action)
      for {
         result <- future // unpack future
         m <- result // extract record
      } {
         println("--------- query4 ------------")
         println(m)
      }
   }
   def query3 : Unit = {
      val action = messages.map(t => (t.sender ++ ": " ++ t.content)).result
      val futureResult = db.run(action)
      for {
         sequence <- futureResult
         data <- sequence
      } {
         println("--------- query3 ------------")
         println(data)
      }
   }

   def query2: Unit = {
      val action = messages.filter(_.sender =!= "DAVE").map(t => (t.id, t.content) <> (Foo.tupled, Foo.unapply)).result
      val f1 = db.run(action)
      for {
         records <- f1
         record <- records
      } {
         println("--------- query2 ------------")
         println(s"${record.id} ${record.content}")
      }
   }

   def query1: Unit = {
      val action = messages.filter(_.sender === "HAL").map(t => t.content).result
      val f1 = db.run(action)
      for {
         v <- f1
         c <- v
      } {
         println("--------- query1 ------------")
         println(c)
      }
   }

   def createSchema: Unit = {
      val tableExists = MTable.getTables map { tables =>
         tables.exists(_.name.name == messages.baseTableRow.tableName)
      }

      val dropAction = messages.schema.drop
      val createAction = messages.schema.create

      val freshMessages = Seq(
         Message("Dave", Some("Hello HAL. Do you read me? HAL?")),
         Message("HAL", Some("Affirmtive, Dave. I read you.")),
         Message("Dave", Some("Open the pod doors, HAL.")),
         Message("HAL", Some("I'm Sorry. Dave. I'm afraid, I cannot do that."))
      )

      val insertAction = messages ++= freshMessages
      val finalAction = tableExists >> dropAction >> createAction >> insertAction
      val finalFuture = db.run(finalAction)
      finalFuture.onComplete {
         case Success(s) => println("table initialized successfully ")
         case Failure(f) => println(f.getMessage)
      }
      Await.result(finalFuture, 5 seconds)
   }


   scala.io.StdIn.readLine()
   db.close
}

final case class Message (sender: String, content: Option[String], id: Long = 0L)

final case class MessageTable(tag: Tag) extends Table[Message](tag, "Message") {
   def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
   def sender = column[String]("sender")
   def content = column[Option[String]]("content")
   def * = (sender, content, id) <> (Message.tupled, Message.unapply)
}
case class Foo(id: Long, content: Option[String])
