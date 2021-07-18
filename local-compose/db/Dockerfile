FROM angeloop/dynamodb-local:1.13.0
USER root
USER dynamodblocal
WORKDIR /home/dynamodblocal
CMD ["-jar", "DynamoDBLocal.jar", "-sharedDb", "-cors", "*"]