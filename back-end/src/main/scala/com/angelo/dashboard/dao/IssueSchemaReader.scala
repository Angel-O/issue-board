package com.angelo.dashboard.dao

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

object IssueSchemaReader {

  val ID          = "id"
  val TITLE       = "title"
  val CONTENT     = "content"
  val IS_ARCHIVED = "isArchived"
  val LIFECYCLE   = "lifeCycle"

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
}
