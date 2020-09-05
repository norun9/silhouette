package repositories

import java.util.UUID

import javax.inject.Inject
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import scala.concurrent.{ ExecutionContext, Future }
import models.Goal
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson._
import org.mongodb.scala.ReadPreference
import reactivemongo.bson.BSONDocument
import reactivemongo.api._
//import reactivemongo.api.bson.{ BSONObjectID }
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json._
import play.api.libs.json._

class GoalRepository @Inject() (
    implicit
    ec: ExecutionContext,
    reactiveMongoApi: ReactiveMongoApi
) {
  private def collection: Future[JSONCollection] =
    reactiveMongoApi.database.map(_.collection("goal"))

  def list(limit: Int = 100): Future[Seq[Goal]] =
    collection.flatMap(_.find(BSONDocument.empty)
      .cursor[Goal]().collect[Seq](limit, Cursor.FailOnError[Seq[Goal]]()))

  def create(goal: Goal): Future[WriteResult] =
    collection.flatMap((_.insert(goal)))

  def read(id: BSONObjectID): Future[Option[Goal]] =
    collection.flatMap(_.find(BSONDocument("_id" -> id)).one[Goal])

  def goalSort: Future[Seq[Goal]] =
  collection.flatMap(_.find(Json.obj())
    // sort by lastName
    .sort(Json.obj("challengers_num" -> -1))
    .cursor[Goal]()
    // Collect the results as a list
    .collect[Seq](Int.MaxValue, Cursor.FailOnError[Seq[Goal]]()))

  def update(id: BSONObjectID, goal: Goal): Future[Option[Goal]] =
    collection.flatMap(_.findAndUpdate(
      BSONDocument("_id" -> id),
      BSONDocument(
        f"$$set" -> BSONDocument(
          "name" -> goal.name,
          "learning_time" -> goal.learning_time,
          "challengers_num" -> goal.challengers_num
        )
      ),
      true
    ).map(_.result[Goal]))

  def destroy(id: BSONObjectID): Future[Option[Goal]] =
    collection.flatMap(_.findAndRemove(BSONDocument("_id" ->
      id)).map(_.result[Goal]))
}
