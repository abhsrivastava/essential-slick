package com.abhi

import slick.driver.MySQLDriver.api._
import slick.jdbc.meta.MTable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

/**
  * Created by abhsrivastava on 8/28/16.
  */

object MySlickApp extends App {
   val db = Database.forConfig("essential-slick ")
   val messages = TableQuery[MessageTable]
   query1
   query2
   query3
   (0 to 3).foreach(query4 _)
   count
   filterUsingFor

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
         Message("Dave", "Hello HAL. Do you read me? HAL?"),
         Message("HAL", "Affirmtive, Dave. I read you."),
         Message("Dave", "Open the pod doors, HAL."),
         Message("HAL", "I'm Sorry. Dave. I'm afraid, I cannot do that.")
      )

      val insertAction = messages ++= freshMessages
      val finalAction = tableExists >> dropAction >> createAction >> insertAction
      val finalFuture = db.run(finalAction)
      finalFuture.onComplete {
         case Success(s) => println("table initialized successfully "); db.close
         case Failure(f) => println(f.getMessage); db.close
      }
   }


   scala.io.StdIn.readLine()
}

final case class Message (sender: String, content: String, id: Long = 0L)

final case class MessageTable(tag: Tag) extends Table[Message](tag, "Message") {
   def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
   def sender = column[String]("sender")
   def content = column[String]("content")
   def * = (sender, content, id) <> (Message.tupled, Message.unapply)
}
case class Foo(id: Long, content: String)
