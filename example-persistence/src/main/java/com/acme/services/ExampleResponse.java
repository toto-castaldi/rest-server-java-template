package com.acme.services;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by github on 08/11/15.
 */
@AllArgsConstructor (staticName = "of")
public class ExampleResponse {

    @Getter
    private String name;
}
