package com.angelo.dashboard.dao

import cats.implicits._
import com.angelo.dashboard.data.Implicits._
import com.angelo.dashboard.{Issue, LifeCycle}
import io.circe.syntax.EncoderOps
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

import java.util.Collections
import scala.jdk.CollectionConverters._

object IssueSchemaReaderWriter {

  val EXPRESSION_PLACEHOLDER = ":newValue"
  val ID                     = "id"
  val TITLE                  = "title"
  val CONTENT                = "content"
  val IS_ARCHIVED            = "isArchived"
  val LIFECYCLE              = "lifeCycle"

  def readId(implicit record: Map[String, AttributeValue]) =
    record.get(ID).map(_.s)

  def readTitle(implicit record: Map[String, AttributeValue]) =
    record.get(TITLE).map(_.s)

  def readContent(implicit record: Map[String, AttributeValue]) =
    record.get(CONTENT).map(_.s)

  def readIsArchived(implicit record: Map[String, AttributeValue]) =
    record.get(IS_ARCHIVED).map(_.bool).map(Boolean.unbox)

  def readIsLifeCycle(implicit record: Map[String, AttributeValue]) =
    record.get(LIFECYCLE).map(_.s)

  def expressionValue[V: AvBuilder](value: V) =
    Collections.singletonMap(EXPRESSION_PLACEHOLDER, attributeValue(value))

  def key(issueId: String) =
    Collections.singletonMap(ID, attributeValue(issueId))

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

  def asDbFields(issue: Issue) =
    Map(
      (ID, attributeValue(issue.id)),
      (TITLE, attributeValue(issue.title)),
      (CONTENT, attributeValue(issue.content)),
      (IS_ARCHIVED, attributeValue(issue.isArchived)),
      (LIFECYCLE, attributeValue(issue.lifeCycle.asJson.noSpaces))
    ).asJava

  private def attributeValue[V: AvBuilder](value: V) =
    implicitly[AvBuilder[V]].from(value).build
}
