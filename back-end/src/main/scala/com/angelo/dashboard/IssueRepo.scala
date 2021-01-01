package com.angelo.dashboard

import io.circe.syntax._
import cats.implicits._
import com.angelo.dashboard.Issue.LifeCycle
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{
  AttributeValue,
  DeleteItemRequest,
  PutItemRequest,
  ScanRequest,
  UpdateItemRequest
}

import scala.jdk.CollectionConverters._

class IssueRepo(client: DynamoDbClient, tableName: String) {

  def putIssue(item: Issue) =
    client.putItem(putItemRequest(item))

  def countNotArchived: Integer = client.scan(findArchived).count()

  def retrieveIssues: Seq[Issue] =
    client
      .scanPaginator(queryItemsRequest)
      .items
      .asScala
      .toSeq
      .map(res => asIssues(res.asScala.toMap))
      .collect { case (Some(issue)) => issue }

  def archiveIssue(issue: Issue) = client.updateItem(archiveItemRequest(issue))

  def deleteIssue(issueId: String) = client.deleteItem(deleteItemRequest(issueId))

  private def putItemRequest(issue: Issue): PutItemRequest = {
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

  private def findArchived =
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

  private def queryItemsRequest =
    ScanRequest.builder().tableName(tableName).build()

  def deleteItemRequest(issueId: String): DeleteItemRequest = {

    val key = Map("id" -> AttributeValue.builder().s(issueId).build())

    DeleteItemRequest.builder().tableName(tableName).key(key.asJava).build()
  }

  private def archiveItemRequest(issue: Issue): UpdateItemRequest = {

    val key = Map("id" -> AttributeValue.builder().s(issue.id).build())

    UpdateItemRequest
      .builder()
      .tableName(tableName)
      .key(key.asJava)
      .updateExpression("SET isArchived=:newValue")
      .expressionAttributeValues(
        Map(
          ":newValue" -> AttributeValue.builder().bool(true).build()
        ).asJava
      )
      .build()

  }

  private def asIssues(value: Map[String, AttributeValue]) =
    for {
      id         <- value.get("id").map(_.s())
      title      <- value.get("title").map(_.s())
      content    <- value.get("content").map(_.s())
      isArchived <- value.get("isArchived").map(_.bool())
      lifeCycle  <- value.get("lifeCycle").map(_.s()) >>= LifeCycle.decodeJsonString andThen (_.toOption)
      //TODO change project conf (cross project so that this apply is only available on the jvm side)
    } yield Issue(id, title, content, isArchived, lifeCycle)

}
