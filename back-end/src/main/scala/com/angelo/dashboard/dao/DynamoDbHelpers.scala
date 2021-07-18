package com.angelo.dashboard.dao

import cats.implicits._
import com.angelo.dashboard.dao.IssueSchemaReader._
import com.angelo.dashboard.data.Implicits._
import com.angelo.dashboard.{Issue, LifeCycle}
import io.circe.syntax.EncoderOps
import software.amazon.awssdk.services.dynamodb.model._

import java.util.Collections
import scala.jdk.CollectionConverters._
import scala.util.control.NoStackTrace

object DynamoDbHelpers {

  private val EXPRESSION_PLACEHOLDER = ":newValue"
  private val readCapacity           = 5L
  private val writeCapacity          = 5L

  def createTableRequest(tableName: String): CreateTableRequest =
    CreateTableRequest.builder
      .tableName(tableName)
      .attributeDefinitions(AttributeDefinition.builder.attributeName(ID).attributeType("S").build)
      .keySchema(KeySchemaElement.builder.attributeName(ID).keyType(KeyType.HASH).build)
      .provisionedThroughput(
        ProvisionedThroughput.builder.readCapacityUnits(readCapacity).writeCapacityUnits(writeCapacity).build
      )
      .build

  def scanAllRequest(tableName: String): ScanRequest =
    ScanRequest.builder
      .tableName(tableName)
      .build

  def findActiveRequest(tableName: String): ScanRequest =
    ScanRequest.builder
      .tableName(tableName)
      .filterExpression(s"$IS_ARCHIVED=$EXPRESSION_PLACEHOLDER")
      .expressionAttributeValues(archivedQueryValue(archived = false))
      .build

  def getItemRequest(tableName: String)(issueId: String): GetItemRequest =
    GetItemRequest.builder
      .tableName(tableName)
      .key(key(issueId))
      .build

  def putItemRequest(tableName: String)(issue: Issue): PutItemRequest =
    PutItemRequest.builder
      .tableName(tableName)
      .item(itemValues(issue))
      .build

  def deleteItemRequest(tableName: String)(issueId: String): DeleteItemRequest =
    DeleteItemRequest.builder
      .tableName(tableName)
      .key(key(issueId))
      .returnValues(ReturnValue.ALL_OLD)
      .build

  def archiveItemRequest(tableName: String)(issue: Issue): UpdateItemRequest =
    UpdateItemRequest.builder
      .tableName(tableName)
      .key(key(issue.id))
      .updateExpression(s"SET $IS_ARCHIVED=$EXPRESSION_PLACEHOLDER")
      .conditionExpression(s"attribute_exists($ID)")
      .expressionAttributeValues(archivedQueryValue(archived = true))
      .build

  def asOptionalIssue(implicit record: Map[String, AttributeValue]): Option[Issue] =
    (
      readId,
      readTitle,
      readContent,
      readIsArchived,
      readIsLifeCycle.flatMap(LifeCycle.decodeJsonString andThen (_.toOption))
    ).mapN(Issue.make)

  def asAttemptedIssue(implicit record: Map[String, AttributeValue]): Either[Throwable, Issue] =
    (
      readId.toRight(FieldNotFound(ID)),
      readTitle.toRight(FieldNotFound(TITLE)),
      readContent.toRight(FieldNotFound(CONTENT)),
      readIsArchived.toRight(FieldNotFound(IS_ARCHIVED)),
      readIsLifeCycle.toRight(FieldNotFound(LIFECYCLE)).flatMap(LifeCycle.decodeJsonString)
    ).parMapN(Issue.make)

  private def archivedQueryValue(archived: Boolean) =
    Collections.singletonMap(EXPRESSION_PLACEHOLDER, AttributeValue.builder.bool(archived).build)

  private def key(issueId: String) =
    Collections.singletonMap(ID, AttributeValue.builder.s(issueId).build)

  private def itemValues(issue: Issue) =
    Map(
      (ID, AttributeValue.builder.s(issue.id).build),
      (TITLE, AttributeValue.builder.s(issue.title).build),
      (CONTENT, AttributeValue.builder.s(issue.content).build),
      (IS_ARCHIVED, AttributeValue.builder.bool(issue.isArchived).build),
      (LIFECYCLE, AttributeValue.builder.s(issue.lifeCycle.asJson.noSpaces).build)
    ).asJava

  final case class FieldNotFound(fieldName: String)(implicit record: Map[String, AttributeValue]) extends NoStackTrace {
    override def getMessage = s"field `$fieldName` not found in record $record."
  }
}
