package com.angelo.dashboard.dao

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

import scala.util.control.NoStackTrace

final case class FieldNotFound(fieldName: String)(implicit record: Map[String, AttributeValue]) extends NoStackTrace {
  override def getMessage = s"field `$fieldName` not found in record $record."
}
