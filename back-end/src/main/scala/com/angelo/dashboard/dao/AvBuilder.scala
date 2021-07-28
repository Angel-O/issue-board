package com.angelo.dashboard.dao

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

trait AvBuilder[A] {
  def from(a: A): AttributeValue.Builder
}

object AvBuilder {

  implicit val boolQueryValue: AvBuilder[Boolean] = new AvBuilder[Boolean] {
    override def from(a: Boolean): AttributeValue.Builder = AttributeValue.builder.bool(a)
  }

  implicit val stringQueryValue: AvBuilder[String] = new AvBuilder[String] {
    override def from(a: String): AttributeValue.Builder = AttributeValue.builder.s(a)
  }
}
