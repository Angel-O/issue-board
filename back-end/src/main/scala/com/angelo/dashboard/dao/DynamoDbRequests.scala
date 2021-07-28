package com.angelo.dashboard.dao

import com.angelo.dashboard.Issue
import com.angelo.dashboard.dao.IssueSchemaReaderWriter._
import software.amazon.awssdk.services.dynamodb.model._

object DynamoDbRequests {

  def scanRequest(tableName: String): ScanRequest =
    ScanRequest.builder
      .tableName(tableName)
      .build

  def filterRequest[V: AvBuilder](tableName: String)(fieldName: String, value: V): ScanRequest =
    ScanRequest.builder
      .tableName(tableName)
      .filterExpression(s"$fieldName=$EXPRESSION_PLACEHOLDER")
      .expressionAttributeValues(expressionValue(value))
      .build

  def getItemRequest(tableName: String)(issueId: String): GetItemRequest =
    GetItemRequest.builder
      .tableName(tableName)
      .key(key(issueId))
      .build

  def putItemRequest(tableName: String)(issue: Issue): PutItemRequest =
    PutItemRequest.builder
      .tableName(tableName)
      .item(asDbFields(issue))
      .build

  def deleteItemRequest(tableName: String)(issueId: String): DeleteItemRequest =
    DeleteItemRequest.builder
      .tableName(tableName)
      .key(key(issueId))
      .returnValues(ReturnValue.ALL_OLD)
      .build

  def createTableRequest(table: String, readCp: Long, writeCp: Long): CreateTableRequest =
    CreateTableRequest.builder
      .tableName(table)
      .attributeDefinitions(AttributeDefinition.builder.attributeName(ID).attributeType("S").build)
      .keySchema(KeySchemaElement.builder.attributeName(ID).keyType(KeyType.HASH).build)
      .provisionedThroughput(ProvisionedThroughput.builder.readCapacityUnits(readCp).writeCapacityUnits(writeCp).build)
      .build

  def archiveItemRequest(tableName: String)(issue: Issue): UpdateItemRequest =
    UpdateItemRequest.builder
      .tableName(tableName)
      .key(key(issue.id))
      .updateExpression(s"SET $IS_ARCHIVED=$EXPRESSION_PLACEHOLDER")
      .conditionExpression(s"attribute_exists($ID)")
      .expressionAttributeValues(expressionValue(true))
      .build
}
