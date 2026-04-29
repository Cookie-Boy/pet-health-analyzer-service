package ru.sibsutis.pet_health_analyzer.core.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import lombok.Data;

@Deprecated
@Document(collection = "breed_norms")
@Data
@CompoundIndexes({
        @CompoundIndex(name = "breed_test_parameter_idx",
                def = "{'breed': 1, 'testName': 1, 'parameter': 1}",
                unique = true)
})
public class BreedNorm {
    @Id
    private String id;

    private String breed;
    private String testName;
    private String testCategory;
    private String parameter;
    private Double minValue;
    private Double maxValue;
    private String unit;
}