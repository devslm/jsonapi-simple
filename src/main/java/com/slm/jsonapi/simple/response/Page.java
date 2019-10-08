package com.slm.jsonapi.simple.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@ToString
@AllArgsConstructor
@Accessors(chain = true)
public class Page<T> {
    private T data;
    private String prev;
    private String next;
    private long total;
}
