package github.ag777.util.http.apache.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PoolStatus {
    public final int activeConnections;
    public final int availableConnections;
    public final int poolSize;

}
