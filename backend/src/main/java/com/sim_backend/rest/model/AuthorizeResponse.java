package com.sim_backend.rest.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class AuthorizeResponse {
  @SerializedName("message")
  private String message;
}
