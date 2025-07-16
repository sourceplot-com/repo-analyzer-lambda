package com.sourceplot.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class RepoStats {
    private String repo;
    private String date;
    private String languageData;
    
    @DynamoDbPartitionKey
    @DynamoDbAttribute("repo")
    public String getRepo() {
        return repo;
    }
    
    @DynamoDbSortKey
    @DynamoDbAttribute("date")
    public String getDate() {
        return date;
    }
    
    @DynamoDbSecondaryPartitionKey(indexNames = "DateIndex")
    @DynamoDbAttribute("date")
    public String getDateForIndex() {
        return date;
    }
    
    @DynamoDbSecondarySortKey(indexNames = "DateIndex")
    @DynamoDbAttribute("repo")
    public String getRepoForIndex() {
        return repo;
    }

    @DynamoDbAttribute("languageData")
    public String getLanguageData() {
        return languageData;
    }
} 