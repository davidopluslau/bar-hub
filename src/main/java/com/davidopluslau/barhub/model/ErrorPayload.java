package com.davidopluslau.barhub.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * ErrorPayload payload.
 */
public class ErrorPayload {

  private String message;
  private Integer code;

  /**
   * Constructor.
   */
  public ErrorPayload() {
  }

  /**
   * Get Message.
   *
   * @return message
   */
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  /**
   * Set the message.
   *
   * @param message the message
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Get the error code.
   *
   * @return error code
   */
  @JsonProperty("code")
  public Integer getCode() {
    return code;
  }

  /**
   * Set the error code.
   *
   * @param code the error code
   */
  public void setCode(Integer code) {
    this.code = code;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ErrorPayload)) {
      return false;
    }
    ErrorPayload errorPayload = (ErrorPayload) o;

    return Objects.equals(message, errorPayload.message) && Objects.equals(code, errorPayload.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message, code);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ErrorPayload {\n");

    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   *
   * @param o object
   *
   * @return indented string
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
