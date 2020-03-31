FROM angeloop/dynamodb-local:1.13.0
USER root
COPY ./db-entrypoint.sh /home/dynamodblocal/db-entrypoint.sh
RUN chown dynamodblocal /home/dynamodblocal/db-entrypoint.sh
USER dynamodblocal
WORKDIR /home/dynamodblocal
CMD ["-jar", "DynamoDBLocal.jar", "-sharedDb", "-cors", "*"]