/*
 * Copyright 2014 Click Travel Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.clicktravel.infrastructure.provisioning.aws.cloudformation;

import static com.clicktravel.common.random.Randoms.randomString;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.*;

public class StackTest {

    private Stack stack;
    private String stackName;
    private AmazonCloudFormation mockCloudFormationClient;

    @Before
    public void setUp() {
        stackName = randomString();
        mockCloudFormationClient = mock(AmazonCloudFormation.class);
        stack = new Stack(mockCloudFormationClient, stackName);
    }

    @Test
    public void shouldReturnStackResourceDetail_withLogicalResourceId() {
        // Given
        final String logicalResourceId = randomString();
        final StackResourceDetail stackResourceDetail = new StackResourceDetail();
        final DescribeStackResourceResult describeStackResourceResult = new DescribeStackResourceResult()
                .withStackResourceDetail(stackResourceDetail);
        final DescribeStackResourceRequest expectedRequest = new DescribeStackResourceRequest()
                .withLogicalResourceId(logicalResourceId).withStackName(stackName);

        when(mockCloudFormationClient.describeStackResource(expectedRequest)).thenReturn(describeStackResourceResult);

        // When
        final StackResourceDetail result = stack.describeResource(logicalResourceId);

        // Then
        assertSame(stackResourceDetail, result);
    }

    @Test
    public void shouldReturnPhysicalResourceId_withLogicalResourceId() {
        // Given
        final String logicalResourceId = randomString();
        final String physicalResourceId = randomString();
        final StackResourceDetail stackResourceDetail = new StackResourceDetail()
                .withPhysicalResourceId(physicalResourceId);
        final DescribeStackResourceResult describeStackResourceResult = new DescribeStackResourceResult()
                .withStackResourceDetail(stackResourceDetail);
        final DescribeStackResourceRequest expectedRequest = new DescribeStackResourceRequest()
                .withLogicalResourceId(logicalResourceId).withStackName(stackName);

        when(mockCloudFormationClient.describeStackResource(expectedRequest)).thenReturn(describeStackResourceResult);

        // When
        final String result = stack.physicalResourceId(logicalResourceId);

        // Then
        assertSame(physicalResourceId, result);
    }

    @Test
    public void shouldReturnStackDescription() {
        // Given
        final com.amazonaws.services.cloudformation.model.Stack mockStack = mock(
                com.amazonaws.services.cloudformation.model.Stack.class);
        final DescribeStacksRequest expectedRequest = new DescribeStacksRequest().withStackName(stackName);
        final DescribeStacksResult describeStacksResult = new DescribeStacksResult().withStacks(asList(mockStack));

        when(mockCloudFormationClient.describeStacks(expectedRequest)).thenReturn(describeStacksResult);

        // When
        final com.amazonaws.services.cloudformation.model.Stack result = stack.describe();

        // Then
        assertSame(mockStack, result);
    }
}
