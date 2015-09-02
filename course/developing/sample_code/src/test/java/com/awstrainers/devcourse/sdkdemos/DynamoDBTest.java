/*
# Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

*/

package com.awstrainers.devcourse.sdkdemos;

import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import static java.text.MessageFormat.format;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

public class DynamoDBTest {

	private static final Log log = LogFactory.getLog(DynamoDBTest.class);

	private static final int RETRIES_TIMEOUT = 1000*60*2;

	public static final AWSCredentials cred = new ClasspathPropertiesFileCredentialsProvider(
			"AwsCredentials.properties").getCredentials();

	public static final String tableName = "patiens" + UUID.randomUUID();

	@BeforeClass
	public static void checkCreateTable() throws Exception {
		AmazonDynamoDB client = new AmazonDynamoDBClient(cred);
		client.setRegion(Region.getRegion(Regions.US_EAST_1));

		log.info(format("Creating table {0}.", tableName));
		CreateTableRequest creationRequest = new CreateTableRequest()
				.withTableName(tableName);


		creationRequest.setAttributeDefinitions(new ArrayList<AttributeDefinition>());
		creationRequest.getAttributeDefinitions().add(new AttributeDefinition()
			.withAttributeName("id").withAttributeType(
				ScalarAttributeType.N));
		creationRequest.getAttributeDefinitions().add(new AttributeDefinition()
			.withAttributeName("timestamp").withAttributeType(
					ScalarAttributeType.N));
		creationRequest.withKeySchema(new KeySchemaElement()
								.withAttributeName("id")
								.withKeyType(KeyType.HASH));
		creationRequest.withKeySchema(new KeySchemaElement()
								.withAttributeName("timestamp")
								.withKeyType(KeyType.RANGE));

		creationRequest.setProvisionedThroughput(new ProvisionedThroughput()
				.withReadCapacityUnits(5L).withWriteCapacityUnits(5L));

		client.createTable(creationRequest);

		// wait!
		DescribeTableRequest descriptionRequest = new DescribeTableRequest()
			.withTableName(tableName);
		long tf = System.currentTimeMillis();
		while(true) {
			TableDescription td = client.describeTable(descriptionRequest).getTable();
			log.debug(format("Table status: {0}.", td.getTableStatus()));
			if ("ACTIVE".equals(td.getTableStatus()) == true) {
				break;
			}
			if (System.currentTimeMillis() - tf > RETRIES_TIMEOUT) {
				throw new /*TooManyRetries*/RuntimeException("Too many retries creating dynamodb table.");
			}
			Thread.sleep(1000*5);
		};
	}

	@AfterClass
	public static void checkDeleteTable() {
		AmazonDynamoDB client = new AmazonDynamoDBClient(cred);
		client.setRegion(Region.getRegion(Regions.US_EAST_1));
		log.info(format("deleting table {0}.", tableName));

		DeleteTableRequest request = new DeleteTableRequest()
			.withTableName(tableName);
		client.deleteTable(request);
	}


	@Test
	public void checkAsyncOperation() throws Exception {
		AmazonDynamoDBAsync client = new AmazonDynamoDBAsyncClient();
		client.setRegion(Region.getRegion(Regions.US_EAST_1));
		Future<DescribeTableResult> future = client
				.describeTableAsync(new DescribeTableRequest()
						.withTableName(tableName));
		DescribeTableResult result = future.get();
		Assert.assertNotNull("A null result indicates the program didn't wait. ", result);
	}

	@Test
	public void checkTableDescription() {
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(cred);
		client.setRegion(Region.getRegion(Regions.US_EAST_1));

		DescribeTableRequest request = new DescribeTableRequest()
				.withTableName(tableName);
		TableDescription td = client.describeTable(request).getTable();
		Assert.assertEquals(tableName, td.getTableName());

		log.debug("Name: " + td.getTableName());
		log.debug("Status: " + td.getTableStatus());
		log.debug("RC: "
				+ td.getProvisionedThroughput().getReadCapacityUnits());
		log.debug("WC: "
				+ td.getProvisionedThroughput().getWriteCapacityUnits());
		log.debug("Items: " + td.getItemCount());
		log.debug("Size: " + td.getTableSizeBytes());

		for (AttributeDefinition def : td.getAttributeDefinitions()) {
			log.debug(def.getAttributeName() + ", "
					+ def.getAttributeType());
		}

	}

	@Test
	public void checkCRUD() {
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(cred);
		client.setRegion(Region.getRegion(Regions.US_EAST_1));

		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put("id", new AttributeValue().withN("33333"));
		item.put("timestamp", new AttributeValue().withN(String.valueOf(new Date().getTime())));

		char letter = (char) (Math.random() * ('z' - 'a') + 'a');
		item.put("payload", new AttributeValue().withS(String.valueOf(letter)));

		PutItemRequest request = new PutItemRequest(tableName, item)
				.withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
		PutItemResult result = client.putItem(request);
		Assert.assertNotEquals(result.getConsumedCapacity(), 0);
	}

	@Test
	public void checkMapping() {
		AmazonDynamoDB client = new AmazonDynamoDBClient(cred);
		client.setRegion(Region.getRegion(Regions.US_EAST_1));
		DynamoDBMapper mapper = new DynamoDBMapper(client);

		Patient p = new Patient(3343, new Date().getTime(), "XXX");
		mapper.save(p);

		p.setPayload("XXXx2");
		mapper.save(p);

		mapper.delete(p);
	}

}
