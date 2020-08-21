package fi.rebased.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class McapRespDTO {
    BigDecimal marketCap;
    String error;
}
