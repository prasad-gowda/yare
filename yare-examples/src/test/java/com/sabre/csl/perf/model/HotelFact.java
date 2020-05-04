package com.sabre.csl.perf.model;


import java.time.ZonedDateTime;
import java.util.List;

public class HotelFact {
    private Integer globalPropertyId;
    private List<Integer> globalPropertyIdList;
    private List<String> chainCodes;
    private String preferredProgramLabel;
    private Integer sortOrder;
    private ZonedDateTime stayStartDate;
    private ZonedDateTime stayEndDate;
    private Integer preferredProgramId;
    private Boolean isSupplierPreferenceApplied;
    private String tierLabelType;
    private Integer chainSortOrder;

    private List<PreferencePredicates> chainContracts;
    private List<PreferencePredicates> propertyContracts;

    public HotelFact() {
    }

    public HotelFact(Integer globalPropertyId) {
        this.globalPropertyId = globalPropertyId;
    }

    public Integer getGlobalPropertyId() {
        return globalPropertyId;
    }

    public void setGlobalPropertyId(Integer globalPropertyId) {
        this.globalPropertyId = globalPropertyId;
    }

    public List<Integer> getGlobalPropertyIdList() {
        return globalPropertyIdList;
    }

    public void setGlobalPropertyIdList(List<Integer> globalPropertyIdList) {
        this.globalPropertyIdList = globalPropertyIdList;
    }

    public List<String> getChainCodes() {
        return chainCodes;
    }

    public void setChainCodes(List<String> chainCodes) {
        this.chainCodes = chainCodes;
    }

    public String getPreferredProgramLabel() {
        return preferredProgramLabel;
    }

    public void setPreferredProgramLabel(String preferredProgramLabel) {
        this.preferredProgramLabel = preferredProgramLabel;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public ZonedDateTime getStayStartDate() {
        return stayStartDate;
    }

    public void setStayStartDate(ZonedDateTime stayStartDate) {
        this.stayStartDate = stayStartDate;
    }

    public ZonedDateTime getStayEndDate() {
        return stayEndDate;
    }

    public void setStayEndDate(ZonedDateTime stayEndDate) {
        this.stayEndDate = stayEndDate;
    }

    public Integer getPreferredProgramId() {
        return preferredProgramId;
    }

    public void setPreferredProgramId(Integer preferredProgramId) {
        this.preferredProgramId = preferredProgramId;
    }

    public Boolean getSupplierPreferenceApplied() {
        return isSupplierPreferenceApplied;
    }

    public void setSupplierPreferenceApplied(Boolean supplierPreferenceApplied) {
        isSupplierPreferenceApplied = supplierPreferenceApplied;
    }

    public String getTierLabelType() {
        return tierLabelType;
    }

    public void setTierLabelType(String tierLabelType) {
        this.tierLabelType = tierLabelType;
    }

    public Integer getChainSortOrder() {
        return chainSortOrder;
    }

    public void setChainSortOrder(Integer chainSortOrder) {
        this.chainSortOrder = chainSortOrder;
    }

    public List<PreferencePredicates> getChainContracts() {
        return chainContracts;
    }

    public void setChainContracts(List<PreferencePredicates> chainContracts) {
        this.chainContracts = chainContracts;
    }

    public List<PreferencePredicates> getPropertyContracts() {
        return propertyContracts;
    }

    public void setPropertyContracts(List<PreferencePredicates> propertyContracts) {
        this.propertyContracts = propertyContracts;
    }
}
