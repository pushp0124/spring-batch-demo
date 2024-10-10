package com.demo.spring.batch.organisation.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "organisation")
public record Organisation(
     @Id String id,
     Long index,
     String name,
     String website,
     String country,
     String description,
     Integer yearFounded,
     String industry,
     Long numberOfEmployees)
{

}