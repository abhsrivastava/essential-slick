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
   DBIO.
   val finalFuture = db.run(finalAction)
   finalFuture.onComplete{
      case Success(s) => println("table initialized successfully "); db.close
      case Failure(f) => println(f.getMessage); db.close
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