package com.github.frimtec.ideatodoinspectionplugin.library.jira;

public enum ApiVersion {
  V2(2, ""),
  V3(3, "jql");

  private final int version;
  private final String searchType;

  ApiVersion(int version, String searchType) {
    this.version = version;
    this.searchType = searchType;
  }

  public int version() {
    return this.version;
  }

  public String searchType() {
    return searchType;
  }
}
