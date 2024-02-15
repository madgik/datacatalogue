package ebrainsv2.mip.datacatalogue.datamodelinfo;

import com.google.gson.annotations.Expose;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`data_model_info`")
public class DataModelInfoDAO {
    @Id
    @Column(nullable = false, unique = true)
    private String code;

    @Expose
    private String description;

    @Expose
    private String datasetLocations;

    @Expose
    private int records;
}
