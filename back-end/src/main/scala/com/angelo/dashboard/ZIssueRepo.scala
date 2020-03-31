package com.angelo.dashboard

import cats.implicits._
import com.angelo.dashboard.Issue.LifeCycle
import com.angelo.dashboard.ZConfig.ZConfig
import com.angelo.dashboard.ZDbClient.ZDbClient
import com.angelo.dashboard.ZIssueRepo.Helpers._
import io.circe.syntax._
import software.amazon.awssdk.services.dynamodb.model._
import zio.{Task, _}

import scala.jdk.CollectionConverters._

object ZIssueRepo {

  type ZIssueRepo = Has[ZIssueRepo.Service]

  trait Service {
    def putIssue(item: Issue): Task[String]
    def getIssue(issueId: String): Task[Issue]
    def countNotArchived: Task[Int]
    def retrieveIssues: Task[Seq[Issue]]
    def archiveIssue(issue: Issue): Task[String]
    def deleteIssue(issueId: String): Task[Unit]
  }

  case object IssueNotFound extends Throwable //TODO remove 'extends Throwable' & replace with Union type when scala 3 drops

  //TODO ifA not working properly
  val live: URLayer[ZDbClient with ZConfig, ZIssueRepo] =
    ZLayer.fromServices[ZDbClient.Service, ZConfig.Service, ZIssueRepo.Service] { (db, config) =>
      new ZIssueRepo.Service {

        private val tableName = config.dynamoDb.issueTable

        def putIssue(item: Issue): Task[String] =
          db.useClient(_.putItem(putItemRequest(tableName)(item)))
            .as(item.id)

        def getIssue(issueId: String): Task[Issue] =
          db.useClient(_.getItem(getItemRequest(tableName)(issueId)).item.asScala.toMap)
            .map(asIssue) >>= (ZIO.fromOption(_).orElseFail(IssueNotFound))

        def countNotArchived: Task[Int] =
          db.useClient(_.scan(findActiveRequest(tableName)).count.toInt)

        def archiveIssue(item: Issue): Task[String] =
          db.useClient(_.updateItem(archiveItemRequest(tableName)(item)))
            .as(item.id)
            .catchSome { case _: ConditionalCheckFailedException => ZIO.fail(IssueNotFound) }

        def deleteIssue(issueId: String): Task[Unit] =
          db.useClient(_.deleteItem(deleteItemRequest(tableName)(issueId)))
            .map(_.attributes().asScala.nonEmpty) >>= (ZIO.cond(_, (), IssueNotFound))

        def retrieveIssues: Task[Seq[Issue]] =
          db.useClient(
            _.scanPaginator(scanAllRequest(tableName)).items.asScala.toSeq
              .map(res => asIssue(res.asScala.toMap))
              .collect { case Some(issue) => issue }
          )

      }
    }

  object Helpers {

    def scanAllRequest(tableName: String): ScanRequest =
      ScanRequest.builder().tableName(tableName).build()

    def findActiveRequest(tableName: String): ScanRequest =
      ScanRequest
        .builder()
        .tableName(tableName)
        .filterExpression("isArchived=:newValue")
        .expressionAttributeValues(
          Map(
            ":newValue" -> AttributeValue.builder().bool(false).build()
          ).asJava
        )
        .build()

    def getItemRequest(tableName: String)(issueId: String): GetItemRequest = {
      val key = Map("id" -> AttributeValue.builder().s(issueId).build())

      GetItemRequest
        .builder()
        .tableName(tableName)
        .key(key.asJava)
        .build()
    }

    def archiveItemRequest(tableName: String)(issue: Issue): UpdateItemRequest = {

      val key = Map("id" -> AttributeValue.builder().s(issue.id).build())

      UpdateItemRequest
        .builder()
        .tableName(tableName)
        .key(key.asJava)
        .updateExpression("SET isArchived=:newValue")
        .conditionExpression("attribute_exists(id)")
        .expressionAttributeValues(
          Map(
            ":newValue" -> AttributeValue.builder().bool(true).build()
          ).asJava
        )
        .build()
    }

    def putItemRequest(tableName: String)(issue: Issue): PutItemRequest = {
      val itemValues = Map(
        "id"         -> AttributeValue.builder().s(issue.id).build(),
        "title"      -> AttributeValue.builder().s(issue.title).build(),
        "content"    -> AttributeValue.builder().s(issue.content).build(),
        "isArchived" -> AttributeValue.builder().bool(issue.isArchived).build(),
        "lifeCycle"  -> AttributeValue.builder().s(issue.lifeCycle.asJson.noSpaces).build()
      )

      PutItemRequest
        .builder()
        .tableName(tableName)
        .item(itemValues.asJava)
        .build()
    }

    def deleteItemRequest(tableName: String)(issueId: String): DeleteItemRequest = {

      val key = Map("id" -> AttributeValue.builder().s(issueId).build())

      DeleteItemRequest
        .builder()
        .tableName(tableName)
        .key(key.asJava)
        .returnValues(ReturnValue.ALL_OLD)
        .build()
    }

    def asIssue(value: Map[String, AttributeValue]): Option[Issue] =
      for {
        id         <- value.get("id").map(_.s())
        title      <- value.get("title").map(_.s())
        content    <- value.get("content").map(_.s())
        isArchived <- value.get("isArchived").map(_.bool())
        lifeCycle  <- value.get("lifeCycle").map(_.s()) >>= LifeCycle.decodeJsonString andThen (_.toOption)
        //TODO change project conf (cross project so that this apply is only available on the jvm side)
      } yield Issue(id, title, content, isArchived, lifeCycle)
  }
}
