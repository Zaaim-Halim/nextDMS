package io.nextdms.dto.explorer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public class JcrValue implements Serializable {

    private Boolean booleanValue;
    private Date dateValue;
    private LocalDate localDateValue;
    private LocalDateTime localDateTimeValue;
    private LocalTime localTimeValue;
    private BigDecimal decimalValue;
    private Double doubleValue;
    private Long longValue;
    private String stringValue;
    private Boolean readOnly;
    private String propertyType;

    public JcrValue() {}

    public JcrValue(
        Boolean booleanValue,
        Date dateValue,
        LocalDate localDateValue,
        LocalDateTime localDateTimeValue,
        LocalTime localTimeValue,
        BigDecimal decimalValue,
        Double doubleValue,
        Long longValue,
        String stringValue,
        Boolean readOnly,
        String propertyType
    ) {
        this.booleanValue = booleanValue;
        this.dateValue = dateValue;
        this.localDateValue = localDateValue;
        this.localDateTimeValue = localDateTimeValue;
        this.localTimeValue = localTimeValue;
        this.decimalValue = decimalValue;
        this.doubleValue = doubleValue;
        this.longValue = longValue;
        this.stringValue = stringValue;
        this.readOnly = readOnly;
        this.propertyType = propertyType;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public LocalDateTime getLocalDateTimeValue() {
        return localDateTimeValue;
    }

    public void setLocalDateTimeValue(LocalDateTime localDateTimeValue) {
        this.localDateTimeValue = localDateTimeValue;
    }

    public LocalDate getLocalDateValue() {
        return localDateValue;
    }

    public void setLocalDateValue(LocalDate localDateValue) {
        this.localDateValue = localDateValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public LocalTime getLocalTimeValue() {
        return localTimeValue;
    }

    public void setLocalTimeValue(LocalTime localTimeValue) {
        this.localTimeValue = localTimeValue;
    }

    public BigDecimal getDecimalValue() {
        return decimalValue;
    }

    public void setDecimalValue(BigDecimal decimalValue) {
        this.decimalValue = decimalValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }
}
