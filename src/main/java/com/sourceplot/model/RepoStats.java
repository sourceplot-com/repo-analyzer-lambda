package com.sourceplot.model;

import java.util.Map;

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
    private String dateHour;
    private Map<String, Integer> bytesByLanguage;
    
    @DynamoDbPartitionKey
    @DynamoDbAttribute("repo")
    public String getRepo() {
        return repo;
    }
    
    @DynamoDbSortKey
    @DynamoDbAttribute("date")
    public String getDateHour() {
        return dateHour;
    }
    
    @DynamoDbSecondaryPartitionKey(indexNames = "DateIndex")
    @DynamoDbAttribute("date")
    public String getDateForIndex() {
        return dateHour;
    }
    
    @DynamoDbSecondarySortKey(indexNames = "DateIndex")
    @DynamoDbAttribute("repo")
    public String getRepoForIndex() {
        return repo;
    }

    @DynamoDbAttribute("languageData")
    public Map<String, Integer> getBytesByLanguage() {
        return bytesByLanguage;
    }
} 